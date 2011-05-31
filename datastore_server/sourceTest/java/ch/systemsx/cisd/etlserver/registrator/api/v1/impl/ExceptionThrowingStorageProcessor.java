/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.FileRenamer;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ExceptionThrowingStorageProcessor extends DefaultStorageProcessor
{

    static final String NO_RENAME = "Couldn't rename '%s' to '%s'.";

    /**
     * @param properties
     */
    public ExceptionThrowingStorageProcessor(Properties properties)
    {
        super(properties);
    }

    @Override
    public IStorageProcessorTransaction createTransaction()
    {
        return new ExceptionThrowingStorageProcessorTransaction();
    }

    private class ExceptionThrowingStorageProcessorTransaction extends
            AbstractStorageProcessorTransaction
    {

        @Override
        protected File storeData(DataSetInformation dataSetInformation,
                ITypeExtractor typeExtractor, IMailClient mailClient)
        {
            checkParameters(incomingDataSetDirectory, rootDirectory);
            File originalDir = getOriginalDirectory(rootDirectory);
            if (originalDir.mkdir() == false)
            {
                throw new EnvironmentFailureException("Couldn't create "
                        + originalDir.getAbsolutePath());
            }
            final File targetFile = new File(originalDir, incomingDataSetDirectory.getName());
            if (FileRenamer.renameAndLog(incomingDataSetDirectory, targetFile) == false)
            {
                throw new EnvironmentFailureException(String.format(NO_RENAME,
                        incomingDataSetDirectory, targetFile));
            }
            unzipIfMatching(targetFile, originalDir);

            throw new RuntimeException("Here is your exception");
        }

        @Override
        protected void executeCommit()
        {
            // nothing to do
        }

        @Override
        protected UnstoreDataAction executeRollback(Throwable ex)
        {
            checkParameters(incomingDataSetDirectory, storedDataDirectory);
            File targetFile =
                    new File(getOriginalDirectory(storedDataDirectory),
                            incomingDataSetDirectory.getName());
            // Note that this will move back <code>targetFilePath</code> to its original place but
            // the
            // directory structure will persist. Right now, we consider this is fine as these empty
            // directories will not disturb the running application.
            FileRenamer.renameAndLog(targetFile, incomingDataSetDirectory);
            return getDefaultUnstoreDataAction(ex);
        }

        /**
         * returns the only file or directory which is expected to be found inside original
         * directory
         */
        public final File tryGetProprietaryData()
        {
            File originalDir = getOriginalDirectory(storedDataDirectory);
            List<File> files = FileUtilities.listFilesAndDirectories(originalDir, false, null);
            if (files.size() != 1)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Exactly one file expected in '%s' directory, but %d found.",
                        originalDir.getPath(), files.size());
            }
            return files.get(0);
        }
    }

}
