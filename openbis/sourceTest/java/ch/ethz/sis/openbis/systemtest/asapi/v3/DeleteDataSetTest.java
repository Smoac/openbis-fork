/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class DeleteDataSetTest extends AbstractDeletionTest
{

    private static DataSetDeletionOptions options;

    public static DataSetDeletionOptions getOptions()
    {
        if (options == null)
        {
            options = new DataSetDeletionOptions();
            options.setReason("Just for testing");
        }
        return options;
    }

    @Test
    public void testDeleteEmptyList()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, new ArrayList<DataSetPermId>(), getOptions());
        Assert.assertNull(deletionId);
    }

    @Test
    public void testDeleteDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("COMPONENT_1A");

        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(dataSetId), getOptions());
        Assert.assertNotNull(deletionId);

        assertDataSetDoesNotExist(dataSetId);
    }

    @Test
    public void testDeleteContainerDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId container = new DataSetPermId("CONTAINER_1");
        DataSetPermId component1 = new DataSetPermId("COMPONENT_1A");
        DataSetPermId component2 = new DataSetPermId("COMPONENT_1B");

        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(container), getOptions());
        Assert.assertNotNull(deletionId);

        assertDataSetDoesNotExist(container);
        assertDataSetDoesNotExist(component1);
        assertDataSetDoesNotExist(component2);
    }

    @Test
    public void testDeleteComplexContainerDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId container = new DataSetPermId("CONTAINER_3B");
        DataSetPermId component1 = new DataSetPermId("COMPONENT_3A");
        DataSetPermId component2 = new DataSetPermId("COMPONENT_3AB");

        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(container), getOptions());
        Assert.assertNotNull(deletionId);

        assertDataSetDoesNotExist(container);
        assertDataSetExists(component1);
        assertDataSetExists(component2);
    }

    @Test
    public void testDeleteComplexContainerDataSet1()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId container = new DataSetPermId("CONTAINER_3A");
        DataSetPermId component1 = new DataSetPermId("COMPONENT_3A");
        DataSetPermId component2 = new DataSetPermId("COMPONENT_3AB");

        IDeletionId deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(container), getOptions());
        Assert.assertNotNull(deletionId);

        assertDataSetDoesNotExist(container);
        assertDataSetDoesNotExist(component1);
        assertDataSetExists(component2);
    }

    @Test
    public void testDeleteDSWithAdminUserInAnotherSpace()
    {
        final DataSetPermId permId = new DataSetPermId("20120619092259000-22");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    v3api.deleteDataSets(sessionToken, Collections.singletonList(permId), getOptions());
                }
            }, permId);
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testDeleteWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        String sessionToken = v3api.login(user.getUserId(), PASSWORD);

        IDataSetId dataSetId = new DataSetPermId("20120628092259000-41");

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.deleteDataSets(sessionToken, Arrays.asList(dataSetId), getOptions());
                    }
                });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            v3api.deleteDataSets(sessionToken, Arrays.asList(dataSetId), getOptions());
        } else
        {
            assertUnauthorizedObjectAccessException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        v3api.deleteDataSets(sessionToken, Arrays.asList(dataSetId), getOptions());
                    }
                }, dataSetId);
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetDeletionOptions o = new DataSetDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteDataSets(sessionToken, Arrays.asList(new DataSetPermId("TEST-LOGGING-1"), new DataSetPermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-data-sets  DATA_SET_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('DataSetDeletionOptions[reason=test-reason]')");
    }

    // waiting for better times
    // @Test
    // public void testDeleteContainerInDifferentExperiment()
    // {
    // String sessionToken = v3api.login(TEST_USER, PASSWORD);
    //
    // DataSetPermId containerA = new DataSetPermId("CONTAINER_3A");
    // DataSetPermId containerB = new DataSetPermId("CONTAINER_3B");
    // DataSetPermId component1 = new DataSetPermId("COMPONENT_3A");
    // DataSetPermId component2 = new DataSetPermId("COMPONENT_3AB");
    //
    // DataSetPermId componentDifferentExperiment = new DataSetPermId("COMPONENT_3Ax");
    //
    // assertDataSetExists(containerA);
    // assertDataSetExists(containerB);
    // assertDataSetExists(component1);
    // assertDataSetExists(component2);
    // assertDataSetExists(componentDifferentExperiment);
    //
    // IDeletionId deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(containerA), getOptions());
    // Assert.assertNotNull(deletionId);
    // deletionId = v3api.deleteDataSets(sessionToken, Collections.singletonList(containerB), getOptions());
    // Assert.assertNotNull(deletionId);
    //
    // assertDataSetDoesNotExist(containerA);
    // assertDataSetDoesNotExist(containerB);
    // assertDataSetDoesNotExist(component1);
    // assertDataSetDoesNotExist(component2);
    // assertDataSetExists(componentDifferentExperiment);
    // }

}
