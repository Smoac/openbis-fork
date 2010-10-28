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
import static org.testng.AssertJUnit.assertNull;

import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * Test cases for {@link ScriptDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "script" })
@Friend(toClasses = GroupPE.class)
public final class ScriptDAOTest extends AbstractDAOTest
{

    private static final String DESCRIPTION = "desc";

    private static final String SCRIPT = "1+1";

    private static final String NAME = "name";

    private static String createScriptDescription(int i)
    {
        return DESCRIPTION + i;
    }

    private static String createScriptText(int i)
    {
        return SCRIPT + i;
    }

    private static String createScriptName(int i)
    {
        return NAME + i;
    }

    @Test
    public void testCreateScript()
    {
        String name = NAME;
        AssertJUnit.assertNull(daoFactory.getScriptDAO().tryFindByName(name));
        String scriptText = SCRIPT;
        String description = DESCRIPTION;
        final ScriptPE script = createScriptInDB(name, scriptText, description);
        final ScriptPE retrievedScript = daoFactory.getScriptDAO().tryFindByName(name);
        AssertJUnit.assertNotNull(retrievedScript);
        assertEquals(script.getRegistrator(), retrievedScript.getRegistrator());
        assertEquals(script.getDatabaseInstance(), daoFactory.getHomeDatabaseInstance());
        assertEquals(script.getScript(), scriptText);
        assertEquals(script.getDescription(), description);
        assertEquals(script.getName(), name);
    }

    @Test
    public void testListScripts()
    {
        assertEquals(0, daoFactory.getScriptDAO().listAllEntities().size());
        int numberOfScripts = 10;
        for (int i = 0; i < numberOfScripts; i++)
        {
            createScriptInDB(createScriptName(i), createScriptText(i), createScriptDescription(i));
        }
        final List<ScriptPE> scripts = daoFactory.getScriptDAO().listAllEntities();
        assertEquals(numberOfScripts, scripts.size());
        Collections.sort(scripts);
        int index = 3;
        ScriptPE script = scripts.get(index);
        assertEquals(daoFactory.getHomeDatabaseInstance(), script.getDatabaseInstance());
        assertEquals(createScriptName(index), script.getName());
        assertEquals(createScriptText(index), script.getScript());
        assertEquals(createScriptDescription(index), script.getDescription());
    }

    @Test
    public final void testDelete()
    {
        assertNull(daoFactory.getScriptDAO().tryFindByName(NAME));
        ScriptPE script = createScriptInDB(NAME, SCRIPT, DESCRIPTION);
        assertNotNull(daoFactory.getScriptDAO().tryFindByName(NAME));
        daoFactory.getScriptDAO().delete(script);
        assertNull(daoFactory.getScriptDAO().tryFindByName(NAME));
    }

}