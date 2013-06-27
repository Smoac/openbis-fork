/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.ColumnDisplaySettingsUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Jakub Straszewski
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SessionUpdateTest extends SystemTestCase
{

    private static final String ADMIN = "test";

    @Test
    public void testCreateSpaceForUserAndAssignIdenticalRoleFailsWithNoSideeffect()
    {
        String sessionToken = authenticateAs(ADMIN);
        String sessionTokenUser = authenticateAs("test_space");

        commonServer.tryGetSession(sessionTokenUser);

        String spaceIdentifier = new SpaceIdentifier("TEST_SPACE_1").toString();

        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(ADMIN).space("TEST_SPACE_1", "test_space").
                        assignRoleToSpace(RoleCode.ADMIN, spaceIdentifier, Arrays.asList("test_space"), null).create();

        try
        {
            etlService.performEntityOperations(sessionToken, eo);
            fail("Exception expected");
        } catch (UserFailureException ufe)
        {
            // this is expected
        }

        commonServer.updateDisplaySettings(sessionTokenUser,
                new ColumnDisplaySettingsUpdate("id_a_b_C", Collections.<ColumnSetting> emptyList()));
    }

    @Test
    public void testSpaceWithRoleAssignmentDeleted()
    {
        String sessionTokenForInstanceAdmin = commonServer.tryAuthenticate("test", "a").getSessionToken();

        // reproduce

        AtomicEntityOperationDetails eo =
                new EntityOperationBuilder().user(ADMIN).space("TEST_SPACE_1", "test_space").create();

        etlService.performEntityOperations(sessionTokenForInstanceAdmin, eo);

        String sessionTokenForSpaceAdmin = commonServer.tryAuthenticate("test_space", "a").getSessionToken();

        List<Space> spaces = commonServer.listSpaces(sessionTokenForSpaceAdmin, DatabaseInstanceIdentifier.HOME_INSTANCE);
        Space space = findSpace(spaces, "TEST_SPACE_1");

        commonServer.deleteSpaces(sessionTokenForInstanceAdmin, Arrays.asList(new TechId(space.getId())), "no reason");

        commonServer.updateDisplaySettings(sessionTokenForSpaceAdmin,
                new ColumnDisplaySettingsUpdate("id_a_b_C", Collections.<ColumnSetting> emptyList()));
    }

    @Test
    public void testRoleAssingmentDeleted()
    {
        String sessionTokenForInstanceAdmin = commonServer.tryAuthenticate("test", "a").getSessionToken();
        String sessionTokenForSpaceAdmin = commonServer.tryAuthenticate("test_space", "a").getSessionToken();

        // reproduce

        commonServer.deleteSpaceRole(sessionTokenForInstanceAdmin, RoleCode.ADMIN,
                new SpaceIdentifier("TEST-SPACE"), Grantee.createPerson("test_space"));

        commonServer.updateDisplaySettings(sessionTokenForSpaceAdmin,
                new ColumnDisplaySettingsUpdate("id_a_b_C", Collections.<ColumnSetting> emptyList()));
        // clean up
        commonServer.registerSpaceRole(sessionTokenForInstanceAdmin, RoleCode.ADMIN,
                new SpaceIdentifier("TEST-SPACE"), Grantee.createPerson("test_space"));
    }

    @Test
    public void testRoleAssignmentAdded()
    {
        String spaceCode = "TESTGROUP";

        String sessionTokenForInstanceAdmin = commonServer.tryAuthenticate("test", "a").getSessionToken();
        String sessionTokenForSpaceAdmin = commonServer.tryAuthenticate("test_space", "a").getSessionToken();

        List<Space> spaces = commonServer.listSpaces(sessionTokenForSpaceAdmin, DatabaseInstanceIdentifier.createHome());
        boolean matchingSpaces = containsSpace(spaces, spaceCode);
        Assert.assertFalse(spaceCode + " should not be in test_space user groups before the role assignment" + spaces, matchingSpaces);

        commonServer.registerSpaceRole(sessionTokenForInstanceAdmin, RoleCode.ADMIN,
                new SpaceIdentifier(spaceCode), Grantee.createPerson("test_space"));

        spaces = commonServer.listSpaces(sessionTokenForSpaceAdmin, DatabaseInstanceIdentifier.createHome());
        matchingSpaces = containsSpace(spaces, spaceCode);
        Assert.assertTrue("Couldn't find " + spaceCode + " space in spaces of test_space user. Found only " + spaces, matchingSpaces);

        // cleanup

        commonServer.deleteSpaceRole(sessionTokenForInstanceAdmin, RoleCode.ADMIN,
                new SpaceIdentifier(spaceCode), Grantee.createPerson("test_space"));

    }

    private boolean containsSpace(List<Space> spaces, final String spaceCode)
    {
        int matchingSpaces = CollectionUtils.countMatches(spaces, new Predicate<Space>()
            {
                @Override
                public boolean evaluate(Space object)
                {
                    return object.getCode().equals(spaceCode);
                }

            });
        return matchingSpaces > 0;
    }

    private Space findSpace(List<Space> spaces, final String spaceCode)
    {
        return CollectionUtils.find(spaces, new Predicate<Space>()
            {
                @Override
                public boolean evaluate(Space object)
                {
                    return object.getCode().equals(spaceCode);
                }

            });
    }

}
