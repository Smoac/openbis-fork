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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForProteinResults extends AbstractDataSetInfoExtractorWithService
{
    @Private static final String EXPERIMENT_TYPE_CODE_KEY = "experiment-type-code";
    @Private static final String EXPERIMENT_PROPERTIES_FILE_NAME_KEY = "experiment-properties-file-name";
    @Private static final String DEFAULT_EXPERIMENT_TYPE_CODE = "MS_SEARCH";
    @Private static final String SEPARATOR_KEY = "separator";
    @Private static final String DEFAULT_SEPARATOR = "&";
    @Private static final String DEFAULT_EXPERIMENT_PROPERTIES_FILE_NAME = "search.properties";
    static final String PARENT_DATA_SET_CODES = "parent-data-set-codes";
    static final String EXPERIMENT_IDENTIFIER_KEY = "base-experiment";
    
    private final String separator;
    private final String experimentPropertiesFileName;
    private final String experimentTypeCode;
    
    public DataSetInfoExtractorForProteinResults(Properties properties)
    {
        this(properties, ServiceProvider.getOpenBISService());
    }

    DataSetInfoExtractorForProteinResults(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(service);
        separator = properties.getProperty(SEPARATOR_KEY, DEFAULT_SEPARATOR);
        experimentPropertiesFileName =
                properties.getProperty(EXPERIMENT_PROPERTIES_FILE_NAME_KEY,
                        DEFAULT_EXPERIMENT_PROPERTIES_FILE_NAME);
        experimentTypeCode = properties.getProperty(EXPERIMENT_TYPE_CODE_KEY, DEFAULT_EXPERIMENT_TYPE_CODE);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        String name = incomingDataSetPath.getName();
        String[] items = StringUtils.splitByWholeSeparator(name, separator);
        if (items.length < 2)
        {
            throw new UserFailureException(
                    "The name of the data set should have at least two parts separated by '" + separator
                            + "': " + name);
        }
        ProjectIdentifier projectIdentifier = new ProjectIdentifier(items[0], items[1]);
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier(projectIdentifier, "E" + service.drawANewUniqueID());
        NewExperiment experiment =
                new NewExperiment(experimentIdentifier.toString(), experimentTypeCode);
        ExperimentType experimentType = service.getExperimentType(experimentTypeCode);

        Properties properties =
                loadSearchProperties(new File(incomingDataSetPath, experimentPropertiesFileName));
        experiment.setProperties(Util.getAndCheckProperties(properties, experimentType));
        service.registerExperiment(experiment);
        DataSetInformation info = new DataSetInformation();
        info.setExperimentIdentifier(experimentIdentifier);
        String parentDataSetCodesOrNull = getProperty(properties, PARENT_DATA_SET_CODES);
        if (parentDataSetCodesOrNull != null)
        {
            info.setParentDataSetCodes(Arrays.asList(StringUtils.split(parentDataSetCodesOrNull, ", ")));
        } else 
        {
            String baseExperimentIdentifier = getProperty(properties, EXPERIMENT_IDENTIFIER_KEY);
            if (baseExperimentIdentifier != null)
            {
                ExperimentIdentifier identifier = new ExperimentIdentifierFactory(baseExperimentIdentifier).createIdentifier();
                Experiment baseExperiment = service.tryToGetExperiment(identifier);
                if (baseExperiment == null)
                {
                    throw new UserFailureException("Property " + EXPERIMENT_IDENTIFIER_KEY
                            + " specifies an unknown experiment: " + baseExperimentIdentifier);
                }
                List<ExternalData> dataSets = service.listDataSetsByExperimentID(baseExperiment.getId());
                List<String> parentDataSetCodes = new ArrayList<String>();
                for (ExternalData dataSet : dataSets)
                {
                    parentDataSetCodes.add(dataSet.getCode());
                }
                info.setParentDataSetCodes(parentDataSetCodes);
            }
        }
        return info;
    }
    
    private String getProperty(Properties properties, String key)
    {
        String property = properties.getProperty(key);
        if (property == null)
        {
            property = properties.getProperty(key.toUpperCase());
        }
        return property;
    }
    
    private Properties loadSearchProperties(File propertiesFile)
    {
        Properties properties;
        if (propertiesFile.exists() == false)
        {
            properties = new Properties();
        } else
        {
            properties = PropertyUtils.loadProperties(propertiesFile);
        }
        return properties;
    }

}
