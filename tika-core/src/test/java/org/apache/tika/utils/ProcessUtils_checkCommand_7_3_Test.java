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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests for {@link ProcessUtils#checkCommand(String[], int...)}: failure paths and
 * the errorValue override.
 *
 * Abstraction exposed: when the executable cannot even be launched,
 * Runtime.exec throws IOException, which the method catches and maps to false.
 * The generated asserts were inverted ("ls" -> false, "invalid" -> true) and three
 * tests (timeout / security / error) were copies of the same "invalid" case that
 * did not exercise the branch their name claimed.
 */
@DisabledOnOs(OS.WINDOWS) // relies on "sh"
public class ProcessUtils_checkCommand_7_3_Test {

    // ===== IOException branch (binary not found) =====

    // 1. Unknown executable, array overload, default error values -> false.
    @Test
    public void testUnknownCommandReturnsFalse() {
        assertFalse(ProcessUtils.checkCommand(new String[]{"nonexistentcommand_xyz"}));
    }

    // 2. Same through the single-String overload.
    @Test
    public void testUnknownCommandStringOverloadReturnsFalse() {
        assertFalse(ProcessUtils.checkCommand("nonexistentcommand_xyz"));
    }

    // 3. The IOException branch ignores a custom errorValue list: still false.
    @Test
    public void testUnknownCommandWithCustomErrorValuesReturnsFalse() {
        assertFalse(ProcessUtils.checkCommand(new String[]{"nonexistentcommand_xyz"}, 1, 2));
    }

    // ===== errorValue defaulting / override =====

    // 4. Empty varargs -> default {127} is applied: exit 127 counts as an error.
    @Test
    public void testEmptyErrorValueFallsBackTo127() {
        assertFalse(ProcessUtils.checkCommand(new String[]{"sh", "-c", "exit 127"}));
    }

    // 5. A custom list REPLACES the default: 127 is no longer an error.
    @Test
    public void testCustomErrorValueReplacesDefault() {
        assertTrue(ProcessUtils.checkCommand(new String[]{"sh", "-c", "exit 127"}, 1));
    }
}
