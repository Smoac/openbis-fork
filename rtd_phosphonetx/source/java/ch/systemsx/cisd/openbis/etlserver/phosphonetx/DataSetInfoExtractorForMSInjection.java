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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Data set info extractor for MS injection data sets. Information is extracted from a properties
 * file (ms-injection.properties) which is expected t be a part of the data set. As a side effect a
 * corresponding sample of type MS_INJECTION is created with the properties of this file.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForMSInjection extends AbstractDataSetInfoExtractorWithService
{
    static final String MS_INJECTION_PROPERTIES_FILE = "ms-injection.properties";

    static final String DATA_SET_PROPERTIES_FILE = "data-set.properties";

    static final String PROJECT_CODE_KEY = "PROJECT_CODE";

    static final String EXPERIMENT_CODE_KEY = "EXPERIMENT_CODE";

    static final String SAMPLE_CODE_KEY = "SAMPLE_CODE";

    static final String USER_KEY = "USER";

    static final String DATA_SET_TYPE_KEY = "DATA_SET_TYPE";

    static final String FILE_TYPE_KEY = "FILE_TYPE";

    static final String PARENT_TYPE_KEY = "PARENT_TYPE";

    static final String EXPERIMENT_TYPE_CODE = "MS_INJECT";

    static final String SAMPLE_TYPE_CODE = "MS_INJECTION";

    public DataSetInfoExtractorForMSInjection(Properties properties)
    {
        this(ServiceProvider.getOpenBISService());
    }

    DataSetInfoExtractorForMSInjection(IEncapsulatedOpenBISService service)
    {
        super(service);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        Properties sampleProperties =
                Util.loadPropertiesFile(incomingDataSetPath, MS_INJECTION_PROPERTIES_FILE);
        DataSetInformation info = new DataSetInformation();
        info.setSpaceCode(Constants.MS_DATA_SPACE);
        info.setSampleCode(PropertyUtils.getMandatoryProperty(sampleProperties, SAMPLE_CODE_KEY));
        SampleIdentifier sampleIdentifier = info.getSampleIdentifier();
        ExperimentIdentifier experimentIdentifier = getExperimentIdentifier(sampleProperties);
        getOrCreateExperiment(experimentIdentifier);
        info.setExperimentIdentifier(experimentIdentifier);
        long sampleID =
                registerOrUpdateSample(sampleIdentifier, experimentIdentifier, sampleProperties);

        Properties dataSetProperties =
                Util.loadPropertiesFile(incomingDataSetPath, DATA_SET_PROPERTIES_FILE);
        String dataSetTypeCode =
                PropertyUtils.getMandatoryProperty(dataSetProperties, DATA_SET_TYPE_KEY);
        String parentTypeOrNull = dataSetProperties.getProperty(PARENT_TYPE_KEY);
        dataSetProperties.remove(DATA_SET_TYPE_KEY);
        dataSetProperties.remove(FILE_TYPE_KEY);
        dataSetProperties.remove(PARENT_TYPE_KEY);
        setDataSetPropertiesFor(info, dataSetProperties, dataSetTypeCode);
        if (parentTypeOrNull != null)
        {
            List<ExternalData> dataSets = service.listDataSetsBySampleID(sampleID, false);
            ExternalData youngestDataSet = null;
            for (ExternalData dataSet : dataSets)
            {
                if (dataSet.getDataSetType().getCode().equals(parentTypeOrNull))
                {
                    if (youngestDataSet == null || timeStamp(youngestDataSet) < timeStamp(dataSet))
                    {
                        youngestDataSet = dataSet;
                    }
                }
            }
            if (youngestDataSet != null)
            {
                info.setParentDataSetCodes(Arrays.asList(youngestDataSet.getCode()));
                info.setSampleCode(null);
            }
        }
        return info;
    }

    private long registerOrUpdateSample(SampleIdentifier sampleIdentifier,
            ExperimentIdentifier experimentIdentifier, Properties properties)
    {
        SampleType sampleType = service.getSampleType(SAMPLE_TYPE_CODE);
        Sample sample = service.tryGetSampleWithExperiment(sampleIdentifier);
        if (sample == null)
        {
            NewSample newSample = new NewSample();
            newSample.setSampleType(sampleType);
            newSample.setExperimentIdentifier(experimentIdentifier.toString());
            newSample.setIdentifier(sampleIdentifier.toString());
            IEntityProperty[] sampleProperties = Util.getAndCheckProperties(properties, sampleType);
            newSample.setProperties(sampleProperties);
            return service.registerSample(newSample, properties.getProperty(USER_KEY));
        } else
        {
            TechId sampleID = new TechId(sample.getId());
            List<IEntityProperty> propertiesList =
                    Util.getUpdatedProperties(sample, sampleType, properties);
            Set<NewAttachment> emptySet = Collections.<NewAttachment> emptySet();
            Date version = sample.getModificationDate();
            service.updateSample(new SampleUpdatesDTO(sampleID, propertiesList,
                    experimentIdentifier, emptySet, version, sampleIdentifier, null, null));
            return sample.getId();
        }
    }

    private long timeStamp(ExternalData dataSet)
    {
        return dataSet.getRegistrationDate().getTime();
    }

    private void setDataSetPropertiesFor(DataSetInformation info, Properties dataSetProperties,
            String dataSetTypeCode)
    {
        DataSetType dataSetType = service.getDataSetType(dataSetTypeCode).getDataSetType();
        IEntityProperty[] props = Util.getAndCheckProperties(dataSetProperties, dataSetType);
        List<NewProperty> properties = new ArrayList<NewProperty>();
        for (IEntityProperty p : props)
        {
            properties.add(new NewProperty(p.getPropertyType().getCode(), p.tryGetAsString()));
        }
        info.setDataSetProperties(properties);
    }

    private long getOrCreateExperiment(ExperimentIdentifier identifier)
    {
        Experiment experiment = service.tryToGetExperiment(identifier);
        if (experiment == null)
        {
            return service.registerExperiment(new NewExperiment(identifier.toString(),
                    EXPERIMENT_TYPE_CODE));
        }
        return experiment.getId();
    }

    private ExperimentIdentifier getExperimentIdentifier(Properties msInjectionProperties)
    {
        String projectCode =
                PropertyUtils.getMandatoryProperty(msInjectionProperties, PROJECT_CODE_KEY);
        String experimentCode =
                PropertyUtils.getMandatoryProperty(msInjectionProperties, EXPERIMENT_CODE_KEY);
        ExperimentIdentifier identifier =
                new ExperimentIdentifier(null, Constants.MS_DATA_SPACE, projectCode, experimentCode);
        return identifier;
    }
}
