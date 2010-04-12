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
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetInfoExtractorForSearchExperiment extends AbstractDataSetInfoExtractorWithService
{
    private static final String EXPERIMENT_TYPE_CODE = "MS_SEARCH";
    private static final String SEPARATOR_KEY = "separator";
    private static final String DEFAULT_SEPARATOR = "&";
    private static final String SEARCH_PROPERTIES = "search.properties";
    
    private final String separator;
    
    public DataSetInfoExtractorForSearchExperiment(Properties properties)
    {
        this(properties, ServiceProvider.getOpenBISService());
    }

    DataSetInfoExtractorForSearchExperiment(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(service);
        separator = properties.getProperty(SEPARATOR_KEY, DEFAULT_SEPARATOR);
    }

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        String name = incomingDataSetPath.getName();
        String[] items = StringUtils.splitByWholeSeparator(name, separator);
        if (items.length != 2)
        {
            throw new UserFailureException(
                    "The name of the data set Should have two parts separated by '" + separator
                            + "': " + name);
        }
        ProjectIdentifier projectIdentifier = new ProjectIdentifier(items[0], items[1]);
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifier(projectIdentifier, "E" + service.drawANewUniqueID());
        NewExperiment experiment = new NewExperiment(experimentIdentifier.toString(),
                EXPERIMENT_TYPE_CODE);
        ExperimentType experimentType = service.getExperimentType(EXPERIMENT_TYPE_CODE);
        experiment.setProperties(getProperties(new File(incomingDataSetPath, SEARCH_PROPERTIES),
                experimentType));
        service.registerExperiment(experiment);
        DataSetInformation info = new DataSetInformation();
        info.setExperimentIdentifier(experimentIdentifier);
        return info;
    }
    
    private IEntityProperty[] getProperties(File propertiesFile, EntityType entityType)
    {
        if (propertiesFile.exists() == false)
        {
            return new IEntityProperty[0];
        }
        Properties properties = PropertyUtils.loadProperties(propertiesFile);
        return Util.getAndCheckProperties(properties, entityType);
    }

}
