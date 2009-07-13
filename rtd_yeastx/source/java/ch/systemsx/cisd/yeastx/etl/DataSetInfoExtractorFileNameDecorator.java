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

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Uses a delegator extractor specified at {@link #DELEGATOR_CLASS_PROPERTY} property and enriches
 * the extracted information by adding a dataset property with a file name of the original dataset.
 * The name of the property used for the file name must be specified in
 * {@link #FILE_NAME_PROPERTY_NAME} property.
 * 
 * @author Tomasz Pylak
 */
public class DataSetInfoExtractorFileNameDecorator extends AbstractDelegatingDataSetInfoExtractor
{
    /**
     * Name of the property, which holds the property code in the database, at which the original
     * dataset file name should be stored.
     */
    public static final String FILE_NAME_PROPERTY_NAME = "file-name-property-code";

    private final String fileNamePropertyCode;

    public DataSetInfoExtractorFileNameDecorator(Properties properties)
    {
        super(properties);
        String code = PropertyUtils.getMandatoryProperty(properties, FILE_NAME_PROPERTY_NAME);
        this.fileNamePropertyCode = DatasetMappingResolver.adaptPropertyCode(code);
    }

    @Override
    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        DataSetInformation info = super.getDataSetInformation(incomingDataSetPath, openbisService);
        List<NewProperty> properties = info.getDataSetProperties();
        properties.add(createFileNameProperty(incomingDataSetPath));
        info.setDataSetProperties(properties);
        return info;
    }

    private NewProperty createFileNameProperty(File incomingDataSetPath)
    {
        return new NewProperty(fileNamePropertyCode, incomingDataSetPath.getName());
    }
}
