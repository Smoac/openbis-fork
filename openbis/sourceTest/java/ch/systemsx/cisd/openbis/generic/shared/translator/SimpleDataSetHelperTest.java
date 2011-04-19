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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.types.BooleanOrUnknown;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SimpleDataSetHelperTest extends AssertJUnit
{
    @Test
    public void testTranslateList()
    {
        ExternalDataPE ds1 = create(1);
        ExternalDataPE ds2 = create(2);

        List<SimpleDataSetInformationDTO> list = SimpleDataSetHelper.translate(Arrays.asList(ds1, ds2));
        
        check(1, list.get(0));
        check(2, list.get(1));
        assertEquals("42", list.get(1).getDataSetShareId());
        assertEquals(2, list.size());
    }
    
    @Test 
    public void testTranslateDescription()
    {
        ExternalData ds = ExternalDataTranslator.translate(create(1), "", false);
        DatasetDescription description = ExternalDataTranslator.translateToDescription(ds);
        
        SimpleDataSetInformationDTO result = SimpleDataSetHelper.translate(description);
        
        check(1, result);
    }
    
    private ExternalDataPE create(long id)
    {
        ExternalDataPE dataSet = new ExternalDataPE();
        dataSet.setId(id);
        dataSet.setCode("ds-" + id);
        dataSet.setComplete(BooleanOrUnknown.T);
        dataSet.setDataProducerCode("producer");
        DataSetTypePE dataSetType = new DataSetTypePE();
        dataSetType.setCode("MY-TYPE");
        dataSet.setDataSetType(dataSetType);
        DataStorePE store = new DataStorePE();
        store.setCode("MY-STORE");
        dataSet.setDataStore(store);
        dataSet.setDerived(true);
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode("MY-EXPERIMENT");
        ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setCode("MY-EXPERIMENT-TYPE");
        experiment.setExperimentType(experimentType);
        ProjectPE project = new ProjectPE();
        project.setCode("MY-PROJECT");
        SpacePE space = new SpacePE();
        space.setCode("MY-SPACE");
        DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode("MY-DB");
        space.setDatabaseInstance(databaseInstance);
        project.setSpace(space);
        experiment.setProject(project);
        dataSet.setExperiment(experiment);
        FileFormatTypePE fileFormatType = new FileFormatTypePE();
        fileFormatType.setCode("MY_FILE_FORMAT");
        dataSet.setFileFormatType(fileFormatType);
        dataSet.setLocation("my-location");
        dataSet.setPresentInArchive(true);
        SamplePE sample = new SamplePE();
        sample.setCode("MY-SAMPLE");
        dataSet.setSample(sample);
        dataSet.setShareId("42");
        dataSet.setSize(137L);
        dataSet.setSpeedHint(42);
        dataSet.setStatus(DataSetArchivingStatus.ARCHIVED);
        return dataSet;
    }
    
    public void check(long expectedID, SimpleDataSetInformationDTO dataSet)
    {
        assertEquals("ds-" + expectedID, dataSet.getDataSetCode());
        assertEquals("MY-TYPE", dataSet.getDataSetType());
        assertEquals("my-location", dataSet.getDataSetLocation());
        assertEquals("MY-EXPERIMENT", dataSet.getExperimentCode());
        assertEquals("MY-PROJECT", dataSet.getProjectCode());
        assertEquals("MY-SPACE", dataSet.getGroupCode());
        assertEquals("MY-DB", dataSet.getDatabaseInstanceCode());
        assertEquals("MY-SAMPLE", dataSet.getSampleCode());
        assertEquals(new Long(137), dataSet.getDataSetSize());
        assertEquals(42, dataSet.getSpeedHint());
    }
}
