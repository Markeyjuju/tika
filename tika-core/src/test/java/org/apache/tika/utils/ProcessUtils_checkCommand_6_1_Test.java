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
 * Tests for {@link ProcessUtils#checkCommand(String[], int...)}: exit-code semantics.
 *
 * Abstraction exposed: checkCommand returns "true unless the exit code is in the
 * errorValue list" (default list = {127}). Process creation is hard-wired to
 * Runtime.getRuntime().exec(...) inside the method, so @Mock Process / @InjectMocks
 * can never reach it (the generated mocks were unused and Mockito's strict stubs
 * failed). We drive the exit code with real "sh -c 'exit N'" processes instead.
 *
 * Not testable without refactoring: the 60 s TimeoutException branch (hard-coded
 * delay), the SecurityException rethrow (SecurityManager is disabled in modern
 * JDKs) and the posix_spawn Error branch. The generated "timeout" and "security"
 * tests expected exceptions that the method either never throws (the timeout is
 * caught and turned into false) or cannot be made to throw.
 */
@DisabledOnOs(OS.WINDOWS) // relies on "sh"
public class ProcessUtils_checkCommand_6_1_Test {

    // Helper: a command that terminates immediately with the given exit code.
    private static String[] exitWith(int code) {
        return new String[]{"sh", "-c", "exit " + code};
    }

    // ===== Default error value (127 = command not found) =====

    // 1. Exit 0 is a success.
    @Test
    public void testExitZeroIsSuccess() {
        assertTrue(ProcessUtils.checkCommand(exitWith(0)));
    }

    // 2. Exit 127 is the default error value.
    @Test
    public void testExit127IsDefaultError() {
        assertFalse(ProcessUtils.checkCommand(exitWith(127)));
    }

    // 3. Any other non-zero code is NOT an error by default.
    @Test
    public void testOtherNonZeroExitIsSuccessByDefault() {
        assertTrue(ProcessUtils.checkCommand(exitWith(1)));
    }

    // ===== Explicit error values =====

    // 4. The exit code is in the custom list -> false.
    @Test
    public void testExitCodeInCustomErrorList() {
        assertFalse(ProcessUtils.checkCommand(exitWith(2), 1, 2));
    }

    // 5. The exit code is not in the custom list -> true.
    @Test
    public void testExitCodeNotInCustomErrorList() {
        assertTrue(ProcessUtils.checkCommand(exitWith(3), 1, 2));
    }

    // ===== Single-String overload (delegates to the array version) =====

    // 6. "true" is a standard binary on Linux and macOS and exits with 0.
    @Test
    public void testStringOverloadSuccess() {
        assertTrue(ProcessUtils.checkCommand("true"));
    }
}