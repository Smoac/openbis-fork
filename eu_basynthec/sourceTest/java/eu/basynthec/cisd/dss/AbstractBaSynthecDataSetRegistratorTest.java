/*
 * Copyright 2011 ETH Zuerich, CISD
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

package eu.basynthec.cisd.dss;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractBaSynthecDataSetRegistratorTest extends
        AbstractJythonDataSetHandlerTest
{

    protected static final String STRAIN_NAMES_PROP = "STRAIN_NAMES";

    protected static final String DATA_SET_CODE = "data-set-code";

    private static final String EXPERIMENT_IDENTIFIER = "/TEST/TEST/TEST";

    /**
     *
     *
     */
    public AbstractBaSynthecDataSetRegistratorTest()
    {
        super();
    }

    protected RecordingMatcher<AtomicEntityOperationDetails> setUpDataSetRegistrationExpectations(
            final DataSetType dataSetType)
    {
        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(dataSetValidator).assertValidDataSet(dataSetType,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "data"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });
        return atomicatOperationDetails;
    }

    protected Properties createThreadProperties()
    {
        return createThreadPropertiesRelativeToScriptsFolder("data-set-handler.py",
                "dist/etc/shared/shared-classes.py," + getRegistrationScriptsFolderPath()
                        + "/data-set-validator.py");
    }

    protected void createData(String fileName) throws IOException
    {
        File dataFile = new File("sourceTest/examples/" + fileName);
        FileUtils.copyFileToDirectory(dataFile, workingDirectory);
        incomingDataSetFile = new File(workingDirectory, dataFile.getName());

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + dataFile.getName());
        FileUtilities.writeToFile(markerFile, "");
    }

    protected HashMap<String, NewProperty> getDataSetPropertiesMap(
            List<NewProperty> dataSetProperties)
    {
        HashMap<String, NewProperty> propertyMap = new HashMap<String, NewProperty>();
        for (NewProperty prop : dataSetProperties)
        {
            propertyMap.put(prop.getPropertyCode(), prop);
        }
        return propertyMap;
    }

}