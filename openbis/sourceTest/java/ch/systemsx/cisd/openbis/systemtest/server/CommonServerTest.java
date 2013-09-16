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

package ch.systemsx.cisd.openbis.systemtest.server;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.ExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.ExternalDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.MaterialAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.SampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityValidationEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author pkupczyk
 */
@Test(groups = "system test")
public class CommonServerTest extends SystemTestCase
{

    private String sessionToken;

    private BufferedAppender logRecorder;

    @BeforeClass
    public void beforeClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.DEBUG);
    }

    @AfterClass
    public void afterClass()
    {
        Logger.getLogger("OPERATION.Resources").setLevel(Level.INFO);
    }

    @BeforeMethod
    public void beforeMethod()
    {
        sessionToken = commonServer.tryAuthenticate("test", "a").getSessionToken();
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        commonServer.logout(sessionToken);
    }

    @Test
    public void testExperimentAdaptor()
    {
        testAdaptorCommon(EntityKind.EXPERIMENT, "/CISD/DEFAULT/EXP-REUSE",
                "experiment_adaptor_test.py");
        assertEntitiesReleased(EntityKind.EXPERIMENT, "EXP-REUSE");
    }

    @Test
    public void testExperimentAdaptorSamples()
    {
        testAdaptorCommon(EntityKind.EXPERIMENT, "/CISD/DEFAULT/EXP-REUSE",
                "experiment_adaptor_test__samples.py");
        assertEntitiesReleased(EntityKind.EXPERIMENT, "EXP-REUSE");
        assertEntitiesReleased(EntityKind.SAMPLE, "CP1-A1", "CP1-A2", "CP1-B1", "CP2-A1", "RP1-A2X", "RP1-B1X", "RP2-A1X");
        assertScrollableResultsReleased(3);
    }

    @Test
    public void testExperimentAdaptorDataSets()
    {
        testAdaptorCommon(EntityKind.EXPERIMENT, "/CISD/DEFAULT/EXP-REUSE",
                "experiment_adaptor_test__datasets.py");
    }

    @Test
    public void testSampleAdaptor()
    {
        testAdaptorCommon(EntityKind.SAMPLE, "/CISD/CP-TEST-1", "sample_adaptor_test.py");
    }

    @Test
    public void testSampleAdaptorExperiment()
    {
        testAdaptorCommon(EntityKind.SAMPLE, "/CISD/3VCP6", "sample_adaptor_test__experiment.py");
    }

    @Test
    public void testSampleAdaptorParents()
    {
        testAdaptorCommon(EntityKind.SAMPLE, "/CISD/3VCP6", "sample_adaptor_test__parents.py");
    }

    @Test
    public void testSampleAdaptorChildren()
    {
        testAdaptorCommon(EntityKind.SAMPLE, "/CISD/DP1-A", "sample_adaptor_test__children.py");
    }

    @Test
    public void testSampleAdaptorDataSets()
    {
        testAdaptorCommon(EntityKind.SAMPLE, "/CISD/CP-TEST-3", "sample_adaptor_test__datasets.py");
    }

    @Test
    public void testSampleAdaptorContainer()
    {
        testAdaptorCommon(EntityKind.SAMPLE, "/CISD/CL1:A01", "sample_adaptor_test__container.py");
    }

    @Test
    public void testSampleAdaptorContained()
    {
        testAdaptorCommon(EntityKind.SAMPLE, "/CISD/CL1", "sample_adaptor_test__contained.py");
    }

    @Test
    public void testDataSetAdaptor()
    {
        testAdaptorCommon(EntityKind.DATA_SET, "20081105092159111-1", "dataset_adaptor_test.py");
    }

    @Test
    public void testDataSetAdaptorExperiment()
    {
        testAdaptorCommon(EntityKind.DATA_SET, "20081105092159188-3",
                "dataset_adaptor_test__experiment.py");
    }

    @Test
    public void testDataSetAdaptorSample()
    {
        testAdaptorCommon(EntityKind.DATA_SET, "20081105092159222-2",
                "dataset_adaptor_test__sample.py");
    }

    @Test
    public void testDataSetAdaptorParents()
    {
        testAdaptorCommon(EntityKind.DATA_SET, "20081105092259000-9",
                "dataset_adaptor_test__parents.py");
    }

    @Test
    public void testDataSetAdaptorChildren()
    {
        testAdaptorCommon(EntityKind.DATA_SET, "20081105092259000-9",
                "dataset_adaptor_test__children.py");
    }

    @Test
    public void testDataSetAdaptorContainer()
    {
        testAdaptorCommon(EntityKind.DATA_SET, "20110509092359990-11",
                "dataset_adaptor_test__container.py");
    }

    @Test
    public void testDataSetAdaptorContained()
    {
        testAdaptorCommon(EntityKind.DATA_SET, "20110509092359990-10",
                "dataset_adaptor_test__contained.py");
    }

    @Test
    public void testMaterialAdaptor()
    {
        testAdaptorCommon(EntityKind.MATERIAL, "C-NO-TIME (CONTROL)", "material_adaptor_test.py");
    }

    private void testAdaptorCommon(EntityKind entityKind, String entityIdentifier, String scriptName)
    {
        TestResources resources = new TestResources(getClass());
        String commonScript = FileUtilities.loadToString(resources.getResourceFile("common_adaptor_test.py"));
        String specificScript = FileUtilities.loadToString(resources.getResourceFile(scriptName));
        String script = commonScript + "\n" + specificScript;

        EntityValidationEvaluationInfo info =
                new EntityValidationEvaluationInfo(entityKind, entityIdentifier, false,
                        PluginType.JYTHON, "common_adaptor_test.py", script);

        String result = commonServer.evaluate(sessionToken, info);
        Assert.assertEquals("Validation OK", result);
    }

    private void assertEntitiesReleased(EntityKind kind, String... codes)
    {
        Class<?> adaptorClass = null;

        if (EntityKind.EXPERIMENT.equals(kind))
        {
            adaptorClass = ExperimentAdaptor.class;
        } else if (EntityKind.SAMPLE.equals(kind))
        {
            adaptorClass = SampleAdaptor.class;
        } else if (EntityKind.DATA_SET.equals(kind))
        {
            adaptorClass = ExternalDataAdaptor.class;
        } else if (EntityKind.MATERIAL.equals(kind))
        {
            adaptorClass = MaterialAdaptor.class;
        } else
        {
            throw new RuntimeException("Unsupported entity kind: " + kind);
        }

        for (String code : codes)
        {
            AssertionUtil.assertContainsLines("DEBUG OPERATION.Resources - Successfully released a resource: " + adaptorClass.getSimpleName()
                    + "{code="
                    + code + "}",
                    logRecorder.getLogContent());
        }
    }

    private void assertScrollableResultsReleased(int count)
    {
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            expected.append("DEBUG OPERATION.Resources - Successfully released a resource: ScrollableResultsIterator.ScrollableResultReleasable{}\n");
        }
        AssertionUtil.assertContainsLines(expected.toString(), logRecorder.getLogContent());
    }

}