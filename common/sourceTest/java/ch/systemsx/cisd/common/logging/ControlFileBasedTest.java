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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.common.action.IDelegatedActionWithResult;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.TestResources;

/**
 * @author pkupczyk
 */
public class ControlFileBasedTest
{

    @BeforeMethod
    protected void beforeMethod()
    {
        deleteControlFileDirectory();
        getControlFileDirectory().mkdirs();
    }

    @AfterTest
    protected void afterTest()
    {
        deleteControlFileDirectory();
    }

    protected void testEvent(String eventName, IDelegatedActionWithResult<Boolean> isEventAction) throws IOException
    {
        Assert.assertFalse(isEventAction.execute(true));

        new File(getControlFileDirectory(), eventName).createNewFile();

        Assert.assertTrue(isEventAction.execute(true));
        Assert.assertFalse(isEventAction.execute(true));
    }

    protected void testSwitchBooleanParameter(String parameterName, boolean parameterDefault,
            IDelegatedActionWithResult<Boolean> getParameterValueAction) throws IOException
    {
        Assert.assertFalse(getParameterValueAction.execute(true));

        new File(getControlFileDirectory(), parameterName + "-on").createNewFile();
        Assert.assertTrue(getParameterValueAction.execute(true));

        new File(getControlFileDirectory(), parameterName + "-off").createNewFile();
        Assert.assertFalse(getParameterValueAction.execute(true));
    }

    protected File getControlFileDirectory()
    {
        TestResources resources = new TestResources(getClass());
        return resources.getResourcesDirectory();
    }

    protected void deleteControlFileDirectory()
    {
        File directory = getControlFileDirectory();
        if (directory.exists())
        {
            FileUtilities.deleteRecursively(directory);
        }
    }

}
