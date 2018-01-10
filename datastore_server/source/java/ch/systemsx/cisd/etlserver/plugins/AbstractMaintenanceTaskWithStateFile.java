/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractMaintenanceTaskWithStateFile implements IMaintenanceTask
{
    public static final String STATE_FILE_KEY = "state-file";

    protected File getStateFile(Properties properties, File storeRoot)
    {
        String path = properties.getProperty(STATE_FILE_KEY);
        if (path == null)
        {
            return new File(storeRoot, getClass().getSimpleName() + "-state.txt");
        }
        File file = new File(path);
        if (file.isDirectory())
        {
            throw new ConfigurationFailureException("File '" + file.getAbsolutePath()
                    + "' (specified by property '" + STATE_FILE_KEY + "') is a directory.");
        }
        return file;
    }

}
