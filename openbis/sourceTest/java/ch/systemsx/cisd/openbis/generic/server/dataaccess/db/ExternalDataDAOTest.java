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
import static org.testng.AssertJUnit.fail;

import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
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

    @Test
    public final void testListExternalData()
    {
        testCreateDataSet();
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        List<ExternalDataPE> list = externalDataDAO.listExternalData(pickASample());

        assertEquals(1, list.size());
        ExternalDataPE dataSet = list.get(0);
        assertEquals("abcd", dataSet.getLocation());
        assertEquals(BooleanOrUnknown.U, dataSet.getComplete());
    }

    @Test
    public void testCreateDataSet()
    {
        IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        ExternalDataPE externalData = new ExternalDataPE();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        externalData.setCode(dataSetCode);
        externalData.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        externalData.setExperiment(pickAnExperiment());
        externalData.setSampleAcquiredFrom(pickASample());
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        externalData.setLocation("abcd");
        externalData.setComplete(BooleanOrUnknown.U);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setPlaceholder(true);
        externalData.setDataStore(pickADataStore());
        daoFactory.getExternalDataDAO().createDataSet(externalData);

        ExternalDataPE dataSet =
                (ExternalDataPE) externalDataDAO.tryToFindDataSetByCode(dataSetCode);
        assertEquals(externalData.getCode(), dataSet.getCode());
        assertEquals(externalData.getDataSetType(), dataSet.getDataSetType());
        assertEquals(externalData.getExperiment(), dataSet.getExperiment());
        assertEquals(externalData.getFileFormatType(), dataSet.getFileFormatType());
        assertEquals(externalData.getLocatorType(), dataSet.getLocatorType());
        assertEquals(externalData.getLocation(), dataSet.getLocation());
        assertEquals(externalData.getComplete(), dataSet.getComplete());
        assertEquals(externalData.getStorageFormat(), dataSet.getStorageFormat());
        assertEquals(externalData.isPlaceholder(), dataSet.isPlaceholder());
    }

    @Test
    public void testUpdateDataSet()
    {
        IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        DataPE data = new DataPE();
        String dataSetCode = daoFactory.getPermIdDAO().createPermId();
        data.setCode(dataSetCode);
        data.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        data.setExperiment(pickAnExperiment());
        data.setSampleDerivedFrom(pickASample());
        data.setPlaceholder(true);
        data.setDataStore(pickADataStore());
        data.setModificationDate(new Date());
        externalDataDAO.createDataSet(data);

        ExternalDataPE externalData = new ExternalDataPE();
        externalData.setId(externalDataDAO.tryToFindDataSetByCode(dataSetCode).getId());
        externalData.setCode(dataSetCode);
        externalData.setDataSetType(getDataSetType(DataSetTypeCode.HCS_IMAGE));
        externalData.setDataStore(pickADataStore());
        externalData.setExperiment(pickAnExperiment());
        externalData.setSampleAcquiredFrom(pickASample());
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        externalData.setLocation("abcd");
        externalData.setComplete(BooleanOrUnknown.U);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setPlaceholder(true);
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
        assertEquals(externalData.getLocation(), dataSet.getLocation());
        assertEquals(externalData.getComplete(), dataSet.getComplete());
        assertEquals(externalData.getStorageFormat(), dataSet.getStorageFormat());
        assertEquals(externalData.isPlaceholder(), dataSet.isPlaceholder());
        assertFalse(externalData.getModificationDate().equals(modificationTimestamp));
    }

    @Test
    public void testDelete()
    {
        testCreateDataSet();
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        SamplePE sample = pickASample();
        List<ExternalDataPE> list = externalDataDAO.listExternalData(sample);
        ExternalDataPE data = list.get(0);

        externalDataDAO.delete(data, getTestPerson(), "description", "testing deletion");

        assertEquals(0, externalDataDAO.listExternalData(sample).size());
        DataPE retrievedData = externalDataDAO.tryToFindDataSetByCode(data.getCode());
        assertEquals(null, retrievedData);

        // TODO 2009-06-02, Piotr Buczek: find Event by identifier

        // Set<EventPE> events = retrievedData.getEvents();
        // assertEquals(1, events.size());
        // EventPE event = events.iterator().next();
        // assertEquals("description", event.getDescription());
        // assertEquals("testing deletion", event.getReason());
        // assertEquals(EventType.DELETION, event.getEventType());
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
        SamplePE sample =
                sampleDAO.tryFindByCodeAndDatabaseInstance("MP", dbInstance, HierarchyType.CHILD);
        assertNotNull(sample);
        return sample;
    }

    protected DataStorePE pickADataStore()
    {
        return daoFactory.getExternalDataDAO().tryToFindDataSetByCode("20081105092158673-1")
                .getDataStore();
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
}
