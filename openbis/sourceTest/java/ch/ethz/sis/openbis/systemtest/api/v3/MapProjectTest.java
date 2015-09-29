/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.ProjectUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;

/**
 * @author pkupczyk
 */
public class MapProjectTest extends AbstractTest
{

    @Test
    public void testMapByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectPermId permId1 = new ProjectPermId("20120814110011738-103");
        ProjectPermId permId2 = new ProjectPermId("20120814110011738-105");

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(permId1, permId2), new ProjectFetchOptions());

        assertEquals(2, map.size());

        Iterator<Project> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectIdentifier identifier1 = new ProjectIdentifier("/CISD/NEMO");
        ProjectIdentifier identifier2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(identifier1, identifier2), new ProjectFetchOptions());

        assertEquals(2, map.size());

        Iterator<Project> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectPermId permId1 = new ProjectPermId("IDONTEXIST");
        ProjectIdentifier identifier1 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectPermId permId2 = new ProjectPermId("20120814110011738-103");
        ProjectIdentifier identifier2 = new ProjectIdentifier("/IDONT/EXIST");

        Map<IProjectId, Project> map =
                v3api.mapProjects(sessionToken, Arrays.asList(permId1, identifier1, permId2, identifier2), new ProjectFetchOptions());

        assertEquals(2, map.size());

        Iterator<Project> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectPermId permId1 = new ProjectPermId("20120814110011738-103");
        ProjectIdentifier identifier = new ProjectIdentifier("/CISD/NEMO");
        ProjectPermId permId2 = new ProjectPermId("20120814110011738-103");

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(permId1, identifier, permId2), new ProjectFetchOptions());

        assertEquals(2, map.size());

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(identifier).getIdentifier(), identifier);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsUnauthorized()
    {
        ProjectPermId permId = new ProjectPermId("20120814110011738-101");
        ProjectIdentifier identifier1 = new ProjectIdentifier("/CISD/NEMO");
        ProjectIdentifier identifier2 = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");

        List<? extends IProjectId> ids = Arrays.asList(permId, identifier1, identifier2);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, ids, new ProjectFetchOptions());

        assertEquals(map.size(), 3);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.mapProjects(sessionToken, ids, new ProjectFetchOptions());

        assertEquals(map.size(), 1);

        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsWithFetchOptionsEmpty()
    {
        ProjectIdentifier identifier = new ProjectIdentifier("/CISD/NEMO");
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(identifier), fetchOptions);

        Project project = map.get(identifier);

        assertEquals(project.getPermId(), new ProjectPermId("20120814110011738-103"));
        assertEquals(project.getIdentifier(), identifier);
        assertEquals(project.getCode(), "NEMO");
        assertEquals(project.getDescription(), "nemo description");
        assertEqualsDate(project.getRegistrationDate(), "2008-11-05 09:21:43");
        assertEqualsDate(project.getModificationDate(), "2009-04-03 15:56:37");

        assertExperimentsNotFetched(project);
        assertSpaceNotFetched(project);
        assertRegistratorNotFetched(project);
        assertModifierNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testMapByIdsWithExperiments()
    {
        ProjectIdentifier identifier = new ProjectIdentifier("/CISD/DEFAULT");

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withExperiments();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(identifier), fetchOptions);

        Project project = map.get(identifier);
        List<Experiment> experiments = project.getExperiments();

        assertExperimentIdentifiers(experiments, "/CISD/DEFAULT/EXP-REUSE", "/CISD/DEFAULT/EXP-WELLS", "/CISD/DEFAULT/EXP-Y");

        assertSpaceNotFetched(project);
        assertRegistratorNotFetched(project);
        assertModifierNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testMapByIdsWithSpace()
    {
        ProjectIdentifier identifier = new ProjectIdentifier("/CISD/DEFAULT");

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withSpace();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(identifier), fetchOptions);

        Project project = map.get(identifier);

        Assert.assertEquals(project.getSpace().getCode(), "CISD");

        assertExperimentsNotFetched(project);
        assertRegistratorNotFetched(project);
        assertModifierNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testMapByIdsWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        ProjectCreation creation = new ProjectCreation();
        creation.setCode("TEST_REGISTRATOR");
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));

        List<ProjectPermId> permIds = v3api.createProjects(sessionToken, Arrays.asList(creation));

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withRegistrator();

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, permIds, fetchOptions);
        Project project = map.values().iterator().next();

        Assert.assertEquals(project.getRegistrator().getUserId(), TEST_SPACE_USER);

        assertExperimentsNotFetched(project);
        assertSpaceNotFetched(project);
        assertModifierNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testMapByIdsWithModifier()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        ProjectIdentifier projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withModifier();

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(projectId), fetchOptions);
        Project project = map.get(projectId);

        Assert.assertEquals(project.getModifier().getUserId(), TEST_USER);

        ProjectUpdate update = new ProjectUpdate();
        update.setProjectId(projectId);

        v3api.updateProjects(sessionToken, Arrays.asList(update));

        map = v3api.mapProjects(sessionToken, Arrays.asList(projectId), fetchOptions);
        project = map.get(projectId);

        Assert.assertEquals(project.getModifier().getUserId(), TEST_SPACE_USER);

        assertExperimentsNotFetched(project);
        assertSpaceNotFetched(project);
        assertRegistratorNotFetched(project);
        assertLeaderNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testMapByIdsWithLeader()
    {
        String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        ProjectIdentifier projectId = new ProjectIdentifier("/TEST-SPACE/TEST-PROJECT");
        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withLeader();

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, Arrays.asList(projectId), fetchOptions);
        Project project = map.get(projectId);

        assertEquals(project.getLeader().getUserId(), SYSTEM_USER);

        assertExperimentsNotFetched(project);
        assertSpaceNotFetched(project);
        assertRegistratorNotFetched(project);
        assertModifierNotFetched(project);
        assertAttachmentsNotFetched(project);
    }

    @Test
    public void testMapWithHistoryEmpty()
    {
        ProjectCreation creation = new ProjectCreation();
        creation.setCode("PROJECT_WITH_EMPTY_HISTORY");
        creation.setSpaceId(new SpacePermId("CISD"));

        List<HistoryEntry> history = testMapWithHistory(creation, null);

        assertEquals(history, Collections.emptyList());
    }

    @Test
    public void testMapWithHistorySpace()
    {
        ProjectCreation creation = new ProjectCreation();
        creation.setCode("PROJECT_WITH_SPACE_HISTORY");
        creation.setSpaceId(new SpacePermId("CISD"));

        ProjectUpdate update = new ProjectUpdate();
        update.setSpaceId(new SpacePermId("TEST-SPACE"));

        List<HistoryEntry> history = testMapWithHistory(creation, update);

        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), ProjectRelationType.SPACE);
        assertEquals(entry.getRelatedObjectId(), new SpacePermId("CISD"));
    }

    @Test
    public void testMapWithHistoryExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("PROJECT_WITH_EXPERIMENT_HISTORY");
        projectCreation.setSpaceId(new SpacePermId("CISD"));

        List<ProjectPermId> projectPermIds = v3api.createProjects(sessionToken, Arrays.asList(projectCreation));

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experimentCreation.setCode("EXPERIMENT_WITH_PROJECT_HISTORY");
        experimentCreation.setProperty("DESCRIPTION", "description");
        experimentCreation.setProjectId(projectPermIds.get(0));

        List<ExperimentPermId> experimentPermIds = v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation));

        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experimentPermIds.get(0));
        experimentUpdate.setProjectId(new ProjectIdentifier("/CISD/NEMO"));

        v3api.updateExperiments(sessionToken, Arrays.asList(experimentUpdate));

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withHistory();

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, projectPermIds, fetchOptions);
        assertEquals(map.size(), 1);

        Project project = map.get(projectPermIds.get(0));

        List<HistoryEntry> history = project.getHistory();
        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), ProjectRelationType.EXPERIMENT);
        assertEquals(entry.getRelatedObjectId(), experimentPermIds.get(0));
    }

    private List<HistoryEntry> testMapWithHistory(ProjectCreation creation, ProjectUpdate update)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        List<ProjectPermId> permIds = v3api.createProjects(sessionToken, Arrays.asList(creation));

        if (update != null)
        {
            update.setProjectId(permIds.get(0));
            v3api.updateProjects(sessionToken, Arrays.asList(update));
        }

        ProjectFetchOptions fetchOptions = new ProjectFetchOptions();
        fetchOptions.withHistory();

        Map<IProjectId, Project> map = v3api.mapProjects(sessionToken, permIds, fetchOptions);

        assertEquals(map.size(), 1);
        Project project = map.get(permIds.get(0));

        v3api.logout(sessionToken);

        return project.getHistory();
    }

}
