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
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.SampleUpdatesCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE.SampleOwnerType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class SampleUpdatesCollectionPredicateTest extends AuthorizationTestCase
{
    @Test
    public void testEvaluateOK()
    {
        SampleUpdatesDTO sampleWithId =
                new SampleUpdatesDTO(new TechId(42L), null, null, null, null, null, null, null);
        SampleUpdatesDTO sampleWithIdAndExperiment =
                new SampleUpdatesDTO(new TechId(43L), null, ExperimentIdentifierFactory.parse("/"
                        + SPACE_CODE + "/B/E"), null, null, null, null, null);
        SampleUpdatesDTO sampleWithIdAndIdentifer =
                new SampleUpdatesDTO(new TechId(44L), null, null, null, null,
                        SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S1"), null, null);
        prepareProvider(createDatabaseInstance(), createGroups());
        SampleUpdatesCollectionPredicate predicate = new SampleUpdatesCollectionPredicate();
        predicate.init(provider);
        context.checking(new Expectations()
            {
                {
                    one(provider).getSampleCollectionAccessData(TechId.createList(42L, 43L, 44L));
                    SampleAccessPE sample = new SampleAccessPE();
                    sample.setOwnerCode(SPACE_CODE);
                    sample.setOwnerType(SampleOwnerType.SPACE);
                    will(returnValue(new HashSet<SampleAccessPE>(Arrays.asList(sample))));
                }
            });

        Status result =
                predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(sampleWithId,
                        sampleWithIdAndExperiment, sampleWithIdAndIdentifer));

        assertEquals("OK", result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testEvaluateFailedBecauseOfSampleId()
    {
        SampleUpdatesDTO sampleWithId =
                new SampleUpdatesDTO(new TechId(42L), null, null, null, null, null, null, null);
        SampleUpdatesDTO sampleWithIdAndExperiment =
                new SampleUpdatesDTO(new TechId(43L), null, ExperimentIdentifierFactory.parse("/"
                        + SPACE_CODE + "/B/E"), null, null, null, null, null);
        SampleUpdatesDTO sampleWithIdAndIdentifer =
                new SampleUpdatesDTO(new TechId(44L), null, null, null, null,
                        SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S1"), null, null);
        prepareProvider(createDatabaseInstance(), createGroups());
        SampleUpdatesCollectionPredicate predicate = new SampleUpdatesCollectionPredicate();
        predicate.init(provider);
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance());
        context.checking(new Expectations()
            {
                {
                    one(provider).getSampleCollectionAccessData(TechId.createList(42L, 43L, 44L));
                    SampleAccessPE sample = new SampleAccessPE();
                    sample.setOwnerCode(ANOTHER_INSTANCE_CODE);
                    sample.setOwnerType(SampleOwnerType.DATABASE_INSTANCE);
                    will(returnValue(new HashSet<SampleAccessPE>(Arrays.asList(sample))));
                }
            });

        Status result =
                predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(sampleWithId,
                        sampleWithIdAndExperiment, sampleWithIdAndIdentifer));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges to read "
                + "from database instance 'DB2'.\"", result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testEvaluateFailedBecauseOfExperiment()
    {
        SampleUpdatesDTO sampleWithId =
                new SampleUpdatesDTO(new TechId(42L), null, null, null, null, null, null, null);
        SampleUpdatesDTO sampleWithIdAndExperiment =
                new SampleUpdatesDTO(new TechId(43L), null, ExperimentIdentifierFactory.parse("/"
                        + ANOTHER_GROUP_CODE + "/B/E"), null, null, null, null, null);
        SampleUpdatesDTO sampleWithIdAndIdentifer =
                new SampleUpdatesDTO(new TechId(44L), null, null, null, null,
                        SampleIdentifierFactory.parse("/" + SPACE_CODE + "/S1"), null, null);
        prepareProvider(createDatabaseInstance(), createGroups());
        SampleUpdatesCollectionPredicate predicate = new SampleUpdatesCollectionPredicate();
        predicate.init(provider);

        Status result =
                predicate.evaluate(createPerson(), createRoles(false), Arrays.asList(sampleWithId,
                        sampleWithIdAndExperiment, sampleWithIdAndIdentifer));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testEvaluateFailedBecauseOfSampleIdentifier()
    {
        SampleUpdatesDTO sampleWithId =
                new SampleUpdatesDTO(new TechId(42L), null, null, null, null, null, null, null);
        SampleUpdatesDTO sampleWithIdAndExperiment =
                new SampleUpdatesDTO(new TechId(43L), null, ExperimentIdentifierFactory.parse("/"
                        + SPACE_CODE + "/B/E"), null, null, null, null, null);
        SampleUpdatesDTO sampleWithIdAndIdentifer =
                new SampleUpdatesDTO(new TechId(44L), null, null, null, null,
                        SampleIdentifierFactory.parse("/" + ANOTHER_GROUP_CODE + "/S1"), null, null);
        prepareProvider(createDatabaseInstance(), createGroups());
        SampleUpdatesCollectionPredicate predicate = new SampleUpdatesCollectionPredicate();
        predicate.init(provider);
        context.checking(new Expectations()
            {
                {
                    one(provider).getSampleCollectionAccessData(TechId.createList(42L, 43L, 44L));
                }
            });

        Status result =
                predicate.evaluate(createPerson(), createRoles(true), Arrays.asList(sampleWithId,
                        sampleWithIdAndExperiment, sampleWithIdAndIdentifer));

        assertEquals("ERROR: \"User 'megapixel' does not have enough privileges.\"",
                result.toString());
        context.assertIsSatisfied();
    }
}
