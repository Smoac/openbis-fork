/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_PERSON;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.openbis.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewLinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * @author Franz-Josef Elmer
 */
// TODO 2009-09-10, Piotr Buczek: write tests with many parents and cycle check
public class DataBOTest extends AbstractBOTest
{
    private static final String EXTERNAL_DATA_MANAGEMENT_SYSTEM_CODE = "dms";

    private static final int SPEED_HINT = (Constants.DEFAULT_SPEED_HINT + Constants.MAX_SPEED) / 2;

    private static final TechId TECH_ID = new TechId(42l);

    private static final DatabaseInstanceIdentifier DATABASE_INSTANCE_IDENTIFIER =
            new DatabaseInstanceIdentifier(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE.getCode());

    private static final SpaceIdentifier SPACE_IDENTIFIER = new SpaceIdentifier(
            DATABASE_INSTANCE_IDENTIFIER, ManagerTestTool.EXAMPLE_GROUP.getCode());

    private static final SampleIdentifier SAMPLE_IDENTIFIER = new SampleIdentifier(
            SPACE_IDENTIFIER, "EXAMPLE_SAMPLE");

    private static final ExperimentIdentifier EXPERIMENT_IDENTIFIER = new ExperimentIdentifier(
            new ProjectIdentifier(SPACE_IDENTIFIER, ManagerTestTool.EXAMPLE_PROJECT.getCode()),
            "EXPERIMENT_CODE");

    private static final String DATA_STORE_CODE = "dss1";

    private static final String PARENT_CODE = "parent1";

    private static final String COMPONENT_CODE = "component1";

    private static final Date PRODUCTION_DATE = new Date(1001);

    private static final String DATA_PRODUCER_CODE = "DPC";

    private static final String DATA_SET_CODE = "DS1";

    private static final String LOCATION = "folder/subfolder/file.fft";

    private static final LocatorType LOCATOR_TYPE = new LocatorType("LT");

    private static final FileFormatType FILE_FORMAT_TYPE = new FileFormatType("FFT");

    private static final DataSetType DATA_SET_TYPE = new DataSetType("DST");

    private static final class DataMatcher extends BaseMatcher<ExternalDataPE>
    {
        @Override
        public void describeTo(Description description)
        {
            description.appendText(DATA_SET_CODE);
        }

        @Override
        public boolean matches(Object item)
        {
            if (item instanceof DataPE)
            {
                DataPE data = (DataPE) item;
                return data.getCode().equals(DATA_SET_CODE);
            }
            return false;
        }
    }

    @Test
    public void testDefineWithDirectSampleConnection()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.PHYSICAL);
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        SamplePE sample = new SamplePE();
        ExperimentPE experimentPE = new ExperimentPE();
        sample.setExperiment(experimentPE);
        prepareDefineExternalData(dataSetType, fileFormatType, vocabulary, locatorType,
                new DataStorePE());

        IDataBO dataBO = createDataBO();
        dataBO.define(createDataSet(null), sample, SourceType.DERIVED);
        ExternalDataPE externalData = dataBO.getData().tryAsExternalData();

        assertNotNull(externalData);
        assertEquals(DATA_SET_CODE, externalData.getCode());
        assertEquals(BooleanOrUnknown.U, externalData.getComplete());
        assertEquals(DATA_PRODUCER_CODE, externalData.getDataProducerCode());
        assertSame(dataSetType, externalData.getDataSetType());
        assertSame(fileFormatType, externalData.getFileFormatType());
        assertSame(locatorType, externalData.getLocatorType());
        assertEquals(LOCATION, externalData.getLocation());
        assertEquals(0, externalData.getParents().size());
        assertSame(experimentPE, externalData.getExperiment());
        assertEquals(PRODUCTION_DATE, externalData.getProductionDate());
        assertSame(sample, externalData.tryGetSample());
        assertSame(true, externalData.isDerived());
        assertEquals(StorageFormat.PROPRIETARY, externalData.getStorageFormat());
        assertSame(vocabularyTerm, externalData.getStorageFormatVocabularyTerm());
        assertEquals(null, externalData.getRegistrator());
        assertEquals(SPEED_HINT, externalData.getSpeedHint());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithUserID()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.PHYSICAL);
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        SamplePE sample = new SamplePE();
        ExperimentPE experimentPE = new ExperimentPE();
        sample.setExperiment(experimentPE);
        prepareDefineExternalData(dataSetType, fileFormatType, vocabulary, locatorType,
                new DataStorePE());
        final NewExternalData data = createDataSet(null);
        data.setUserId("my-id");
        final PersonPE registrator = new PersonPE();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(data.getUserId());
                    will(returnValue(registrator));
                }
            });

        IDataBO dataBO = createDataBO();
        dataBO.define(data, sample, SourceType.DERIVED);
        ExternalDataPE externalData = dataBO.getData().tryAsExternalData();

        assertNotNull(externalData);
        assertEquals(DATA_SET_CODE, externalData.getCode());
        assertEquals(BooleanOrUnknown.U, externalData.getComplete());
        assertEquals(DATA_PRODUCER_CODE, externalData.getDataProducerCode());
        assertSame(dataSetType, externalData.getDataSetType());
        assertSame(fileFormatType, externalData.getFileFormatType());
        assertSame(locatorType, externalData.getLocatorType());
        assertEquals(LOCATION, externalData.getLocation());
        assertEquals(0, externalData.getParents().size());
        assertSame(experimentPE, externalData.getExperiment());
        assertEquals(PRODUCTION_DATE, externalData.getProductionDate());
        assertSame(sample, externalData.tryGetSample());
        assertSame(true, externalData.isDerived());
        assertEquals(StorageFormat.PROPRIETARY, externalData.getStorageFormat());
        assertSame(vocabularyTerm, externalData.getStorageFormatVocabularyTerm());
        assertSame(registrator, externalData.getRegistrator());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithUserEMail()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.PHYSICAL);
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        SamplePE sample = new SamplePE();
        ExperimentPE experimentPE = new ExperimentPE();
        sample.setExperiment(experimentPE);
        prepareDefineExternalData(dataSetType, fileFormatType, vocabulary, locatorType,
                new DataStorePE());
        final NewExternalData data = createDataSet(null);
        data.setUserEMail("my-email");
        final PersonPE registrator = new PersonPE();
        registrator.setEmail(data.getUserEMail());
        context.checking(new Expectations()
            {
                {
                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(new PersonPE(), registrator)));
                }
            });

        IDataBO dataBO = createDataBO();
        dataBO.define(data, sample, SourceType.DERIVED);
        ExternalDataPE externalData = dataBO.getData().tryAsExternalData();

        assertNotNull(externalData);
        assertEquals(DATA_SET_CODE, externalData.getCode());
        assertEquals(BooleanOrUnknown.U, externalData.getComplete());
        assertEquals(DATA_PRODUCER_CODE, externalData.getDataProducerCode());
        assertSame(dataSetType, externalData.getDataSetType());
        assertSame(fileFormatType, externalData.getFileFormatType());
        assertSame(locatorType, externalData.getLocatorType());
        assertEquals(LOCATION, externalData.getLocation());
        assertEquals(0, externalData.getParents().size());
        assertSame(experimentPE, externalData.getExperiment());
        assertEquals(PRODUCTION_DATE, externalData.getProductionDate());
        assertSame(sample, externalData.tryGetSample());
        assertSame(true, externalData.isDerived());
        assertEquals(StorageFormat.PROPRIETARY, externalData.getStorageFormat());
        assertSame(vocabularyTerm, externalData.getStorageFormatVocabularyTerm());
        assertSame(registrator, externalData.getRegistrator());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithExistingParentDataSet()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.PHYSICAL);
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        ExperimentPE experimentPE = new ExperimentPE();
        DataStorePE dataStore = new DataStorePE();
        prepareDefineExternalData(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        final DataPE data = new DataPE();
        data.setCode(PARENT_CODE);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(data));

                    one(relationshipService).addParentToDataSet(with(any(IAuthSession.class)),
                            with(any(DataPE.class)), with(any(DataPE.class)));
                }
            });

        IDataBO dataBO = createDataBO();
        dataBO.define(createDataSet(PARENT_CODE), experimentPE, SourceType.MEASUREMENT);
        DataPE loadedData = dataBO.getData();

        assertSame(experimentPE, loadedData.getExperiment());
        assertEquals(null, loadedData.tryGetSample());
        assertSame(true, loadedData.isMeasured());
        assertSame(dataStore, loadedData.getDataStore());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithNonExistingParentDataSet()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.PHYSICAL);
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        final ExperimentPE experiment = new ExperimentPE();
        final DataStorePE dataStore = new DataStorePE();
        prepareDefineExternalData(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        final DataSetTypePE dataSetTypeUnknown = createDataSetType(DataSetKind.PHYSICAL);
        final DataPE parentData = new DataPE();
        parentData.setCode(PARENT_CODE);
        parentData.setDataSetType(dataSetTypeUnknown);
        parentData.setExperiment(createExperiment("EXP1"));
        parentData.setPlaceholder(true);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(null));

                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode("UNKNOWN");
                    will(returnValue(dataSetTypeUnknown));

                    one(dataDAO).createDataSet(parentData, EXAMPLE_PERSON);

                    one(relationshipService).addParentToDataSet(with(any(IAuthSession.class)),
                            with(any(DataPE.class)), with(any(DataPE.class)));

                }
            });

        IDataBO dataBO = createDataBO();
        dataBO.define(createDataSet(PARENT_CODE), experiment, SourceType.MEASUREMENT);
        DataPE data = dataBO.getData();

        assertSame(experiment, data.getExperiment());
        assertEquals(null, data.tryGetSample());
        assertSame(true, data.isMeasured());
        assertSame(dataStore, data.getDataStore());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithNonExistingParentDataSetAndNonExistingExperiment()
    {
        final DataSetTypePE dataSetType = createDataSetType();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        final DataStorePE dataStore = new DataStorePE();
        prepareDefineExternalData(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        final DataSetTypePE dataSetTypeUnknown = createDataSetType(DataSetKind.PHYSICAL);
        final DataPE parentData = new DataPE();
        parentData.setCode(PARENT_CODE);
        parentData.setDataSetType(dataSetTypeUnknown);
        ExperimentPE experiment = createExperiment("EXP1");
        parentData.setExperiment(experiment);
        parentData.setPlaceholder(true);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(null));

                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode(
                            DataSetTypeCode.UNKNOWN.getCode());
                    will(returnValue(dataSetTypeUnknown));

                    one(dataDAO).createDataSet(parentData, EXAMPLE_PERSON);

                    one(relationshipService).addParentToDataSet(with(any(IAuthSession.class)),
                            with(any(DataPE.class)), with(any(DataPE.class)));

                }
            });

        IDataBO dataBO = createDataBO();
        dataBO.define(createDataSet(PARENT_CODE), experiment, SourceType.MEASUREMENT);
        DataPE data = dataBO.getData();

        assertSame(null, data.tryGetSample());
        assertSame(true, data.isMeasured());
        assertSame(dataStore, data.getDataStore());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineContainerWithExistingComponent()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.CONTAINER);
        ExperimentPE experimentPE = createExperiment("EXP1");
        DataStorePE dataStore = new DataStorePE();
        prepareDefineData(dataSetType, dataStore);
        final DataPE component = new DataPE();
        component.setExperiment(createExperiment("EXP2")); // different experiment, same space
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(COMPONENT_CODE);
                    will(returnValue(component));
                }
            });

        IDataBO dataBO = createDataBO();
        NewContainerDataSet newData = createContainerDataSetWithComponents(COMPONENT_CODE);
        dataBO.define(newData, experimentPE, SourceType.MEASUREMENT);
        dataBO.setContainedDataSets(experimentPE, newData);
        DataPE loadedData = dataBO.getData();

        assertSame(experimentPE, loadedData.getExperiment());
        assertEquals(null, loadedData.tryGetSample());
        assertSame(true, loadedData.isMeasured());
        assertSame(dataStore, loadedData.getDataStore());
        assertEquals(1, loadedData.getContainedDataSets().size());
        assertSame(component, loadedData.getContainedDataSets().iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineContainerWithNonExistingComponent()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.CONTAINER);
        final ExperimentPE experiment = createExperiment("EXP1");
        final DataStorePE dataStore = new DataStorePE();
        prepareDefineData(dataSetType, dataStore);
        final DataSetTypePE dataSetTypeUnknown = createDataSetType(DataSetKind.PHYSICAL);
        final DataPE component = new DataPE();
        component.setCode(COMPONENT_CODE);
        component.setDataSetType(dataSetTypeUnknown);
        component.setExperiment(experiment);
        component.setPlaceholder(true);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(COMPONENT_CODE);
                    will(returnValue(null));

                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode("UNKNOWN");
                    will(returnValue(dataSetTypeUnknown));

                    one(dataDAO).createDataSet(component, EXAMPLE_PERSON);
                }
            });

        IDataBO dataBO = createDataBO();
        NewContainerDataSet newData = createContainerDataSetWithComponents(COMPONENT_CODE);
        dataBO.define(newData, experiment, SourceType.MEASUREMENT);
        dataBO.setContainedDataSets(experiment, newData);
        DataPE data = dataBO.getData();

        assertSame(experiment, data.getExperiment());
        assertEquals(null, data.tryGetSample());
        assertSame(true, data.isMeasured());
        assertSame(dataStore, data.getDataStore());
        assertEquals(1, data.getContainedDataSets().size());
        assertEquals(component, data.getContainedDataSets().iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineContainerWithComponentFromDifferentSpaceFails()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.CONTAINER);
        ExperimentPE experiment1 = createExperiment("EXP1", "S1");
        ExperimentPE experiment2 = createExperiment("EXP2", "S2");
        DataStorePE dataStore = new DataStorePE();
        prepareDefineData(dataSetType, dataStore);
        final DataPE data = new DataPE();
        data.setCode(COMPONENT_CODE);
        data.setExperiment(createExperiment("EXP1", "S2"));

        IDataBO dataBO = createDataBO();
        NewContainerDataSet newData = createContainerDataSetWithComponents(COMPONENT_CODE);
        dataBO.define(newData, experiment1, SourceType.MEASUREMENT);

        // sanity check
        try
        {
            dataBO.setContainedDataSets(experiment2, newData);
            fail("Expected UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "Contained data sets need to be in the same space ('S1') as the container.",
                    ex.getMessage());
        }

        // existing data set from different space
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(COMPONENT_CODE);
                    will(returnValue(data));
                }
            });
        try
        {
            dataBO.setContainedDataSets(experiment1, newData);
            fail("Expected UserFailureException");
        } catch (UserFailureException ex)
        {
            assertEquals("Data set '" + COMPONENT_CODE
                    + "' must be in the same space ('S1') as its container.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineLinkDataSet()
    {
        DataSetTypePE dataSetType = createDataSetType(DataSetKind.LINK);
        dataSetType.setCode("my-type");
        DataStorePE dataStore = new DataStorePE();
        prepareDefineData(dataSetType, dataStore);
        ExperimentPE experimentPE = new ExperimentPE();
        final NewExternalData data = createLinkDataSetWithComponents("x2");
        final ExternalDataManagementSystemPE dms = new ExternalDataManagementSystemPE();
        dms.setCode(EXTERNAL_DATA_MANAGEMENT_SYSTEM_CODE);
        context.checking(new Expectations()
            {
                {
                    one(dataManagementSystemDAO).tryToFindExternalDataManagementSystemByCode(
                            EXTERNAL_DATA_MANAGEMENT_SYSTEM_CODE);
                    will(returnValue(dms));
                }
            });
        IDataBO dataBO = createDataBO();

        dataBO.define(data, experimentPE, SourceType.MEASUREMENT);
        DataPE dataSet = dataBO.getData();

        assertEquals(DATA_SET_CODE, dataSet.getCode());
        assertEquals("my-type", dataSet.getDataSetType().getCode());
        assertEquals(true, dataSet.isLinkData());
        assertEquals(dms, dataSet.tryAsLinkData().getExternalDataManagementSystem());
        assertEquals("x2", dataSet.tryAsLinkData().getExternalCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateStatuses()
    {
        final List<String> codes = Arrays.asList(new String[]
            { "CODE-1", "CODE-2" });
        context.checking(new Expectations()
            {
                {
                    one(dataDAO)
                            .updateDataSetStatuses(codes, DataSetArchivingStatus.ARCHIVED, true);
                }
            });

        IDataBO dataBO = createDataBO();
        dataBO.updateStatuses(codes, DataSetArchivingStatus.ARCHIVED, true);
        // TODO 2010-04-26, Piotr Buczek: write a DAO test
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdate()
    {
        final SamplePE sample = new SamplePE();
        sample.setCode(SAMPLE_IDENTIFIER.getSampleCode());
        final ExternalDataPE dataSet = createExternalData(sample, null);
        DataSetUpdatesDTO dataSetUpdatesDTO =
                createDataSetUpdates(dataSet, SAMPLE_IDENTIFIER, null);
        prepareForUpdate(dataSet, sample);
        context.checking(new Expectations()
            {
                {
                    one(fileFormatTypeDAO)
                            .tryToFindFileFormatTypeByCode(FILE_FORMAT_TYPE.getCode());
                    FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
                    fileFormatTypePE.setCode(FILE_FORMAT_TYPE.getCode());
                    will(returnValue(fileFormatTypePE));

                    one(propertiesConverter).checkMandatoryProperties(
                            Collections.<DataSetPropertyPE> emptySet(), dataSet.getDataSetType());

                    one(dataDAO).validateAndSaveUpdatedEntity(dataSet);
                }
            });

        IDataBO dataBO = createDataBO();
        dataBO.update(dataSetUpdatesDTO);
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateWithDataSetAsItsOwnParent()
    {
        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(EXPERIMENT_IDENTIFIER.getExperimentCode());
        experiment.setProject(ManagerTestTool.EXAMPLE_PROJECT);

        final ExternalDataPE dataSet = createExternalData(null, experiment);
        DataSetUpdatesDTO dataSetUpdatesDTO =
                createDataSetUpdates(dataSet, null, EXPERIMENT_IDENTIFIER);
        String[] parentCodes =
            { dataSet.getCode() };
        dataSetUpdatesDTO.setModifiedParentDatasetCodesOrNull(parentCodes);
        prepareForUpdate(dataSet, experiment);

        IDataBO dataBO = createDataBO();
        try
        {
            dataBO.update(dataSetUpdatesDTO);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Data set 'DS1' can not be its own parent.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateWithNonExistingParentDataSet()
    {
        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(EXPERIMENT_IDENTIFIER.getExperimentCode());
        experiment.setProject(ManagerTestTool.EXAMPLE_PROJECT);

        final ExternalDataPE dataSet = createExternalData(null, experiment);
        DataSetUpdatesDTO dataSetUpdatesDTO =
                createDataSetUpdates(dataSet, null, EXPERIMENT_IDENTIFIER);
        String[] parentCodes =
            { PARENT_CODE };
        dataSetUpdatesDTO.setModifiedParentDatasetCodesOrNull(parentCodes);
        prepareForUpdate(dataSet, experiment);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(null));
                }
            });

        IDataBO dataBO = createDataBO();
        try
        {
            dataBO.update(dataSetUpdatesDTO);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Data Sets with following codes do not exist: '[" + PARENT_CODE + "]'.",
                    e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateWithNonExistingComponentDataSet()
    {
        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(EXPERIMENT_IDENTIFIER.getExperimentCode());
        experiment.setProject(ManagerTestTool.EXAMPLE_PROJECT);

        final DataPE dataSet = createDataSet(null, experiment);
        DataSetUpdatesDTO dataSetUpdatesDTO =
                createDataSetUpdates(dataSet, null, EXPERIMENT_IDENTIFIER);
        String[] componentCodes =
            { COMPONENT_CODE };
        dataSetUpdatesDTO.setModifiedContainedDatasetCodesOrNull(componentCodes);
        prepareForUpdate(dataSet, experiment);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(COMPONENT_CODE);
                    will(returnValue(null));
                }
            });

        IDataBO dataBO = createDataBO();
        try
        {
            dataBO.update(dataSetUpdatesDTO);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals(
                    "Data Sets with following codes do not exist: '[" + COMPONENT_CODE + "]'.",
                    e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateWithDataSetAsItsOwnContainer()
    {
        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(EXPERIMENT_IDENTIFIER.getExperimentCode());
        experiment.setProject(ManagerTestTool.EXAMPLE_PROJECT);

        final DataPE dataSet = createDataSet(null, experiment);
        DataSetUpdatesDTO dataSetUpdatesDTO =
                createDataSetUpdates(dataSet, null, EXPERIMENT_IDENTIFIER);
        String[] componentCodes =
            { dataSet.getCode() };
        dataSetUpdatesDTO.setModifiedContainedDatasetCodesOrNull(componentCodes);
        prepareForUpdate(dataSet, experiment);

        IDataBO dataBO = createDataBO();
        try
        {
            dataBO.update(dataSetUpdatesDTO);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Data set 'DS1' cannot contain itself as a "
                    + "component neither directly nor via subordinate components.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateWithContainerRecursivelyContainingItself()
    {
        final ExperimentPE experiment =
                createExperiment(EXPERIMENT_IDENTIFIER.getExperimentCode(), "spaceCode");

        final DataPE container1 = createDataSet("container-1", null, experiment);
        container1.getDataSetType().setDataSetKind(DataSetKind.CONTAINER.name());
        final DataPE container2 = createDataSet("container-2", null, experiment);
        container2.getDataSetType().setDataSetKind(DataSetKind.CONTAINER.name());
        container2.addComponent(container1, EXAMPLE_PERSON);

        DataSetUpdatesDTO dataSetUpdatesDTO =
                createDataSetUpdates(container1, null, EXPERIMENT_IDENTIFIER);
        String[] componentCodes =
            { container2.getCode() };
        dataSetUpdatesDTO.setModifiedContainedDatasetCodesOrNull(componentCodes);

        prepareForUpdate(container1, experiment);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(container2.getCode());
                    will(returnValue(container2));
                }
            });

        IDataBO dataBO = createDataBO();
        try
        {
            dataBO.update(dataSetUpdatesDTO);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Data set 'container-1' cannot contain itself as a component"
                    + " neither directly nor via subordinate components.", e.getMessage());
        }
        context.assertIsSatisfied();
    }

    private void prepareForUpdate(final ExternalDataPE dataSet, final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryGetByTechId(TECH_ID, DataBO.PROPERTY_TYPES,
                            DataBO.DATA_SET_TYPE);
                    will(returnValue(dataSet));

                    one(propertiesConverter).updateProperties(
                            Collections.<DataSetPropertyPE> emptySet(), dataSet.getDataSetType(),
                            null, ManagerTestTool.EXAMPLE_PERSON, Collections.<String> emptySet());
                    will(returnValue(Collections.emptySet()));

                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(
                            DATABASE_INSTANCE_IDENTIFIER.getDatabaseInstanceCode());
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    one(spaceDAO).tryFindSpaceByCodeAndDatabaseInstance(
                            SPACE_IDENTIFIER.getSpaceCode(),
                            ManagerTestTool.EXAMPLE_DATABASE_INSTANCE);
                    will(returnValue(ManagerTestTool.EXAMPLE_GROUP));

                    one(sampleDAO).tryFindByCodeAndSpace(SAMPLE_IDENTIFIER.getSampleCode(),
                            ManagerTestTool.EXAMPLE_GROUP);
                    will(returnValue(sample));
                }
            });
    }

    private void prepareForUpdate(final DataPE dataSet, final ExperimentPE experiment)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryGetByTechId(TECH_ID, DataBO.PROPERTY_TYPES,
                            DataBO.DATA_SET_TYPE);
                    will(returnValue(dataSet));

                    ExperimentIdentifier identifier = EXPERIMENT_IDENTIFIER;

                    one(projectDAO).tryFindProject(identifier.getDatabaseInstanceCode(),
                            identifier.getSpaceCode(), identifier.getProjectCode());
                    will(returnValue(ManagerTestTool.EXAMPLE_PROJECT));

                    one(experimentDAO).tryFindByCodeAndProject(ManagerTestTool.EXAMPLE_PROJECT,
                            identifier.getExperimentCode());
                    will(returnValue(experiment));
                }
            });
    }

    private DataSetUpdatesDTO createDataSetUpdates(final DataPE dataSet,
            final SampleIdentifier sampleIdentifierOrNull,
            final ExperimentIdentifier experimentIdentifierOrNull)
    {
        DataSetUpdatesDTO dataSetUpdatesDTO = new DataSetUpdatesDTO();
        dataSetUpdatesDTO.setDatasetId(TECH_ID);
        dataSetUpdatesDTO.setVersion(dataSet.getModificationDate());
        dataSetUpdatesDTO.setSampleIdentifierOrNull(sampleIdentifierOrNull);
        dataSetUpdatesDTO.setExperimentIdentifierOrNull(experimentIdentifierOrNull);
        if (dataSet.isExternalData())
        {
            dataSetUpdatesDTO.setFileFormatTypeCode(FILE_FORMAT_TYPE.getCode());
        }
        return dataSetUpdatesDTO;
    }

    private ExternalDataPE createExternalData(final String code, final SamplePE sampleOrNull,
            final ExperimentPE experimentOrNull)
    {
        final ExternalDataPE dataSet = new ExternalDataPE();
        dataSet.setId(TECH_ID.getId());
        dataSet.setCode(code);
        dataSet.setModificationDate(PRODUCTION_DATE);
        dataSet.setSample(sampleOrNull);
        dataSet.setExperiment(experimentOrNull);
        DataSetTypePE dataSetType = createDataSetType(DataSetKind.PHYSICAL);
        dataSetType.setCode(DATA_SET_TYPE.getCode());
        DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode("db");
        dataSetType.setDatabaseInstance(databaseInstance);
        dataSetType.setDataSetTypePropertyTypes(new HashSet<DataSetTypePropertyTypePE>());
        dataSet.setDataSetType(dataSetType);
        return dataSet;
    }

    private ExternalDataPE createExternalData(final SamplePE sampleOrNull,
            final ExperimentPE experimentOrNull)
    {
        return createExternalData(DATA_SET_CODE, sampleOrNull, experimentOrNull);
    }

    private DataPE createDataSet(final String code, final SamplePE sampleOrNull,
            final ExperimentPE experimentOrNull)
    {
        final DataPE dataSet = new DataPE();
        dataSet.setId(TECH_ID.getId());
        dataSet.setCode(code);
        dataSet.setModificationDate(PRODUCTION_DATE);
        dataSet.setSample(sampleOrNull);
        dataSet.setExperiment(experimentOrNull);
        DataSetTypePE dataSetType = createDataSetType(DataSetKind.PHYSICAL);
        dataSetType.setCode(DATA_SET_TYPE.getCode());
        DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode("db");
        dataSetType.setDatabaseInstance(databaseInstance);
        dataSetType.setDataSetTypePropertyTypes(new HashSet<DataSetTypePropertyTypePE>());
        dataSet.setDataSetType(dataSetType);
        return dataSet;
    }

    private DataPE createDataSet(final SamplePE sampleOrNull, final ExperimentPE experimentOrNull)
    {
        return createDataSet(DATA_SET_CODE, sampleOrNull, experimentOrNull);
    }

    @Test
    public void testSaveNewDataSet()
    {
        final DataSetTypePE dataSetType = createDataSetType();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        SamplePE sample = new SamplePE();
        sample.setExperiment(new ExperimentPE());
        final DataStorePE dataStore = new DataStorePE();
        prepareDefineExternalData(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(DATA_SET_CODE);
                    will(returnValue(null));

                    one(dataDAO).createDataSet(with(new DataMatcher()), with(EXAMPLE_PERSON));

                    expectMandatoryPropertiesCheck(this, dataSetType);
                }
            });

        IDataBO dataBO = createDataBO();
        dataBO.define(createDataSet(null), sample, SourceType.DERIVED);
        dataBO.save();

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdatePlaceholderDataSet()
    {
        final DataSetTypePE dataSetType = createDataSetType();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        SamplePE sample = new SamplePE();
        sample.setExperiment(new ExperimentPE());
        final DataStorePE dataStore = new DataStorePE();
        prepareDefineExternalData(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).tryToFindDataSetByCode(DATA_SET_CODE);
                    DataPE data = new DataPE();
                    data.setId(4711L);
                    data.setPlaceholder(true);
                    will(returnValue(data));

                    one(dataDAO).updateDataSet(with(new DataMatcher()), with(EXAMPLE_PERSON));

                    expectMandatoryPropertiesCheck(this, dataSetType);
                }

            });

        IDataBO dataBO = createDataBO();
        dataBO.define(createDataSet(null), sample, SourceType.DERIVED);
        dataBO.save();

        DataPE data = dataBO.getData();
        assertSame(dataStore, data.getDataStore());
        assertEquals(false, data.isPlaceholder());
        assertEquals(4711, data.getId().longValue());
        assertSame(sample, data.tryGetSample());
        assertSame(true, data.isDerived());

        context.assertIsSatisfied();
    }

    public void testStorageConfirmed()
    {
        // TODO:KUBA
        // create a new data object with external data.
        // check that externaldataPE has isstorage confirmed set to false

        // call dataBo. setstorageconfirmed on this instance of external data

        // check that the isstorage confirmed is now true on this element

    }

    @SuppressWarnings("unchecked")
    private void expectMandatoryPropertiesCheck(Expectations exp, final DataSetTypePE type)
    {
        exp.one(propertiesConverter).checkMandatoryProperties(
                exp.with(Expectations.any(Collection.class)), exp.with(type));
    }

    private DataSetTypePE createDataSetType()
    {
        final DataSetTypePE dataSetType = createDataSetType(DataSetKind.PHYSICAL);
        dataSetType.setCode("data-set-type-code");
        dataSetType.setDatabaseInstance(new DatabaseInstancePE());
        return dataSetType;
    }

    private DataSetTypePE createDataSetType(DataSetKind dataSetKind)
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        dataSetType.setDataSetKind(dataSetKind.toString());
        return dataSetType;
    }

    private ExperimentPE createExperiment(String experimentCode)
    {
        return createExperiment(experimentCode, "S");
    }

    private ExperimentPE createExperiment(String experimentCode, String spaceCode)
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(experimentCode);
        ProjectPE project = new ProjectPE();
        project.setCode("P");
        SpacePE space = new SpacePE();
        space.setCode(spaceCode);
        DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode("DB");
        space.setDatabaseInstance(databaseInstance);
        project.setSpace(space);
        experiment.setProject(project);
        return experiment;
    }

    private void prepareDefineData(final DataSetTypePE dataSetType, final DataStorePE dataStore)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode(DATA_SET_TYPE.getCode());
                    will(returnValue(dataSetType));

                    one(propertiesConverter).convertProperties(new IEntityProperty[0],
                            dataSetType.getCode(), EXAMPLE_SESSION.tryGetPerson());
                    ArrayList<DataSetPropertyPE> properties = new ArrayList<DataSetPropertyPE>();
                    will(returnValue(properties));

                    one(dataStoreDAO).tryToFindDataStoreByCode(DATA_STORE_CODE);
                    will(returnValue(dataStore));
                }
            });
    }

    private void prepareDefineExternalData(final DataSetTypePE dataSetType,
            final FileFormatTypePE fileFormatType, final VocabularyPE vocabulary,
            final LocatorTypePE locatorType, final DataStorePE dataStore)
    {
        prepareDefineData(dataSetType, dataStore);
        context.checking(new Expectations()
            {
                {
                    one(fileFormatTypeDAO)
                            .tryToFindFileFormatTypeByCode(FILE_FORMAT_TYPE.getCode());
                    will(returnValue(fileFormatType));
                    one(vocabularyDAO).tryFindVocabularyByCode(StorageFormat.VOCABULARY_CODE);
                    will(returnValue(vocabulary));
                    one(locatorTypeDAO).tryToFindLocatorTypeByCode(LOCATOR_TYPE.getCode());
                    will(returnValue(locatorType));
                }
            });
    }

    private NewExternalData createDataSet(String parentDataSetCodeOrNull)
    {
        NewExternalData data = new NewExternalData();
        data.setCode(DATA_SET_CODE);
        if (parentDataSetCodeOrNull != null)
        {
            data.setParentDataSetCodes(Collections.singletonList(parentDataSetCodeOrNull));
        }
        data.setDataProducerCode(DATA_PRODUCER_CODE);
        data.setProductionDate(PRODUCTION_DATE);
        data.setComplete(BooleanOrUnknown.U);
        data.setStorageFormat(StorageFormat.PROPRIETARY);
        data.setDataSetType(DATA_SET_TYPE);
        data.setFileFormatType(FILE_FORMAT_TYPE);
        data.setLocatorType(LOCATOR_TYPE);
        data.setLocation(LOCATION);
        data.setDataStoreCode(DATA_STORE_CODE);
        data.setSpeedHint(SPEED_HINT);
        return data;
    }

    private NewContainerDataSet createContainerDataSetWithComponents(String... components)
    {
        NewContainerDataSet data = new NewContainerDataSet();
        data.setCode(DATA_SET_CODE);
        data.setContainedDataSetCodes(Arrays.asList(components));
        data.setDataProducerCode(DATA_PRODUCER_CODE);
        data.setProductionDate(PRODUCTION_DATE);
        data.setComplete(BooleanOrUnknown.U);
        data.setStorageFormat(StorageFormat.PROPRIETARY);
        data.setDataSetType(DATA_SET_TYPE);
        data.setDataStoreCode(DATA_STORE_CODE);
        return data;
    }

    private NewLinkDataSet createLinkDataSetWithComponents(String externalCode)
    {
        NewLinkDataSet data = new NewLinkDataSet();
        data.setCode(DATA_SET_CODE);
        data.setExternalCode(externalCode);
        data.setExternalDataManagementSystemCode(EXTERNAL_DATA_MANAGEMENT_SYSTEM_CODE);
        data.setDataSetType(DATA_SET_TYPE);
        data.setDataStoreCode(DATA_STORE_CODE);
        return data;
    }

    private final IDataBO createDataBO()
    {
        return new DataBO(daoFactory, EXAMPLE_SESSION, propertiesConverter, relationshipService,
                conversationClient);
    }

}
