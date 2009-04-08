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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;

/**
 * A default {@link IStorageProcessor} implementation. The data set is stored in subfolder
 * {@link #ORIGINAL_DIR}.
 * 
 * @author Christian Ribeaud
 */
public class DefaultStorageProcessor extends AbstractStorageProcessor
{
    static final String ORIGINAL_DIR = "original";
    static final String NO_RENAME = "Couldn't rename '%s' to '%s'.";

    public DefaultStorageProcessor(final Properties properties)
    {
        super(properties);
    }

    //
    // AbstractStorageProcessor
    //

    public final File storeData(final ExperimentPE experiment,
            final DataSetInformation dataSetInformation,
            final IProcedureAndDataTypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir)
    {
        checkParameters(incomingDataSetDirectory, rootDir);
        File originalDir = new File(rootDir, ORIGINAL_DIR);
        if (originalDir.mkdir() == false)
        {
            throw new EnvironmentFailureException("Couldn't create " + originalDir.getAbsolutePath());
        }
        final File targetFile = new File(originalDir, incomingDataSetDirectory.getName());
        if (FileRenamer.renameAndLog(incomingDataSetDirectory, targetFile) == false)
        {
            throw new EnvironmentFailureException(String.format(NO_RENAME,
                    incomingDataSetDirectory, targetFile));
        }
        return rootDir;
    }

    public final void unstoreData(final File incomingDataSetDirectory,
            final File storedDataDirectory)
    {
        checkParameters(incomingDataSetDirectory, storedDataDirectory);
        File targetFile = new File(new File(storedDataDirectory, ORIGINAL_DIR),
                incomingDataSetDirectory.getName());
        // Note that this will move back <code>targetFilePath</code> to its original place but the
        // directory structure will persist. Right now, we consider this is fine as these empty
        // directories will not disturb the running application.
        FileRenamer.renameAndLog(targetFile, incomingDataSetDirectory);
    }

    public final StorageFormat getStorageFormat()
    {
        return StorageFormat.PROPRIETARY;
    }

    public final File tryGetProprietaryData(final File storedDataDirectory)
    {
        assert storedDataDirectory != null : "Unspecified stored data directory.";
        return storedDataDirectory;
    }
}
