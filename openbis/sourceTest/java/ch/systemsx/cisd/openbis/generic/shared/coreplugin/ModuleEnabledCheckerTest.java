/*
 * Copyright 2012 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.shared.coreplugin;

import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

/**
 * @author Franz-Josef Elmer
 */
public class ModuleEnabledCheckerTest extends TestCase
{
    @Test
    public void testFromList()
    {
        ModuleEnabledChecker checker = new ModuleEnabledChecker(Arrays.asList("abc", "abc-.*"));

        assertEquals(
                "[true, false, true, false]",
                Arrays.asList(checker.isModuleEnabled("abc"), checker.isModuleEnabled("abcd"),
                        checker.isModuleEnabled("abc-d"), checker.isModuleEnabled("ABC"))
                        .toString());
    }

    @Test
    public void testFromProperty()
    {
        Properties properties = new Properties();
        properties.setProperty(Constants.ENABLED_MODULES_KEY, "a.*, beta");
        ModuleEnabledChecker checker = new ModuleEnabledChecker(properties);

        assertEquals(
                "[true, false, true, false]",
                Arrays.asList(checker.isModuleEnabled("abc"),
                        checker.isModuleEnabled("betablocker"), checker.isModuleEnabled("beta"),
                        checker.isModuleEnabled("ABC")).toString());
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testWithInvalidRegEx()
    {
        new ModuleEnabledChecker(Arrays.asList("[a-b)*"));
    }
}
