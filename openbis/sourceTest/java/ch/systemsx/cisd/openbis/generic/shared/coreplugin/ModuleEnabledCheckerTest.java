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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;

import junit.framework.TestCase;

/**
 * @author Franz-Josef Elmer
 */
public class ModuleEnabledCheckerTest extends TestCase
{
    private interface IModuleCheckerFactory
    {
        ModuleEnabledChecker create(String items);
    }

    private IModuleCheckerFactory listFactory = new IModuleCheckerFactory()
        {

            @Override
            public ModuleEnabledChecker create(String items)
            {
                ArrayList<String> a = new ArrayList<>();
                for (String item : items.split(", "))
                {
                    a.add(item);
                }
                return new ModuleEnabledChecker(a);
            }
        };

    private IModuleCheckerFactory propertyFactory = new IModuleCheckerFactory()
        {

            @Override
            public ModuleEnabledChecker create(String items)
            {
                Properties properties = new Properties();
                properties.setProperty(Constants.ENABLED_MODULES_KEY, items);
                return new ModuleEnabledChecker(properties, Constants.ENABLED_MODULES_KEY);
            }
        };

    @Test
    public void testSimple()
    {
        testSimple(listFactory);
        testSimple(propertyFactory);
    }

    @Test
    public void testInOrder()
    {
        testInOrder(listFactory);
        testInOrder(propertyFactory);
    }

    @Test
    public void testABeta()
    {
        testABeta(listFactory);
        testABeta(propertyFactory);
    }

    private void testSimple(IModuleCheckerFactory factory)
    {
        ModuleEnabledChecker checker = factory.create("abc, abc-.*");
        assertModulesEnabled("[abc, abc-d]", Arrays.asList("abc", "abcd", "abc-d", "ABC"), checker);
    }

    private void testInOrder(IModuleCheckerFactory factory)
    {
        ModuleEnabledChecker checker = factory.create("abc, something, eln, abc.*, v.*, xyz, absent");
        assertModulesEnabled("[abc, something, abc-d, abc-z, vat, vzz, votm8, xyz]",
                Arrays.asList("something", "xyz", "vat", "vzz", "abc-d", "naha", "abc", "ABC", "abc-z", "votm8", "nah"), checker);
    }

    private void testABeta(IModuleCheckerFactory factory)
    {
        ModuleEnabledChecker checker = factory.create("a.*, beta");
        assertModulesEnabled("[abc, beta]", Arrays.asList("abc", "betablocker", "beta", "ABC"), checker);
    }

    /**
     * Asserts that both <code>getListOfEnabledModules</code>co, and <code>isModuleEnabled methods</code> are returning proper values
     */
    private void assertModulesEnabled(String expectedResult, List<String> pluginsToCheck, ModuleEnabledChecker checker)
    {
        List<String> result = checker.getListOfEnabledModules(pluginsToCheck);
        assertEquals(expectedResult, result.toString());
        for (String plugin : pluginsToCheck)
        {
            assertEquals(result.contains(plugin), checker.isModuleEnabled(plugin));
        }

        ArrayList<CorePlugin> corePluginsList = new ArrayList<CorePlugin>();
        ArrayList<CorePlugin> expectedCorePluginsList = new ArrayList<CorePlugin>();
        for (String plugin : pluginsToCheck)
        {
            corePluginsList.add(new CorePlugin(plugin, 1));
        }
        for (String plugin : result)
        {
            expectedCorePluginsList.add(new CorePlugin(plugin, 1));
        }
        List<CorePlugin> enabledPlugins = checker.getListOfEnabledPlugins(corePluginsList);
        assertEquals(expectedCorePluginsList.toString(), enabledPlugins.toString());
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testWithInvalidRegEx()
    {
        new ModuleEnabledChecker(Arrays.asList("[a-b)*"));
    }
}
