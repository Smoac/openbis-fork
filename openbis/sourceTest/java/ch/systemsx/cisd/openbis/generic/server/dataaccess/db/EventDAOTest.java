/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.Date;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Test cases for {@link EventDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "event" })
public class EventDAOTest extends AbstractDAOTest
{

    private static final long SINCE_LONG = 1000000L;

    private static final Date BEFORE = new Date(SINCE_LONG - 1000000);

    private static final Date SINCE = new Date(SINCE_LONG);

    private static final Date AFTER = new Date(SINCE_LONG + 1000000);

    private static final String KEEP_ME = "KEEP-ME";

    private static final String DELETE_ME = "DELETE-ME";

    private void assertCorrectResult(int numberOfDataSets, List<DeletedDataSet> result)
    {
        AssertJUnit.assertEquals(numberOfDataSets, result.size());
        for (DeletedDataSet d : result)
        {
            AssertJUnit.assertTrue(d.getIdentifier().startsWith(DELETE_ME));
        }
    }

    private List<DeletedDataSet> listDataDeletionEvents(Date parameter)
    {
        return daoFactory.getEventDAO().listDeletedDataSets(parameter);
    }

    @Test
    public void testListDeletedDataSetsWithoutSinceDate() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.SAMPLE, KEEP_ME, AFTER);
        saveEvent(EventType.INVALIDATION, EntityType.DATASET, KEEP_ME, AFTER);
        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {

            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER);
        }
        saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME, BEFORE);

        List<DeletedDataSet> result = listDataDeletionEvents(null);
        assertCorrectResult(numberOfDataSets + 1, result);
    }

    @Test
    public void testListSomeDataSets() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.DATASET, KEEP_ME, BEFORE);
        saveEvent(EventType.DELETION, EntityType.SAMPLE, KEEP_ME, AFTER);
        saveEvent(EventType.INVALIDATION, EntityType.DATASET, KEEP_ME, AFTER);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER);
        }
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);

        assertCorrectResult(numberOfDataSets, result);
    }

    @Test
    public void testListDataSetsWithNothingNew() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.DATASET, KEEP_ME, BEFORE);
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);
        assertCorrectResult(0, result);
    }

    @Test
    public void testListDataSetsAndNoSamples() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.SAMPLE, KEEP_ME, AFTER);
        saveEvent(EventType.INVALIDATION, EntityType.SAMPLE, KEEP_ME, AFTER);
        saveEvent(EventType.MOVEMENT, EntityType.SAMPLE, KEEP_ME, AFTER);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER);
        }
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);

        assertCorrectResult(numberOfDataSets, result);
    }

    @Test
    public void testListDeletedDataSetsAndNotCreated() throws Exception
    {
        saveEvent(EventType.INVALIDATION, EntityType.DATASET, KEEP_ME, AFTER);
        saveEvent(EventType.MOVEMENT, EntityType.DATASET, KEEP_ME, AFTER);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER);
        }
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);

        assertCorrectResult(numberOfDataSets, result);
    }

    private void saveEvent(EventType eventType, EntityType entityType, String identifier, Date date)
    {
        String description = eventType.name() + " " + entityType.name();
        PersonPE person = getSystemPerson();
        Long personId = HibernateUtils.getId(person);
        // Done via SQL because it's not possible to set the value of 'date' field in EventPE to
        // chosen date
        simpleJdbcTemplate
                .update(
                        "insert into events "
                                + "(id, event_type, description, reason, pers_id_registerer, registration_timestamp, identifier, entity_type) "
                                + "values(nextval('event_id_seq'), ?, ?, ?, ?, ?, ?, ?)", eventType
                                .name(), description, description, personId, date, identifier,
                        entityType.name());
    }
}