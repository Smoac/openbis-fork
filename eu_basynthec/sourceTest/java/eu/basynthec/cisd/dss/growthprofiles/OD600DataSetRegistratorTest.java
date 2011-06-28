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

package eu.basynthec.cisd.dss.growthprofiles;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class OD600DataSetRegistratorTest extends AbstractJythonDataSetHandlerTest
{
    private static final String STRAIN_NAMES_PROP = "STRAIN_NAMES";

    private static final String DATA_SET_CODE = "data-set-code";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("OD600");

    private static final String EXPERIMENT_IDENTIFIER = "/TEST/TEST/TEST";

    @Test
    public void testSimpleTransaction() throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("data-set-handler.py",
                        "dist/etc/shared/shared-classes.py,dist/etc/growth-profiles/data-set-validator.py");
        createHandler(properties, false, true);
        createData();

        ExperimentBuilder builder = new ExperimentBuilder().identifier(EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue(DATA_SET_CODE));
                    atLeast(1).of(openBisService).tryToGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    one(dataSetValidator).assertValidDataSet(DATA_SET_TYPE,
                            new File(new File(stagingDirectory, DATA_SET_CODE), "data"));
                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));
                }
            });

        handler.handle(markerFile);

        assertEquals(1, atomicatOperationDetails.recordedObject().getDataSetRegistrations().size());

        NewExternalData dataSet =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations().get(0);

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals(DATA_SET_TYPE, dataSet.getDataSetType());

        HashMap<String, NewProperty> propertyMap =
                getDataSetPropertiesMap(dataSet.getDataSetProperties());
        NewProperty strainProperty = propertyMap.get(STRAIN_NAMES_PROP);

        assertNotNull(strainProperty);
        assert null != strainProperty;
        assertEquals("MGP1,MGP100,MGP20,MGP999", strainProperty.getValue());
        context.assertIsSatisfied();
    }

    private void createData() throws IOException
    {
        File dataFile = new File("sourceTest/examples/OD600-Example.xlsx");
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

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/growth-profiles/";
    }
}
