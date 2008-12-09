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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link ExperimentTable} class.
 * 
 * @author Izabela Adamczyk
 */
public final class ExperimentTableTest
{

    private Mockery context;

    private IDAOFactory daoFactory;

    private IEntityTypeDAO experimentTypeDAO;

    private IProjectDAO projectDAO;

    private IExperimentDAO experimentDAO;

    private final ExperimentTable createExperimentTable()
    {
        return new ExperimentTable(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IDAOFactory.class);
        experimentTypeDAO = context.mock(IEntityTypeDAO.class);
        projectDAO = context.mock(IProjectDAO.class);
        experimentDAO = context.mock(IExperimentDAO.class);
    }

    @AfterMethod
    public final void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testLoad() throws Exception
    {
        final ProjectIdentifier projectIdentifier = CommonTestUtils.createProjectIdentifier();
        final ExperimentTypePE experimentType = CommonTestUtils.createExperimentType();
        final ProjectPE project = CommonTestUtils.createProject(projectIdentifier);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(experimentTypeDAO));

                    allowing(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    one(experimentTypeDAO).tryToFindEntityTypeByCode(experimentType.getCode());
                    will(returnValue(experimentType));

                    one(projectDAO).tryFindProject(projectIdentifier.getDatabaseInstanceCode(),
                            projectIdentifier.getGroupCode(), projectIdentifier.getProjectCode());
                    will(returnValue(project));

                    one(experimentDAO).listExperiments(experimentType, project);
                    will(returnValue(new ArrayList<ExperimentPE>()));
                }
            });
        createExperimentTable().load(experimentType.getCode(), projectIdentifier);
        context.assertIsSatisfied();
    }
}
