/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.logging;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class ControlFileBasedLogConfigurationTest extends ControlFileBasedTest
{

    private static final String TEST_EVENT_NAME = "test-event";

    private static final String BOOLEAN_PARAMETER_NAME = "boolean-parameter";

    @Test
    public void testHasEvent() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);

        Assert.assertFalse(config.hasEvent(TEST_EVENT_NAME));

        new File(getControlFileDirectory(), TEST_EVENT_NAME).createNewFile();

        Assert.assertTrue(config.hasEvent(TEST_EVENT_NAME));
        Assert.assertFalse(config.hasEvent(TEST_EVENT_NAME));
    }

    @Test
    public void testHasEventAndSwitchBooleanParameter() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanParameter(BOOLEAN_PARAMETER_NAME, false);

        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
        Assert.assertFalse(config.hasEvent(TEST_EVENT_NAME));

        new File(getControlFileDirectory(), BOOLEAN_PARAMETER_NAME + "-on").createNewFile();
        new File(getControlFileDirectory(), TEST_EVENT_NAME).createNewFile();

        Assert.assertTrue(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
        Assert.assertTrue(config.hasEvent(TEST_EVENT_NAME));

        Assert.assertTrue(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
        Assert.assertFalse(config.hasEvent(TEST_EVENT_NAME));

        new File(getControlFileDirectory(), BOOLEAN_PARAMETER_NAME + "-off").createNewFile();

        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
        Assert.assertFalse(config.hasEvent(TEST_EVENT_NAME));

        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
        Assert.assertFalse(config.hasEvent(TEST_EVENT_NAME));
    }

    @Test
    public void testSwitchBooleanParameter() throws IOException
    {
        final ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanParameter(BOOLEAN_PARAMETER_NAME, false);

        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));

        new File(getControlFileDirectory(), BOOLEAN_PARAMETER_NAME + "-on").createNewFile();
        Assert.assertTrue(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));

        new File(getControlFileDirectory(), BOOLEAN_PARAMETER_NAME + "-off").createNewFile();
        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
    }

    @Test
    public void testSwitchBooleanParameterWithValueInDifferentCase() throws IOException
    {
        ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanParameter(BOOLEAN_PARAMETER_NAME, true);

        Assert.assertTrue(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));

        new File(getControlFileDirectory(), BOOLEAN_PARAMETER_NAME + "-OFf").createNewFile();
        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
    }

    @Test
    public void testSwitchBooleanParameterWithIncorrectValue() throws IOException
    {
        ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(getControlFileDirectory(), -1);
        config.addBooleanParameter(BOOLEAN_PARAMETER_NAME, false);

        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));

        new File(getControlFileDirectory(), BOOLEAN_PARAMETER_NAME + "-incorrect-value").createNewFile();
        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
    }

    @Test
    public void testGetBooleanParameterWithNotExistingControlDirectory() throws IOException
    {
        File notExistingFile = new File(getControlFileDirectory(), "notExisting");

        ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(notExistingFile, -1);
        config.addBooleanParameter(BOOLEAN_PARAMETER_NAME, false);

        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
    }

    @Test
    public void testGetBooleanParameterWithControlDirectoryThatIsAFile() throws IOException
    {
        File file = new File(getControlFileDirectory(), "aFile");
        file.createNewFile();

        ControlFileBasedLogConfiguration config = new ControlFileBasedLogConfiguration(file, -1);
        config.addBooleanParameter(BOOLEAN_PARAMETER_NAME, false);

        Assert.assertFalse(config.getBooleanParameterValue(BOOLEAN_PARAMETER_NAME));
    }

}
