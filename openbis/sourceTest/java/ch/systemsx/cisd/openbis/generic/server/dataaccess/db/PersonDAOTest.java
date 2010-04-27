/*
 * Copyright 2007 ETH Zuerich, CISD
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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Test cases for corresponding {@link PersonDAO} class.
 * 
 * @author Christian Ribeaud
 */
@Test(groups =
    { "db", "person" })
public final class PersonDAOTest extends AbstractDAOTest
{
    public final static String getTestUserId()
    {
        return "ribose".toUpperCase();
    }

    public final static PersonPE createPerson()
    {
        final PersonPE person = new PersonPE();
        person.setId(2L);
        person.setFirstName("Christian");
        person.setLastName("Ribeaud");
        person.setEmail("christian.ribeaud@systemsx.ch");
        person.setUserId(getTestUserId());
        return person;
    }

    @Test
    public final void testCreatePersonWithAnotherDatabaseInstance()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final PersonPE testPerson = createPerson();
        // Change database instance id.
        changeDatabaseInstanceId(personDAO);
        testPerson.setUserId("NOT_RIBOSE");
        personDAO.createPerson(testPerson);
        resetDatabaseInstanceId(personDAO);
        final PersonPE person = personDAO.tryFindPersonByUserId(testPerson.getUserId());
        assertNull(person);
    }

    @Test
    @Rollback(false)
    public final void testCreatePerson()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final PersonPE testPerson = createPerson();
        try
        {
            // Try with <code>null</code>
            personDAO.createPerson(null);
            fail("AssertionError expected");
        } catch (final AssertionError e)
        {
            assertEquals("Given person can not be null.", e.getMessage());
        }
        personDAO.createPerson(testPerson);
        final List<PersonPE> persons = personDAO.listPersons();
        assertEquals(5, persons.size());
        final PersonPE testPersonFromDB = personDAO.getPerson(testPerson.getId());
        assertEquals(testPerson, testPersonFromDB);
    }

    @Test
    public final void testCreatePersonWithValidationFailed()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final PersonPE testPerson = createPerson();
        testPerson.setUserId(StringUtils.repeat("A", 51));
        // User id too long
        try
        {
            personDAO.createPerson(testPerson);
            fail("User id exceeds the maximum length");
        } catch (final DataIntegrityViolationException ex)
        {
            assertTrue(ex.getMessage().indexOf("is too long") > -1);
        }
    }

    @Test
    public final void testCreatePersonWithWhiteSpacesInEmail()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final PersonPE testPerson = createPerson();
        testPerson.setUserId("myUserId");
        // White spaces in email address
        testPerson.setEmail("h@g.com ");
        personDAO.createPerson(testPerson);
    }

    @Test(dependsOnMethods = "testCreatePerson")
    public final void testTryFindPersonIdByUserID()
    {
        final PersonPE testPerson = createPerson();
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        boolean fail = true;
        try
        {
            personDAO.tryFindPersonByUserId(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertEquals(false, fail);
        // Get a person given its user Id.
        final Long id = personDAO.tryFindPersonByUserId(testPerson.getUserId()).getId();
        assertNotNull(id);
        // Change database instance id.
        changeDatabaseInstanceId(personDAO);
        assertNull(personDAO.tryFindPersonByUserId(testPerson.getUserId()));
        resetDatabaseInstanceId(personDAO);

    }

    public final void testListPersons()
    {
        testCreatePersonWithAnotherDatabaseInstance();
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final List<PersonPE> list = personDAO.listPersons();
        assertTrue(list.size() > 0);
        final PersonPE personPE = list.get(0);
        personPE.getRoleAssignments();
        // Change database instance id.
        changeDatabaseInstanceId(personDAO);
        assertEquals(1, personDAO.listPersons().size());
        resetDatabaseInstanceId(personDAO);
    }

    @Test
    public final void testGetPersonForId()
    {
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        try
        {
            personDAO.getPerson(100L);
            fail("Given id does not exist.");
        } catch (final DataAccessException ex)
        {
            // Nothing to do here.
        }
        final PersonPE personDTO = personDAO.listPersons().get(0);
        final Long id = personDTO.getId();
        assertEquals(personDTO, personDAO.getPerson(id));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUpdate()
    {
        IPersonDAO personDAO = daoFactory.getPersonDAO();
        List<PersonPE> persons = personDAO.listPersons();
        assertEquals(false, persons.isEmpty());

        PersonPE person = persons.get(0);
        assertEquals(0, person.getDisplaySettings().getColumnSettings().size());

        DisplaySettings displaySettings = new DisplaySettings();
        ColumnSetting columnSetting = new ColumnSetting();
        columnSetting.setColumnID("column1");
        displaySettings.getColumnSettings().put("id", Arrays.asList(columnSetting));
        person.setDisplaySettings(displaySettings);

        personDAO.updatePerson(person);

        PersonPE reloadedPerson = personDAO.tryFindPersonByUserId(person.getUserId());
        displaySettings = reloadedPerson.getDisplaySettings();
        Map<String, List<ColumnSetting>> columnSettings = displaySettings.getColumnSettings();
        List<ColumnSetting> settings = columnSettings.get("id");
        assertEquals(columnSetting.getColumnID(), settings.get(0).getColumnID());
    }
}