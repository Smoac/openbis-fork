/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDropboxProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A dropbox processing plugin that gets its dropbox directory from properties.
 * 
 * @author Tomasz Pylak
 */
public class DropboxProcessingPluginYeastX extends AbstractDropboxProcessingPlugin
{
    public final static String DROPBOX_INCOMING_DIRECTORY_PROPERTY = "dropbox-dir";

    private static final long serialVersionUID = 1L;

    public DropboxProcessingPluginYeastX()
    {
        this(null, null);
    }

    public DropboxProcessingPluginYeastX(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, new DatasetDropboxHandler(properties));
    }

    private static final class DatasetDropboxHandler extends AbstractDatasetDropboxHandlerYeastX
    {
        private static final long serialVersionUID = 1L;

        private final File dropboxDir;

        public DatasetDropboxHandler(Properties properties)
        {
            super(properties);
            this.dropboxDir = getDropboxDir(properties);
        }

        private File getDropboxDir(Properties properties)
        {
            File dir = tryGetDirectory(DROPBOX_INCOMING_DIRECTORY_PROPERTY, properties);
            if (dir == null)
            {
                throw EnvironmentFailureException.fromTemplate("Property '%s' is not set.",
                        DROPBOX_INCOMING_DIRECTORY_PROPERTY);
            }
            return dir;
        }

        @Override
        protected File tryGetDropboxDir(File originalData, DataSetInformation dataSetInformation)
        {
            return dropboxDir;
        }
    }

}
