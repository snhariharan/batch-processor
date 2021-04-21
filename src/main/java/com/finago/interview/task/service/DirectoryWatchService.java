package com.finago.interview.task.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryWatchService {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    Logger logger = Logger.getLogger(DirectoryWatchService.class.getName());

    public DirectoryWatchService(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        walkAndRegisterDirectories(dir);
    }

    private void walkAndRegisterDirectories(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void registerDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE);
        keys.put(key, dir);
    }

    public void processEvents() throws IOException {
        WatchKey key;
        do {
            logger.log(Level.INFO, "*beep boop* ...processing data... *beep boop*");
            try {
                key = watcher.take();
                Path dir = keys.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!!");
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    @SuppressWarnings("rawtypes")
                    WatchEvent.Kind kind = event.kind();
                    @SuppressWarnings("unchecked")
                    Path name = ((WatchEvent<Path>)event).context();
                    Path path = dir.resolve(name);
                    logger.log(Level.INFO, event.kind().name() + ": " + path + "\n");
                    if (kind == ENTRY_CREATE) {
                        FileProcessor fileProcessor = new FileProcessor();
                        fileProcessor.process(path);
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            } catch (InterruptedException x) {
                return;
            }
        } while (true);
    }
}