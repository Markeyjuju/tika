package org.apache.tika.utils;

import org.apache.tika.utils.ProcessUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ExtendWith(MockitoExtension.class)
public class ProcessUtils_escapeCommandLine_2_0_Test {

    @InjectMocks
    private ProcessUtils processUtils;

    @Mock
    private static SystemUtils systemUtils;

    @BeforeAll
    public static void setup() {
        try {
            when(systemUtils.IS_OS_WINDOWS).thenReturn(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEscapeCommandLineWithSpaces() throws IOException {
        String input = "some command with spaces";
        String expected = "\"some command with spaces\"";
        String result = processUtils.escapeCommandLine(input);
        assertEquals(expected, result);
    }

    @Test
    public void testEscapeCommandLineWithoutSpaces() throws IOException {
        String input = "somecommandwithnospaces";
        String expected = "somecommandwithnospaces";
        String result = processUtils.escapeCommandLine(input);
        assertEquals(expected, result);
    }

    @Test
    public void testEscapeCommandLineWithQuotes() throws IOException {
        String input = "\"some command with spaces\"";
        String expected = "\"some command with spaces\"";
        String result = processUtils.escapeCommandLine(input);
        assertEquals(expected, result);
    }

    @Test
    public void testEscapeCommandLineWithEndingQuotes() throws IOException {
        String input = "some command with spaces\"";
        String expected = "\"some command with spaces\"\"";
        String result = processUtils.escapeCommandLine(input);
        assertEquals(expected, result);
    }

    @Test
    public void testEscapeCommandLineWithStartingQuotes() throws IOException {
        String input = "\"some command with spaces";
        String expected = "\"some command with spaces\"\"";
        String result = processUtils.escapeCommandLine(input);
        assertEquals(expected, result);
    }
}
