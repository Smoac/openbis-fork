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

    private static final long SINCE_LONG = 10;

    private static final long BEFORE = SINCE_LONG - 5;

    private static final long SINCE = SINCE_LONG;

    private static final long AFTER = SINCE_LONG + 5;

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

    private List<DeletedDataSet> listDataDeletionEvents(Long lastSeenDeletionEventIdOrNull)
    {
        return daoFactory.getEventDAO().listDeletedDataSets(lastSeenDeletionEventIdOrNull);
    }

    @Test
    public void testListDeletedDataSetsWithoutSinceDate() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.SAMPLE, KEEP_ME, AFTER);
        saveEvent(EventType.INVALIDATION, EntityType.DATASET, KEEP_ME, AFTER + 1);
        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {

            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER + 2 + i);
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
        saveEvent(EventType.INVALIDATION, EntityType.DATASET, KEEP_ME, AFTER + 1);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER + 2 + i);
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
        saveEvent(EventType.INVALIDATION, EntityType.SAMPLE, KEEP_ME, AFTER + 1);
        saveEvent(EventType.MOVEMENT, EntityType.SAMPLE, KEEP_ME, AFTER + 2);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER + 3 + i);
        }
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);

        assertCorrectResult(numberOfDataSets, result);
    }

    @Test
    public void testListDeletedDataSetsAndNotCreated() throws Exception
    {
        saveEvent(EventType.INVALIDATION, EntityType.DATASET, KEEP_ME, AFTER);
        saveEvent(EventType.MOVEMENT, EntityType.DATASET, KEEP_ME, AFTER + 1);

        int numberOfDataSets = 3;
        for (int i = 0; i < numberOfDataSets; i++)
        {
            saveEvent(EventType.DELETION, EntityType.DATASET, DELETE_ME + i, AFTER + 2 + i);
        }
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);

        assertCorrectResult(numberOfDataSets, result);
    }

    @Test
    public void testListDeletedDataSetsWithoutSince() throws Exception
    {
        saveEvent(EventType.DELETION, EntityType.DATASET, KEEP_ME + 1, BEFORE);
        saveEvent(EventType.DELETION, EntityType.DATASET, KEEP_ME + 2, SINCE);
        List<DeletedDataSet> result = listDataDeletionEvents(SINCE);
        assertCorrectResult(0, result);
    }

    private void saveEvent(EventType eventType, EntityType entityType, String identifier,
            long eventId)
    {
        String description = eventType.name() + " " + entityType.name();
        PersonPE person = getSystemPerson();
        Long personId = HibernateUtils.getId(person);
        simpleJdbcTemplate
                .update(
                        "insert into events "
                                + "(id, event_type, description, reason, pers_id_registerer, registration_timestamp, identifier, entity_type) "
                                + "values(?, ?, ?, ?, ?, ?, ?, ?)", eventId, eventType.name(),
                        description, description, personId, new Date(), identifier, entityType
                                .name());
    }
}