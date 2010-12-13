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

package ch.systemsx.cisd.etlserver.entityregistration;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;

/**
 * An object that represents a sample/data set pair defined in a file
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleDataSetPair
{
    private final static String SAMPLE_IDENTIFIER = "S_identifier";

    private final static String SAMPLE_CONTAINER = "S_container";

    private final static String SAMPLE_PARENT = "S_parent";

    private final static String SAMPLE_EXPERIMENT = "S_experiment";

    private final static String DATA_SET_CODE = "D_code";

    private final static String DATA_SET_TYPE = "D_data_set_type";

    private final static String DATA_SET_FILE_TYPE = "D_file_type";

    private final static String FOLDER = "FOLDER";

    private final NewSample newSample;

    private final DataSetInformation dataSetInformation;

    private String[] tokens;

    private String folderName;

    public SampleDataSetPair()
    {
        newSample = new NewSample();
        dataSetInformation = new DataSetInformation();
    }

    public String[] getTokens()
    {
        return tokens;
    }

    public void setTokens(String[] tokens)
    {
        this.tokens = tokens;
    }

    public NewSample getNewSample()
    {
        return newSample;
    }

    public DataSetInformation getDataSetInformation()
    {
        return dataSetInformation;
    }

    @BeanProperty(label = SAMPLE_IDENTIFIER, optional = true)
    public void setSampleIdentifier(String sampleIdentifier)
    {
        this.newSample.setIdentifier(sampleIdentifier);
    }

    @BeanProperty(label = SAMPLE_CONTAINER, optional = true)
    public void setSampleContainerIdentifier(String container)
    {
        this.newSample.setContainerIdentifier(container);
    }

    @BeanProperty(label = SAMPLE_PARENT, optional = true)
    public void setSampleParent(String parent)
    {
        this.newSample.setParents(parent);
    }

    @BeanProperty(label = SAMPLE_EXPERIMENT, optional = true)
    public void setSampleExperimentIdentifier(String experimentIdentifier)
    {
        this.newSample.setExperimentIdentifier(experimentIdentifier);
    }

    @BeanProperty(label = DATA_SET_CODE, optional = true)
    public void setDataSetCode(String code)
    {
        dataSetInformation.setDataSetCode(code);
    }

    @BeanProperty(label = DATA_SET_TYPE, optional = true)
    public void setDataSetType(String parent)
    {
    }

    @BeanProperty(label = DATA_SET_FILE_TYPE, optional = true)
    public void setDataSetFileType(String experimentIdentifier)
    {
    }

    public String getFolderName()
    {
        return folderName;
    }

    @BeanProperty(label = FOLDER, optional = true)
    public void setFolderName(String folderName)
    {
        this.folderName = folderName;
    }

    public void setSampleProperties(IEntityProperty[] properties)
    {
        newSample.setProperties(properties);
    }

    public void setDataSetProperties(IEntityProperty[] properties)
    {
        dataSetInformation.setProperties(properties);
    }
}
