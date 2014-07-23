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
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.GetProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.ITryGetProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class GetProjectByIdExecutorTest extends AbstractExecutorTest
{

    private ITryGetProjectByIdExecutor tryGetProjectByIdExecutor;

    @Override
    protected void init()
    {
        tryGetProjectByIdExecutor = context.mock(ITryGetProjectByIdExecutor.class);
    }

    @Test
    public void testExistent()
    {
        final ProjectPermId permId = new ProjectPermId("EXISTENT");
        final ProjectPE project = new ProjectPE();
        project.setId(123L);

        context.checking(new Expectations()
            {
                {
                    one(tryGetProjectByIdExecutor).tryGet(operationContext, permId);
                    will(returnValue(project));
                }
            });

        ProjectPE result = execute(permId);
        Assert.assertEquals(Long.valueOf(123L), result.getId());
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "No project found.*")
    public void testNonexistent()
    {
        final ProjectPermId permId = new ProjectPermId("NONEXISTENT");
        final ProjectPE project = new ProjectPE();
        project.setId(123L);

        context.checking(new Expectations()
            {
                {
                    one(tryGetProjectByIdExecutor).tryGet(operationContext, permId);
                    will(returnValue(null));
                }
            });

        execute(permId);
    }

    private ProjectPE execute(IProjectId projectId)
    {
        GetProjectByIdExecutor executor = new GetProjectByIdExecutor(tryGetProjectByIdExecutor);
        return executor.get(operationContext, projectId);
    }

}
