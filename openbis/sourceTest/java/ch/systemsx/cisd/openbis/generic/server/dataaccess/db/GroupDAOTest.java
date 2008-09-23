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

import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = "db")
public class GroupDAOTest extends AbstractDAOTest
{
    @Test
    public void testCreateGroup()
    {
        String groupCode = "test-group";
        GroupPE group = createGroup(groupCode);
        
        DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        GroupPE retrievedGroup =
                daoFactory.getGroupDAO().tryFindGroupByCodeAndDatabaseInstanceId(groupCode,
                        databaseInstance.getId());
        AssertJUnit.assertNotNull(retrievedGroup);
        assertEquals(group.getRegistrator(), retrievedGroup.getRegistrator());
    }
    
    @Test
    public void testListGroups()
    {
        List<GroupPE> groups = daoFactory.getGroupDAO().listGroups();
        Collections.sort(groups);
        assertEquals("3V", groups.get(0).getCode());
        assertEquals("DEFAULT", groups.get(1).getCode());
        assertEquals("IMSB", groups.get(2).getCode());
        assertEquals(3, groups.size());
    }
    
    @Test
    public void testListGroupsOfHomeDatabaseInstance()
    {
        List<GroupPE> groups = daoFactory.getGroupDAO().listGroups(daoFactory.getHomeDatabaseInstance());
        Collections.sort(groups);
        assertEquals("3V", groups.get(0).getCode());
        assertEquals("DEFAULT", groups.get(1).getCode());
        assertEquals("IMSB", groups.get(2).getCode());
        assertEquals(3, groups.size());
    }
    
    @Test
    public void testListGroupsOfAnotherDatabaseInstance()
    {
        DatabaseInstancePE databaseInstance = createDatabaseInstance("another-db");
        databaseInstance.setOriginalSource(true); // to cheat GroupDAO
        createGroup("test-group", databaseInstance);
        List<GroupPE> groups = daoFactory.getGroupDAO().listGroups(databaseInstance);
        assertEquals("TEST-GROUP", groups.get(0).getCode());
        assertEquals(1, groups.size());
    }
}
