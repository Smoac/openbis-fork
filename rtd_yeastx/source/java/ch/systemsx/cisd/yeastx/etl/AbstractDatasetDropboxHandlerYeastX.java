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

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.AbstractDatasetDropboxHandler;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Tomasz Pylak
 */
abstract class AbstractDatasetDropboxHandlerYeastX extends AbstractDatasetDropboxHandler
{
    private static final long serialVersionUID = 1L;

    public AbstractDatasetDropboxHandlerYeastX(Properties properties)
    {
        super(properties);
    }

    // sampleCode-groupCode-datasetCode.originalFileExtension
    @Override
    protected final String createDropboxDestinationFileName(DataSetInformation dataSetInformation,
            File incomingDataSetDirectory)
    {
        String dataSetCode = dataSetInformation.getDataSetCode();
        String originalName = incomingDataSetDirectory.getName();
        String subSep = "&";
        ExperimentIdentifier expIdent = dataSetInformation.getExperimentIdentifier();
        String expIdentText =
                expIdent.getSpaceCode() + subSep + expIdent.getProjectCode() + subSep
                        + expIdent.getExperimentCode();
        String newFileName =
                expIdentText + datasetCodeSeparator + dataSetCode
                        + stripFileExtension(originalName);
        return newFileName;
    }

    // returns file extension with the "." at the beginning or empty string if file has no
    // extension
    private static String stripFileExtension(String originalName)
    {
        int ix = originalName.lastIndexOf(".");
        if (ix == -1)
        {
            return "";
        } else
        {
            return originalName.substring(ix);
        }
    }
}
