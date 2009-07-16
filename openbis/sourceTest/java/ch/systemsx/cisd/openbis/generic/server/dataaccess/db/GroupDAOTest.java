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

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * Test cases for corresponding {@link GroupDAO} class.
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups =
    { "db", "group" })
@Friend(toClasses = GroupPE.class)
public final class GroupDAOTest extends AbstractDAOTest
{
    private static final String NEW_TEST_GROUP = "test-group";

    @Test
    public void testCreateGroup()
    {
        final String groupCode = NEW_TEST_GROUP;
        final GroupPE group = createGroup(groupCode);

        final DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        final GroupPE retrievedGroup =
                daoFactory.getGroupDAO().tryFindGroupByCodeAndDatabaseInstance(groupCode,
                        databaseInstance);
        AssertJUnit.assertNotNull(retrievedGroup);
        assertEquals(group.getRegistrator(), retrievedGroup.getRegistrator());
    }

    @Test
    public void testListGroups()
    {
        final List<GroupPE> groups = daoFactory.getGroupDAO().listGroups();
        Collections.sort(groups);
        assertEquals("CISD", groups.get(0).getCode());
        assertEquals("TESTGROUP", groups.get(1).getCode());
        assertEquals(2, groups.size());
    }

    @Test
    public void testListGroupsOfHomeDatabaseInstance()
    {
        final List<GroupPE> groups =
                daoFactory.getGroupDAO().listGroups(daoFactory.getHomeDatabaseInstance());
        Collections.sort(groups);
        assertEquals("CISD", groups.get(0).getCode());
        assertEquals("TESTGROUP", groups.get(1).getCode());
        assertEquals(2, groups.size());
    }

    @Test(groups = "broken")
    public void testListGroupsOfAnotherDatabaseInstance()
    {
        final DatabaseInstancePE databaseInstance = createDatabaseInstance("another-db");
        databaseInstance.setOriginalSource(true); // to cheat GroupDAO
        createGroup("test-group", databaseInstance);
        final List<GroupPE> groups = daoFactory.getGroupDAO().listGroups(databaseInstance);
        assertEquals("TEST-GROUP", groups.get(0).getCode());
        assertEquals(1, groups.size());
    }

    @Test
    public final void testDelete()
    {
        // create new group with no connections
        final String groupCode = NEW_TEST_GROUP;
        createGroup(groupCode);

        final IGroupDAO groupDAO = daoFactory.getGroupDAO();
        final GroupPE deletedGroup = findGroup(groupCode);

        // Deleted group should have all collections which prevent it from deletion empty.
        assertTrue(deletedGroup.getProjects().isEmpty());

        // delete
        groupDAO.delete(deletedGroup);

        // test successful deletion of group
        assertNull(groupDAO.tryGetByTechId(TechId.create(deletedGroup)));
    }

    @Test(expectedExceptions = DataIntegrityViolationException.class)
    public final void testDeleteFail()
    {
        final IGroupDAO groupDAO = daoFactory.getGroupDAO();
        final GroupPE deletedGroup = findGroup("TESTGROUP");

        // Deleted project should have projects which prevent it from deletion.
        assertFalse(deletedGroup.getProjects().isEmpty());

        // delete
        groupDAO.delete(deletedGroup);
    }

    private final GroupPE findGroup(String code)
    {
        final IGroupDAO groupDAO = daoFactory.getGroupDAO();
        final DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        final GroupPE group =
                groupDAO.tryFindGroupByCodeAndDatabaseInstance(code, databaseInstance);
        assertNotNull(group);

        return group;
    }
}
