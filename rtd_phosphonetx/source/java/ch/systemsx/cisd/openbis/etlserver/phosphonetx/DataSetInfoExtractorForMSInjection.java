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
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * Data set info extractor for MS injection data sets. Information is extracted from a properties
 * file (ms-injection.properties) which is expected t be a part of the data set. As a side effect a
 * corresponding sample of type MS_INJECTION is created with the properties of this file.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForMSInjection extends AbstractDataSetInfoExtractorWithService
{
    static final String MS_INJECTION_PROPERTIES_FILE_KEY = "ms-injection-properties-file";
    static final String DEFAULT_MS_INJECTION_PROPERTIES_FILE = "ms-injection.properties";

    static final String PROJECT_CODE_KEY = "PROJECT_CODE";
    static final String EXPERIMENT_CODE_KEY = "EXPERIMENT_CODE";
    static final String SAMPLE_CODE_KEY = "SAMPLE_CODE";
    static final String USER_KEY = "USER";
    
    static final String EXPERIMENT_TYPE_CODE = "MS_INJECT";

    static final String SAMPLE_TYPE_CODE = "MS_INJECTION";


    private final String msInjectionPropertiesFileName;

    public DataSetInfoExtractorForMSInjection(Properties properties)
    {
        this(properties.getProperty(MS_INJECTION_PROPERTIES_FILE_KEY,
                DEFAULT_MS_INJECTION_PROPERTIES_FILE), ServiceProvider.getOpenBISService());
    }

    DataSetInfoExtractorForMSInjection(String msInjectionPropertiesFileName,
            IEncapsulatedOpenBISService service)
    {
        super(service);
        this.msInjectionPropertiesFileName = msInjectionPropertiesFileName;
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        Properties properties =
                loadMSInjectionProperties(incomingDataSetPath);
        DataSetInformation info = new DataSetInformation();
        info.setSpaceCode(Constants.MS_DATA_SPACE);
        info.setSampleCode(PropertyUtils.getMandatoryProperty(properties, SAMPLE_CODE_KEY));
        NewSample sample = new NewSample();
        SampleType sampleType = service.getSampleType(SAMPLE_TYPE_CODE);
        sample.setSampleType(sampleType);
        sample.setExperimentIdentifier(getOrCreateExperiment(properties));
        sample.setIdentifier(info.getSampleIdentifier().toString());
        sample.setProperties(Util.getAndCheckProperties(properties, sampleType));
        service.registerSample(sample, properties.getProperty(USER_KEY));
        return info;
    }

    private String getOrCreateExperiment(Properties msInjectionProperties)
    {
        String projectCode =
                PropertyUtils.getMandatoryProperty(msInjectionProperties, PROJECT_CODE_KEY);
        String experimentCode =
                PropertyUtils.getMandatoryProperty(msInjectionProperties, EXPERIMENT_CODE_KEY);
        ExperimentIdentifier identifier =
                new ExperimentIdentifier(null, Constants.MS_DATA_SPACE, projectCode, experimentCode);
        Experiment experiment = service.tryToGetExperiment(identifier);
        if (experiment == null)
        {
            service.registerExperiment(new NewExperiment(identifier.toString(),
                    EXPERIMENT_TYPE_CODE));
        }
        return identifier.toString();
    }

    private Properties loadMSInjectionProperties(File incomingDataSetDirectory)
    {
        File msInjectionPropertiesFile =
                new File(incomingDataSetDirectory, msInjectionPropertiesFileName);
        if (msInjectionPropertiesFile.exists() == false)
        {
            throw new UserFailureException("Missing MS injection properties file '"
                    + msInjectionPropertiesFileName + "'.");
        }
        if (msInjectionPropertiesFile.isFile() == false)
        {
            throw new UserFailureException("Properties file '" + msInjectionPropertiesFileName
                    + "' is a folder.");
        }
        return PropertyUtils.loadProperties(msInjectionPropertiesFile);
    }
}
