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

package ch.systemsx.cisd.etlserver;

import java.io.File;

/**
 * A utility class for working with the directory structure of the "log-registrations" directory
 * which stores log information about data set registrations.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssRegistrationLogDirectoryHelper
{
    private final static String IN_PROCESS_DIR_NAME = "in-process";

    private final static String SUCCEEDED_DIR_NAME = "succeeded";

    private final static String FAILED_DIR_NAME = "failed";

    private final File dssRegistrationLogDir;

    public DssRegistrationLogDirectoryHelper(File dssRegistrationLogDir)
    {
        this.dssRegistrationLogDir = dssRegistrationLogDir;
    }

    public void initializeSubdirectories()
    {
        createDirectoryIfNecessary(getInProcessDir());
        createDirectoryIfNecessary(getSucceededDir());
        createDirectoryIfNecessary(getFailedDir());
    }

    public File getInProcessDir()
    {
        return new File(dssRegistrationLogDir, IN_PROCESS_DIR_NAME);
    }

    public File getSucceededDir()
    {
        return new File(dssRegistrationLogDir, SUCCEEDED_DIR_NAME);
    }

    public File getFailedDir()
    {
        return new File(dssRegistrationLogDir, FAILED_DIR_NAME);
    }

    private void createDirectoryIfNecessary(File dir)
    {
        if (false == dir.exists())
        {
            dir.mkdir();
        }
    }
}