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
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;

/**
 * Data set info extractor for MS injection data sets. Information is extracted from a properties
 * file (ms-injection.properties) which is expected t be a part of the data set. As a side effect a
 * corresponding sample of type MS_INJECTION is created with the properties of this file.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForMSInjection implements IDataSetInfoExtractor
{
    static final String MS_INJECTION_PROPERTIES_FILE_KEY = "ms-injection-properties-file";
    static final String DEFAULT_MS_INJECTION_PROPERTIES_FILE = "ms-injection.properties";

    static final String PROJECT_CODE_KEY = "PROJECT_CODE";
    static final String MZXML_FILENAME_KEY = "MZXML_FILENAME";
    
    static final String GROUP_CODE = "MS_DATA";

    static final String SAMPLE_TYPE_CODE = "MS_INJECTION";


    private static final SimpleDateFormat EXPERIMENT_CODE_TEMPLATE =
            new SimpleDateFormat("yyyy-MM");

    private final String msInjectionPropertiesFileName;

    private final IEncapsulatedOpenBISService service;

    private final ITimeProvider timeProvider;

    public DataSetInfoExtractorForMSInjection(Properties properties)
    {
        this(properties.getProperty(MS_INJECTION_PROPERTIES_FILE_KEY,
                DEFAULT_MS_INJECTION_PROPERTIES_FILE), ServiceProvider.getOpenBISService(),
                SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    DataSetInfoExtractorForMSInjection(String msInjectionPropertiesFileName,
            IEncapsulatedOpenBISService service, ITimeProvider timeProvider)
    {
        this.msInjectionPropertiesFileName = msInjectionPropertiesFileName;
        this.service = service;
        this.timeProvider = timeProvider;

    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        Properties properties =
                loadMSInjectionProperties(incomingDataSetPath);
        DataSetInformation info = new DataSetInformation();
        info.setGroupCode(GROUP_CODE);
        info.setSampleCode(PropertyUtils.getMandatoryProperty(properties, MZXML_FILENAME_KEY));
        NewSample sample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode(SAMPLE_TYPE_CODE);
        sample.setSampleType(sampleType);
        sample.setExperimentIdentifier(createExperimentIdentifer(properties));
        sample.setIdentifier(info.getSampleIdentifier().toString());
        List<EntityProperty> sampleProperties = createSampleProperties(properties);
        sample.setProperties(sampleProperties.toArray(new EntityProperty[sampleProperties.size()]));
        service.registerSample(sample);
        return info;
    }

    private String createExperimentIdentifer(Properties msInjectionProperties)
    {
        String projectCode =
                PropertyUtils.getMandatoryProperty(msInjectionProperties, PROJECT_CODE_KEY);
        String experimentCode =
                EXPERIMENT_CODE_TEMPLATE.format(new Date(timeProvider.getTimeInMilliseconds()));
        return DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + GROUP_CODE
                + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + projectCode
                + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + experimentCode;
    }

    private List<EntityProperty> createSampleProperties(Properties msInjectionProperties)
    {
        List<String> missingMandatoryProperties = new ArrayList<String>();
        List<EntityProperty> sampleProperties = new ArrayList<EntityProperty>();
        SampleType sampleType = service.getSampleType(SAMPLE_TYPE_CODE);
        List<SampleTypePropertyType> sampleTypePropertyTypes = sampleType.getAssignedPropertyTypes();
        for (SampleTypePropertyType sampleTypePropertyType : sampleTypePropertyTypes)
        {
            boolean mandatory = sampleTypePropertyType.isMandatory();
            PropertyType propertyType = sampleTypePropertyType.getPropertyType();
            String key = propertyType.getCode();
            String value = msInjectionProperties.getProperty(key);
            if (value == null)
            {
                if (mandatory)
                {
                    missingMandatoryProperties.add(key);
                }
            } else
            {
                EntityProperty property = new EntityProperty();
                property.setPropertyType(propertyType);
                property.setValue(value);
                sampleProperties.add(property);
            }
        }
        if (missingMandatoryProperties.isEmpty() == false)
        {
            throw new UserFailureException("The following mandatory properties are missed: "
                    + missingMandatoryProperties);
        }
        return sampleProperties;
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
        return loadPropertiesFile(msInjectionPropertiesFile);
    }

    private Properties loadPropertiesFile(File msInjectionPropertiesFile)
    {
        Properties properties = new Properties();
        List<String> lines = FileUtilities.loadToStringList(msInjectionPropertiesFile);
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            if (line.length() == 0 || line.startsWith("#"))
            {
                continue;
            }
            int indexOfEqualSymbol = line.indexOf('=');
            if (indexOfEqualSymbol < 0)
            {
                throw new UserFailureException("Missing '=' in line " + (i + 1)
                        + " of MS injection properties file " + msInjectionPropertiesFileName
                        + ": " + line);
            }
            String key = line.substring(0, indexOfEqualSymbol).trim();
            String value = line.substring(indexOfEqualSymbol + 1).trim();
            properties.setProperty(key, value);
        }
        return properties;
    }
}
