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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class AtomicEntityOperationDetailsTest extends AssertJUnit
{

    @Test
    public void testToString()
    {
        ArrayList<NewSpace> spaceRegistrations = new ArrayList<NewSpace>();
        spaceRegistrations.add(new NewSpace("SPACE1", "description", "adminUser1"));
        spaceRegistrations.add(new NewSpace("SPACE2", "description", "adminUser2"));

        Map<String, List<NewMaterial>> materialRegistrations =
                new HashMap<String, List<NewMaterial>>();
        List<NewMaterial> newMaterials1 = new ArrayList<NewMaterial>();
        newMaterials1.add(new NewMaterial("material-one"));
        newMaterials1.add(new NewMaterial("material-two"));
        List<NewMaterial> newMaterials2 = new ArrayList<NewMaterial>();
        newMaterials2.add(new NewMaterial("material-three"));
        materialRegistrations.put("material-type-1", newMaterials1);
        materialRegistrations.put("material-type-2", newMaterials2);

        List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();

        ArrayList<NewProject> projectRegistrations = new ArrayList<NewProject>();
        projectRegistrations.add(new NewProject("/SPACE/P1", "description"));
        projectRegistrations.add(new NewProject("/SPACE/P2", "description"));
        ArrayList<ProjectUpdatesDTO> projectUpdates = new ArrayList<ProjectUpdatesDTO>();

        ArrayList<NewExperiment> experimentRegistrations = new ArrayList<NewExperiment>();
        experimentRegistrations.add(new NewExperiment("/SPACE/PROJECT/EXP-ID1", "EXP-TYPE"));
        experimentRegistrations.add(new NewExperiment("/SPACE/PROJECT/EXP-ID2", "EXP-TYPE"));

        List<ExperimentUpdatesDTO> experimentUpdates = new ArrayList<ExperimentUpdatesDTO>();

        ArrayList<SampleUpdatesDTO> sampleUpdates = new ArrayList<SampleUpdatesDTO>();

        ArrayList<NewSample> sampleRegistrations = new ArrayList<NewSample>();
        sampleRegistrations.add(new NewSample("/SPACE/SAMPLE-ID1", new SampleType(), null, null,
                "/SPACE/PROJECT/EXP-ID1", null, null, new IEntityProperty[0],
                new ArrayList<NewAttachment>()));
        sampleRegistrations.add(new NewSample("/SPACE/SAMPLE-ID2", new SampleType(), null, null,
                "/SPACE/PROJECT/EXP-ID1", null, null, new IEntityProperty[0],
                new ArrayList<NewAttachment>()));

        ArrayList<NewExternalData> dataSetRegistrations = new ArrayList<NewExternalData>();
        NewExternalData newExternalData = new NewExternalData();
        newExternalData.setCode("DATA-SET-CODE");
        newExternalData.setSampleIdentifierOrNull(new SampleIdentifierFactory("/SPACE/SAMPLE-ID1")
                .createIdentifier());
        dataSetRegistrations.add(newExternalData);

        List<DataSetBatchUpdatesDTO> dataSetUpdates = new ArrayList<DataSetBatchUpdatesDTO>();
        DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
        dataSetUpdate.setDatasetId(new TechId(1L));
        dataSetUpdates.add(dataSetUpdate);

        List<NewMetaproject> metaprojectRegistrations =
                Collections.singletonList(new NewMetaproject("TEST-AEOD-TAG", "short description",
                        "test"));

        List<MetaprojectUpdatesDTO> metaprojectUpdates = new ArrayList<MetaprojectUpdatesDTO>();

        List<VocabularyUpdatesDTO> vocabularyUpdates = Collections.emptyList();

        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(null, null, spaceRegistrations,
                        projectRegistrations, projectUpdates, experimentRegistrations,
                        experimentUpdates, sampleUpdates, sampleRegistrations,
                        materialRegistrations, materialUpdates,
                        dataSetRegistrations, dataSetUpdates, metaprojectRegistrations,
                        metaprojectUpdates, vocabularyUpdates);

        assertEquals(
                "AtomicEntityOperationDetails[registrationIdOrNull=<null>"
                        + ",userIdOrNull=<null>"
                        + ",spaceRegistrations=[SPACE1, SPACE2]"
                        + ",projectRegistrations=[/SPACE/P1, /SPACE/P2]"
                        + ",projectUpdates=[]"
                        + ",experimentUpdates=[]"
                        + ",experimentRegistrations=[/SPACE/PROJECT/EXP-ID1, /SPACE/PROJECT/EXP-ID2]"
                        + ",sampleUpdates=[]"
                        + ",sampleRegistrations=[/SPACE/SAMPLE-ID1, /SPACE/SAMPLE-ID2]"
                        + ",materialRegistrations={material-type-1=[material-one, material-two], material-type-2=[material-three]}"
                        + ",dataSetRegistrations=[NewExternalData[code=DATA-SET-CODE,type=<null>,kind=<null>,fileFormat=<null>,properties=[]]]"
                        + ",dataSetUpdates=[1]"
                        + ",metaprojectRegistrations=[NewMetaproject[name=TEST-AEOD-TAG,description=short description,ownerId=test]]"
                        + ",metaprojectUpdates=[]" + ",vocabularyUpdates=[]"
                        + ",spaceRoleAssignments=[]" + ",spaceRoleRevocations=[]]", details.toString());

    }
}
