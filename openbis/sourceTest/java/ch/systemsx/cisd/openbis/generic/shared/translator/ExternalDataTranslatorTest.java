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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LocatorTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

/**
 * @author Franz-Josef Elmer
 */
public class ExternalDataTranslatorTest extends AssertJUnit
{
    private static final String BASE_URL = "url";

    private static final String BASE_INDEX_URL = "index.html";

    @Test
    public void testTranslationOfEmptyExternalDataPE()
    {
        ExternalDataPE externalDataPE = new ExternalDataPE();
        externalDataPE.setDataStore(new DataStorePE());
        ExternalData externalData =
                ExternalDataTranslator.translate(externalDataPE, BASE_URL, BASE_INDEX_URL);

        assertEquals(null, externalData.getCode());
        assertEquals(null, externalData.getExperiment());
        assertEquals(null, externalData.getProductionDate());
        assertEquals(null, externalData.getComplete());
        assertEquals(0, externalData.getParents().size());
    }

    @Test
    public void testTranslationOfFullFleshedExternalDataPE()
    {
        ExternalDataPE externalDataPE = new ExternalDataPE();
        externalDataPE.setCode("code");
        externalDataPE.setDataStore(new DataStorePE());
        externalDataPE.setComplete(BooleanOrUnknown.F);
        externalDataPE.setDataProducerCode("dataProducerCode");
        DataSetTypePE dataSetTypePE = new DataSetTypePE();
        dataSetTypePE.setCode("dataSetTypeCode");
        dataSetTypePE.setDescription("dataSetTypeDescription");
        externalDataPE.setDataSetType(dataSetTypePE);
        FileFormatTypePE fileFormatTypePE = new FileFormatTypePE();
        fileFormatTypePE.setCode("fileFormatTypeCode");
        fileFormatTypePE.setDescription("fileFormatTypeDescription");
        externalDataPE.setFileFormatType(fileFormatTypePE);
        externalDataPE.setLocation("location");
        LocatorTypePE locatorTypePE = new LocatorTypePE();
        locatorTypePE.setCode("locatorTypeCode");
        locatorTypePE.setDescription("locatorTypeDescription");
        externalDataPE.setLocatorType(locatorTypePE);
        ExperimentPE experimentPE = new ExperimentPE();
        experimentPE.setCode("my-experiment");
        experimentPE.setExperimentType(new ExperimentTypePE());
        ProjectPE projectPE = new ProjectPE();
        projectPE.setCode("my-project");
        GroupPE groupPE = new GroupPE();
        groupPE.setCode("my-group");
        DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode("my-instance");
        groupPE.setDatabaseInstance(databaseInstancePE);
        projectPE.setGroup(groupPE);
        experimentPE.setProject(projectPE);
        externalDataPE.setupExperiment(experimentPE);
        externalDataPE.setProductionDate(new Date(1));
        externalDataPE.setRegistrationDate(new Date(2));
        SamplePE samplePE = new SamplePE();
        samplePE.setCode("sample");
        SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setCode("sampleTypeCode");
        sampleTypePE.setDescription("sampleTypeDescription");
        samplePE.setSampleType(sampleTypePE);
        InvalidationPE invalidationPE = new InvalidationPE();
        invalidationPE.setReason("reason");
        invalidationPE.setRegistrationDate(new Date(3));
        PersonPE personPE = new PersonPE();
        personPE.setUserId("user");
        personPE.setDatabaseInstance(databaseInstancePE);
        invalidationPE.setRegistrator(personPE);
        samplePE.setInvalidation(invalidationPE);
        externalDataPE.setSampleAcquiredFrom(samplePE);

        ExternalData externalData =
                ExternalDataTranslator.translate(externalDataPE, BASE_URL, BASE_INDEX_URL);

        assertEquals(BASE_URL, externalData.getDataStore().getDownloadUrl());
        assertEquals("code", externalData.getCode());
        assertEquals(Boolean.FALSE, externalData.getComplete());
        assertEquals("dataProducerCode", externalData.getDataProducerCode());
        assertEquals("dataSetTypeCode", externalData.getDataSetType().getCode());
        assertEquals("dataSetTypeDescription", externalData.getDataSetType().getDescription());
        assertEquals("fileFormatTypeCode", externalData.getFileFormatType().getCode());
        assertEquals("fileFormatTypeDescription", externalData.getFileFormatType().getDescription());
        assertEquals("location", externalData.getLocation());
        assertEquals("locatorTypeCode", externalData.getLocatorType().getCode());
        assertEquals("locatorTypeDescription", externalData.getLocatorType().getDescription());
        assertEquals(0, externalData.getParents().size());
        assertEquals("my-experiment", externalData.getExperiment().getCode());
        assertEquals(1, externalData.getProductionDate().getTime());
        assertEquals(2, externalData.getRegistrationDate().getTime());
        assertEquals("sample", externalData.getSampleIdentifier());
        assertEquals("sampleTypeCode", externalData.getSampleType().getCode());
        assertEquals("sampleTypeDescription", externalData.getSampleType().getDescription());
        assertEquals(false, externalData.isDerived());
        assertEquals("reason", externalData.getInvalidation().getReason());
        assertEquals(3, externalData.getInvalidation().getRegistrationDate().getTime());
        assertEquals("user", externalData.getInvalidation().getRegistrator().getUserId());
    }

    @Test
    public void testTranslationADerivedExternalDataPEWithParents()
    {
        ExternalDataPE externalDataPE = new ExternalDataPE();
        externalDataPE.setDataStore(new DataStorePE());
        externalDataPE.setDerived(true);

        ExperimentPE experimentPE = new ExperimentPE();
        experimentPE.setCode("my-experiment");
        experimentPE.setExperimentType(new ExperimentTypePE());
        ProjectPE projectPE = new ProjectPE();
        projectPE.setCode("my-project");
        GroupPE groupPE = new GroupPE();
        groupPE.setCode("my-group");
        DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setCode("my-instance");
        groupPE.setDatabaseInstance(databaseInstancePE);
        projectPE.setGroup(groupPE);
        experimentPE.setProject(projectPE);
        externalDataPE.setupExperiment(experimentPE);

        externalDataPE.addParent(createParent("parent-1"));
        externalDataPE.addParent(createParent("parent-2"));

        ExternalData externalData =
                ExternalDataTranslator.translate(externalDataPE, BASE_URL, BASE_INDEX_URL);

        assertEquals("my-experiment", externalData.getExperiment().getCode());
        assertEquals(2, externalData.getParents().size());
        Set<String> parentCodes = extractParentCodes(externalData);
        assertEquals(true, parentCodes.contains("parent-1"));
        assertEquals(true, parentCodes.contains("parent-2"));
        assertEquals(true, externalData.isDerived());
        assertEquals(null, externalData.getInvalidation());
    }

    private Set<String> extractParentCodes(ExternalData externalData)
    {
        final Set<String> result = new HashSet<String>();
        for (ExternalData parent : externalData.getParents())
        {
            result.add(parent.getCode());
        }
        return result;
    }

    private DataPE createParent(String parentCode)
    {
        DataPE parent = new DataPE();
        parent.setCode(parentCode);
        parent.setDataStore(new DataStorePE());
        return parent;
    }

}
