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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.authorization.validator;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
public class RawDataSampleValidatorTest extends AssertJUnit
{

    private static final MsInjectionSample NO_PARENT = create("no-parent", null);

    private static final MsInjectionSample WITH_INSTANCE_PARENT = create("with-instance-parent", "/parent");

    private static final MsInjectionSample WITH_PARENT_IN_G1 = create("with-parent-in-g1", "/g1/parent");

    private static final MsInjectionSample WITH_PARENT_IN_G2 = create("with-parent-in-g2", "/g2/parent");

    private Mockery context;

    private RawDataSampleValidator validator;

    private IDAOFactory daoFactory;

    @BeforeMethod
    protected void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        validator = new RawDataSampleValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));
    }

    @AfterMethod
    protected void afterMethod()
    {
        context.assertIsSatisfied();
    }

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
                parent.setSpace(space);
            }
            sample.setGeneratedFrom(parent);
        }
        return new MsInjectionSample(sample, Arrays.<AbstractExternalData> asList());
    }

    @Test
    public void testUserWithNoRights()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));
                }
            });

        PersonPE person = createPersonWithRoles();

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    @Test
    public void testUserWithRightsForGroupG1()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));
                }
            });

        PersonPE person = createPersonWithRoles(createRole("G1"));

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(false, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    @Test
    public void testUserWithRightsForGroupG1AndG2()
    {
        PersonPE person = createPersonWithRoles(createRole("G1"), createRole("G2"));

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    @Test
    public void testUserWithRightsForForInstanceT()
    {
        PersonPE person = createPersonWithRoles(createRole(null));

        assertEquals(false, validator.isValid(person, NO_PARENT));
        assertEquals(true, validator.isValid(person, WITH_INSTANCE_PARENT));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G1));
        assertEquals(true, validator.isValid(person, WITH_PARENT_IN_G2));
    }

    private PersonPE createPersonWithRoles(RoleAssignmentPE... roles)
    {
        PersonPE person = new PersonPE();
        person.setRoleAssignments(new LinkedHashSet<RoleAssignmentPE>(Arrays.asList(roles)));
        return person;
    }

    private RoleAssignmentPE createRole(String groupCodeOrNull)
    {
        RoleAssignmentPE role = new RoleAssignmentPE();
        if (groupCodeOrNull != null)
        {
            SpacePE group = new SpacePE();
            group.setCode(groupCodeOrNull);
            role.setSpace(group);
        }
        return role;
    }
}
