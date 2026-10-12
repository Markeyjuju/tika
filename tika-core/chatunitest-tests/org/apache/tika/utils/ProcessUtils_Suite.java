package org.apache.tika.utils;

import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;

@RunWith(value = JUnitPlatform.class)
@SelectClasses(value = { ProcessUtils_escapeCommandLine_2_0_Test.class, ProcessUtils_execute_5_3_Test.class, ProcessUtils_unescapeCommandLine_3_0_Test.class, ProcessUtils_checkCommand_7_3_Test.class, ProcessUtils_checkCommand_6_1_Test.class })
public class ProcessUtils_Suite {
}
