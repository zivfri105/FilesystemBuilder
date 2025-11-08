package com.axowattle.fileDeleter;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileBuilder {
    private final FileEnumerator file_enumerator;
    private final PositionDecider position_decider;
    private final BlockNotifier notifier;
    private Thread worker;

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
        worker = new Thread(this::run_thread, "FileQueueDrainer");
        worker.setDaemon(true);
        worker.start();
    }

    private void run_thread() {
        file_enumerator.start();

        try {
            Path file = file_enumerator.take();

            while (file != null){
                position_decider.add_file(file);


                file = file_enumerator.take();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        notifier.current_enumerator = null;
    }
}
