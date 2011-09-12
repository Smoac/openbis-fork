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

package ch.systemsx.cisd.openbis.generic.client.jython.api.v1.impl;

import java.io.File;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.AssertingLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Kaloyan Enimanev
 */
public class JythonPluginScriptRunnerTest extends AssertJUnit
{

    private static final String SESSION_TOKEN = "SESSION_TOKEN";

    private static final String SCRIPTS_FOLDER =
            "sourceTest/java/ch/systemsx/cisd/openbis/plugin/jython/api/v1/impl";

    private Mockery context;

    private ICommonServer commonServer;

    private JythonPluginScriptRunner pluginScriptRunner;

    private AssertingLogger errorLogger;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        commonServer = context.mock(ICommonServer.class);
        EncapsulatedCommonServer encapsulatedServer =
                new EncapsulatedCommonServer(commonServer, SESSION_TOKEN);
        errorLogger = new AssertingLogger();
        pluginScriptRunner = new JythonPluginScriptRunner(encapsulatedServer, errorLogger);

    }

    @Test
    public void testSimpleTransaction()
    {
        final RecordingMatcher<ExperimentType> experimentTypeMatcher =
                new RecordingMatcher<ExperimentType>();
        final RecordingMatcher<SampleType> sampleTypeMatcher = new RecordingMatcher<SampleType>();
        final RecordingMatcher<DataSetType> dataSetTypeMatcher =
                new RecordingMatcher<DataSetType>();
        final RecordingMatcher<MaterialType> materialTypeMatcher =
                new RecordingMatcher<MaterialType>();
        final RecordingMatcher<PropertyType> propertyTypeMatcher =
                new RecordingMatcher<PropertyType>();
        final RecordingMatcher<NewETPTAssignment> assignmentMatcher =
                new RecordingMatcher<NewETPTAssignment>();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).registerExperimentType(with(equal(SESSION_TOKEN)),
                            with(experimentTypeMatcher));
                    one(commonServer).registerSampleType(with(equal(SESSION_TOKEN)),
                            with(sampleTypeMatcher));
                    one(commonServer).registerDataSetType(with(equal(SESSION_TOKEN)),
                            with(dataSetTypeMatcher));
                    one(commonServer).registerMaterialType(with(equal(SESSION_TOKEN)),
                            with(materialTypeMatcher));
                    exactly(2).of(commonServer).registerPropertyType(with(equal(SESSION_TOKEN)),
                            with(propertyTypeMatcher));
                    exactly(2).of(commonServer).assignPropertyType(with(equal(SESSION_TOKEN)),
                            with(assignmentMatcher));
                }
            });

        File scriptFile = getScriptFile("simple-transaction.py");
        pluginScriptRunner.executeScript(scriptFile);
        errorLogger.assertNumberOfMessage(0);

        assertEquals(1, experimentTypeMatcher.getRecordedObjects().size());
        ExperimentType experimentType = experimentTypeMatcher.recordedObject();
        assertEquals("EXPERIMENT-TYPE", experimentType.getCode());
        assertEquals("Experiment type description.", experimentType.getDescription());

        assertEquals(1, sampleTypeMatcher.getRecordedObjects().size());
        SampleType sampleType = sampleTypeMatcher.recordedObject();
        assertEquals("SAMPLE-TYPE", sampleType.getCode());
        assertEquals(true, sampleType.isListable());
        assertEquals(true, sampleType.isSubcodeUnique());
        assertEquals(true, sampleType.isAutoGeneratedCode());
        assertEquals("G_", sampleType.getGeneratedCodePrefix());
        assertEquals("Sample type description.", sampleType.getDescription());

        assertEquals(1, dataSetTypeMatcher.getRecordedObjects().size());
        DataSetType dataSetType = dataSetTypeMatcher.recordedObject();
        assertEquals("DATA-SET-TYPE", dataSetType.getCode());
        assertEquals(true, dataSetType.isContainerType());
        assertEquals("Data set type description.", dataSetType.getDescription());

        assertEquals(1, materialTypeMatcher.getRecordedObjects().size());
        MaterialType materialType = materialTypeMatcher.recordedObject();
        assertEquals("MATERIAL-TYPE", materialType.getCode());
        assertEquals("Material type description.", materialType.getDescription());

        assertEquals(2, propertyTypeMatcher.getRecordedObjects().size());
        PropertyType p1 = propertyTypeMatcher.getRecordedObjects().get(0);
        assertEquals("VARCHAR-PROPERTY-TYPE", p1.getCode());
        assertEquals("Varchar property type description.", p1.getDescription());
        assertEquals("STRING", p1.getLabel());

        PropertyType p2 = propertyTypeMatcher.getRecordedObjects().get(1);
        assertEquals("MATERIAL-PROPERTY-TYPE", p2.getCode());
        assertEquals("Material property type description.", p2.getDescription());
        assertEquals("MATERIAL", p2.getLabel());
        assertEquals("MATERIAL-TYPE", p2.getMaterialType().getCode());

        assertEquals(2, assignmentMatcher.getRecordedObjects().size());
        NewETPTAssignment a1 = assignmentMatcher.getRecordedObjects().get(0);
        assertEquals(EntityKind.SAMPLE, a1.getEntityKind());
        assertEquals("SAMPLE-TYPE", a1.getEntityTypeCode());
        assertEquals("MATERIAL-PROPERTY-TYPE", a1.getPropertyTypeCode());
        assertEquals(true, a1.isMandatory());

        NewETPTAssignment a2 = assignmentMatcher.getRecordedObjects().get(1);
        assertEquals(EntityKind.EXPERIMENT, a2.getEntityKind());
        assertEquals("EXPERIMENT-TYPE", a2.getEntityTypeCode());
        assertEquals("VARCHAR-PROPERTY-TYPE", a2.getPropertyTypeCode());
        assertEquals("Default STRING Value", a2.getDefaultValue());
        assertEquals(false, a2.isMandatory());

        context.assertIsSatisfied();
    }

    @Test
    public void testErrorsLogged()
    {
        context.checking(new Expectations()
            {
                {
                    one(commonServer).registerExperimentType(with(any(String.class)),
                            with(any(ExperimentType.class)));
                    will(throwException(new RuntimeException("FAILED1")));
                    one(commonServer).registerSampleType(with(any(String.class)),
                            with(any(SampleType.class)));
                    will(throwException(new RuntimeException("FAILED2")));
                    one(commonServer).registerDataSetType(with(any(String.class)),
                            with(any(DataSetType.class)));
                    will(throwException(new RuntimeException("FAILED3")));
                    one(commonServer).registerMaterialType(with(any(String.class)),
                            with(any(MaterialType.class)));
                    will(throwException(new RuntimeException("FAILED4")));
                    exactly(2).of(commonServer).registerPropertyType(with(any(String.class)),
                            with(any(PropertyType.class)));
                    will(throwException(new RuntimeException("FAILED5")));
                    exactly(2).of(commonServer).assignPropertyType(with(any(String.class)),
                            with(any(NewETPTAssignment.class)));
                    will(throwException(new RuntimeException("FAILED6")));
                }
            });

        File scriptFile = getScriptFile("simple-transaction.py");
        pluginScriptRunner.executeScript(scriptFile);
        errorLogger.assertNumberOfMessage(9);
        errorLogger.assertMatches(0, LogLevel.ERROR,
                "Failed to commit all transactions for script .*");
        errorLogger.assertEq(1, LogLevel.ERROR,
                "Failed to register type 'EXPERIMENT-TYPE': FAILED1");
        errorLogger.assertEq(2, LogLevel.ERROR, "Failed to register type 'SAMPLE-TYPE': FAILED2");
        errorLogger.assertEq(3, LogLevel.ERROR, "Failed to register type 'DATA-SET-TYPE': FAILED3");
        errorLogger.assertEq(4, LogLevel.ERROR, "Failed to register type 'MATERIAL-TYPE': FAILED4");
        errorLogger.assertEq(5, LogLevel.ERROR,
                "Failed to register type 'VARCHAR-PROPERTY-TYPE': FAILED5");
        errorLogger.assertEq(6, LogLevel.ERROR,
                "Failed to register type 'MATERIAL-PROPERTY-TYPE': FAILED5");
        errorLogger.assertEq(7, LogLevel.ERROR,
                "Failed to assign property 'SAMPLE-TYPE' <-> 'MATERIAL-PROPERTY-TYPE': FAILED6");
        errorLogger.assertEq(8, LogLevel.ERROR,
                "Failed to assign property 'EXPERIMENT-TYPE' <-> 'VARCHAR-PROPERTY-TYPE': FAILED6");
    }

    private File getScriptFile(String scriptFilename)
    {

        return new File(SCRIPTS_FOLDER, scriptFilename);
    }
}
