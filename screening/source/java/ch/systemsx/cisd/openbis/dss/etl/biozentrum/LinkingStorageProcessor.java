/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Creates a symbolic link to original data in directory specified in {@link #TARGET_DIR} and a
 * marker file ({@link Constants#IS_FINISHED_PREFIX}&lt;filename&gt;) containing the code of
 * registered data set.
 * 
 * @author Izabela Adamczyk
 */
public class LinkingStorageProcessor extends AbstractDelegatingStorageProcessor
{

    static final String TARGET_DIR = "target-dir";

    private final File targetDir;

    public LinkingStorageProcessor(Properties properties)
    {
        super(properties);
        targetDir = new File(PropertyUtils.getMandatoryProperty(properties, TARGET_DIR));
        if (targetDir.exists() == false)
        {
            throw new ConfigurationFailureException(String.format("'%s' (%s) does not exist",
                    TARGET_DIR, targetDir));
        }
        if (targetDir.isDirectory() == false)
        {
            throw new ConfigurationFailureException(String.format("'%s' (%s) is not a directory",
                    TARGET_DIR, targetDir));
        }
        if (targetDir.canWrite() == false)
        {
            throw new ConfigurationFailureException(String.format("No write access to '%s' (%s)",
                    TARGET_DIR, targetDir));
        }
    }

    @Override
    public File storeData(DataSetInformation dataSetInformation, ITypeExtractor typeExtractor,
            IMailClient mailClient, File incomingDataSetDirectory, File rootDir)
    {
        File resultFile =
                super.storeData(dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);
        File source = tryGetProprietaryData(resultFile);
        boolean success = SoftLinkMaker.createSymbolicLink(source, targetDir);
        if (success)
        {
            File markerFile = new File(targetDir, Constants.IS_FINISHED_PREFIX + source.getName());
            FileUtilities.writeToFile(markerFile, dataSetInformation.getDataSetCode());
        } else
        {
            throw EnvironmentFailureException.fromTemplate(
                    "Can not create symbolic link to '%s' in '%s'.", source.getPath(),
                    targetDir.getPath());
        }
        return resultFile;
    }

}
