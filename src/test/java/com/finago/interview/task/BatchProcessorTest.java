package com.finago.interview.task;

import com.finago.interview.task.service.DirectoryWatchService;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for BatchProcessor.
 */
public class BatchProcessorTest extends TestCase {

    @Before
    protected void setUp() throws InterruptedException {
        File rootDirectory = new File("data/in/");
        File outDirectory = new File("data/out/");
        File directory = new File("src/test/root/");
        final String rootPath = directory.getAbsolutePath();
        Path watchDirectory = Paths.get(rootPath + "/in/");
        moveFiles(rootDirectory, watchDirectory);
        Killable task = new Killable();
        Thread thread = new Thread(task);
        thread.start();
        TimeUnit.SECONDS.sleep(10);
        task.kill();
        thread.interrupt();
    }

    @Test
    public void testsOutputFolder() throws InterruptedException {
        File outDirectory0 = new File("data/out/0");
        File outDirectory1 = new File("data/out/1");
        assertTrue(outDirectory0.exists());
        assertTrue(outDirectory1.exists());
        String[] pathnames = outDirectory0.list();
        assert pathnames != null;
        assertEquals(pathnames.length, 8);
        File firstIn0 = new File("data/out/0/8842");
        assertTrue(firstIn0.exists());
        String[] files = firstIn0.list();
        assert files != null;
        assertEquals(files.length, 4);
    }

    private void moveFiles(File rootDirectory, Path watchDirectory) {
        new Timer().schedule(
            new TimerTask() {
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
    }

    static class Killable implements Runnable {
        private volatile boolean killed = false;

        public void run() {
            while (!killed) {
                try {
                    doOnce();
                } catch (InterruptedException | IOException ex) {
                    killed = true;
                }
            }
        }

        public void kill() {
            System.out.println("Killing service called");
            killed = true;
        }

        private void doOnce() throws InterruptedException, IOException {
            File directory = new File("src/test/root/");
            new DirectoryWatchService(directory.getAbsolutePath() + "/").processEvents();
        }
    }
}