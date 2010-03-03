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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Test cases for {@link NewSamplesWithTypePredicate}.
 * 
 * @author Izabela Adamczyk
 */
public class NewSamplesWithTypePredicateTest extends AuthorizationTestCase
{

    private IPredicate<SampleOwnerIdentifier> delegate;

    @Override
    @BeforeMethod
    public void setUp()
    {
        super.setUp();
        delegate = createSampleOwnerPredicate();
    }

    @SuppressWarnings("unchecked")
    private IPredicate<SampleOwnerIdentifier> createSampleOwnerPredicate()
    {
        return context.mock(IPredicate.class);
    }

    @SuppressWarnings("deprecation")
    private NewSamplesWithTypePredicate createPredicate()
    {
        return new NewSamplesWithTypePredicate(delegate);
    }

    @Test
    public void testAllowAllSamples() throws Exception
    {
        NewSamplesWithTypePredicate predicate = createPredicate();
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> roles = createRoles(true);
        final SampleIdentifier sampleIdentifier1 =
                new SampleIdentifier(new DatabaseInstanceIdentifier(INSTANCE_CODE), "s1");
        final SampleIdentifier sampleIdentifier2 =
                new SampleIdentifier(new DatabaseInstanceIdentifier(INSTANCE_CODE), "s2");
        context.checking(new Expectations()
            {
                {
                    one(delegate).init(provider);
                    one(delegate).evaluate(person, roles, sampleIdentifier1);
                    will(returnValue(Status.OK));
                    one(delegate).evaluate(person, roles, sampleIdentifier2);
                    will(returnValue(Status.OK));
                }
            });
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);
        Status status =
                predicate.evaluate(person, roles, new NewSamplesWithTypes(new SampleType(), Arrays
                        .asList(new NewSample(sampleIdentifier1.toString(), null, null, null),
                                new NewSample(sampleIdentifier2.toString(), null, null, null))));
        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testRejectFirstSample() throws Exception
    {
        NewSamplesWithTypePredicate predicate = createPredicate();
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> roles = createRoles(true);
        final SampleIdentifier sampleIdentifier1 =
                new SampleIdentifier(new DatabaseInstanceIdentifier(INSTANCE_CODE), "s1");
        final SampleIdentifier sampleIdentifier2 =
                new SampleIdentifier(new DatabaseInstanceIdentifier(INSTANCE_CODE), "s2");
        context.checking(new Expectations()
            {
                {
                    one(delegate).init(provider);
                    one(delegate).evaluate(person, roles, sampleIdentifier1);
                    will(returnValue(Status.createError()));
                }
            });
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);
        Status status =
                predicate.evaluate(person, roles, new NewSamplesWithTypes(new SampleType(), Arrays
                        .asList(new NewSample(sampleIdentifier1.toString(), null, null, null),
                                new NewSample(sampleIdentifier2.toString(), null, null, null))));
        assertEquals(true, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testRejectLastSample() throws Exception
    {
        NewSamplesWithTypePredicate predicate = createPredicate();
        final PersonPE person = createPerson();
        final List<RoleWithIdentifier> roles = createRoles(true);
        final SampleIdentifier sampleIdentifier1 =
                new SampleIdentifier(new DatabaseInstanceIdentifier(INSTANCE_CODE), "s1");
        final SampleIdentifier sampleIdentifier2 =
                new SampleIdentifier(new DatabaseInstanceIdentifier(INSTANCE_CODE), "s2");
        context.checking(new Expectations()
            {
                {
                    one(delegate).init(provider);
                    one(delegate).evaluate(person, roles, sampleIdentifier1);
                    will(returnValue(Status.OK));
                    one(delegate).evaluate(person, roles, sampleIdentifier2);
                    will(returnValue(Status.createError()));
                }
            });
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);
        Status status =
                predicate.evaluate(person, roles, new NewSamplesWithTypes(new SampleType(), Arrays
                        .asList(new NewSample(sampleIdentifier1.toString(), null, null, null),
                                new NewSample(sampleIdentifier2.toString(), null, null, null))));
        assertEquals(true, status.isError());
        context.assertIsSatisfied();
    }
}
