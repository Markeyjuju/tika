package org.apache.tika.utils;

import org.apache.tika.utils.ProcessUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class ProcessUtils_checkCommand_6_1_Test {

    @InjectMocks
    private ProcessUtils processUtils;

    @Mock
    private StreamGobbler streamGobbler;

    @Mock
    private Process process;

    @Mock
    private Thread outThread;

    @Mock
    private Thread errThread;

    @BeforeEach
    public void setUp() {
        Mockito.reset(streamGobbler, process, outThread, errThread);
    }

    @Test
    public void testCheckCommandSuccess() throws IOException, InterruptedException, TimeoutException {
        // Arrange
        String[] checkCmd = { "ls" };
        int[] errorValue = { 127 };
        when(process.waitFor(60000, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(process.exitValue()).thenReturn(0);
        // Act
        boolean result = processUtils.checkCommand(checkCmd, errorValue);
        // Assert
        assertTrue(result);
        verify(process).waitFor(60000, TimeUnit.MILLISECONDS);
        verify(process).exitValue();
    }

    @Test
    public void testCheckCommandTimeout() throws IOException, InterruptedException, TimeoutException {
        // Arrange
        String[] checkCmd = { "ls" };
        int[] errorValue = { 127 };
        when(process.waitFor(60000, TimeUnit.MILLISECONDS)).thenReturn(false);
        // Act & Assert
        assertThrows(TimeoutException.class, () -> processUtils.checkCommand(checkCmd, errorValue));
    }

    @Test
    public void testCheckCommandError() throws IOException, InterruptedException, TimeoutException {
        // Arrange
        String[] checkCmd = { "nonexistent" };
        int[] errorValue = { 127 };
        when(process.waitFor(60000, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(process.exitValue()).thenReturn(127);
        // Act
        boolean result = processUtils.checkCommand(checkCmd, errorValue);
        // Assert
        assertFalse(result);
        verify(process).waitFor(60000, TimeUnit.MILLISECONDS);
        verify(process).exitValue();
    }

    @Test
    public void testCheckCommandSecurityException() throws IOException, InterruptedException, TimeoutException {
        // Arrange
        String[] checkCmd = { "ls" };
        int[] errorValue = { 127 };
        when(process.waitFor(60000, TimeUnit.MILLISECONDS)).thenThrow(new SecurityException());
        // Act & Assert
        assertThrows(SecurityException.class, () -> processUtils.checkCommand(checkCmd, errorValue));
    }

    @Test
    public void testCheckCommandErrorWithOtherError() throws IOException, InterruptedException, TimeoutException {
        // Arrange
        String[] checkCmd = { "ls" };
        int[] errorValue = { 127 };
        when(process.waitFor(60000, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(process.exitValue()).thenReturn(1);
        // Act
        boolean result = processUtils.checkCommand(checkCmd, errorValue);
        // Assert
        assertTrue(result);
        verify(process).waitFor(60000, TimeUnit.MILLISECONDS);
        verify(process).exitValue();
    }
}
