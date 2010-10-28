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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for {@link ScriptBO}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = ScriptBO.class)
public final class ScriptBOTest extends AbstractBOTest
{
    private static final String SCRIPT = "1+1";

    private static final String NAME = "name";

    private static final String DESCRIPTION = "desc";

    private final ScriptBO createScriptBO()
    {
        return new ScriptBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, scriptFactory);
    }

    @Test
    public final void testSaveWithNullScript()
    {
        boolean fail = true;
        try
        {
            createScriptBO().save();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineAndSave()
    {
        final ScriptBO scriptBO = createScriptBO();
        final DatabaseInstancePE instance = createDatabaseInstance();

        final Script newScript = new Script();
        newScript.setDescription(DESCRIPTION);
        newScript.setName(NAME);
        newScript.setScript(SCRIPT);

        final ScriptPE scriptPE = new ScriptPE();

        context.checking(new Expectations()
            {
                {
                    one(scriptFactory).create();
                    will(returnValue(scriptPE));

                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(instance));

                    one(scriptDAO).createOrUpdate(scriptPE);
                }
            });
        scriptBO.define(newScript);
        scriptBO.save();

        assertEquals(newScript.getDescription(), scriptPE.getDescription());
        assertEquals(newScript.getName(), scriptPE.getName());
        assertEquals(ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(), scriptPE.getRegistrator());
        assertEquals(newScript.getScript(), scriptPE.getScript());
        assertEquals(instance, scriptPE.getDatabaseInstance());
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteByTechId() throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();
        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setId(1L);
        final TechId techId = TechId.create(scriptPE);
        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(techId);
                    will(returnValue(scriptPE));

                    one(scriptDAO).delete(scriptPE);
                }
            });
        scriptBO.deleteByTechId(techId);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteUnexistentScript() throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();
        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setId(1L);
        final TechId techId = TechId.create(scriptPE);
        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(techId);
                    will(throwException(new DataRetrievalFailureException("Not found")));

                }
            });
        boolean excetionThrown = false;
        try
        {
            scriptBO.deleteByTechId(techId);
        } catch (UserFailureException ex)
        {
            excetionThrown = true;
        }
        assertTrue(excetionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteConnectedScript() throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();
        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setId(1L);
        final TechId techId = TechId.create(scriptPE);
        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(techId);
                    will(returnValue(scriptPE));

                    one(scriptDAO).delete(scriptPE);
                    will(throwException(new DataIntegrityViolationException("")));

                }
            });
        boolean excetionThrown = false;
        try
        {
            scriptBO.deleteByTechId(techId);
        } catch (UserFailureException ex)
        {
            excetionThrown = true;
        }
        assertTrue(excetionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateScriptNotChanged() throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();

        final Script updates = new Script();
        String description = DESCRIPTION;
        updates.setDescription(description);
        String name = NAME;
        updates.setName(name);
        String script = SCRIPT;
        updates.setScript(script);
        updates.setId(1L);

        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setName(name + 1);
        scriptPE.setScript(script);
        scriptPE.setDescription(description + 1);

        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(TechId.create(updates));
                    will(returnValue(scriptPE));

                    one(scriptDAO).createOrUpdate(scriptPE);
                }
            });

        assertFalse(updates.getDescription().equals(scriptPE.getDescription()));
        assertFalse(updates.getName().equals(scriptPE.getName()));
        assertEquals(updates.getScript(), scriptPE.getScript());

        scriptBO.update(updates);

        assertEquals(updates.getDescription(), scriptPE.getDescription());
        assertEquals(updates.getName(), scriptPE.getName());
        assertEquals(updates.getScript(), scriptPE.getScript());
        context.assertIsSatisfied();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUpdateScriptChanged() throws Exception
    {
        final ScriptBO scriptBO = createScriptBO();

        final Script updates = new Script();
        String description = DESCRIPTION;
        updates.setDescription(description);
        String name = NAME;
        updates.setName(name);
        String script = SCRIPT;
        updates.setScript(script);
        updates.setId(1L);

        final ScriptPE scriptPE = new ScriptPE();
        scriptPE.setName(name + 1);
        scriptPE.setScript(script + 1);
        scriptPE.setDescription(description + 1);
        final SampleTypePropertyTypePE etpt = new SampleTypePropertyTypePE();
        SampleTypePE sampleType = new SampleTypePE();
        etpt.setEntityType(sampleType);
        etpt.setDynamic(true);
        etpt.setScript(scriptPE);
        scriptPE.getSampleAssignments().add(etpt);

        context.checking(new Expectations()
            {
                {
                    one(scriptDAO).getByTechId(TechId.create(updates));
                    will(returnValue(scriptPE));

                    one(scriptDAO).createOrUpdate(scriptPE);

                    one(daoFactory).getEntityPropertyTypeDAO(EntityKind.SAMPLE);
                    will(returnValue(entityPropertyTypeDAO));

                    one(entityPropertyTypeDAO).scheduleDynamicPropertiesEvaluation(etpt);
                }
            });

        assertFalse(updates.getDescription().equals(scriptPE.getDescription()));
        assertFalse(updates.getName().equals(scriptPE.getName()));
        assertFalse(updates.getScript().equals(scriptPE.getScript()));

        scriptBO.update(updates);

        assertEquals(updates.getDescription(), scriptPE.getDescription());
        assertEquals(updates.getName(), scriptPE.getName());
        assertEquals(updates.getScript(), scriptPE.getScript());
        context.assertIsSatisfied();
    }

}