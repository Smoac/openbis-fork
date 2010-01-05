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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.RawDataService.GROUP_CODE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.RawDataService.RAW_DATA_SAMPLE_TYPE;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=RawDataService.class)
public class RawDataServiceTest extends AbstractServerTestCase
{
    private static final String USER_ID = "a-user";
    private static final Sample NO_PARENT = create("no-parent", null);
    private static final Sample WITH_INSTANCE_PARENT = create("with-instance-parent", "t:/parent");
    private static final Sample WITH_PARENT_IN_G1 = create("with-parent-in-g1", "t:/g1/parent");
    private static final Sample WITH_PARENT_IN_G2 = create("with-parent-in-g2", "t:/g2/parent");
    
    private static Sample create(String sampleCode, String parentSampleIdentifierOrNull)
    {
        Sample sample = new Sample();
        sample.setCode(sampleCode);
        if (parentSampleIdentifierOrNull != null)
        {
            Sample parent = new Sample();
            parent.setIdentifier(parentSampleIdentifierOrNull);
            SampleIdentifier identifier = SampleIdentifierFactory.parse(parentSampleIdentifierOrNull);
            parent.setCode(identifier.getSampleCode());
            GroupIdentifier groupLevel = identifier.getGroupLevel();
            if (groupLevel != null)
            {
                Group group = new Group();
                group.setCode(groupLevel.getGroupCode());
                group.setInstance(createDatabaseInstance(groupLevel.getDatabaseInstanceCode()));
                parent.setGroup(group);
            }
            DatabaseInstanceIdentifier databaseInstanceLevel = identifier.getDatabaseInstanceLevel();
            if (databaseInstanceLevel != null)
            {
                String code = databaseInstanceLevel.getDatabaseInstanceCode();
                DatabaseInstance databaseInstance = createDatabaseInstance(code);
                parent.setDatabaseInstance(databaseInstance);
            }
            sample.setGeneratedFrom(parent);
        }
        return sample;
    }

    private static DatabaseInstance createDatabaseInstance(String code)
    {
        DatabaseInstance databaseInstance = new DatabaseInstance();
        databaseInstance.setCode(code);
        databaseInstance.setUuid(code);
        return databaseInstance;
    }
    
    private ICommonServer commonServer;
    private RawDataService rawDataService;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonServer = context.mock(ICommonServer.class);
        rawDataService = new RawDataService(sessionManager, daoFactory, commonServer);
    }

    @Test
    public void testListRawDataSamplesForUnknownUser()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(USER_ID);
                }
            });
        
        try
        {
            rawDataService.listRawDataSamples(SESSION_TOKEN, USER_ID);
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown user ID: " + USER_ID, ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testListRawDataSamplesForUserWithNoRoles()
    {
        prepareGetSession();
        prepareListRawDataSamples();
        
        List<Sample> samples = rawDataService.listRawDataSamples(SESSION_TOKEN, USER_ID);

        assertSame(WITH_INSTANCE_PARENT, samples.get(0));
        assertEquals(1, samples.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListRawDataSamplesForUserWithRightsForGroupG1()
    {
        prepareGetSession();
        prepareListRawDataSamples(createRole("G1", "T"));
        
        List<Sample> samples = rawDataService.listRawDataSamples(SESSION_TOKEN, USER_ID);
        
        assertSame(WITH_INSTANCE_PARENT, samples.get(0));
        assertSame(WITH_PARENT_IN_G1, samples.get(1));
        assertEquals(2, samples.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListRawDataSamplesForUserWithRightsForGroupG1AndG2()
    {
        prepareGetSession();
        prepareListRawDataSamples(createRole("G1", "T"), createRole("G2", "T"));
        
        List<Sample> samples = rawDataService.listRawDataSamples(SESSION_TOKEN, USER_ID);
        
        assertSame(WITH_INSTANCE_PARENT, samples.get(0));
        assertSame(WITH_PARENT_IN_G1, samples.get(1));
        assertSame(WITH_PARENT_IN_G2, samples.get(2));
        assertEquals(3, samples.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testListRawDataSamplesForUserWithRightsForGroupG1ButWrongInstance()
    {
        prepareGetSession();
        prepareListRawDataSamples(createRole("G1", "X"));
        
        List<Sample> samples = rawDataService.listRawDataSamples(SESSION_TOKEN, USER_ID);
        
        assertSame(WITH_INSTANCE_PARENT, samples.get(0));
        assertEquals(1, samples.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListRawDataSamplesForUserWithRightsForInstanceT()
    {
        prepareGetSession();
        prepareListRawDataSamples(createRole(null, "T"));
        
        List<Sample> samples = rawDataService.listRawDataSamples(SESSION_TOKEN, USER_ID);
        
        assertSame(WITH_INSTANCE_PARENT, samples.get(0));
        assertSame(WITH_PARENT_IN_G1, samples.get(1));
        assertSame(WITH_PARENT_IN_G2, samples.get(2));
        assertEquals(3, samples.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListRawDataSamplesForUserWithRightsForInstanceX()
    {
        prepareGetSession();
        prepareListRawDataSamples(createRole(null, "X"));
        
        List<Sample> samples = rawDataService.listRawDataSamples(SESSION_TOKEN, USER_ID);
        
        assertSame(WITH_INSTANCE_PARENT, samples.get(0));
        assertEquals(1, samples.size());
        context.assertIsSatisfied();
    }
    
    private void prepareListRawDataSamples(final RoleAssignmentPE... roles)
    {
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(USER_ID);
                    PersonPE person = new PersonPE();
                    person.setRoleAssignments(new LinkedHashSet<RoleAssignmentPE>(Arrays.asList(roles)));
                    will(returnValue(person));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(RAW_DATA_SAMPLE_TYPE);
                    final SampleTypePE sampleType = new SampleTypePE();
                    sampleType.setCode(RAW_DATA_SAMPLE_TYPE);
                    sampleType.setId(20100104l);
                    sampleType.setListable(Boolean.TRUE);
                    sampleType.setAutoGeneratedCode(Boolean.FALSE);
                    sampleType.setGeneratedFromHierarchyDepth(0);
                    sampleType.setContainerHierarchyDepth(0);
                    will(returnValue(sampleType));

                    one(commonServer).listSamples(with(SESSION_TOKEN),
                            with(new BaseMatcher<ListSampleCriteria>()
                                {
                                    public boolean matches(Object item)
                                    {
                                        if (item instanceof ListSampleCriteria)
                                        {
                                            ListSampleCriteria criteria = (ListSampleCriteria) item;
                                            assertEquals(GROUP_CODE, criteria.getGroupCode());
                                            assertEquals(true, criteria.isIncludeGroup());
                                            SampleType type = criteria.getSampleType();
                                            assertEquals(RAW_DATA_SAMPLE_TYPE, type.getCode());
                                            assertEquals(sampleType.getId(), type.getId());
                                            return true;
                                        }
                                        return false;
                                    }

                                    public void describeTo(Description description)
                                    {
                                        description.appendValue(sampleType);
                                    }
                                }));
                    will(returnValue(Arrays.asList(NO_PARENT, WITH_INSTANCE_PARENT,
                            WITH_PARENT_IN_G1, WITH_PARENT_IN_G2)));
                }
            });
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
