/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.delete.EntityTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Franz-Josef Elmer
 */
public class DeleteEntityTypeTest extends AbstractTest
{
    @Test
    public void testDelete()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<EntityTypePermId> entityTypeIds = new ArrayList<>();
        DataSetTypeCreation dataSetTypeCreation = new DataSetTypeCreation();
        dataSetTypeCreation.setCode("DATA_SET_TYPE_" + System.currentTimeMillis());
        entityTypeIds.addAll(v3api.createDataSetTypes(sessionToken, Arrays.asList(dataSetTypeCreation)));
        ExperimentTypeCreation experimentTypeCreation = new ExperimentTypeCreation();
        experimentTypeCreation.setCode("EXPERIMENT_TYPE_" + System.currentTimeMillis());
        entityTypeIds.addAll(v3api.createExperimentTypes(sessionToken, Arrays.asList(experimentTypeCreation)));
        MaterialTypeCreation materialTypeCreation = new MaterialTypeCreation();
        materialTypeCreation.setCode("MATERIAL_TYPE_" + System.currentTimeMillis());
        entityTypeIds.addAll(v3api.createMaterialTypes(sessionToken, Arrays.asList(materialTypeCreation)));
        SampleTypeCreation sampleTypeCreation = new SampleTypeCreation();
        sampleTypeCreation.setCode("SAMPLE_TYPE_" + System.currentTimeMillis());
        entityTypeIds.addAll(v3api.createSampleTypes(sessionToken, Arrays.asList(sampleTypeCreation)));
        EntityTypeDeletionOptions deletionOptions = new EntityTypeDeletionOptions();
        deletionOptions.setReason("test");

        // When
        v3api.deleteEntityTypes(sessionToken, entityTypeIds, deletionOptions);

        // Then
        DataSetTypeSearchCriteria dataSetTypeSearchCriteria = new DataSetTypeSearchCriteria();
        dataSetTypeSearchCriteria.withCode().thatEquals(dataSetTypeCreation.getCode());
        assertEquals(v3api.searchDataSetTypes(sessionToken, dataSetTypeSearchCriteria, new DataSetTypeFetchOptions()).getObjects().toString(), "[]");
        ExperimentTypeSearchCriteria experimentTypeSearchCriteria = new ExperimentTypeSearchCriteria();
        experimentTypeSearchCriteria.withCode().thatEquals(experimentTypeCreation.getCode());
        assertEquals(
                v3api.searchExperimentTypes(sessionToken, experimentTypeSearchCriteria, new ExperimentTypeFetchOptions()).getObjects().toString(),
                "[]");
        MaterialTypeSearchCriteria materialTypeSearchCriteria = new MaterialTypeSearchCriteria();
        materialTypeSearchCriteria.withCode().thatEquals(materialTypeCreation.getCode());
        assertEquals(v3api.searchMaterialTypes(sessionToken, materialTypeSearchCriteria, new MaterialTypeFetchOptions()).getObjects().toString(),
                "[]");
        SampleTypeSearchCriteria sampleTypeSearchCriteria = new SampleTypeSearchCriteria();
        sampleTypeSearchCriteria.withCode().thatEquals(sampleTypeCreation.getCode());
        assertEquals(v3api.searchSampleTypes(sessionToken, sampleTypeSearchCriteria, new SampleTypeFetchOptions()).getObjects().toString(), "[]");
    }

    @Test
    public void testDeleteWithUnspecifiedEntityKind()
    {
        EntityTypePermId typeId = new EntityTypePermId("DELETION_TEST");
        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // Given
                    String sessionToken = v3api.login(TEST_USER, PASSWORD);
                    EntityTypeDeletionOptions deletionOptions = new EntityTypeDeletionOptions();
                    deletionOptions.setReason("test");

                    // When
                    v3api.deleteEntityTypes(sessionToken, Arrays.asList(typeId), deletionOptions);
                }
            }, "Entity type entity kind cannot be null");
    }

    @Test(dataProvider = "usersNotAllowedToDelete")
    public void testDeleteWithUserCausingAuthorizationFailure(EntityKind entityKind, final String user)
    {
        EntityTypePermId typeId = new EntityTypePermId("DELETION_TEST", entityKind);
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    // Given
                    String sessionToken = v3api.login(user, PASSWORD);
                    EntityTypeDeletionOptions deletionOptions = new EntityTypeDeletionOptions();
                    deletionOptions.setReason("test");

                    // When
                    v3api.deleteEntityTypes(sessionToken, Arrays.asList(typeId), deletionOptions);
                }
            }, typeId);
    }

    @DataProvider
    Object[][] usersNotAllowedToDelete()
    {
        List<String> users = Arrays.asList(TEST_GROUP_ADMIN, TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER,
                TEST_INSTANCE_OBSERVER, TEST_OBSERVER_CISD, TEST_POWER_USER_CISD, TEST_SPACE_USER);
        EntityKind[] entityKinds = EntityKind.values();
        Object[][] objects = new Object[entityKinds.length * users.size()][];
        for (int i = 0; i < entityKinds.length; i++)
        {
            for (int j = 0; j < users.size(); j++)
            {

                objects[users.size() * i + j] = new Object[] { entityKinds[i], users.get(j) };
            }
        }
        return objects;
    }

}
