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

import ch.systemsx.cisd.etlserver.DelegatingStorageProcessorWithDropbox;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.AbstractDatasetDropboxHandler;

/**
 * Storage processor which is able to create a copy of incoming data for additional processing. The
 * copy has a changed name to trace back the dataset to which the original data belong.
 * <p>
 * Assumes that {@link IDataSetInfoExtractor} returns extended information about datasets using
 * {@link DataSetInformationYeastX} class.
 * </p>
 * <p>
 * The processor uses following properties: {@link #DELEGATE_PROCESSOR_CLASS_PROPERTY},
 * {@link #DROPBOX_EICML_INCOMING_DIRECTORY_PROPERTY},
 * {@link #DROPBOX_FIAML_INCOMING_DIRECTORY_PROPERTY} and
 * {@link AbstractDatasetDropboxHandler#DATASET_CODE_SEPARATOR_PROPERTY}. All the properties are
 * also passed for the default processor.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class StorageProcessorWithDropboxes extends DelegatingStorageProcessorWithDropbox
{
    /**
     * The path to the directory where an additional copy of the original incoming data will be
     * created. The file will be converted by external mechanism to eicML format.
     */
    public final static String DROPBOX_EICML_INCOMING_DIRECTORY_PROPERTY = "eicml-dropbox-dir";

    /**
     * The path to the directory where an additional copy of the original incoming data will be
     * created. The file will be converted by external mechanism to fiaML format.
     */
    public final static String DROPBOX_FIAML_INCOMING_DIRECTORY_PROPERTY = "fiaml-dropbox-dir";

    public StorageProcessorWithDropboxes(Properties properties)
    {
        super(properties, new DatasetDropboxHandler(properties));

    }

    private static class DatasetDropboxHandler extends AbstractDatasetDropboxHandlerYeastX
    {
        private static final long serialVersionUID = 1L;

        private final File eicmlDropboxOrNull;

        private final File fiamlDropboxOrNull;

        public DatasetDropboxHandler(Properties properties)
        {
            super(properties);
            this.eicmlDropboxOrNull =
                    tryGetDirectory(DROPBOX_EICML_INCOMING_DIRECTORY_PROPERTY, properties);
            this.fiamlDropboxOrNull =
                    tryGetDirectory(DROPBOX_FIAML_INCOMING_DIRECTORY_PROPERTY, properties);
        }

        @Override
        protected File tryGetDropboxDir(File originalData, DataSetInformation dataSetInformation)
        {
            DataSetInformationYeastX info = (DataSetInformationYeastX) dataSetInformation;
            MLConversionType conversion = info.getConversion();
            if (conversion == MLConversionType.EICML)
            {
                return eicmlDropboxOrNull;
            } else if (conversion == MLConversionType.FIAML)
            {
                return fiamlDropboxOrNull;
            } else
            {
                return null;
            }
        }
    }
}
