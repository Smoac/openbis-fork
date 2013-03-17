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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class NewSamplePredicateTest extends AuthorizationTestCase
{
    @Test
    public void testAllowRegisteringInstanceSample()
    {
        NewSamplePredicate predicate = new NewSamplePredicate();
        SampleType sampleType = new SampleType();
        SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier(ANOTHER_INSTANCE_CODE), "s1");
        NewSample sample =
                NewSample.createWithParent(sampleIdentifier.toString(), sampleType, "container",
                        "parent");
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(createPerson(), createRoles(true), sample);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringInstanceSampleNotAllowedBecauseOfWrongInstance()
    {
        NewSamplePredicate predicate = new NewSamplePredicate();
        SampleType sampleType = new SampleType();
        SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier(INSTANCE_CODE), "s1");
        NewSample sample =
                NewSample.createWithParent(sampleIdentifier.toString(), sampleType, "container",
                        "parent");
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(createPerson(), createRoles(false), sample);

        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges to modify "
                + "database instance 'DB1'.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testAllowRegisteringGroupSample()
    {
        NewSamplePredicate predicate = new NewSamplePredicate();
        SampleType sampleType = new SampleType();
        SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new SpaceIdentifier(INSTANCE_CODE, SPACE_CODE), "s1");
        NewSample sample =
                NewSample.createWithParent(sampleIdentifier.toString(), sampleType, "container",
                        "parent");
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(createPerson(), createRoles(false), sample);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisteringGroupSampleNotAllowedBecauseOfWrongGroup()
    {
        NewSamplePredicate predicate = new NewSamplePredicate();
        SampleType sampleType = new SampleType();
        SpaceIdentifier groupIdentifier = new SpaceIdentifier(INSTANCE_CODE, ANOTHER_SPACE_CODE);
        SampleIdentifier sampleIdentifier = new SampleIdentifier(groupIdentifier, "s1");
        NewSample sample =
                NewSample.createWithParent(sampleIdentifier.toString(), sampleType, "container",
                        "parent");
        List<SpacePE> groups = Arrays.asList(createSpace(groupIdentifier));
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), groups);
        predicate.init(provider);

        Status status = predicate.evaluate(createPerson(), createRoles(false), sample);

        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges.",
                status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
