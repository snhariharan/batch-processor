package com.finago.interview.task;

import com.finago.interview.task.service.DirectoryWatchService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple main method as an example.
 */
public class BatchProcessor {

    static Logger logger = Logger.getLogger(BatchProcessor.class.getName());

    public static void main(String[] args) {
        logger.log(Level.INFO, "*beep boop* ...processing data... *beep boop*");
        Path dir = Paths.get("/opt/batch-processor/data/in");
        try {
            new DirectoryWatchService(dir).processEvents();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while starting to watch directory path: " + dir);
        }
    }

}
