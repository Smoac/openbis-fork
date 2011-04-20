/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;

/**
 * Test cases for corresponding {@link ExternalDataDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "externalData" })
public final class ExternalDataDAOTest extends AbstractDAOTest
{

    private static final int SPEED_HINT = Constants.DEFAULT_SPEED_HINT / 4;

    private final String PARENT_CODE = "20081105092158673-1";

    private final String CHILD_CODE = "20081105092159188-3";

    @Test
    public final void testListExternalData()
    {
        testCreateDataSetWithSample();
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        List<ExternalDataPE> list = externalDataDAO.listExternalData(pickASample());

        assertEquals(1, list.size());
        ExternalDataPE dataSet = list.get(0);
        assertEquals("abcd", dataSet.getLocation());
        assertEquals(BooleanOrUnknown.U, dataSet.getComplete());
    }

    @Test
    public void testCreateDataSetWithSample()
    {
        IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        SamplePE sample = pickASample();
        ExternalDataPE externalData = createExternalData(dataSetCode, sample);
        externalData.setShareId("42");
        externalData.setSize(4711L);
        externalData.setSpeedHint(SPEED_HINT);
        externalDataDAO.createDataSet(externalData);

        ExternalDataPE dataSet =
                (ExternalDataPE) externalDataDAO.tryToFindDataSetByCode(dataSetCode);
        assertDataEqual(externalData, dataSet);
        assertEquals("42", dataSet.getShareId());
        assertEquals(4711L, dataSet.getSize().longValue());
        assertEquals(SPEED_HINT, dataSet.getSpeedHint());
    }

    @Test
    public void testCreateDataSetWithNoSample()
    {
        IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        ExternalDataPE externalData = createExternalData(dataSetCode, null);
        externalDataDAO.createDataSet(externalData);

        ExternalDataPE dataSet =
                (ExternalDataPE) externalDataDAO.tryToFindDataSetByCode(dataSetCode);
        assertDataEqual(externalData, dataSet);
        assertEquals(Constants.DEFAULT_SPEED_HINT, dataSet.getSpeedHint());
    }

    private ExternalDataPE createExternalData(String dataSetCode, SamplePE sampleOrNull)
    {
        ExternalDataPE externalData = new ExternalDataPE();

        externalData.setCode(dataSetCode);
        externalData.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        externalData.setExperiment(pickAnExperiment());
        if (sampleOrNull != null)
        {
            externalData.setSampleAcquiredFrom(sampleOrNull);
        } else
        {
            externalData.setDerived(true);
        }
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        externalData.setLocation("abcd");
        externalData.setComplete(BooleanOrUnknown.U);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setPlaceholder(true);
        externalData.setDataStore(pickADataStore());
        return externalData;
    }

    private void assertDataEqual(ExternalDataPE externalData, ExternalDataPE dataSet)
    {
        assertEquals(externalData.getCode(), dataSet.getCode());
        assertEquals(externalData.getDataSetType(), dataSet.getDataSetType());
        assertEquals(externalData.getExperiment(), dataSet.getExperiment());
        assertEquals(externalData.getFileFormatType(), dataSet.getFileFormatType());
        assertEquals(externalData.getLocatorType(), dataSet.getLocatorType());
        assertEquals(externalData.getLocation(), dataSet.getLocation());
        assertEquals(externalData.getComplete(), dataSet.getComplete());
        assertEquals(externalData.getStorageFormat(), dataSet.getStorageFormat());
        assertEquals(externalData.isPlaceholder(), dataSet.isPlaceholder());
        assertEquals(externalData.isMeasured(), dataSet.isMeasured());
        assertEquals(externalData.tryGetSample(), dataSet.tryGetSample());
    }

    @Test
    public void testUpdateDataSetAquiredFromSampleWithoutDataSetSize()
    {
        checkUpdate(null);
    }

    @Test
    public void testUpdateDataSetAquiredFromSampleWithDataSetSize()
    {
        checkUpdate(4711L);
    }
    
    private void checkUpdate(Long size)
    {
        IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        DataPE data = new DataPE();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        data.setCode(dataSetCode);
        data.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        data.setExperiment(pickAnExperiment());
        data.setSampleAcquiredFrom(pickASample());
        data.setPlaceholder(true);
        data.setDataStore(pickADataStore());
        data.setModificationDate(new Date());
        externalDataDAO.createDataSet(data);

        ExternalDataPE externalData = new ExternalDataPE();
        externalData.setId(externalDataDAO.tryToFindDataSetByCode(dataSetCode).getId());
        externalData.setCode(dataSetCode);
        externalData.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        externalData.setDataStore(pickADataStore());
        externalData.setExperiment(pickAnExperiment());
        externalData.setSampleAcquiredFrom(pickASample());
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        externalData.setLocation("abcd");
        externalData.setShareId("share-42");
        externalData.setSize(size);
        externalData.setComplete(BooleanOrUnknown.U);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setPlaceholder(true);
        externalData.setStatus(DataSetArchivingStatus.AVAILABLE);
        final Date modificationTimestamp = data.getModificationDate();
        externalData.setModificationDate(modificationTimestamp);
        externalDataDAO.updateDataSet(externalData);

        ExternalDataPE dataSet =
                (ExternalDataPE) externalDataDAO.tryToFindDataSetByCode(dataSetCode);
        assertEquals(externalData.getCode(), dataSet.getCode());
        assertEquals(externalData.getDataSetType(), dataSet.getDataSetType());
        assertEquals(externalData.getExperiment(), dataSet.getExperiment());
        assertEquals(externalData.getFileFormatType(), dataSet.getFileFormatType());
        assertEquals(externalData.getLocatorType(), dataSet.getLocatorType());
        assertEquals(externalData.getShareId(), dataSet.getShareId());
        assertEquals(externalData.getLocation(), dataSet.getLocation());
        assertEquals(externalData.getSize(), dataSet.getSize());
        assertEquals(externalData.getComplete(), dataSet.getComplete());
        assertEquals(externalData.getStorageFormat(), dataSet.getStorageFormat());
        assertEquals(externalData.isPlaceholder(), dataSet.isPlaceholder());
        assertEquals(externalData.isMeasured(), dataSet.isMeasured());
        assertFalse(externalData.getModificationDate().equals(modificationTimestamp));
    }

    @Test
    public void testUpdateDataSetWithParent()
    {
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();

        // try to add a parent to a data set that already had one
        final ExternalDataPE dataSetConnectedWithParent = findExternalData(CHILD_CODE);
        assertFalse(dataSetConnectedWithParent.getParents().isEmpty());
        final ExternalDataPE anotherDataSet = findExternalData("20081105092159111-1");
        dataSetConnectedWithParent.addParent(anotherDataSet);
        externalDataDAO.updateDataSet(dataSetConnectedWithParent);

        ExternalDataPE dataSet =
                (ExternalDataPE) externalDataDAO.tryToFindDataSetByCode(CHILD_CODE);
        assertEquals(dataSetConnectedWithParent.getParents().size(), dataSet.getParents().size());
        Set<DataPE> extractedParents = dataSet.getParents();
        for (DataPE parent : dataSetConnectedWithParent.getParents())
        {
            assertTrue(extractedParents.contains(parent));
        }
        assertTrue(extractedParents.contains(anotherDataSet));
    }

    @Test
    public final void testDeleteWithPropertiesButParentPreserved()
    {
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        final ExternalDataPE deletedData = findExternalData(CHILD_CODE);

        // Deleted data set should have all collections which prevent it from deletion empty.
        assertTrue(deletedData.getChildren().isEmpty());

        // delete
        externalDataDAO.delete(deletedData);

        // test successful deletion of data set
        assertNull(externalDataDAO.tryGetByTechId(TechId.create(deletedData)));

        // test successful deletion of data set properties
        assertFalse(deletedData.getProperties().isEmpty());
        List<EntityTypePropertyTypePE> retrievedPropertyTypes =
                daoFactory.getEntityPropertyTypeDAO(EntityKind.DATA_SET).listEntityPropertyTypes(
                        deletedData.getEntityType());
        for (DataSetPropertyPE property : deletedData.getProperties())
        {
            int index = retrievedPropertyTypes.indexOf(property.getEntityTypePropertyType());
            EntityTypePropertyTypePE retrievedPropertyType = retrievedPropertyTypes.get(index);
            assertFalse(retrievedPropertyType.getPropertyValues().contains(property));
        }

        // deleted sample had parent connected that should not have been deleted
        // NOTE: somehow cannot get parents even though connection is the same as with children
        // DataPE parent = deletedData.tryGetParent();
        // assertNotNull(parent);
        // assertNotNull(externalDataDAO.tryGetByTechId(new TechId(HibernateUtils.getId(parent))));
        findExternalData(PARENT_CODE);
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFailWithChildrenDatasets()
    {
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        final ExternalDataPE deletedData = findExternalData(PARENT_CODE);

        // Deleted data set should have 'child' data sets which prevent it from deletion.
        assertFalse(deletedData.getChildren().isEmpty());

        // delete
        externalDataDAO.delete(deletedData);
    }

    protected VocabularyTermPE pickAStorageFormatVocabularyTerm()
    {
        String code = StorageFormat.VOCABULARY_CODE;
        VocabularyPE vocabulary = daoFactory.getVocabularyDAO().tryFindVocabularyByCode(code);
        assertNotNull(vocabulary);
        return vocabulary.getTerms().iterator().next();
    }

    protected FileFormatTypePE pickAFileFormatType()
    {
        IFileFormatTypeDAO fileFormatTypeDAO = daoFactory.getFileFormatTypeDAO();
        FileFormatTypePE fileFormatType = fileFormatTypeDAO.tryToFindFileFormatTypeByCode("TIFF");
        assertNotNull(fileFormatType);
        return fileFormatType;
    }

    protected LocatorTypePE pickALocatorType()
    {
        ILocatorTypeDAO locatorTypeDAO = daoFactory.getLocatorTypeDAO();
        LocatorTypePE locatorType = locatorTypeDAO.tryToFindLocatorTypeByCode("RELATIVE_LOCATION");
        assertNotNull(locatorType);
        return locatorType;
    }

    protected SamplePE pickASample()
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        DatabaseInstancePE dbInstance = daoFactory.getHomeDatabaseInstance();
        SamplePE sample = sampleDAO.tryFindByCodeAndDatabaseInstance("MP", dbInstance);
        assertNotNull(sample);
        return sample;
    }

    protected DataStorePE pickADataStore()
    {
        return daoFactory.getDataStoreDAO().tryToFindDataStoreByCode("STANDARD");
    }

    protected DataSetTypePE getDataSetType(DataSetTypeCode type)
    {
        IDataSetTypeDAO dataSetTypeDAO = daoFactory.getDataSetTypeDAO();
        String code = type.getCode();
        DataSetTypePE dataSetType = dataSetTypeDAO.tryToFindDataSetTypeByCode(code);
        assertNotNull(dataSetType);
        return dataSetType;
    }

    protected ExperimentPE pickAnExperiment()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        for (ExperimentPE experimentPE : experiments)
        {
            if (experimentPE.getInvalidation() == null)
            {
                return experimentPE;
            }
        }
        fail("No valid experiment found.");
        return null; // to make the compiler happy
    }

    @Test
    public void testCreateDataSetWithBothSampleAndParent()
    {
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();

        // try to create a dataset connected with a sample and a parent at the same time
        final String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        final SamplePE sample = pickASample();
        ExternalDataPE parentData = findExternalData(PARENT_CODE);
        ExternalDataPE externalData = createExternalData(dataSetCode, sample);
        externalData.addParent(parentData);
        externalDataDAO.createDataSet(externalData);
    }

    @Test
    public void testUpdateOfDataSetAddParentWhenThereIsSampleConnected()
    {
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();

        // try to update dataset connected with a sample - adding a parent should succeed
        final ExternalDataPE dataSetConnectedWithSample = findExternalData(PARENT_CODE);
        assertNotNull(dataSetConnectedWithSample.tryGetSample());
        final ExternalDataPE anotherDataSet = findExternalData("20081105092159111-1");
        dataSetConnectedWithSample.addParent(anotherDataSet);
        externalDataDAO.updateDataSet(dataSetConnectedWithSample);
    }

    @Test
    public void testUpdateOfDataSetConnectSampleWhenThereIsParent()
    {
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();

        // try to update dataset connected with a parent - connecting with a sample should succeed
        final ExternalDataPE dataSetConnectedWithParent = findExternalData(CHILD_CODE);
        assertFalse(dataSetConnectedWithParent.getParents().isEmpty());
        dataSetConnectedWithParent.setSampleAcquiredFrom(pickASample());
        externalDataDAO.updateDataSet(dataSetConnectedWithParent);
    }

    @Test
    public void testLoadByPermId() throws Exception
    {
        ExternalDataPE exp = daoFactory.getExternalDataDAO().listAllEntities().get(0);
        HashSet<String> keys = new HashSet<String>();
        keys.add(exp.getCode());
        keys.add("nonexistent");
        List<ExternalDataPE> result = daoFactory.getExternalDataDAO().listByCode(keys);
        AssertJUnit.assertEquals(1, result.size());
        AssertJUnit.assertEquals(exp, result.get(0));
    }

    @Test
    public void testLoadByPermIdNoEntries() throws Exception
    {
        HashSet<String> keys = new HashSet<String>();
        List<ExternalDataPE> result = daoFactory.getExternalDataDAO().listByCode(keys);
        AssertJUnit.assertTrue(result.isEmpty());
    }

}
