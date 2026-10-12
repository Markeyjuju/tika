package org.apache.tika.utils;

import org.apache.commons.io.FileUtils;
import org.apache.tika.utils.ProcessUtils;
import org.apache.tika.utils.StreamGobbler;
import org.apache.tika.utils.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtils_execute_5_3_Test {

    private ProcessUtils processUtils;

    private StreamGobbler streamGobbler;

    private Path stdoutRedirect;

    @BeforeEach
    public void setUp() throws IOException {
        processUtils = new ProcessUtils();
        streamGobbler = new StreamGobbler(new ByteArrayInputStream("stderr output".getBytes()), 10000);
        stdoutRedirect = Files.createTempFile("stdout", ".tmp");
    }

    @Test
    public void testExecuteSuccess() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("echo", "stdout output");
        pb.redirectErrorStream(true);
        FileProcessResult result = processUtils.execute(pb, 10000, stdoutRedirect, 10000);
        assertEquals(0, result.exitValue);
        assertEquals("stdout output", new String(Files.readAllBytes(stdoutRedirect)));
        assertEquals("stderr output", result.stderr);
        assertFalse(result.isTimeout);
    }

    @Test
    public void testExecuteTimeout() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("sleep", "10");
        pb.redirectErrorStream(true);
        FileProcessResult result = processUtils.execute(pb, 1000, stdoutRedirect, 10000);
        assertEquals(-1000, result.exitValue);
        assertTrue(result.isTimeout);
    }

    @Test
    public void testExecuteProcessException() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("nonexistentcommand");
        pb.redirectErrorStream(true);
        try {
            processUtils.execute(pb, 10000, stdoutRedirect, 10000);
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test
    public void testExecuteStreamGobbler() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("echo", "stderr output");
        pb.redirectErrorStream(true);
        FileProcessResult result = processUtils.execute(pb, 10000, stdoutRedirect, 10000);
        assertEquals(0, result.exitValue);
        assertEquals("stderr output", result.stderr);
        assertEquals("stdout output", new String(Files.readAllBytes(stdoutRedirect)));
        assertFalse(result.isTimeout);
    }

    @Test
    public void testExecuteProcessTimeMillis() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("sleep", "1");
        pb.redirectErrorStream(true);
        FileProcessResult result = processUtils.execute(pb, 1000, stdoutRedirect, 10000);
        assertTrue(result.processTimeMillis >= 1000);
        assertTrue(result.processTimeMillis <= 1001);
    }
}
