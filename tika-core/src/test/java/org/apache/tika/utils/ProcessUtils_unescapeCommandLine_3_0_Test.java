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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests for {@link ProcessUtils#unescapeCommandLine(String)}.
 *
 * Abstraction exposed: the quotes are stripped only when ALL three conditions hold:
 *   contains(" ") && IS_OS_WINDOWS && startsWith("\"") && endsWith("\"")
 * Two bugs in the generated test: (a) it expected stripping on every OS, and
 * (b) it expected stripping for an argument with NO space ("escaped_at_start").
 */
public class ProcessUtils_unescapeCommandLine_3_0_Test {

    // ===== Condition not met (OS independent) =====

    // 1. No space, no quotes: unchanged.
    @Test
    public void testNoSpacesNoQuotes() {
        assertEquals("no_spaces", ProcessUtils.unescapeCommandLine("no_spaces"));
    }

    // 2. Quoted but WITHOUT a space: contains(" ") is false, so unchanged
    //    (this was the wrong expectation in the generated "case 4").
    @Test
    public void testQuotedWithoutSpaceIsUnchanged() {
        assertEquals("\"escaped_without_space\"",
                ProcessUtils.unescapeCommandLine("\"escaped_without_space\""));
    }

    // 3. Space + leading quote only: not both ends quoted, unchanged.
    @Test
    public void testOnlyLeadingQuote() {
        assertEquals("\"escaped at start",
                ProcessUtils.unescapeCommandLine("\"escaped at start"));
    }

    // 4. Space + trailing quote only: not both ends quoted, unchanged.
    @Test
    public void testOnlyTrailingQuote() {
        assertEquals("escaped at end\"",
                ProcessUtils.unescapeCommandLine("escaped at end\""));
    }

    // ===== Condition met, depends on the OS =====

    // 5. Windows: surrounding quotes are removed.
    @Test
    @EnabledOnOs(OS.WINDOWS)
    public void testQuotedWithSpacesOnWindows() {
        assertEquals("escaped with spaces",
                ProcessUtils.unescapeCommandLine("\"escaped with spaces\""));
    }

    // 6. Non-Windows: IS_OS_WINDOWS is false, so the quotes stay.
    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void testQuotedWithSpacesOnNonWindows() {
        assertEquals("\"escaped with spaces\"",
                ProcessUtils.unescapeCommandLine("\"escaped with spaces\""));
    }

    // ===== Documented asymmetry with escapeCommandLine =====

    // 7. Unlike escapeCommandLine, null is NOT guarded: arg.contains(..) throws.
    @Test
    public void testNullThrowsNpe() {
        assertThrows(NullPointerException.class, () -> ProcessUtils.unescapeCommandLine(null));
    }
}
