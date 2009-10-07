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

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.ProcedureTypeCode;

/**
 * @author Franz-Josef Elmer
 */
// TODO 2009-09-10, Piotr Buczek: write tests with many parents and cycle check
public class ExternalDataBOTest extends AbstractBOTest
{
    private static final TechId TECH_ID = new TechId(42l);

    private static final DatabaseInstanceIdentifier DATABASE_INSTANCE_IDENTIFIER =
            new DatabaseInstanceIdentifier(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE.getCode());

    private static final GroupIdentifier GROUP_IDENTIFIER =
            new GroupIdentifier(DATABASE_INSTANCE_IDENTIFIER, ManagerTestTool.EXAMPLE_GROUP
                    .getCode());

    private static final SampleIdentifier SAMPLE_IDENTIFIER =
            new SampleIdentifier(GROUP_IDENTIFIER, "EXAMPLE_SAMPLE");

    private static final ExperimentIdentifier EXPERIMENT_IDENTIFIER =
            new ExperimentIdentifier(new ProjectIdentifier(GROUP_IDENTIFIER,
                    ManagerTestTool.EXAMPLE_PROJECT.getCode()), "EXPERIMENT_CODE");

    private static final String DATA_STORE_CODE = "dss1";

    private static final String PARENT_CODE = "parent";

    private static final Date PRODUCTION_DATE = new Date(1001);

    private static final String DATA_PRODUCER_CODE = "DPC";

    private static final String DATA_SET_CODE = "DS1";

    private static final String LOCATION = "folder/subfolder/file.fft";

    private static final LocatorType LOCATOR_TYPE = new LocatorType("LT");

    private static final FileFormatType FILE_FORMAT_TYPE = new FileFormatType("FFT");

    private static final DataSetType DATA_SET_TYPE = new DataSetType("DST");

    private static final class DataMatcher extends BaseMatcher<ExternalDataPE>
    {
        public void describeTo(Description description)
        {
            description.appendText(DATA_SET_CODE);
        }

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
        final DataSetTypePE dataSetType = new DataSetTypePE();
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
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType, new DataStorePE());

        IExternalDataBO dataBO = createExternalDataBO();
        dataBO.define(createData(null), sample, SourceType.DERIVED);
        ExternalDataPE externalData = dataBO.getExternalData();

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
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithExistingParentDataSet()
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        ExperimentPE experimentPE = new ExperimentPE();
        DataStorePE dataStore = new DataStorePE();
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        final DataPE data = new DataPE();
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(data));
                }
            });

        IExternalDataBO dataBO = createExternalDataBO();
        dataBO.define(createData(PARENT_CODE), experimentPE, SourceType.MEASUREMENT);
        ExternalDataPE externalData = dataBO.getExternalData();

        assertSame(experimentPE, externalData.getExperiment());
        assertEquals(null, externalData.tryGetSample());
        assertSame(true, externalData.isMeasured());
        assertSame(dataStore, externalData.getDataStore());
        assertEquals(1, externalData.getParents().size());
        assertSame(data, externalData.getParents().iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithNonExistingParentDataSet()
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        final FileFormatTypePE fileFormatType = new FileFormatTypePE();
        final VocabularyPE vocabulary = new VocabularyPE();
        vocabulary.addTerm(new VocabularyTermPE());
        VocabularyTermPE vocabularyTerm = new VocabularyTermPE();
        vocabularyTerm.setCode(StorageFormat.PROPRIETARY.toString());
        vocabulary.addTerm(vocabularyTerm);
        final LocatorTypePE locatorType = new LocatorTypePE();
        final ExperimentPE experiment = new ExperimentPE();
        final DataStorePE dataStore = new DataStorePE();
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        final DataSetTypePE dataSetTypeUnknown = new DataSetTypePE();
        final DataPE parentData = new DataPE();
        parentData.setCode(PARENT_CODE);
        parentData.setDataSetType(dataSetTypeUnknown);
        parentData.setupExperiment(createExperiment("EXP1"));
        parentData.setPlaceholder(true);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(null));

                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode(
                            ProcedureTypeCode.UNKNOWN.getCode());
                    will(returnValue(dataSetTypeUnknown));

                    one(externalDataDAO).createDataSet(parentData);
                }
            });

        IExternalDataBO dataBO = createExternalDataBO();
        dataBO.define(createData(PARENT_CODE), experiment, SourceType.MEASUREMENT);
        ExternalDataPE externalData = dataBO.getExternalData();

        assertSame(experiment, externalData.getExperiment());
        assertEquals(null, externalData.tryGetSample());
        assertSame(true, externalData.isMeasured());
        assertSame(dataStore, externalData.getDataStore());
        assertEquals(1, externalData.getParents().size());
        assertEquals(parentData, externalData.getParents().iterator().next());
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
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        final DataSetTypePE dataSetTypeUnknown = new DataSetTypePE();
        final DataPE parentData = new DataPE();
        parentData.setCode(PARENT_CODE);
        parentData.setDataSetType(dataSetTypeUnknown);
        ExperimentPE experiment = createExperiment("EXP1");
        parentData.setupExperiment(experiment);
        parentData.setPlaceholder(true);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(null));

                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode(
                            DataSetTypeCode.UNKNOWN.getCode());
                    will(returnValue(dataSetTypeUnknown));

                    one(externalDataDAO).createDataSet(parentData);
                }
            });

        IExternalDataBO dataBO = createExternalDataBO();
        dataBO.define(createData(PARENT_CODE), experiment, SourceType.MEASUREMENT);
        ExternalDataPE externalData = dataBO.getExternalData();

        assertSame(null, externalData.tryGetSample());
        assertSame(true, externalData.isMeasured());
        assertSame(dataStore, externalData.getDataStore());
        assertEquals(1, externalData.getParents().size());
        assertEquals(parentData, externalData.getParents().iterator().next());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdate()
    {
        final SamplePE sample = new SamplePE();
        sample.setCode(SAMPLE_IDENTIFIER.getSampleCode());
        final ExternalDataPE dataSet = createDataSet(sample, null);
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
                            Collections.<DataSetPropertyPE> emptySet(), null);

                    one(externalDataDAO).validateAndSaveUpdatedEntity(dataSet);
                }
            });

        IExternalDataBO dataBO = createExternalDataBO();
        dataBO.update(dataSetUpdatesDTO);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateWithDataSetAsItsOwnParent()
    {
        final ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(EXPERIMENT_IDENTIFIER.getExperimentCode());

        final ExternalDataPE dataSet = createDataSet(null, experiment);
        DataSetUpdatesDTO dataSetUpdatesDTO =
                createDataSetUpdates(dataSet, null, EXPERIMENT_IDENTIFIER);
        String[] parentCodes =
            { dataSet.getCode() };
        dataSetUpdatesDTO.setModifiedParentDatasetCodesOrNull(parentCodes);
        prepareForUpdate(dataSet, experiment);

        IExternalDataBO dataBO = createExternalDataBO();
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

        final ExternalDataPE dataSet = createDataSet(null, experiment);
        DataSetUpdatesDTO dataSetUpdatesDTO =
                createDataSetUpdates(dataSet, null, EXPERIMENT_IDENTIFIER);
        String[] parentCodes =
            { PARENT_CODE };
        dataSetUpdatesDTO.setModifiedParentDatasetCodesOrNull(parentCodes);
        prepareForUpdate(dataSet, experiment);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(PARENT_CODE);
                    will(returnValue(null));
                }
            });

        IExternalDataBO dataBO = createExternalDataBO();
        try
        {
            dataBO.update(dataSetUpdatesDTO);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("Data Sets with following codes do not exist: '[" + PARENT_CODE + "]'.", e
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    private void prepareForUpdate(final ExternalDataPE dataSet, final SamplePE sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryGetByTechId(TECH_ID, ExternalDataBO.PROPERTY_TYPES);
                    will(returnValue(dataSet));

                    one(propertiesConverter).updateProperties(
                            Collections.<DataSetPropertyPE> emptySet(), null, null,
                            ManagerTestTool.EXAMPLE_PERSON);
                    will(returnValue(Collections.emptySet()));

                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(
                            DATABASE_INSTANCE_IDENTIFIER.getDatabaseInstanceCode());
                    will(returnValue(ManagerTestTool.EXAMPLE_DATABASE_INSTANCE));

                    one(groupDAO).tryFindGroupByCodeAndDatabaseInstance(
                            GROUP_IDENTIFIER.getGroupCode(),
                            ManagerTestTool.EXAMPLE_DATABASE_INSTANCE);
                    will(returnValue(ManagerTestTool.EXAMPLE_GROUP));

                    one(sampleDAO).tryFindByCodeAndGroup(SAMPLE_IDENTIFIER.getSampleCode(),
                            ManagerTestTool.EXAMPLE_GROUP);
                    will(returnValue(sample));
                }
            });
    }

    private void prepareForUpdate(final ExternalDataPE dataSet, final ExperimentPE experiment)
    {
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryGetByTechId(TECH_ID, ExternalDataBO.PROPERTY_TYPES);
                    will(returnValue(dataSet));

                    ExperimentIdentifier identifier = EXPERIMENT_IDENTIFIER;

                    one(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));
                    one(projectDAO).tryFindProject(identifier.getDatabaseInstanceCode(),
                            identifier.getGroupCode(), identifier.getProjectCode());
                    will(returnValue(ManagerTestTool.EXAMPLE_PROJECT));

                    one(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));
                    one(experimentDAO).tryFindByCodeAndProject(ManagerTestTool.EXAMPLE_PROJECT,
                            identifier.getExperimentCode());
                    will(returnValue(experiment));
                }
            });
    }

    private DataSetUpdatesDTO createDataSetUpdates(final ExternalDataPE dataSet,
            final SampleIdentifier sampleIdentifierOrNull,
            final ExperimentIdentifier experimentIdentifierOrNull)
    {
        DataSetUpdatesDTO dataSetUpdatesDTO = new DataSetUpdatesDTO();
        dataSetUpdatesDTO.setDatasetId(TECH_ID);
        dataSetUpdatesDTO.setVersion(dataSet.getModificationDate());
        dataSetUpdatesDTO.setSampleIdentifierOrNull(sampleIdentifierOrNull);
        dataSetUpdatesDTO.setExperimentIdentifierOrNull(experimentIdentifierOrNull);
        dataSetUpdatesDTO.setFileFormatTypeCode(FILE_FORMAT_TYPE.getCode());
        return dataSetUpdatesDTO;
    }

    private ExternalDataPE createDataSet(final SamplePE sampleOrNull,
            final ExperimentPE experimentOrNull)
    {
        final ExternalDataPE dataSet = new ExternalDataPE();
        dataSet.setId(TECH_ID.getId());
        dataSet.setCode(DATA_SET_CODE);
        dataSet.setModificationDate(PRODUCTION_DATE);
        dataSet.setSample(sampleOrNull);
        dataSet.setupExperiment(experimentOrNull);
        return dataSet;
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
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(DATA_SET_CODE);
                    will(returnValue(null));

                    one(externalDataDAO).createDataSet(with(new DataMatcher()));

                    expectMandatoryPropertiesCheck(this, dataSetType);
                }
            });

        IExternalDataBO dataBO = createExternalDataBO();
        dataBO.define(createData(null), sample, SourceType.DERIVED);
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
        prepareDefine(dataSetType, fileFormatType, vocabulary, locatorType, dataStore);
        context.checking(new Expectations()
            {
                {
                    one(externalDataDAO).tryToFindDataSetByCode(DATA_SET_CODE);
                    DataPE data = new DataPE();
                    data.setId(4711L);
                    data.setPlaceholder(true);
                    will(returnValue(data));

                    one(externalDataDAO).updateDataSet(with(new DataMatcher()));

                    expectMandatoryPropertiesCheck(this, dataSetType);
                }

            });

        IExternalDataBO dataBO = createExternalDataBO();
        dataBO.define(createData(null), sample, SourceType.DERIVED);
        dataBO.save();

        ExternalDataPE externalData = dataBO.getExternalData();
        assertSame(dataStore, externalData.getDataStore());
        assertEquals(false, externalData.isPlaceholder());
        assertEquals(4711, externalData.getId().longValue());
        assertSame(sample, externalData.tryGetSample());
        assertSame(true, externalData.isDerived());

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    private void expectMandatoryPropertiesCheck(Expectations exp, final DataSetTypePE type)
    {
        exp.one(propertiesConverter).checkMandatoryProperties(
                exp.with(Expectations.any(Collection.class)), exp.with(type));
    }

    private DataSetTypePE createDataSetType()
    {
        final DataSetTypePE dataSetType = new DataSetTypePE();
        dataSetType.setCode("data-set-type-code");
        dataSetType.setDatabaseInstance(new DatabaseInstancePE());
        return dataSetType;
    }

    private ExperimentPE createExperiment(String experimentCode)
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(experimentCode);
        ProjectPE project = new ProjectPE();
        project.setCode("P");
        GroupPE group = new GroupPE();
        group.setCode("G");
        DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode("DB");
        group.setDatabaseInstance(databaseInstance);
        project.setGroup(group);
        experiment.setProject(project);
        return experiment;
    }

    private void prepareDefine(final DataSetTypePE dataSetType,
            final FileFormatTypePE fileFormatType, final VocabularyPE vocabulary,
            final LocatorTypePE locatorType, final DataStorePE dataStore)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetTypeDAO).tryToFindDataSetTypeByCode(DATA_SET_TYPE.getCode());
                    will(returnValue(dataSetType));
                    one(fileFormatTypeDAO)
                            .tryToFindFileFormatTypeByCode(FILE_FORMAT_TYPE.getCode());
                    will(returnValue(fileFormatType));
                    one(vocabularyDAO).tryFindVocabularyByCode(StorageFormat.VOCABULARY_CODE);
                    will(returnValue(vocabulary));
                    one(locatorTypeDAO).tryToFindLocatorTypeByCode(LOCATOR_TYPE.getCode());
                    will(returnValue(locatorType));

                    one(propertiesConverter).convertProperties(new IEntityProperty[0],
                            dataSetType.getCode(), EXAMPLE_SESSION.tryGetPerson());
                    ArrayList<DataSetPropertyPE> properties = new ArrayList<DataSetPropertyPE>();
                    will(returnValue(properties));

                    one(dataStoreDAO).tryToFindDataStoreByCode(DATA_STORE_CODE);
                    will(returnValue(dataStore));
                }
            });
    }

    private NewExternalData createData(String parentDataSetCodeOrNull)
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
        return data;
    }

    private final IExternalDataBO createExternalDataBO()
    {
        return new ExternalDataBO(daoFactory, EXAMPLE_SESSION, propertiesConverter);
    }

}
