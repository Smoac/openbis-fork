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

package ch.systemsx.cisd.openbis.screening.systemtests;

import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil.OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.FeatureVectorStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandler;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;

/**
 * @author Kaloyan Enimanev
 */
@Test(groups =
    { "slow", "systemtest" })
public class AggregatedFeatureVectorsTest extends AbstractScreeningSystemTestCase
{
    private String sessionToken;

    private ICommonServer commonServer;
    private IScreeningServer screeningServer;

    @Override
    protected void setUpTestThread()
    {
        setUpTestThread(JythonPlateDataSetHandler.class, FeatureVectorStorageProcessor.class,
                getTestDataFolder() + "data-set-handler.py");
        
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.storage-processor.processor", DefaultStorageProcessor.class.getName());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.storage-processor.data-source", "imaging-db");
    }

    @BeforeMethod
    public void setUp()
    {
        commonServer =
                (ICommonServer) applicationContext
                        .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        screeningServer =
                (IScreeningServer) applicationContext
                        .getBean(ResourceNames.SCREENING_PLUGIN_SERVER);
        SessionContextDTO session = commonServer.tryToAuthenticate("admin", "a");
        sessionToken = session.getSessionToken();
    }


    @Test
    public void testDummy() throws Exception
    {
        File exampleDataSet = createExampleIncoming();
        moveFileToIncoming(exampleDataSet);
        // get feature vector information
        waitUntilDataSetImported();
        Material geneG = commonServer.getMaterialInfo(sessionToken, new MaterialIdentifier("G", "GENE"));
        Experiment experiment =
                commonServer.getExperimentInfo(sessionToken, ExperimentIdentifierFactory
                        .parse("/TEST/TEST-PROJECT/AGGREGATED_FEATURES_EXP"));

        MaterialReplicaFeatureSummaryResult summaryResult =
                screeningServer.getMaterialFeatureVectorSummary(
                sessionToken,
                new MaterialFeaturesOneExpCriteria(new TechId(geneG), AnalysisProcedureCriteria
                        .createNoProcedures(), new TechId(experiment)));

        assertFeatureSummary("X", 3.5, summaryResult);
        assertFeatureSummary("Y", 2.5, summaryResult);
        assertFeatureSummary("A", 15.0, summaryResult);
        assertFeatureSummary("B", 2.0, summaryResult);
    }

    private void assertFeatureSummary(String feature, double featureMedianValue,
            MaterialReplicaFeatureSummaryResult summaryResult)
    {
        MaterialReplicaFeatureSummary featureSummary = getFeatureSummary(feature, summaryResult);
        assertNotNull("No feature with name '" + feature + "' found in summary.", featureSummary);
        assertEquals(featureMedianValue, featureSummary.getFeatureVectorSummary());
    }

    protected MaterialReplicaFeatureSummary getFeatureSummary(String feature,
            MaterialReplicaFeatureSummaryResult summaryResult)
    {
        for (MaterialReplicaFeatureSummary summary : summaryResult.getFeatureSummaries())
        {
            if (summary.getFeatureDescription().getCode().equals(feature))
            {
                return summary;
            }
        }
        return null;
    }

    private File createExampleIncoming() throws IOException
    {
        File exampleDataSet = new File(workingDirectory, "test-data");
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "data-set-1.file"), "dummy1");
        FileUtilities.writeToFile(new File(exampleDataSet, "data-set-2.file"), "dummy2");
        return exampleDataSet;
    }

    private String getTestDataFolder()
    {
        return "../screening/resource/test-data/" + getClass().getSimpleName() + "/";
    }


}
