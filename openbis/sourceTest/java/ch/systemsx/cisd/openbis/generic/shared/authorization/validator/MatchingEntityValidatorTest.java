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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;

/**
 * Test cases for corresponding {@link MatchingEntityValidator} class.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses = MatchingEntityValidator.class)
public final class MatchingEntityValidatorTest
{

    private final static ExperimentPE createExperiment()
    {
        final ExperimentPE experiment = new ExperimentPE();
        final ProjectPE projectPE = new ProjectPE();
        projectPE.setGroup(GroupValidatorTest.createGroup());
        experiment.setProject(projectPE);
        return experiment;
    }

    private final static SamplePE createGroupSample()
    {
        final SamplePE sample = new SamplePE();
        sample.setGroup(GroupValidatorTest.createGroup());
        return sample;
    }

    private final static SamplePE createDatabaseSample()
    {
        final SamplePE sample = new SamplePE();
        sample.setDatabaseInstance(GroupValidatorTest.createDatabaseInstance());
        return sample;
    }

    final static PersonPE createPerson(final boolean withRoleAssignment)
    {
        final PersonPE person = new PersonPE();
        if (withRoleAssignment)
        {
            final Set<RoleAssignmentPE> list = new HashSet<RoleAssignmentPE>();
            // Group assignment
            final RoleAssignmentPE assignment = new RoleAssignmentPE();
            assignment.setRole(RoleCode.USER);
            assignment.setPerson(person);
            assignment.setGroup(GroupValidatorTest.createAnotherGroup());
            list.add(assignment);
            person.setRoleAssignments(list);
        }
        return person;
    }

    @Test
    public final void testIsValidFailed()
    {
        boolean fail = true;
        try
        {
            new MatchingEntityValidator().isValid(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testIsValidWithExperiment()
    {
        final PersonPE person = createPerson(true);
        final ExperimentPE experiment = createExperiment();
        final MatchingEntityValidator validator = new MatchingEntityValidator();
        // Different group
        assertFalse(validator.isValid(person, asHit(experiment)));
        // Same group
        experiment.getProject().getGroup().setId(GroupValidatorTest.ANOTHER_GROUP_ID);
        assertTrue(validator.isValid(person, asHit(experiment)));
    }

    private static SearchHit asHit(IMatchingEntity matchingEntity)
    {
        return new SearchHit(matchingEntity, "unimportant", "?");
    }

    @Test
    public final void testIsValidWithSample()
    {
        final PersonPE person = createPerson(true);
        final SamplePE groupSample = createGroupSample();
        final MatchingEntityValidator validator = new MatchingEntityValidator();
        // Different group
        assertFalse(validator.isValid(person, asHit(groupSample)));
        // Same group
        groupSample.getGroup().setId(GroupValidatorTest.ANOTHER_GROUP_ID);
        assertTrue(validator.isValid(person, asHit(groupSample)));
        // Database sample
        final SamplePE databaseSample = createDatabaseSample();
        assertTrue(validator.isValid(person, asHit(databaseSample)));
    }
}
