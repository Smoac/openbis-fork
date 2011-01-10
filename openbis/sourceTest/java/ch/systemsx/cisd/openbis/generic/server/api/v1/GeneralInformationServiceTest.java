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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * @author Franz-Josef Elmer
 */
// PLEASE, if you add here a new test add also a system test to
// ch.systemsx.cisd.openbis.systemtest.api.v1.GeneralInformationServiceTest
@Friend(toClasses = RoleAssignmentPE.class)
public class GeneralInformationServiceTest extends AbstractServerTestCase
{
    private GeneralInformationService service;

    private ICommonServer commonServer;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonServer = context.mock(ICommonServer.class);
        service = new GeneralInformationService(sessionManager, daoFactory, commonServer);
    }

    @Test
    public void testListNamedRoleSets()
    {
        prepareGetSession();

        Map<String, Set<Role>> namedRoleSets = service.listNamedRoleSets(SESSION_TOKEN);

        List<Entry<String, Set<Role>>> entries =
                new ArrayList<Entry<String, Set<Role>>>(namedRoleSets.entrySet());
        Collections.sort(entries, new Comparator<Entry<String, Set<Role>>>()
            {
                public int compare(Entry<String, Set<Role>> e1, Entry<String, Set<Role>> e2)
                {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
        assertNamedRoles("INSTANCE_ADMIN", "[ADMIN(instance)]", entries.get(0));
        assertNamedRoles("INSTANCE_ETL_SERVER", "[ADMIN(instance), " + "ETL_SERVER(instance)]",
                entries.get(1));
        assertNamedRoles("INSTANCE_OBSERVER", "[ADMIN(instance), OBSERVER(instance)]",
                entries.get(2));
        assertNamedRoles("SPACE_ADMIN", "[ADMIN(instance), ADMIN(space)]", entries.get(3));
        assertNamedRoles("SPACE_ETL_SERVER", "[ADMIN(instance), "
                + "ETL_SERVER(instance), ETL_SERVER(space)]", entries.get(4));
        assertNamedRoles("SPACE_OBSERVER", "[ADMIN(instance), ADMIN(space), OBSERVER(instance), "
                + "OBSERVER(space), POWER_USER(space), USER(space)]", entries.get(5));
        assertNamedRoles("SPACE_POWER_USER", "[ADMIN(instance), ADMIN(space), POWER_USER(space)]",
                entries.get(6));
        assertNamedRoles("SPACE_USER",
                "[ADMIN(instance), ADMIN(space), POWER_USER(space), USER(space)]", entries.get(7));
        assertEquals(8, entries.size());
        context.assertIsSatisfied();
    }

    private void assertNamedRoles(String expectedName, String expectedRoles,
            Entry<String, Set<Role>> entry)
    {
        assertEquals(expectedName, entry.getKey());
        List<Role> roles = new ArrayList<Role>(entry.getValue());
        Collections.sort(roles, new Comparator<Role>()
            {
                public int compare(Role r1, Role r2)
                {
                    return r1.toString().compareTo(r2.toString());
                }
            });
        assertEquals(expectedName, expectedRoles, roles.toString());
    }

    @Test
    public void testListSpacesWithProjectsAndRoleAssignments()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    RoleAssignmentPE assignment1 =
                            createUserAssignment("user1", null, RoleCode.ADMIN);
                    RoleAssignmentPE assignment2 =
                            createUserAssignment("user2", "s2", RoleCode.OBSERVER);
                    RoleAssignmentPE assignment3 =
                            createUserAssignment("user1", "s1", RoleCode.USER);
                    will(returnValue(Arrays.asList(assignment1, assignment2, assignment3)));

                    one(groupDAO).listSpaces(daoFactory.getHomeDatabaseInstance());
                    List<SpacePE> spaces = createSpaces("s1", "s2", "s3");
                    will(returnValue(spaces));

                    one(projectDAO).listProjects(spaces.get(0));
                    ProjectPE a = new ProjectPE();
                    a.setCode("a");
                    a.setSpace(spaces.get(0));
                    ProjectPE b = new ProjectPE();
                    b.setCode("b");
                    b.setSpace(spaces.get(0));
                    will(returnValue(Arrays.asList(a, b)));

                    one(projectDAO).listProjects(spaces.get(1));
                    will(returnValue(Arrays.asList()));

                    one(projectDAO).listProjects(spaces.get(2));
                    ProjectPE c = new ProjectPE();
                    c.setCode("c");
                    c.setSpace(spaces.get(0));
                    will(returnValue(Arrays.asList(c)));
                }
            });

        List<SpaceWithProjectsAndRoleAssignments> spaces =
                service.listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);

        assertSpaceAndProjects("s1", "[/s1/a, /s1/b]", spaces.get(0));
        assertRoles("[]", spaces.get(0).getRoles("unknown user"));
        assertRoles("[ADMIN(instance), USER(space)]", spaces.get(0).getRoles("user1"));
        assertRoles("[]", spaces.get(0).getRoles("user2"));

        assertSpaceAndProjects("s2", "[]", spaces.get(1));
        assertRoles("[ADMIN(instance)]", spaces.get(1).getRoles("user1"));
        assertRoles("[OBSERVER(space)]", spaces.get(1).getRoles("user2"));

        assertSpaceAndProjects("s3", "[/s3/c]", spaces.get(2));
        assertRoles("[ADMIN(instance)]", spaces.get(2).getRoles("user1"));
        assertRoles("[]", spaces.get(2).getRoles("user2"));

        assertEquals(3, spaces.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testSearchForSamples()
    {
        prepareGetSession();
        prepareSearchForSamples();
        List<Sample> result = service.searchForSamples(SESSION_TOKEN, createSearchCriteria());
        assertEquals(1, result.size());
        Sample resultSample = result.get(0);
        assertEquals("/space/code", resultSample.getIdentifier());
        context.assertIsSatisfied();
    }

    private void prepareSearchForSamples()
    {
        context.checking(new Expectations()
            {
                {
                    one(commonServer).searchForSamples(with(SESSION_TOKEN),
                            with(any(DetailedSearchCriteria.class)));
                    SampleBuilder sample =
                            new SampleBuilder("/space/code")
                                    .id(1L)
                                    .permID("permId")
                                    .code("code")
                                    .type(new SampleTypeBuilder().id(1L).code("sample-type")
                                            .getSampleType());
                    will(returnValue(Collections.singletonList(sample.getSample())));
                }
            });
    }

    @Test
    public void testListDataSets()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).listSampleTypes(SESSION_TOKEN);
                    SampleType returnSampleType = new SampleType();
                    returnSampleType.setId(new Long(1));
                    returnSampleType.setCode("sample-type");
                    will(returnValue(Collections.singletonList(returnSampleType)));

                    one(commonServer).listRelatedDataSets(with(SESSION_TOKEN),
                            with(any(DataSetRelatedEntities.class)));
                    DataSetBuilder dataSet =
                            new DataSetBuilder()
                                    .type("ds-type")
                                    .code("ds-code")
                                    .experiment(
                                            new ExperimentBuilder()
                                                    .identifier("/space/project/exp")
                                                    .getExperiment())
                                    .sample(new SampleBuilder("/space/code").getSample());
                    will(returnValue(Collections.singletonList(dataSet.getDataSet())));
                }
            });

        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(new Long(1));
        initializer.setPermId("permId");
        initializer.setCode("code");
        initializer.setIdentifier("/space/code");
        initializer.setSampleTypeId(new Long(1));
        initializer.setSampleTypeCode("sample-type");
        Sample owner = new Sample(initializer);
        List<DataSet> result =
                service.listDataSets(SESSION_TOKEN, Collections.singletonList(owner));
        assertEquals(1, result.size());
        DataSet resultDataSet = result.get(0);
        assertEquals("ds-code", resultDataSet.getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetsWithEmptySampleList()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).listSampleTypes(SESSION_TOKEN);
                    SampleTypeBuilder sampleType = new SampleTypeBuilder().id(1L).code("sample-type");
                    will(returnValue(Collections.singletonList(sampleType.getSampleType())));

                    one(commonServer).listRelatedDataSets(with(SESSION_TOKEN),
                            with(any(DataSetRelatedEntities.class)));
                    will(returnValue(new ArrayList<ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData>()));
                }
            });
        ArrayList<Sample> samples = new ArrayList<Sample>();
        List<DataSet> result = service.listDataSets(SESSION_TOKEN, samples);
        assertEquals(0, result.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetsForSample()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(commonServer).listSampleExternalData(with(SESSION_TOKEN),
                            with(new TechId(1)), with(true));
                    DataSetBuilder dataSet =
                            new DataSetBuilder()
                                    .type("ds-type")
                                    .code("ds-code")
                                    .experiment(
                                            new ExperimentBuilder()
                                                    .identifier("/space/project/exp")
                                                    .getExperiment())
                                    .sample(new SampleBuilder("/space/code").getSample());
                    will(returnValue(Collections.singletonList(dataSet.getDataSet())));
                }
            });

        SampleInitializer initializer = new SampleInitializer();
        initializer.setId(new Long(1));
        initializer.setPermId("permId");
        initializer.setCode("code");
        initializer.setIdentifier("/space/code");
        initializer.setSampleTypeId(new Long(1));
        initializer.setSampleTypeCode("sample-type");
        Sample owner = new Sample(initializer);
        List<DataSet> result = service.listDataSetsForSample(SESSION_TOKEN, owner, true);
        assertEquals(1, result.size());
        DataSet resultDataSet = result.get(0);
        assertEquals("ds-code", resultDataSet.getCode());
        context.assertIsSatisfied();
    }

    private void prepareSearchForExperiments()
    {
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    RoleAssignmentPE assignment0 =
                            createUserAssignment("user0", null, RoleCode.ADMIN);
                    RoleAssignmentPE assignment1 =
                            createUserAssignment("user1", "SPACE-1", RoleCode.USER);
                    RoleAssignmentPE assignment2 =
                            createUserAssignment("user1", "SPACE-2", RoleCode.ADMIN);
                    will(returnValue(Arrays.asList(assignment0, assignment1, assignment2)));

                    one(groupDAO).listSpaces(daoFactory.getHomeDatabaseInstance());
                    List<SpacePE> spaces = createSpaces("SPACE-1", "SPACE-2");
                    will(returnValue(spaces));

                    one(projectDAO).listProjects(spaces.get(0));
                    ProjectPE project1 = new ProjectPE();
                    project1.setCode("PROJECT-1");
                    project1.setSpace(spaces.get(0));
                    will(returnValue(Collections.singletonList(project1)));

                    one(projectDAO).listProjects(spaces.get(1));
                    will(returnValue(Collections.emptyList()));

                    ExperimentType returnExperimentType = new ExperimentType();
                    returnExperimentType.setCode("EXP-TYPE-CODE");
                    one(commonServer).listExperimentTypes(SESSION_TOKEN);
                    will(returnValue(Collections.singletonList(returnExperimentType)));

                    ProjectIdentifier projectIdentifier =
                            new ProjectIdentifier("SPACE-1", "PROJECT-1");
                    one(commonServer).listExperiments(SESSION_TOKEN, returnExperimentType,
                            projectIdentifier);

                    ExperimentBuilder experiment =
                            new ExperimentBuilder().id(1L).code("EXP-CODE").permID("EXP-PERMID")
                                    .identifier("/SPACE-1/PROJECT-1/EXP-CODE")
                                    .type(returnExperimentType.getCode());
                    will(returnValue(Collections.singletonList(experiment.getExperiment())));
                }
            });
    }

    @Test
    public void testListExperiments()
    {
        prepareGetSession();
        prepareSearchForExperiments();
        List<SpaceWithProjectsAndRoleAssignments> enrichedSpaces =
                service.listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);
        ArrayList<Project> projects = new ArrayList<Project>();
        for (SpaceWithProjectsAndRoleAssignments space : enrichedSpaces)
        {
            projects.addAll(space.getProjects());
        }
        List<Experiment> result = service.listExperiments(SESSION_TOKEN, projects, "EXP-TYPE-CODE");
        assertEquals(1, result.size());
        Experiment resultExperiment = result.get(0);
        assertEquals("/SPACE-1/PROJECT-1/EXP-CODE", resultExperiment.getIdentifier());
        context.assertIsSatisfied();
    }

    private SearchCriteria createSearchCriteria()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "a code"));
        sc.addMatchClause(MatchClause.createPropertyMatch("MY_PROPERTY2", "a property value"));
        return sc;
    }

    private void assertSpaceAndProjects(String expectedSpaceCode, String expectedProjects,
            SpaceWithProjectsAndRoleAssignments space)
    {
        assertEquals(expectedSpaceCode, space.getCode());
        List<Project> projects = space.getProjects();
        Collections.sort(projects, new Comparator<Project>()
            {
                public int compare(Project p1, Project p2)
                {
                    return p1.toString().compareTo(p2.toString());
                }
            });
        assertEquals(expectedProjects, projects.toString());
    }

    private void assertRoles(String expectedRoles, Set<Role> roles)
    {
        List<Role> list = new ArrayList<Role>(roles);
        Collections.sort(list, new Comparator<Role>()
            {
                public int compare(Role r1, Role r2)
                {
                    return r1.toString().compareTo(r2.toString());
                }
            });
        assertEquals(expectedRoles, list.toString());
    }

    private RoleAssignmentPE createUserAssignment(String userID, String spaceCodeOrNull,
            RoleCode roleCode)
    {
        RoleAssignmentPE assignment = new RoleAssignmentPE();
        if (spaceCodeOrNull != null)
        {
            assignment.setSpace(createGroup(spaceCodeOrNull));
        }
        assignment.setRole(roleCode);
        PersonPE person = new PersonPE();
        person.setUserId(userID);
        assignment.setPersonInternal(person);
        return assignment;
    }

    private List<SpacePE> createSpaces(String... codes)
    {
        List<SpacePE> list = new ArrayList<SpacePE>();
        for (String code : codes)
        {
            list.add(createGroup(code));
        }
        return list;
    }

}
