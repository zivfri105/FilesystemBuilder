package com.axowattle.file_emulator;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBuilder {
    private final FileEnumerator file_enumerator;
    private final PositionDecider position_decider;
    private final BlockNotifier notifier;
    private boolean is_running;

    public FileBuilder(PositionDecider position_decider, BlockNotifier notifier) {
        this.notifier = notifier;
        Iterable<Path> rootsIterable = FileSystems.getDefault().getRootDirectories();
        List<Path> roots = new ArrayList<>();
        rootsIterable.forEach(roots::add);

        file_enumerator = FileEnumerator.forRoots(roots, 50_000);
        notifier.current_enumerator = file_enumerator;
        this.position_decider = position_decider;
    }

    public void start(){
        Thread worker = new Thread(this::run_thread, "FileQueueDrainer");
        is_running = true;
        worker.start();
    }

    @SuppressWarnings("unused")
    public void kill_thread(){
        is_running = false;
    }

    private void run_thread() {
        file_enumerator.start();

        try {
            Path file = file_enumerator.take();

            while (file != null && is_running){
                position_decider.add_file(file);


                file = file_enumerator.take();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        notifier.current_enumerator = null;
    }
}
