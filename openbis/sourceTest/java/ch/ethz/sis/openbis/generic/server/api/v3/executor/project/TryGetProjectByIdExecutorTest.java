/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.project;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.AbstractExecutorTest;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.TryGetProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnsupportedObjectIdException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class TryGetProjectByIdExecutorTest extends AbstractExecutorTest
{

    private IProjectDAO projectDao;

    @Override
    protected void init()
    {
        projectDao = context.mock(IProjectDAO.class);
    }

    @Test
    public void testGetByPermId()
    {
        final ProjectPermId permId = new ProjectPermId("TEST_PERM_ID");
        final ProjectPE project = new ProjectPE();
        project.setId(123L);

        context.checking(new Expectations()
            {
                {
                    one(projectDao).tryGetByPermID(permId.getPermId());
                    will(returnValue(project));
                }
            });

        ProjectPE result = execute(permId);
        Assert.assertEquals(Long.valueOf(123L), result.getId());
    }

    @Test
    public void testGetByPermIdNonexistent()
    {
        final ProjectPermId permId = new ProjectPermId("NONEXISTENT_PROJECT");

        context.checking(new Expectations()
            {
                {
                    one(projectDao).tryGetByPermID("NONEXISTENT_PROJECT");
                    will(returnValue(null));
                }
            });

        ProjectPE result = execute(permId);
        Assert.assertNull(result);
    }

    @Test
    public void testGetByIdentifier()
    {
        final ProjectIdentifier identifier = new ProjectIdentifier("/TEST_SPACE/TEST_PROJECT");
        final ProjectPE project = new ProjectPE();
        project.setId(123L);

        context.checking(new Expectations()
            {
                {
                    one(projectDao).tryFindProject("TEST_SPACE", "TEST_PROJECT");
                    will(returnValue(project));
                }
            });

        ProjectPE result = execute(identifier);
        Assert.assertEquals(Long.valueOf(123L), result.getId());
    }

    @Test
    public void testGetByIdentifierNonexistent()
    {
        final ProjectIdentifier identifier = new ProjectIdentifier("/TEST_SPACE/NONEXISTENT_PROJECT");

        context.checking(new Expectations()
            {
                {
                    one(projectDao).tryFindProject("TEST_SPACE", "NONEXISTENT_PROJECT");
                    will(returnValue(null));
                }
            });

        ProjectPE result = execute(identifier);
        Assert.assertNull(result);
    }

    @Test(expectedExceptions = { UnsupportedObjectIdException.class })
    public void testGetByUnknownId()
    {
        final IProjectId unknownId = new IProjectId()
            {

                private static final long serialVersionUID = 1L;

            };
        final ProjectPE project = new ProjectPE();
        project.setId(123L);

        execute(unknownId);
    }

    private ProjectPE execute(IProjectId projectId)
    {
        TryGetProjectByIdExecutor executor = new TryGetProjectByIdExecutor(projectDao);
        return executor.tryGet(operationContext, projectId);
    }
}