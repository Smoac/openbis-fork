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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;

import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractBaSynthecDataSetRegistratorTest extends
        AbstractJythonDataSetHandlerTest
{

    private static final String TEST_EXPERIMENT_IDENTIFIER = "/TEST/TEST/TEST";

    protected static final String STRAIN_NAMES_PROP = "STRAIN_NAMES";

    protected static final String VALUE_UNIT_PROP = "VALUE_UNIT";

    protected static final DataSetType TSV_MULTISTRAIN_EXPORT_DATA_SET_TYPE = new DataSetType(
            "TSV_MULTISTRAIN_EXPORT");

    protected static final DataSetType TSV_DATA_SET_TYPE = new DataSetType("TSV_EXPORT");

    /**
     *
     *
     */
    public AbstractBaSynthecDataSetRegistratorTest()
    {
        super();
    }

    protected RecordingMatcher<AtomicEntityOperationDetails> setUpDataSetRegistrationExpectations(
            final DataSetType dataSetType, final DataSetType tsvDataSetType)
    {
        return setUpDataSetRegistrationExpectations(
                dataSetType, tsvDataSetType, true);
    }

    protected RecordingMatcher<AtomicEntityOperationDetails> setUpDataSetRegistrationExpectations(
            final DataSetType dataSetType, final DataSetType tsvDataSetType,
            final boolean requireSingleStrainTsvExport)
    {
        ExperimentBuilder builder = new ExperimentBuilder().identifier(TEST_EXPERIMENT_IDENTIFIER);
        final Experiment experiment = builder.getExperiment();
        final RecordingMatcher<AtomicEntityOperationDetails> atomicatOperationDetails =
                new RecordingMatcher<AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    allowing(openBisService).heartbeat();
                    
                    one(
                            openBisService).createPermId();
                    will(returnValue(DATA_SET_CODE));

                    String excelDataSetCode = DATA_SET_CODE + "-EXCEL";
                    one(
                            openBisService).createPermId();
                    will(returnValue(excelDataSetCode));

                    // Some if there is a multistrain data set type, it needs to be taken care of in
                    // addition to the normal one
                    if (tsvDataSetType == TSV_MULTISTRAIN_EXPORT_DATA_SET_TYPE)
                    {
                        String tsvMultistrain = DATA_SET_CODE + "-TSV-MULTISTRAIN";
                        one(
                                openBisService).createPermId();
                        will(returnValue(tsvMultistrain));
                    }

                    String tsvDataSetCode = DATA_SET_CODE + "-TSV";
                    if (requireSingleStrainTsvExport)
                    {
                        one(
                                openBisService).createPermId();
                        will(returnValue(tsvDataSetCode));
                    }

                    atLeast(
                            1).of(
                            openBisService).tryGetExperiment(
                            new ExperimentIdentifierFactory(experiment.getIdentifier())
                                    .createIdentifier());
                    will(returnValue(experiment));

                    allowing(
                            openBisService).tryGetExperiment(
                            null);
                    will(returnValue(null));

                    one(
                            dataSetValidator).assertValidDataSet(
                            dataSetType, null);

                    one(
                            dataSetValidator).assertValidDataSet(
                            new DataSetType("EXCEL_ORIGINAL"),
                            new File(new File(stagingDirectory, excelDataSetCode), "xls"));

                    if (tsvDataSetType == TSV_MULTISTRAIN_EXPORT_DATA_SET_TYPE)
                    {
                        one(
                                dataSetValidator).assertValidDataSet(
                                TSV_MULTISTRAIN_EXPORT_DATA_SET_TYPE,
                                new File(new File(stagingDirectory, DATA_SET_CODE
                                        + "-TSV-MULTISTRAIN"), "tsv-multi"));
                    }

                    if (requireSingleStrainTsvExport)
                    {
                        one(
                                dataSetValidator).assertValidDataSet(
                                TSV_DATA_SET_TYPE,
                                new File(new File(stagingDirectory, tsvDataSetCode), "tsv"));
                    }

                    one(
                            openBisService).drawANewUniqueID();
                    will(returnValue(new Long(1)));

                    one(
                            openBisService).performEntityOperations(
                            with(atomicatOperationDetails));

                    will(returnValue(new AtomicEntityOperationResult()));

                    allowing(
                            openBisService).setStorageConfirmed(
                            with(any(String.class)));

                }
            });
        return atomicatOperationDetails;
    }

    protected Properties createThreadProperties()
    {
        return createThreadPropertiesRelativeToScriptsFolder(
                "data-set-handler.py", "dist/etc/shared/shared-classes.py,"
                        + getRegistrationScriptsFolderPath() + "/data-set-validator.py");
    }

    protected void createData(String fileName) throws IOException
    {
        File dataFile = new File("sourceTest/examples/" + fileName);
        FileUtils.copyFileToDirectory(
                dataFile, workingDirectory);
        incomingDataSetFile = new File(workingDirectory, dataFile.getName());

        markerFile = new File(workingDirectory, FileConstants.IS_FINISHED_PREFIX + dataFile.getName());
        FileUtilities.writeToFile(
                markerFile, "");
    }

    protected HashMap<String, NewProperty> getDataSetPropertiesMap(
            List<NewProperty> dataSetProperties)
    {
        HashMap<String, NewProperty> propertyMap = new HashMap<String, NewProperty>();
        for (NewProperty prop : dataSetProperties)
        {
            propertyMap.put(
                    prop.getPropertyCode(), prop);
        }
        return propertyMap;
    }

    protected void checkDataTypeProperty(NewExternalData dataSet, String expectedValue)
    {
        HashMap<String, NewProperty> propertyMap =
                getDataSetPropertiesMap(dataSet.getDataSetProperties());
        NewProperty property = propertyMap.get("DATA_TYPE");
        assertEquals(
                expectedValue, property.getValue());
    }

}