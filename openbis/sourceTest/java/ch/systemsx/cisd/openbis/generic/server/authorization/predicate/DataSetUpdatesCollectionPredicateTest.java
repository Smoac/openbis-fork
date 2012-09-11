/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.Arrays;
import java.util.HashSet;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetUpdatesCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetUpdatesCollectionPredicateTest extends AuthorizationTestCase
{
    @Test
    public void testSuccessful()
    {
        DataSetUpdatesDTO ds1 = new DataSetUpdatesDTO();
        ds1.setDatasetId(new TechId(42L));
        ds1.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse("/" + SPACE_CODE
                + "/P/E"));
        ds1.setSampleIdentifierOrNull(SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S"));
        prepareProvider(createDatabaseInstance(), createGroups());
        DataSetUpdatesCollectionPredicate predicate = new DataSetUpdatesCollectionPredicate();
        predicate.init(provider);
        context.checking(new Expectations()
            {
                {
                    one(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.DATASET,
                            TechId.createList(42L));
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(createGroup()))));
                }
            });

        Status result = predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(ds1));

        assertEquals("OK", result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testFailedBecauseOfDataSet()
    {
        DataSetUpdatesDTO ds1 = new DataSetUpdatesDTO();
        ds1.setDatasetId(new TechId(42L));
        ds1.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse("/" + SPACE_CODE
                + "/P/E"));
        ds1.setSampleIdentifierOrNull(SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S"));
        prepareProvider(createDatabaseInstance(), createGroups());
        DataSetUpdatesCollectionPredicate predicate = new DataSetUpdatesCollectionPredicate();
        predicate.init(provider);
        context.checking(new Expectations()
            {
                {
                    one(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.DATASET,
                            TechId.createList(42L));
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(createAnotherGroup()))));
                }
            });

        Status result = predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(ds1));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testFailedBecauseOfExperiment()
    {
        DataSetUpdatesDTO ds1 = new DataSetUpdatesDTO();
        ds1.setDatasetId(new TechId(42L));
        ds1.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse("/"
                + ANOTHER_GROUP_CODE + "/P/E"));
        ds1.setSampleIdentifierOrNull(SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S"));
        prepareProvider(createDatabaseInstance(), createGroups());
        DataSetUpdatesCollectionPredicate predicate = new DataSetUpdatesCollectionPredicate();
        predicate.init(provider);

        Status result = predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(ds1));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testFailedBecauseOfSample()
    {
        DataSetUpdatesDTO ds1 = new DataSetUpdatesDTO();
        ds1.setDatasetId(new TechId(42L));
        ds1.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse("/" + SPACE_CODE
                + "/P/E"));
        ds1.setSampleIdentifierOrNull(SampleIdentifierFactory
                .parse("/" + ANOTHER_GROUP_CODE + "/S"));
        prepareProvider(createDatabaseInstance(), createGroups());
        DataSetUpdatesCollectionPredicate predicate = new DataSetUpdatesCollectionPredicate();
        predicate.init(provider);
        context.checking(new Expectations()
            {
                {
                    one(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.DATASET,
                            TechId.createList(42L));
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(createGroup()))));
                }
            });

        Status result = predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(ds1));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }
}
