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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinResultDataSetParentLinkingTaskTest extends AssertJUnit
{
    private static final String PARENT_DATA_SET_CODES_KEY =
            DataSetInfoExtractorForProteinResults.PARENT_DATA_SET_CODES.toUpperCase();

    private static final String BASE_EXPERIMENT_KEY =
            DataSetInfoExtractorForProteinResults.EXPERIMENT_IDENTIFIER_KEY.toUpperCase();

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private ProteinResultDataSetParentLinkingTask task;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        task = new ProteinResultDataSetParentLinkingTask(service);
    }

    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        final Experiment e1 =
                new ExperimentBuilder().id(1).identifier("/S/P1/E1").property("ABC", "non-sense")
                        .getExperiment();
        final Experiment e2 =
                new ExperimentBuilder().id(2).identifier("/A/P2/E2")
                        .property(PARENT_DATA_SET_CODES_KEY, "non-sense2").getExperiment();
        final Experiment e3 =
                new ExperimentBuilder().id(3).identifier("/A/P2/E3")
                        .property(PARENT_DATA_SET_CODES_KEY, "ds1, ds3").getExperiment();
        final Experiment e4 =
                new ExperimentBuilder().id(4).identifier("/S/P1/E4")
                        .property(BASE_EXPERIMENT_KEY, "/S/P1/E1").getExperiment();
        final PhysicalDataSet ds1 =
                new DataSetBuilder(1).code("ds1").fileFormat("A").experiment(e1)
                        .version(11).getDataSet();
        final PhysicalDataSet ds2 =
                new DataSetBuilder(2).code("ds2").fileFormat("B").experiment(e4)
                        .version(22).getDataSet();
        final PhysicalDataSet ds3 =
                new DataSetBuilder(3).code("ds3").fileFormat("C").experiment(e3)
                        .version(33).property("ALPHA", "3.1").getDataSet();
        final RecordingMatcher<AtomicEntityOperationDetails> operationRecorder =
                new RecordingMatcher<AtomicEntityOperationDetails>();
        context.checking(new Expectations()
            {
                {
                    one(service).listProjects();
                    will(returnValue(Arrays.asList(e1.getProject(), e2.getProject())));

                    one(service).listExperiments(new ProjectIdentifier("S", "P1"));
                    will(returnValue(Arrays.asList(e1, e4)));

                    one(service).listExperiments(new ProjectIdentifier("A", "P2"));
                    will(returnValue(Arrays.asList(e2, e3)));

                    one(service).tryGetExperiment(ExperimentIdentifierFactory.parse("/S/P1/E1"));
                    will(returnValue(e1));

                    one(service).tryGetDataSet("non-sense2");
                    will(returnValue(null));

                    one(service).tryGetDataSet("ds1");
                    will(returnValue(ds1));

                    one(service).tryGetDataSet("ds3");
                    will(returnValue(ds3));

                    one(service).listDataSetsByExperimentID(e1.getId());
                    will(returnValue(Arrays.asList(ds1)));

                    one(service).listDataSetsByExperimentID(e3.getId());
                    will(returnValue(Arrays.asList(ds3)));

                    one(service).listDataSetsByExperimentID(e4.getId());
                    will(returnValue(Arrays.asList(ds2)));

                    one(service).performEntityOperations(with(operationRecorder));
                }
            });

        task.execute();

        assertEquals("INFO  OPERATION.ProteinResultDataSetParentLinkingTask - "
                + "Parent data set links of data set ds2 "
                + "from experiment /S/P1/E4 will be updated.\n"
                + "INFO  OPERATION.ProteinResultDataSetParentLinkingTask - "
                + "Parent data set links of data set ds3 "
                + "from experiment /A/P2/E3 will be updated.\n"
                + "INFO  OPERATION.ProteinResultDataSetParentLinkingTask - "
                + "Parent data set links for 2 data sets have been updated.",
                logRecorder.getLogContent());
        List<DataSetBatchUpdatesDTO> dataSetUpdates =
                operationRecorder.recordedObject().getDataSetUpdates();
        assertEquals(2L, dataSetUpdates.get(0).getDatasetId().getId().longValue());
        assertEquals(22, dataSetUpdates.get(0).getVersion());
        assertEquals("B", dataSetUpdates.get(0).getFileFormatTypeCode());
        assertEquals("[]", dataSetUpdates.get(0).getProperties().toString());
        assertEquals(e4.getIdentifier(), dataSetUpdates.get(0).getExperimentIdentifierOrNull()
                .toString());
        assertEquals("[ds1]",
                Arrays.asList(dataSetUpdates.get(0).getModifiedParentDatasetCodesOrNull())
                        .toString());
        assertEquals(3L, dataSetUpdates.get(1).getDatasetId().getId().longValue());
        assertEquals(33, dataSetUpdates.get(1).getVersion());
        assertEquals("C", dataSetUpdates.get(1).getFileFormatTypeCode());
        assertEquals("[ALPHA: 3.1]", dataSetUpdates.get(1).getProperties().toString());
        assertEquals(e3.getIdentifier(), dataSetUpdates.get(1).getExperimentIdentifierOrNull()
                .toString());
        assertEquals("[ds1, ds3]",
                Arrays.asList(dataSetUpdates.get(1).getModifiedParentDatasetCodesOrNull())
                        .toString());
        assertEquals(2, dataSetUpdates.size());
        context.assertIsSatisfied();
    }

}
