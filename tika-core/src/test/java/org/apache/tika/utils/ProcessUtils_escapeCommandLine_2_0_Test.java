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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests for {@link ProcessUtils#escapeCommandLine(String)}.
 *
 * Abstraction exposed: the method is a pure function of (arg, SystemUtils.IS_OS_WINDOWS).
 * IS_OS_WINDOWS is a static final boolean computed once at class-load time, so it
 * can NOT be mocked (Mockito cannot stub a field, and the original @Mock/@InjectMocks
 * setup threw an NPE in @BeforeAll). Instead of faking the OS, we assert the real
 * behaviour of the current OS and use @EnabledOnOs / @DisabledOnOs to select the
 * branch of the "if" that each test exercises.
 */
public class ProcessUtils_escapeCommandLine_2_0_Test {

    // ===== Branch: arg == null (OS independent) =====

    // 1. null is passed through untouched (first guard of the method).
    @Test
    public void testEscapeNull() {
        assertNull(ProcessUtils.escapeCommandLine(null));
    }

    // ===== Branch: no space in the argument (OS independent) =====

    // 2. Without a space the quoting condition is false, whatever the OS.
    @Test
    public void testEscapeWithoutSpaces() {
        assertEquals("somecommandwithnospaces",
                ProcessUtils.escapeCommandLine("somecommandwithnospaces"));
    }

    // ===== Branch: space in the argument, depends on the OS =====

    // 3. Windows: the argument is wrapped in double quotes.
    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testEscapeWithSpacesOnWindows() {
        assertEquals("\"some command with spaces\"",
                ProcessUtils.escapeCommandLine("some command with spaces"));
    }

    // 4. Non-Windows: ProcessBuilder already handles spaces, so nothing is added.
    //    (This is the case the generated test got wrong: it assumed Windows.)
    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void testEscapeWithSpacesOnNonWindows() {
        assertEquals("some command with spaces",
                ProcessUtils.escapeCommandLine("some command with spaces"));
    }

    // ===== Branch: already quoted (OS independent) =====
    // The wrapping condition is (!startsWith("\"") && !endsWith("\"")), so a single
    // quote at either end is enough to disable it. The generated tests expected an
    // extra quote to be added; the source never does that.

    // 5. Fully quoted: left as is.
    @Test
    public void testEscapeAlreadyQuoted() {
        String input = "\"some command with spaces\"";
        assertEquals(input, ProcessUtils.escapeCommandLine(input));
    }

    // 6. Only a trailing quote: left as is (endsWith is true).
    @Test
    public void testEscapeWithEndingQuoteOnly() {
        String input = "some command with spaces\"";
        assertEquals(input, ProcessUtils.escapeCommandLine(input));
    }

    // 7. Only a leading quote: left as is (startsWith is true).
    @Test
    public void testEscapeWithStartingQuoteOnly() {
        String input = "\"some command with spaces";
        assertEquals(input, ProcessUtils.escapeCommandLine(input));
    }
}