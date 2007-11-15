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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * An <code>abstract</code> test case which accesses the file system.
 * <p>
 * It constructs an appropriate working directory which is test class specific.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractFileSystemTestCase
{
    private static final File UNIT_TEST_ROOT_DIRECTORY = new File("targets" + File.separator + "unit-test-wd");

    protected final File workingDirectory;

    protected AbstractFileSystemTestCase()
    {
        this(true);
    }

    protected AbstractFileSystemTestCase(final boolean deleteOnExit)
    {
        workingDirectory = new File(UNIT_TEST_ROOT_DIRECTORY, getClass().getSimpleName());
        if (deleteOnExit)
        {
            workingDirectory.deleteOnExit();
        }
        LogInitializer.init();
    }

    @BeforeMethod
    public void setup() throws IOException
    {
        workingDirectory.mkdirs();
        FileUtils.cleanDirectory(workingDirectory);
        assert workingDirectory.isDirectory() && workingDirectory.listFiles().length == 0;
    }

}
