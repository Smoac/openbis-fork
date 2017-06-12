/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.authorization;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author pkupczyk
 */
@Component
public class CommonAuthorizationSystemTestService
{

    @Autowired
    private IDAOFactory daoFactory;

    private List<Object> createdObjects;

    @Transactional
    public void createPerson(PersonPE person)
    {
        daoFactory.getPersonDAO().createPerson(person);
        createdObjects.add(person);
    }

    @Transactional
    public void createSpace(SpacePE space)
    {
        daoFactory.getSpaceDAO().createSpace(space);
        createdObjects.add(space);
    }

    @Transactional
    public void createProject(ProjectPE project)
    {
        daoFactory.getProjectDAO().createProject(project, null);
        createdObjects.add(project);
    }

    @Transactional
    public void createExperiment(ExperimentPE experiment)
    {
        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, null);
        createdObjects.add(experiment);
    }

    @Transactional
    public void createType(EntityTypePE entityType, EntityKind entityKind)
    {
        daoFactory.getEntityTypeDAO(entityKind).createOrUpdateEntityType(entityType);
        createdObjects.add(entityType);
    }

    @Transactional
    public PersonPE tryFindPerson(String userId)
    {
        return daoFactory.getPersonDAO().tryFindPersonByUserId(userId);
    }

    @Transactional
    public SpacePE tryFindSpace(String spaceCode)
    {
        return daoFactory.getSpaceDAO().tryFindSpaceByCode(spaceCode);
    }

    @Transactional
    public ProjectPE tryFindProject(String spaceCode, String projectCode)
    {
        ProjectPE project = daoFactory.getProjectDAO().tryFindProject(spaceCode, projectCode);
        HibernateUtils.initialize(project.getExperiments());
        return project;
    }

    @Transactional
    public ExperimentPE tryFindExperiment(ProjectPE project, String experimentCode)
    {
        return daoFactory.getExperimentDAO().tryFindByCodeAndProject(project, experimentCode);
    }

    public void recordCreatedObjects()
    {
        createdObjects = new ArrayList<Object>();
    }

    @Transactional
    public void removeCreatedObjects()
    {
        Session session = daoFactory.getSessionFactory().getCurrentSession();

        for (int i = createdObjects.size() - 1; i >= 0; i--)
        {
            session.delete(createdObjects.get(i));
        }
    }

}
