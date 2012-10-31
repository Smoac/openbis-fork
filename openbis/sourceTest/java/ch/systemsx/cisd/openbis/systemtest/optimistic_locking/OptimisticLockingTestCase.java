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

package ch.systemsx.cisd.openbis.systemtest.optimistic_locking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifierFactory;
import ch.systemsx.cisd.openbis.systemtest.PersistentSystemTestCase;

/**
 * @author Franz-Josef Elmer
 */
public class OptimisticLockingTestCase extends PersistentSystemTestCase
{
    protected static final String USER_ID = "optimist";

    protected static final String SPACE_1 = "OPTIMISTIC_LOCKING_1";

    protected static final String SPACE_2 = "OPTIMISTIC_LOCKING_2";

    protected static final String EXPERIMENT_TYPE_CODE = "SIRNA_HCS";

    protected Space space1;

    protected Space space2;

    protected Project project1;

    protected Project project2;

    @BeforeMethod
    public void createSpacesAndProjects()
    {
        space1 = findOrCreateSpace(SPACE_1);
        space2 = findOrCreateSpace(SPACE_2);
        project1 = findOrCreateProject("/" + SPACE_1 + "/P1");
        project2 = findOrCreateProject("/" + SPACE_2 + "/P2");
        createInstanceAdmin(USER_ID);
    }

    @AfterMethod
    public void deleteSpaces()
    {
        deleteSpace(space1);
        deleteSpace(space2);
    }

    private void deleteSpace(Space space)
    {
        List<Experiment> experiments =
                commonServer.listExperiments(systemSessionToken,
                        new ExperimentTypeBuilder().code(EXPERIMENT_TYPE_CODE).getExperimentType(),
                        new SpaceIdentifierFactory(space.getIdentifier()).createIdentifier());
        commonServer.deleteExperiments(systemSessionToken, TechId.createList(experiments),
                "cleanup", DeletionType.TRASH);
        List<Deletion> deletions = commonServer.listDeletions(systemSessionToken, false);
        commonServer.deletePermanently(systemSessionToken, TechId.createList(deletions));
        List<Project> projects = commonServer.listProjects(systemSessionToken);
        List<TechId> projectIds = new ArrayList<TechId>();
        for (Project project : projects)
        {
            if (project.getSpace().getCode().equals(space.getCode()))
            {
                projectIds.add(new TechId(project));
            }
        }
        commonServer.deleteProjects(systemSessionToken, projectIds, "cleanup");
        commonServer.deleteSpaces(systemSessionToken, Arrays.asList(new TechId(space.getId())),
                "cleanup");
    }

    private void createInstanceAdmin(String userId)
    {
        List<Person> persons = commonServer.listPersons(systemSessionToken);
        for (Person person : persons)
        {
            if (person.getUserId().equals(userId))
            {
                return;
            }
        }
        commonServer.registerPerson(systemSessionToken, userId);
        commonServer.registerInstanceRole(systemSessionToken, RoleCode.ADMIN,
                Grantee.createPerson(userId));
    }

    protected Project findOrCreateProject(String projectIdentifier)
    {
        Project project = tryToFindProject(projectIdentifier);
        if (project != null)
        {
            return project;
        }
        commonServer.registerProject(systemSessionToken, new ProjectIdentifierFactory(
                projectIdentifier).createIdentifier(), "A test project", null, Collections
                .<NewAttachment> emptyList());
        return tryToFindProject(projectIdentifier);
    }

    protected Project tryToFindProject(String projectIdentifier)
    {
        List<Project> projects = commonServer.listProjects(systemSessionToken);
        for (Project project : projects)
        {
            if (project.getIdentifier().equals(projectIdentifier))
            {
                return project;
            }
        }
        return null;
    }

    protected Space findOrCreateSpace(String spaceCode)
    {
        Space space = tryToFindSpace(spaceCode);
        if (space != null)
        {
            return space;
        }
        commonServer.registerSpace(systemSessionToken, spaceCode, "A test space");
        return tryToFindSpace(spaceCode);
    }

    protected Space tryToFindSpace(String spaceCode)
    {
        DatabaseInstanceIdentifier identifier = new DatabaseInstanceIdentifier(null);
        List<Space> spaces = commonServer.listSpaces(systemSessionToken, identifier);
        for (Space space : spaces)
        {
            if (space.getCode().equals(spaceCode))
            {
                return space;
            }
        }
        return null;
    }

    protected NewExperiment experiment(int number)
    {
        NewExperiment experiment =
                new NewExperiment(project1.getIdentifier() + "/OLT-E" + number,
                        EXPERIMENT_TYPE_CODE);
        experiment.setAttachments(Collections.<NewAttachment> emptyList());
        experiment.setProperties(new IEntityProperty[]
            { new PropertyBuilder("DESCRIPTION").value("hello " + number).getProperty() });
        return experiment;
    }

    protected List<String> extractCodes(List<? extends ICodeHolder> codeHolders)
    {
        List<String> result = new ArrayList<String>();
        for (ICodeHolder codeHolder : codeHolders)
        {
            result.add(codeHolder.getCode());
        }
        Collections.sort(result);
        return result;
    }

}
