package com.finago.interview.task;

import com.finago.interview.task.service.DirectoryWatchService;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Unit test for BatchProcessor.
 */
public class BatchProcessorTest extends TestCase {

    @Test
    public void testApp() throws IOException {
        File rootDirectory =  new File("data/in/");
        File directory = new File("src/test/resources/");
        final String rootPath = directory.getAbsolutePath();
        Path watchDirectory = Paths.get(rootPath + "/in/");
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            Files.move(Paths.get(rootDirectory.getAbsolutePath() + "/"), watchDirectory, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                5000
        );
        new DirectoryWatchService(directory.getAbsolutePath() + "/").processEvents();
    }

}
