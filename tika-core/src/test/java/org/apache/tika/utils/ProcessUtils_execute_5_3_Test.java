/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the overload
 * {@link ProcessUtils#execute(ProcessBuilder, long, Path, int)}
 * (stdout redirected to a file, stderr captured in memory).
 *
 * Abstraction exposed: this is an integration test over a REAL child process
 * (sh / sleep). ProcessUtils builds the Process itself via pb.start(), so there
 * is no seam to inject a mock; the stdout/stderr split is verified through the
 * two observable channels: the redirect file and FileProcessResult.stderr.
 *
 * Fixes vs the generated version:
 *  - redirectErrorStream(true) MERGED stderr into the stdout file, so
 *    result.stderr was always "" (the generated asserts expected "stderr output").
 *  - the file content has a trailing "\n" ("echo" adds one).
 *  - on timeout exitValue stays -1 (-1000 is only for InterruptedException).
 *  - timing asserts [1000, 1001] ms were flaky; loose bounds are used instead.
 *  - the unused StreamGobbler field and the manual temp-file are replaced by @TempDir.
 */
@DisabledOnOs(OS.WINDOWS) // relies on "sh" and "sleep"
public class ProcessUtils_execute_5_3_Test {

    // JUnit creates and deletes this directory for each test.
    @TempDir
    Path tempDir;

    private String readFile(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    // ===== Normal completion =====

    // 1. stdout goes to the file, stderr to the result; both channels stay separate.
    @Test
    public void testStdoutRedirectedAndStderrCaptured() throws IOException {
        Path out = tempDir.resolve("stdout.txt");
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "echo out; echo err 1>&2");

        FileProcessResult result = ProcessUtils.execute(pb, 10_000, out, 10_000);

        assertEquals(0, result.getExitValue());
        assertFalse(result.isTimeout());
        assertEquals("out\n", readFile(out));          // redirected stdout (with newline)
        assertEquals("", result.getStdout());          // never filled on the redirect overload
        assertEquals("err", result.getStderr());       // gobbler joins lines with "\n", no trailing one
        assertEquals(4, result.getStdoutLength());     // Files.size(out) = "out\n"
        assertEquals(3, result.getStderrLength());     // gobbler counts chars without newline
        assertFalse(result.isStdoutTruncated());
        assertFalse(result.isStderrTruncated());
    }

    // 2. A non-zero exit code is reported as is (not a timeout, not an exception).
    @Test
    public void testNonZeroExitValue() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "exit 3");

        FileProcessResult result =
                ProcessUtils.execute(pb, 10_000, tempDir.resolve("stdout.txt"), 10_000);

        assertEquals(3, result.getExitValue());
        assertFalse(result.isTimeout());
    }

    // 3. Missing parent directories of the redirect target are created.
    @Test
    public void testParentDirectoriesAreCreated() throws IOException {
        Path out = tempDir.resolve("a/b/stdout.txt");
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "echo hello");

        ProcessUtils.execute(pb, 10_000, out, 10_000);

        assertTrue(Files.isDirectory(out.getParent()));
        assertEquals("hello\n", readFile(out));
    }

    // 4. Elapsed time is measured and stays below the timeout (loose bounds on purpose).
    @Test
    public void testProcessTimeMillis() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("sleep", "1");

        FileProcessResult result =
                ProcessUtils.execute(pb, 10_000, tempDir.resolve("stdout.txt"), 10_000);

        assertTrue(result.getProcessTimeMillis() >= 900,
                "elapsed=" + result.getProcessTimeMillis());
        assertTrue(result.getProcessTimeMillis() < 10_000,
                "elapsed=" + result.getProcessTimeMillis());
    }

    // ===== Timeout =====

    // 5. The process outlives the timeout: it is killed and flagged, exitValue keeps its default -1.
    @Test
    public void testTimeout() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("sleep", "10");

        FileProcessResult result =
                ProcessUtils.execute(pb, 500, tempDir.resolve("stdout.txt"), 10_000);

        assertTrue(result.isTimeout());
        assertEquals(-1, result.getExitValue());
        assertTrue(result.getProcessTimeMillis() >= 500);
    }

    // ===== Failure to start =====

    // 6. pb.start() throws IOException for an unknown binary; it propagates to the caller.
    @Test
    public void testUnknownCommandThrowsIOException() {
        ProcessBuilder pb = new ProcessBuilder("nonexistentcommand_xyz");

        assertThrows(IOException.class,
                () -> ProcessUtils.execute(pb, 10_000, tempDir.resolve("stdout.txt"), 10_000));
    }
}