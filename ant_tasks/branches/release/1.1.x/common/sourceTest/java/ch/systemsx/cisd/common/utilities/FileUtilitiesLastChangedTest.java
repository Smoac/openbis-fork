/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.File;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the {@link FileUtilities#lastChanged(File)} method.
 * 
 * @author Bernd Rinn
 */
public class FileUtilitiesLastChangedTest
{

    private static final File workingDirectory = new File("targets" + File.separator + "unit-test-wd");

    @DataProvider(name = "testLastChanged")
    public Object[][] getDirectories()
    {
        return new Object[][]
            {
                        { ".fsLastChangedCheckerTestDirectory", ".fsLastChangedCheckerTestDirectory" },
                        { ".fsLastChangedCheckerTestDirectory",
                                ".fsLastChangedCheckerTestDirectory" + File.separator + "1" },
                        { ".fsLastChangedCheckerTestDirectory",
                                ".fsLastChangedCheckerTestDirectory" + File.separator + "1" + File.separator + 2 },
                        {
                                ".fsLastChangedCheckerTestDirectory",
                                ".fsLastChangedCheckerTestDirectory" + File.separator + "1" + File.separator + 2
                                        + File.separator + 3 } };
    }

    private void restALittleBit()
    {
        try
        {
            Thread.sleep(1500);
        } catch (InterruptedException e)
        {
            throw new CheckedExceptionTunnel(e);
        }
    }

    @BeforeSuite
    public void init()
    {
        LogInitializer.init();
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
    }

    @Test(groups =
        { "slow" }, dataProvider = "testLastChanged")
    public void testLastChanged(String dirToCheck, String dirToCreate)
    {
        final File testDir = new File(workingDirectory, dirToCheck);
        testDir.deleteOnExit();
        if (testDir.exists() && testDir.isDirectory() == false)
        {
            throw EnvironmentFailureException.fromTemplate("Directory '%s' exists and is not a directory.", testDir
                    .getPath());
        }
        final File createDir = new File(workingDirectory, dirToCreate);
        if (createDir.exists() && FileUtilities.deleteRecursively(createDir) == false)
        {
            throw EnvironmentFailureException.fromTemplate("Directory '%s' exists and cannot be deleted.", createDir
                    .getPath());
        }
        createDir.deleteOnExit();
        final long now = System.currentTimeMillis();
        if (createDir.mkdir() == false)
        {
            throw new EnvironmentFailureException("Can't make directory " + createDir.getPath() + ".");
        }
        final long lastChanged = FileUtilities.lastChanged(testDir);
        final long diff = Math.abs(lastChanged - now);
        assert diff <= 1000 : "expected difference less than 1s, but was " + diff / 1000.0 + "s";

        // We need to wait for more than 1s because that's the granularity of the changed time attribute saved with
        // files.
        restALittleBit();
    }

}
