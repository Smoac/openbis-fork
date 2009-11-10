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
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Stores directories containing quantML files in the DSS store. Additionally extracts and uploads
 * information from *.quantML dataset files to the additional database.
 * 
 * @author Tomasz Pylak
 */
public class QuantMLStorageProcessor extends AbstractDelegatingStorageProcessor
{
    private final ML2DatabaseUploader databaseUploader;

    private final String mlFileExtension;

    // the script which ensures that we have write access to the datasets
    private final PreprocessingExecutor writeAccessSetter;

    public QuantMLStorageProcessor(Properties properties)
    {
        super(properties);
        this.databaseUploader = new ML2DatabaseUploader(properties);
        this.mlFileExtension = ConstantsYeastX.QUANTML_EXT;
        this.writeAccessSetter = PreprocessingExecutor.create(properties);
    }

    @Override
    public File storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir)
    {
        ensureUploadableFileExists(incomingDataSetDirectory);
        acquireWriteAccess(incomingDataSetDirectory);
        File storeData =
                super.storeData(dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);
        File originalData = super.tryGetProprietaryData(storeData);
        File quantML = findFile(originalData, mlFileExtension);
        databaseUploader.upload(quantML, dataSetInformation);
        return storeData;
    }

    private void acquireWriteAccess(final File incomingDataSetDirectory)
    {
        String incomingName = incomingDataSetDirectory.getName();
        boolean ok = writeAccessSetter.execute(incomingName);
        if (ok == false)
        {
            throw UserFailureException.fromTemplate("Cannot get the write access to the dataset: "
                    + incomingName);
        }
    }

    private void ensureUploadableFileExists(File incomingDataSetDirectory)
    {
        findFile(incomingDataSetDirectory, mlFileExtension);
    }

    @Override
    public void commit()
    {
        super.commit();
        databaseUploader.commit();
    }

    @Override
    public UnstoreDataAction rollback(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        try
        {
            super.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
        } finally
        {
            databaseUploader.rollback();
        }
        return UnstoreDataAction.LEAVE_UNTOUCHED;
    }

    // returns the only file with the specified extension or throws an exceptions if none or more
    // than one is found.
    public static File findFile(File incomingItem, String fileExtension)
    {
        if (incomingItem.isFile()
                && FilenameUtils.isExtension(incomingItem.getName(), fileExtension))
        {
            return incomingItem;
        }
        List<File> files = FileOperations.getInstance().listFiles(incomingItem, new String[]
            { fileExtension }, false);
        if (files.size() != 1)
        {
            throw UserFailureException.fromTemplate(
                    "There should be exactly one file with '%s' extension"
                            + " in '%s' directory, but %d have been found.", fileExtension,
                    incomingItem.getPath(), files.size());
        }
        return files.get(0);
    }
}
