package com.axowattle.file_emulator;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public final class FileEnumerator implements AutoCloseable {
    private final BlockingQueue<Path> queue;
    private final Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<Path> roots;
    private final Predicate<Path> dirSkipPredicate;
    private volatile boolean finished = false;

    /**
     * Create an enumerator that scans all system root directories (C:\, D:\ on Windows; / on Linux).
     * Queue capacity is bounded to avoid OOM if consumer is slow.
     */
    public static FileEnumerator forSystemRoots(int queueCapacity) {
        List<Path> roots = new ArrayList<>();
        for (Path r : FileSystems.getDefault().getRootDirectories()) {
            roots.add(r);
        }
        return new FileEnumerator(roots, queueCapacity, FileEnumerator::defaultSkipDir);
    }

    /**
     * Create an enumerator that scans the given roots.
     */
    public static FileEnumerator forRoots(Collection<Path> roots, int queueCapacity) {
        return new FileEnumerator(new ArrayList<>(roots), queueCapacity, FileEnumerator::defaultSkipDir);
    }

    /**
     * Full-control constructor: custom roots & directory-skip predicate.
     */
    public FileEnumerator(Collection<Path> roots, int queueCapacity, Predicate<Path> dirSkipPredicate) {
        if (queueCapacity <= 0) throw new IllegalArgumentException("queueCapacity must be > 0");
        this.queue = new LinkedBlockingQueue<>(queueCapacity);
        this.roots = new ArrayList<>(roots);
        this.dirSkipPredicate = dirSkipPredicate != null ? dirSkipPredicate : p -> false;

        this.worker = new Thread(this::runWalk, "FileEnumerator-Worker");
        this.worker.setDaemon(true);
    }

    /** Start the background crawl thread (idempotent). */
    public void start() {
        if (running.compareAndSet(false, true)) {
            worker.start();
        }
    }

    /** Take next file path, waiting if none available. Returns null only if finished & queue empty. */
    public Path take() throws InterruptedException {
        while (true) {
            Path p = queue.poll(200, TimeUnit.MILLISECONDS);
            if (p != null) return p;
            if (finished && queue.isEmpty()) return null;
        }
    }

    /** Poll with timeout; returns null on timeout or when finished & queue empty. */
    public Path poll(long timeout, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);
        while (true) {
            long remaining = deadlineNanos - System.nanoTime();
            if (remaining <= 0) {
                return queue.poll(); // immediate poll, may be null
            }
            Path p = queue.poll(Math.min(TimeUnit.NANOSECONDS.toMillis(remaining), 200), TimeUnit.MILLISECONDS);
            if (p != null) return p;
            if (finished && queue.isEmpty()) return null;
        }
    }

    /** Non-blocking fetch; returns null if none currently available. */
    public Path tryPoll() {
        return queue.poll();
    }

    /** True when the walk has completed (all roots visited). */
    public boolean isFinished() {
        return finished && queue.isEmpty();
    }

    /** Remaining items currently queued. */
    public int queuedCount() {
        return queue.size();
    }

    /** Stop early and clean up. */
    public void stop() {
        running.set(false);
        worker.interrupt();
    }

    @Override
    public void close() {
        stop();
    }


    // ==================== internals ====================

    private void runWalk() {
        try {
            for (Path root : roots) {
                if (!running.get()) break;
                walkRoot(root);
            }
        } finally {
            finished = true;
        }
    }

    private void walkRoot(Path root) {
        try {
            Files.walkFileTree(root, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!running.get()) return FileVisitResult.TERMINATE;

                    // Skip known problematic/system pseudo-filesystems or junk dirs
                    if (dirSkipPredicate.test(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (!running.get()) return FileVisitResult.TERMINATE;
                    if (attrs.isRegularFile()) {
                        // Backpressure-aware: block if the consumer is slower
                        try {
                            queue.put(file);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return FileVisitResult.TERMINATE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    // Ignore unreadable files/dirs; keep going
                    return running.get() ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
                }
            });
        } catch (IOException ignored) {
            // Ignore root-level traversal errors and continue with next root
        }
    }

    private static boolean defaultSkipDir(Path dir) {
        // Normalize name for checks
        String name = dir.getFileName() != null ? dir.getFileName().toString() : dir.toString();
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            String lname = name.toLowerCase(Locale.ROOT);
            return lname.equals("system volume information")
                    || lname.equals("$recycle.bin")
                    || lname.equals("windowsapps")
                    || lname.equals("windows") && dir.getParent() == null; // skip C:\Windows root if desired
        } else {
            // Linux/Unix: skip pseudo FS and volatile areas that explode traversal size or cause loops
            // You can tweak this list to your needs.
            final Set<String> skipNames = Set.of("proc", "sys", "dev", "run", "var/run", "var/cache", "lost+found");
            // quick checks by tail name:
            if (skipNames.contains(name)) return true;
            // Absolute path checks (handle nested mount points)
            String abs = dir.toAbsolutePath().normalize().toString();
            return abs.equals("/proc") || abs.startsWith("/proc/")
                    || abs.equals("/sys")  || abs.startsWith("/sys/")
                    || abs.equals("/dev")  || abs.startsWith("/dev/")
                    || abs.equals("/run")  || abs.startsWith("/run/")
                    || abs.equals("/snap") || abs.startsWith("/snap/"); // optional
        }
    }

    // ==================== example usage ====================

    public static void main(String[] args) throws Exception {
        // Example: scan all system roots with a queue of 10_000 entries
        try (FileEnumerator fe = FileEnumerator.forSystemRoots(10_000)) {
            fe.start();

            // Consume a few results then exit; replace with your own consumer loop
            int printed = 0;
            while (true) {
                Path p = fe.poll(5, TimeUnit.SECONDS);
                if (p == null && fe.isFinished()) break;
                if (p != null) {
                    System.out.println(p);
                    if (++printed >= 100) {
                        // Demonstration: stop early after 100 files
                        break;
                    }
                }
            }

            // Stop the crawl if you finish early
            fe.stop();
        }
    }
}
