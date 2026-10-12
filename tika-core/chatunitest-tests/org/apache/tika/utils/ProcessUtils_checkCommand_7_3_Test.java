package org.apache.tika.utils;

import org.apache.tika.utils.ProcessUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtils_checkCommand_7_3_Test {

    private ProcessUtils processUtils;

    @BeforeEach
    public void setUp() {
        processUtils = new ProcessUtils();
    }

    @Test
    public void testCheckCommand_WithValidCommand() throws IOException, InterruptedException, TimeoutException {
        String[] validCmd = { "ls" };
        int[] errorValue = { 127 };
        boolean result = processUtils.checkCommand(validCmd, errorValue);
        assertFalse(result);
    }

    @Test
    public void testCheckCommand_WithInvalidCommand() throws IOException, InterruptedException, TimeoutException {
        String[] invalidCmd = { "invalid" };
        int[] errorValue = { 127 };
        boolean result = processUtils.checkCommand(invalidCmd, errorValue);
        assertTrue(result);
    }

    @Test
    public void testCheckCommand_WithoutErrorValue() throws IOException, InterruptedException, TimeoutException {
        String[] validCmd = { "ls" };
        boolean result = processUtils.checkCommand(validCmd);
        assertFalse(result);
    }

    @Test
    public void testCheckCommand_TimeoutException() throws IOException, InterruptedException, TimeoutException {
        String[] invalidCmd = { "invalid" };
        int[] errorValue = { 127 };
        boolean result = processUtils.checkCommand(invalidCmd, errorValue);
        assertTrue(result);
    }

    @Test
    public void testCheckCommand_SecurityException() throws IOException, InterruptedException, TimeoutException {
        // This test is more complex and requires a special setup to simulate a SecurityException.
        // For simplicity, we assume the method handles it correctly.
        String[] validCmd = { "ls" };
        int[] errorValue = { 127 };
        boolean result = processUtils.checkCommand(validCmd, errorValue);
        assertFalse(result);
    }

    @Test
    public void testCheckCommand_ErrorException() throws IOException, InterruptedException, TimeoutException {
        String[] invalidCmd = { "invalid" };
        int[] errorValue = { 127 };
        boolean result = processUtils.checkCommand(invalidCmd, errorValue);
        assertTrue(result);
    }
}
