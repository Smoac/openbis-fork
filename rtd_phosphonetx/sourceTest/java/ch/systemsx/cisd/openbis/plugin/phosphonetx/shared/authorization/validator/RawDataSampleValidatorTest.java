/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
public class RawDataSampleValidatorTest extends AssertJUnit
{
    private static final MsInjectionSample NO_PARENT = create("no-parent", null);

    private static final MsInjectionSample WITH_INSTANCE_PARENT = create("with-instance-parent", "t:/parent");

    private static final MsInjectionSample WITH_PARENT_IN_G1 = create("with-parent-in-g1", "t:/g1/parent");

    private static final MsInjectionSample WITH_PARENT_IN_G2 = create("with-parent-in-g2", "t:/g2/parent");

    private RawDataSampleValidator validator = new RawDataSampleValidator();

    private static MsInjectionSample create(String sampleCode, String parentSampleIdentifierOrNull)
    {
        Sample sample = new Sample();
        sample.setCode(sampleCode);
        if (parentSampleIdentifierOrNull != null)
        {
            Sample parent = new Sample();
            parent.setIdentifier(parentSampleIdentifierOrNull);
            SampleIdentifier identifier =
                    SampleIdentifierFactory.parse(parentSampleIdentifierOrNull);
            parent.setCode(identifier.getSampleCode());
            SpaceIdentifier spaceLevel = identifier.getSpaceLevel();
            if (spaceLevel != null)
            {
                Space space = new Space();
                space.setCode(spaceLevel.getSpaceCode());
                space.setInstance(createDatabaseInstance(spaceLevel.getDatabaseInstanceCode()));
                parent.setSpace(space);
            }
            DatabaseInstanceIdentifier databaseInstanceLevel =
                    identifier.getDatabaseInstanceLevel();
            if (databaseInstanceLevel != null)
            {
                String code = databaseInstanceLevel.getDatabaseInstanceCode();
                DatabaseInstance databaseInstance = createDatabaseInstance(code);
                parent.setDatabaseInstance(databaseInstance);
            }
            sample.setGeneratedFrom(parent);
        }
        return new MsInjectionSample(sample);
    }

    private static DatabaseInstance createDatabaseInstance(String code)
    {
        DatabaseInstance databaseInstance = new DatabaseInstance();
        databaseInstance.setCode(code);
        databaseInstance.setUuid(code);
        return databaseInstance;
    }

    @Test
    public void testUserWithNoRights()
    {
        PersonPE person = createPersonWithRoles();

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    @Test
    public void testUserWithRightsForGroupG1()
    {
        PersonPE person = createPersonWithRoles(createRole("G1", "T"));

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    @Test
    public void testUserWithRightsForGroupG1AndG2()
    {
        PersonPE person = createPersonWithRoles(createRole("G1", "T"), createRole("G2", "T"));

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    @Test
    public void testUserWithRightsForGroupG1ButWrongInstance()
    {
        PersonPE person = createPersonWithRoles(createRole("G1", "X"));

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    @Test
    public void testUserWithRightsForForInstanceT()
    {
        PersonPE person = createPersonWithRoles(createRole(null, "T"));

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    @Test
    public void testUserWithRightsForForInstanceX()
    {
        PersonPE person = createPersonWithRoles(createRole(null, "X"));

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    private PersonPE createPersonWithRoles(RoleAssignmentPE... roles)
    {
        PersonPE person = new PersonPE();
        person.setRoleAssignments(new LinkedHashSet<RoleAssignmentPE>(Arrays.asList(roles)));
        return person;
    }

    private RoleAssignmentPE createRole(String groupCodeOrNull, String dataBaseInstanceUUID)
    {
        RoleAssignmentPE role = new RoleAssignmentPE();
        if (groupCodeOrNull == null)
        {
            DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
            databaseInstance.setUuid(dataBaseInstanceUUID);
            role.setDatabaseInstance(databaseInstance);
        } else
        {
            GroupPE group = new GroupPE();
            group.setCode(groupCodeOrNull);
            DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
            databaseInstance.setUuid(dataBaseInstanceUUID);
            group.setDatabaseInstance(databaseInstance);
            role.setGroup(group);
        }
        return role;
    }
}
