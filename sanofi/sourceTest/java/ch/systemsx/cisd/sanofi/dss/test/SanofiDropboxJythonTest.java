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

package ch.systemsx.cisd.sanofi.dss.test;

import static ch.systemsx.cisd.common.Constants.IS_FINISHED_PREFIX;
import static ch.systemsx.cisd.common.test.AssertionUtil.assertContains;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.DEFAULT_OVERVIEW_IMAGE_DATASET_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.DEFAULT_RAW_IMAGE_CONTAINER_DATASET_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.DEFAULT_SEGMENTATION_IMAGE_CONTAINER_DATASET_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.eodsql.MockDataSet;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.AbstractJythonDataSetHandlerTest;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.DataSetStorageAlgorithmRunner;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.jython.JythonPlateDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Kaloyan Enimanev
 */
public class SanofiDropboxJythonTest extends AbstractJythonDataSetHandlerTest
{
    private static final String PLATE_CODE = "plateCode.variant";

    private static final String LIBRARY_TEMPLATE_PROPNAME = "LIBRARY_TEMPLATE";

    private static final String EXPERIMENT_RECIPIENTS_PROPNAME = "OBSERVER_EMAILS";

    private static final String[] USER_EMAILS = new String[]
        { "donald@duck.com", "mickey@mouse.org" };

    final String[] ADMIN_EMAILS = new String[]
        { "admin@sanofi.com", "admin@openbis.org" };

    final String[] ALL_EMAILS = new String[]
        { "admin@sanofi.com", "admin@openbis.org", "donald@duck.com", "mickey@mouse.org" };

    private static final String COMPOUND_MATERIAL_TYPE = "COMPOUND";

    private static final String BATCH_MATERIAL_TYPE = "COMPOUND_BATCH";

    private static final String BATCH_MATERIAL_COMPOUND_PROP = "COMPOUND";

    private static final String BATCH_MATERIAL_SUFFIX = "BATCH_MATERIAL";

    private static final String POSITIVE_CONTROL_TYPE = "POSITIVE_CONTROL";

    private static final String NEGATIVE_CONTROL_TYPE = "NEGATIVE_CONTROL";

    private static final String COMPOUND_WELL_TYPE = "COMPOUND_WELL";

    private static final String COMPOUND_WELL_CONCENTRATION_PROPNAME = "CONCENTRATION_M";

    private static final String COMPOUND_WELL_BATCH_PROPNAME = "COMPOUND_BATCH";

    private static final String IMAGE_DATA_SET_DIR_NAME = "batchNr_plateCode.variant_2011.07.05";

    private static final String ANALYSIS_DATA_SET_FILE_NAME = "analysis";

    private static final String IMAGE_DATA_SET_CODE = "data-set-code";

    private static final DataSetType IMAGE_DATA_SET_TYPE = new DataSetType("HCS_IMAGE_RAW");

    private static final String IMAGE_DATA_SET_BATCH_PROP = "ACQUISITION_BATCH";

    private static final String OVERLAY_DATA_SET_CODE = "overlay-data-set-code";

    private static final DataSetType OVERLAY_DATA_SET_TYPE = new DataSetType(
            "HCS_IMAGE_SEGMENTATION");

    private static final String ANALYSIS_DATA_SET_CODE = "analysis-data-set-code";

    private static final DataSetType ANALYSIS_DATA_SET_TYPE = new DataSetType(
            "HCS_ANALYSIS_WELL_FEATURES");

    private static final String EXPERIMENT_IDENTIFIER = "/SANOFI/PROJECT/EXP";

    private static final String PLATE_IDENTIFIER = "/SANOFI/TEST-PLATE";

    private RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails> atomicatOperationDetails;

    private RecordingMatcher<ListMaterialCriteria> materialCriteria;

    private RecordingMatcher<String> email;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        extendJythonLibPath(getRegistrationScriptsFolderPath());

        atomicatOperationDetails =
                new RecordingMatcher<ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails>();
        materialCriteria = new RecordingMatcher<ListMaterialCriteria>();
        email = new RecordingMatcher<String>();
    }

    @Test
    public void testLibraryWider() throws IOException
    {
        createDataSetHandler(false, false);
        setUpListAdministratorExpectations();

        final Sample plate =
                plateWithLibTemplateAndGeometry("1.45\t\tH\n0.12\t0.002\tL", "10_WELLS_1X10");
        context.checking(new Expectations()
            {
                {

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ALL_EMAILS)));
                }
            });

        final String error =
                "The property LIBRARY_TEMPLATE of experiment '/SANOFI/PROJECT/EXP' contains 2 rows, "
                        + "but the geometry of plate 'TEST-PLATE' allows a maximum of 1 rows. You should either reduce the "
                        + "number of rows in the library template or change the plate geometry.";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, error);

        handler.handle(markerFile);

        assertContains(error, email.recordedObject());
        assertContains(IMAGE_DATA_SET_DIR_NAME, email.recordedObject());

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testLibraryHigher() throws IOException
    {
        createDataSetHandler(false, false);
        final Sample plate =
                plateWithLibTemplateAndGeometry("1.45\t\tH\n0.12\t0.002\tL", "5_WELLS_5X1");
        setUpListAdministratorExpectations();

        context.checking(new Expectations()
            {
                {

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ALL_EMAILS)));
                }
            });

        final String error =
                "The property LIBRARY_TEMPLATE of experiment '/SANOFI/PROJECT/EXP' contains 3 "
                        + "columns in row 1, but the geometry of plate 'TEST-PLATE' allows a maximum of "
                        + "5 columns. You should either reduce the number of columns in the library "
                        + "template or change the plate geometry.";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, error);

        handler.handle(markerFile);

        assertContains(error, email.recordedObject());
        assertContains(IMAGE_DATA_SET_DIR_NAME, email.recordedObject());

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testNoInformationInAbase() throws IOException
    {
        createDataSetHandler(false, false);
        setUpListAdministratorExpectations();

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();

        final Sample plate = plateWithLibTemplateAndGeometry("1.45\tH\n0.12\tL", "25_WELLS_5X5");
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ALL_EMAILS)));
                }
            });

        final String error = "No information for plate 'TEST-PLATE' stored in the ABASE DB.";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, error);

        handler.handle(markerFile);

        assertEquals(
                "Dear openBIS user,\n"
                        + "Registering new data for plate plateCode.variant has failed with error 'No information for plate 'TEST-PLATE' stored in the ABASE DB.'.\n"
                        + "The name of the incoming folder 'batchNr_plateCode.variant_2011.07.05' was added to '.faulty_paths'. Please,\n"
                        + "repair the problem and remove the entry from '.faulty_paths' to retry registration.\n"
                        + "       \n" + "This email has been generated automatically.\n"
                        + "      \n" + "Administrator", email.recordedObject().trim());

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testLibraryTemplateIncompleteAccordingToAbase() throws IOException
    {
        createDataSetHandler(false, false);
        setUpListAdministratorExpectations();

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1"));
        queryResult.add(createQueryResult("A3"));

        final Sample plate = plateWithLibTemplateAndGeometry("1.45\tH", "25_WELLS_5X5");
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ALL_EMAILS)));
                }
            });

        final String error =
                " Error registering library for plate 'TEST-PLATE'. The ABASE DB contains a material definition "
                        + "for well 'A3', but no valid concentration was found in the library template of experiment "
                        + "'/SANOFI/PROJECT/EXP'. The library template should contain a number for 'A3' but no value was found";
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, error);

        handler.handle(markerFile);

        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCaseWithLibraryCreation() throws IOException
    {
        createDataSetHandler(false, true);
        final Sample plate =
                plateWithLibTemplateAndGeometry("145.034E-0002\t\tH\n0.12E+04\t0.002\tL",
                        "6_WELLS_10X10");

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1"));
        queryResult.add(createQueryResult("B1"));
        queryResult.add(createQueryResult("B2"));

        setUpDataSetExpectations();
        setUpListAdministratorExpectations();

        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    exactly(2).of(openBisService).listMaterials(with(materialCriteria),
                            with(equal(true)));
                    will(returnValue(Collections.emptyList()));

                    exactly(5).of(openBisService).createPermId();
                    will(returnValue("well-permId"));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    allowing(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    allowing(openBisService)
                            .getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));

                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ALL_EMAILS)));
                }
            });

        handler.handle(markerFile);

        assertEquals(COMPOUND_MATERIAL_TYPE, materialCriteria.getRecordedObjects().get(0)
                .tryGetMaterialType().getCode());
        assertEquals(BATCH_MATERIAL_TYPE, materialCriteria.getRecordedObjects().get(1)
                .tryGetMaterialType().getCode());
        assertEquals(true, queryResult.hasCloseBeenInvoked());

        Map<String, List<NewMaterial>> registeredMaterials =
                atomicatOperationDetails.recordedObject().getMaterialRegistrations();
        assertBatchMaterialsHaveCompoundProperties(registeredMaterials);

        List<NewSample> registeredSamples =
                atomicatOperationDetails.recordedObject().getSampleRegistrations();

        assertEquals(5, registeredSamples.size());
        assertAllSamplesHaveContainer(registeredSamples, plate.getIdentifier());
        assertCompoundWell(registeredSamples, "A1", "1.45034");
        assertPositiveControl(registeredSamples, "A3");
        assertCompoundWell(registeredSamples, "B1", "1200");
        assertCompoundWell(registeredSamples, "B2", "0.002");
        assertNegativeControl(registeredSamples, "B3");

        assertDataSetsCreatedCorrectly();

        AssertionUtil
                .assertContains(
                        "New data from folder 'batchNr_plateCode.variant_2011.07.05' has been successfully registered for plate "
                                + "http://openbis-test-bw.sanofi.com:8080/openbis#entity=SAMPLE&sample_type=PLATE&action=SEARCH&code=plateCode.variant",
                        email.recordedObject());
        context.assertIsSatisfied();
    }

    private String extractAnalysisProcedureCode(NewExternalData analysisDataSet)
    {
        NewProperty property =
                EntityHelper.tryFindProperty(analysisDataSet.getDataSetProperties(),
                        ScreeningConstants.ANALYSIS_PROCEDURE);
        return (property != null) ? property.getValue() : null;
    }

    private NewExternalData find(List<? extends NewExternalData> dataSets, String typeCode)
    {
        for (NewExternalData dataSet : dataSets)
        {
            if (dataSet.getDataSetType().getCode().equals(typeCode))
            {
                return dataSet;
            }
        }
        fail(String.format("Cannot find data set of type '%s' in '%s'", typeCode, dataSets));
        // not reachable
        return null;
    }

    @Test
    public void testFatalErrorSentToAdmin() throws IOException
    {
        createDataSetHandler(false, false);
        final Sample plate = plateWithLibTemplateAndGeometry("0.75\tH\n54.12\tL", "8_WELLS_2X4");

        setUpListAdministratorExpectations();
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(throwException(new RuntimeException("Connection to ABASE DB Failed")));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());

                    one(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ADMIN_EMAILS)));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(USER_EMAILS)));

                }

            });

        handler.handle(markerFile);

        assertEquals(0, atomicatOperationDetails.getRecordedObjects().size());

        assertContains("java.lang.RuntimeException: Connection to ABASE DB Failed", email
                .getRecordedObjects().get(0));
        assertEquals(
                "Dear openBIS user,\n"
                        + "Registering new data from incoming folder 'batchNr_plateCode.variant_2011.07.05' has failed due to a system error.\n"
                        + "      \n"
                        + "openBIS has sent a notification to the responsible system administrators and they should be \n"
                        + "fixing the problem as soon as possible. \n" + "      \n"
                        + "We are sorry for any inconveniences this may have caused. \n"
                        + "      \n" + "openBIS Administrators", email.getRecordedObjects().get(1)
                        .trim());

        context.assertIsSatisfied();
    }

    @Test
    public void testLibraryTemplateWithWellNotPresentInAbase() throws IOException
    {
        createDataSetHandler(false, false);

        setUpDataSetExpectations();
        setUpListAdministratorExpectations();

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1"));

        final Sample plate = plateWithLibTemplateAndGeometry("1.45\tH\n0.12\tL", "25_WELLS_5X5");
        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    exactly(2).of(openBisService).listMaterials(with(materialCriteria),
                            with(equal(true)));
                    will(returnValue(Collections.emptyList()));

                    exactly(3).of(openBisService).createPermId();
                    will(returnValue("well-permId"));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    allowing(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    allowing(openBisService)
                            .getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));

                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ALL_EMAILS)));
                }
            });

        handler.handle(markerFile);

        assertEquals(COMPOUND_MATERIAL_TYPE, materialCriteria.getRecordedObjects().get(0)
                .tryGetMaterialType().getCode());
        assertEquals(BATCH_MATERIAL_TYPE, materialCriteria.getRecordedObjects().get(1)
                .tryGetMaterialType().getCode());
        assertEquals(true, queryResult.hasCloseBeenInvoked());

        List<NewSample> registeredSamples =
                atomicatOperationDetails.recordedObject().getSampleRegistrations();

        assertEquals(3, registeredSamples.size());
        assertAllSamplesHaveContainer(registeredSamples, plate.getIdentifier());
        assertCompoundWell(registeredSamples, "A1", "1.45");
        assertPositiveControl(registeredSamples, "A2");
        assertNegativeControl(registeredSamples, "B2");

        assertDataSetsCreatedCorrectly();

        AssertionUtil
                .assertContains(
                        "New data from folder 'batchNr_plateCode.variant_2011.07.05' has been successfully registered for plate "
                                + "http://openbis-test-bw.sanofi.com:8080/openbis#entity=SAMPLE&sample_type=PLATE&action=SEARCH&code=plateCode.variant",
                        email.recordedObject());

        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCaseWithLibraryCreationAndNonUniqueMaterials() throws IOException
    {
        createDataSetHandler(false, true);
        final Sample plate = plateWithLibTemplateAndGeometry("0.75\tH\n54.12\tL", "8_WELLS_2X4");

        final MockDataSet<Map<String, Object>> queryResult = new MockDataSet<Map<String, Object>>();
        queryResult.add(createQueryResult("A1", BATCH_MATERIAL_SUFFIX, "compound_material"));
        queryResult.add(createQueryResult("B1", BATCH_MATERIAL_SUFFIX, "compound_material"));

        setUpDataSetExpectations();
        setUpListAdministratorExpectations();

        context.checking(new Expectations()
            {
                {
                    one(dataSourceQueryService).select(with(any(String.class)),
                            with(any(String.class)), with(anything()));
                    will(returnValue(queryResult));

                    exactly(2).of(openBisService).listMaterials(with(materialCriteria),
                            with(equal(true)));
                    will(returnValue(Collections.emptyList()));

                    exactly(4).of(openBisService).createPermId();
                    will(returnValue("well-permId"));

                    SampleIdentifier sampleIdentifier =
                            SampleIdentifierFactory.parse(plate.getIdentifier());
                    allowing(openBisService).tryGetSampleWithExperiment(sampleIdentifier);
                    will(returnValue(plate));

                    allowing(openBisService)
                            .getPropertiesOfTopSampleRegisteredFor(sampleIdentifier);
                    will(returnValue(new IEntityProperty[0]));

                    one(openBisService).performEntityOperations(with(atomicatOperationDetails));
                    will(returnValue(new AtomicEntityOperationResult()));

                    one(mailClient).sendMessage(with(any(String.class)), with(email),
                            with(aNull(String.class)), with(any(From.class)),
                            with(equal(ALL_EMAILS)));
                }
            });

        handler.handle(markerFile);

        assertEquals(COMPOUND_MATERIAL_TYPE, materialCriteria.getRecordedObjects().get(0)
                .tryGetMaterialType().getCode());
        assertEquals(BATCH_MATERIAL_TYPE, materialCriteria.getRecordedObjects().get(1)
                .tryGetMaterialType().getCode());
        assertEquals(true, queryResult.hasCloseBeenInvoked());

        List<NewSample> registeredSamples =
                atomicatOperationDetails.recordedObject().getSampleRegistrations();

        assertEquals(4, registeredSamples.size());
        assertAllSamplesHaveContainer(registeredSamples, plate.getIdentifier());
        assertCompoundWell(registeredSamples, "A1", "0.75", BATCH_MATERIAL_SUFFIX,
                "compound_material");
        assertPositiveControl(registeredSamples, "A2");
        assertCompoundWell(registeredSamples, "B1", "54.12", BATCH_MATERIAL_SUFFIX,
                "compound_material");

        assertDataSetsCreatedCorrectly();

        Map<String, List<NewMaterial>> materialsRegistered =
                atomicatOperationDetails.recordedObject().getMaterialRegistrations();
        assertEquals(2, materialsRegistered.size());

        final List<NewMaterial> compoundMaterialsRegistered =
                materialsRegistered.get(COMPOUND_MATERIAL_TYPE);
        assertEquals(1, compoundMaterialsRegistered.size());
        assertEquals("compound_material", compoundMaterialsRegistered.get(0).getCode());

        final List<NewMaterial> batchMaterialsRegistered =
                materialsRegistered.get(BATCH_MATERIAL_TYPE);
        assertEquals(1, batchMaterialsRegistered.size());
        assertEquals(BATCH_MATERIAL_SUFFIX, batchMaterialsRegistered.get(0).getCode());

        AssertionUtil
                .assertContains(
                        "New data from folder 'batchNr_plateCode.variant_2011.07.05' has been successfully registered for plate "
                                + "http://openbis-test-bw.sanofi.com:8080/openbis#entity=SAMPLE&sample_type=PLATE&action=SEARCH&code=plateCode.variant",
                        email.recordedObject());
        context.assertIsSatisfied();
    }

    private void assertHasProperty(NewExternalData dataSet, String propCode, String propValue)
    {
        for (NewProperty prop : dataSet.getDataSetProperties())
        {
            if (prop.getPropertyCode().equals(propCode))
            {
                assertEquals("Invalid value in property " + propCode, propValue, prop.getValue());
                return;
            }
        }

        fail(String.format("No property with code %s was found in data set %s", propCode,
                dataSet.getCode()));

    }

    private void assertAllSamplesHaveContainer(List<NewSample> newSamples,
            String containerIdentifier)
    {
        for (NewSample newSample : newSamples)
        {
            assertEquals(containerIdentifier, newSample.getContainerIdentifier());
        }
    }

    private NewSample findByWellCode(List<NewSample> newSamples, String wellCode)
    {
        for (NewSample newSample : newSamples)
        {
            if (newSample.getIdentifier().endsWith(":" + wellCode))
            {
                return newSample;
            }
        }
        throw new RuntimeException("Failed to find sample registration for well " + wellCode);
    }

    private void assertBatchMaterialsHaveCompoundProperties(
            Map<String, List<NewMaterial>> registeredMaterials)
    {

        for (String materialTypeCode : registeredMaterials.keySet())
        {
            if (materialTypeCode.endsWith(BATCH_MATERIAL_SUFFIX))
            {
                for (NewMaterial newMaterial : registeredMaterials.get(materialTypeCode))
                {
                    IEntityProperty property =
                            EntityHelper.tryFindProperty(newMaterial.getProperties(),
                                    BATCH_MATERIAL_COMPOUND_PROP);
                    assertNotNull(property);
                    assertTrue(property.getValue().endsWith(" (COMPOUND)"));
                }
            }
        }

    }

    private void assertNegativeControl(List<NewSample> newSamples, String wellCode)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(NEGATIVE_CONTROL_TYPE, newSample.getSampleType().getCode());
        assertEquals(0, newSample.getProperties().length);
    }

    private void assertPositiveControl(List<NewSample> newSamples, String wellCode)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(POSITIVE_CONTROL_TYPE, newSample.getSampleType().getCode());
        assertEquals(0, newSample.getProperties().length);
    }

    private void assertCompoundWell(List<NewSample> newSamples, String wellCode,
            String concentration)
    {
        String batchMaterialCode = getBatchMaterialCodeByWellCode(wellCode);
        String compoundMaterialCode = getCompoundMaterialCodeByWellCode(wellCode);
        assertCompoundWell(newSamples, wellCode, concentration, batchMaterialCode,
                compoundMaterialCode);
    }

    private void assertCompoundWell(List<NewSample> newSamples, String wellCode,
            String concentration, String batchMaterialCode, String compoundMaterialCode)
    {
        NewSample newSample = findByWellCode(newSamples, wellCode);
        assertEquals(COMPOUND_WELL_TYPE, newSample.getSampleType().getCode());

        IEntityProperty concentrationProp =
                EntityHelper.tryFindProperty(newSample.getProperties(),
                        COMPOUND_WELL_CONCENTRATION_PROPNAME);
        assertNotNull(concentrationProp);
        assertEquals("Invalid concentration value for well '" + wellCode + "': ", concentration,
                concentrationProp.tryGetAsString());

        MaterialIdentifier batchMaterialIdentifier =
                new MaterialIdentifier(batchMaterialCode, BATCH_MATERIAL_TYPE);
        IEntityProperty batchMaterialProp =
                EntityHelper.tryFindProperty(newSample.getProperties(),
                        COMPOUND_WELL_BATCH_PROPNAME);
        assertNotNull(batchMaterialProp);
        assertEquals("Invalid batch material found in well '" + wellCode + "': ",
                batchMaterialIdentifier.print(), batchMaterialProp.tryGetAsString());
    }

    protected void assertDataSetsCreatedCorrectly()
    {
        List<? extends NewExternalData> dataSetsRegistered =
                atomicatOperationDetails.recordedObject().getDataSetRegistrations();
        assertEquals(7, dataSetsRegistered.size());

        NewExternalData imageDataSet = find(dataSetsRegistered, IMAGE_DATA_SET_TYPE.getCode());
        assertEquals(IMAGE_DATA_SET_CODE, imageDataSet.getCode());

        NewExternalData imageContainer =
                find(dataSetsRegistered, DEFAULT_RAW_IMAGE_CONTAINER_DATASET_TYPE);
        assertHasProperty(imageContainer, IMAGE_DATA_SET_BATCH_PROP, "batchNr");

        NewExternalData analysisDataSet =
                find(dataSetsRegistered, ANALYSIS_DATA_SET_TYPE.getCode());
        assertEquals(ANALYSIS_DATA_SET_CODE, analysisDataSet.getCode());
        assertEquals("MatlabGeneral_v1.1_NRF2ProdAnalysis_v1.1",
                extractAnalysisProcedureCode(analysisDataSet));

        NewExternalData overlayDataSet = find(dataSetsRegistered, OVERLAY_DATA_SET_TYPE.getCode());
        assertEquals(OVERLAY_DATA_SET_CODE, overlayDataSet.getCode());
    }

    public Sample plateWithLibTemplateAndGeometry(String libraryTemplate, String plateGeometry)
            throws IOException
    {
        Sample plate = createPlate(libraryTemplate, plateGeometry);
        setUpPlateSearchExpectations(plate);
        setUpLibraryTemplateExpectations(plate);
        return plate;
    }

    private void createDataSetHandler(boolean shouldRegistrationFail, boolean rethrowExceptions)
            throws IOException
    {
        setUpHomeDataBaseExpectations();
        Properties properties =
                createThreadPropertiesRelativeToScriptsFolder("dropbox-all-in-one-with-library.py");
        createHandler(properties, shouldRegistrationFail, rethrowExceptions);
        createData();
    }

    private void setUpDataSetExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBisService).createDataSetCode();
                    will(returnValue("image-raw-thumnails"));

                    one(openBisService).createDataSetCode();
                    will(returnValue(IMAGE_DATA_SET_CODE));

                    one(openBisService).createDataSetCode();
                    will(returnValue("image-raw-container"));

                    one(openBisService).createDataSetCode();
                    will(returnValue(ANALYSIS_DATA_SET_CODE));

                    one(openBisService).createDataSetCode();
                    will(returnValue("overlay-thumnails"));

                    one(openBisService).createDataSetCode();
                    will(returnValue(OVERLAY_DATA_SET_CODE));

                    one(openBisService).createDataSetCode();
                    will(returnValue("overlay-container"));

                    final DataSetType thumnailsDataSetType =
                            new DataSetType(DEFAULT_OVERVIEW_IMAGE_DATASET_TYPE);

                    one(dataSetValidator).assertValidDataSet(
                            thumnailsDataSetType,
                            new File(new File(stagingDirectory, "image-raw-thumnails"),
                                    "thumbnails.h5"));

                    one(dataSetValidator).assertValidDataSet(IMAGE_DATA_SET_TYPE,
                            new File(new File(stagingDirectory, IMAGE_DATA_SET_CODE), "original"));

                    one(dataSetValidator).assertValidDataSet(
                            new DataSetType(DEFAULT_RAW_IMAGE_CONTAINER_DATASET_TYPE), null);

                    one(dataSetValidator).assertValidDataSet(
                            thumnailsDataSetType,
                            new File(new File(stagingDirectory, "overlay-thumnails"),
                                    "thumbnails.h5"));

                    one(dataSetValidator)
                            .assertValidDataSet(
                                    OVERLAY_DATA_SET_TYPE,
                                    new File(new File(stagingDirectory, OVERLAY_DATA_SET_CODE),
                                            "original"));

                    one(dataSetValidator).assertValidDataSet(
                            new DataSetType(DEFAULT_SEGMENTATION_IMAGE_CONTAINER_DATASET_TYPE),
                            null);

                    one(dataSetValidator).assertValidDataSet(
                            ANALYSIS_DATA_SET_TYPE,
                            new File(new File(stagingDirectory, ANALYSIS_DATA_SET_CODE),
                                    ANALYSIS_DATA_SET_FILE_NAME));

                    allowing(openBisService).setStorageConfirmed(with(any(String.class)));
                }
            });
    }

    private void setUpPlateSearchExpectations(final Sample plate)
    {
        context.checking(new Expectations()
            {
                {
                    SearchCriteria sc = new SearchCriteria();
                    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                            "PLATE"));
                    sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                            PLATE_CODE));
                    oneOf(openBisService).searchForSamples(sc);

                    will(returnValue(Arrays.asList(plate)));
                }
            });
    }

    private void setUpLibraryTemplateExpectations(final Sample plate)
    {
        context.checking(new Expectations()
            {
                {
                    final String identifierString = plate.getExperiment().getIdentifier();
                    ExperimentIdentifier identifier =
                            ExperimentIdentifierFactory.parse(identifierString);
                    oneOf(openBisService).tryToGetExperiment(identifier);
                    will(returnValue(plate.getExperiment()));
                }
            });
    }

    private void setUpListAdministratorExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(openBisService).listAdministrators();
                    List<String> adminEmailsList = Arrays.asList(ADMIN_EMAILS);
                    will(returnValue(createAdministrators(adminEmailsList)));
                }
            });
    }

    private List<Person> createAdministrators(List<String> adminEmails)
    {
        List<Person> result = new ArrayList<Person>();
        for (String adminEmail : adminEmails)
        {
            Person person = new Person();
            person.setEmail(adminEmail);
            result.add(person);
        }
        return result;
    }

    private void createData() throws IOException
    {
        File dataDirectory = new File("./sourceTest/examples/" + IMAGE_DATA_SET_DIR_NAME);
        FileUtils.copyDirectoryToDirectory(dataDirectory, workingDirectory);
        incomingDataSetFile = new File(workingDirectory, dataDirectory.getName());

        markerFile = new File(workingDirectory, IS_FINISHED_PREFIX + dataDirectory.getName());
        FileUtilities.writeToFile(markerFile, "");
    }

    private Sample createPlate(String libraryTemplate, String plateGeometry)
    {
        ExperimentBuilder experimentBuilder = new ExperimentBuilder();
        experimentBuilder.identifier(EXPERIMENT_IDENTIFIER);
        experimentBuilder.property(LIBRARY_TEMPLATE_PROPNAME, libraryTemplate);
        String recipients = StringUtils.join(Arrays.asList(USER_EMAILS), ",");
        experimentBuilder.property(EXPERIMENT_RECIPIENTS_PROPNAME, recipients);

        SampleBuilder sampleBuilder = new SampleBuilder();
        sampleBuilder.experiment(experimentBuilder.getExperiment());
        sampleBuilder.identifier(PLATE_IDENTIFIER);
        sampleBuilder.property(ScreeningConstants.PLATE_GEOMETRY, plateGeometry);

        final Sample plate = sampleBuilder.getSample();
        return plate;
    }

    private Map<String, Object> createQueryResult(String wellCode)
    {
        return createQueryResult(wellCode, getBatchMaterialCodeByWellCode(wellCode),
                getCompoundMaterialCodeByWellCode(wellCode));
    }

    private Map<String, Object> createQueryResult(String wellCode, String batchMaterialCode,
            String compoundMaterialCode)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("WELL_CODE", wellCode);
        result.put("MATERIAL_CODE", batchMaterialCode);
        result.put("ABASE_COMPOUND_ID", compoundMaterialCode);
        return result;
    }

    private String getBatchMaterialCodeByWellCode(String wellCode)
    {
        return wellCode + "_" + BATCH_MATERIAL_SUFFIX;
    }

    private String getCompoundMaterialCodeByWellCode(String wellCode)
    {
        return wellCode + "_compound_material";
    }

    @Override
    protected String getRegistrationScriptsFolderPath()
    {
        return "dist/etc/sanofi-dropbox/";
    }

    @Override
    protected void createHandler(Properties threadProperties, final boolean registrationShouldFail,
            boolean shouldReThrowException)
    {
        TopLevelDataSetRegistratorGlobalState globalState = createGlobalState(threadProperties);

        handler =
                new TestingPlateDataSetHandler(globalState, registrationShouldFail,
                        shouldReThrowException);

    }

    private class TestingPlateDataSetHandler extends JythonPlateDataSetHandler
    {
        private final boolean shouldRegistrationFail;

        private final boolean shouldReThrowRollbackException;

        public TestingPlateDataSetHandler(TopLevelDataSetRegistratorGlobalState globalState,
                boolean shouldRegistrationFail, boolean shouldReThrowRollbackException)
        {
            super(globalState);
            this.shouldRegistrationFail = shouldRegistrationFail;
            this.shouldReThrowRollbackException = shouldReThrowRollbackException;
        }

        @Override
        public void registerDataSetInApplicationServer(DataSetInformation dataSetInformation,
                NewExternalData data) throws Throwable
        {
            if (shouldRegistrationFail)
            {
                throw new UserFailureException("Didn't work.");
            } else
            {
                super.registerDataSetInApplicationServer(dataSetInformation, data);
            }
        }

        @Override
        public void rollback(DataSetRegistrationService<DataSetInformation> service,
                Throwable throwable)
        {
            super.rollback(service, throwable);
            if (shouldReThrowRollbackException)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            } else
            {
                throwable.printStackTrace();
            }
        }

        @Override
        public void didRollbackTransaction(DataSetRegistrationService<DataSetInformation> service,
                DataSetRegistrationTransaction<DataSetInformation> transaction,
                DataSetStorageAlgorithmRunner<DataSetInformation> algorithmRunner,
                Throwable throwable)
        {
            super.didRollbackTransaction(service, transaction, algorithmRunner, throwable);

            if (shouldReThrowRollbackException)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
            } else
            {
                throwable.printStackTrace();
            }
        }

    }

}