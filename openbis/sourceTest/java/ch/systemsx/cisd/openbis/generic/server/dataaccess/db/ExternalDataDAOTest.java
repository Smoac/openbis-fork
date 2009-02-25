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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

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
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.HierarchyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
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
    public final void testCreateDataSetCode()
    {
        IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        String code = externalDataDAO.createDataSetCode();
        String[] tokens = code.split("-");
        assertEquals(2, tokens.length);
        int id = Integer.parseInt(tokens[1]) + 1;

        code = externalDataDAO.createDataSetCode();
        assertTrue("Expected id " + id + " at the end of code " + code, code.endsWith("-" + id));
    }

    @Test
    public final void testListExternalData()
    {
        testCreateDataSet();
        final IExternalDataDAO externalDataDAO = daoFactory.getExternalDataDAO();
        List<ExternalDataPE> list = externalDataDAO.listExternalData(pickASample(), SourceType.MEASUREMENT);

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
        String dataSetCode = daoFactory.getExternalDataDAO().createDataSetCode();
        externalData.setCode(dataSetCode);
        externalData.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        externalData.setProcedure(pickAProcedure());
        externalData.setSampleAcquiredFrom(pickASample());
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        externalData.setLocation("abcd");
        externalData.setComplete(BooleanOrUnknown.U);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setPlaceholder(true);
        daoFactory.getExternalDataDAO().createDataSet(externalData);

        ExternalDataPE dataSet = (ExternalDataPE) externalDataDAO.tryToFindDataSetByCode(dataSetCode);
        assertEquals(externalData.getCode(), dataSet.getCode());
        assertEquals(externalData.getDataSetType(), dataSet.getDataSetType());
        assertEquals(externalData.getProcedure(), dataSet.getProcedure());
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
        String dataSetCode = externalDataDAO.createDataSetCode();
        data.setCode(dataSetCode);
        data.setDataSetType(getDataSetType(DataSetTypeCode.UNKNOWN));
        data.setProcedure(pickAProcedure());
        data.setSampleDerivedFrom(pickASample());
        data.setPlaceholder(true);
        externalDataDAO.createDataSet(data);

        ExternalDataPE externalData = new ExternalDataPE();
        externalData.setId(externalDataDAO.tryToFindDataSetByCode(dataSetCode).getId());
        externalData.setCode(dataSetCode);
        externalData.setDataSetType(getDataSetType(DataSetTypeCode.HCS_IMAGE));
        externalData.setProcedure(pickAProcedure());
        externalData.setSampleAcquiredFrom(pickASample());
        externalData.setFileFormatType(pickAFileFormatType());
        externalData.setLocatorType(pickALocatorType());
        externalData.setLocation("abcd");
        externalData.setComplete(BooleanOrUnknown.U);
        externalData.setStorageFormatVocabularyTerm(pickAStorageFormatVocabularyTerm());
        externalData.setPlaceholder(true);
        externalDataDAO.updateDataSet(externalData);

        ExternalDataPE dataSet = (ExternalDataPE) externalDataDAO.tryToFindDataSetByCode(dataSetCode);
        assertEquals(externalData.getCode(), dataSet.getCode());
        assertEquals(externalData.getDataSetType(), dataSet.getDataSetType());
        assertEquals(externalData.getProcedure(), dataSet.getProcedure());
        assertEquals(externalData.getFileFormatType(), dataSet.getFileFormatType());
        assertEquals(externalData.getLocatorType(), dataSet.getLocatorType());
        assertEquals(externalData.getLocation(), dataSet.getLocation());
        assertEquals(externalData.getComplete(), dataSet.getComplete());
        assertEquals(externalData.getStorageFormat(), dataSet.getStorageFormat());
        assertEquals(externalData.isPlaceholder(), dataSet.isPlaceholder());
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
        SamplePE sample = sampleDAO.tryFindByCodeAndDatabaseInstance("MP", dbInstance, HierarchyType.CHILD);
        assertNotNull(sample);
        return sample;
    }

    protected DataSetTypePE getDataSetType(DataSetTypeCode type)
    {
        IDataSetTypeDAO dataSetTypeDAO = daoFactory.getDataSetTypeDAO();
        String code = type.getCode();
        DataSetTypePE dataSetType = dataSetTypeDAO.tryToFindDataSetTypeByCode(code);
        assertNotNull(dataSetType);
        return dataSetType;
    }

    protected ProcedurePE pickAProcedure()
    {
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        for (ExperimentPE experimentPE : experiments)
        {
            List<ProcedurePE> procedures = experimentPE.getProcedures();
            if (procedures.isEmpty() == false)
            {
                return procedures.get(0);
            }
        }
        fail("No experiment with a procedure found.");
        return null; // to make the compiler happy
    }
}
