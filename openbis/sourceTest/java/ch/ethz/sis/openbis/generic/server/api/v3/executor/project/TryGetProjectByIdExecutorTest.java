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

import java.util.Collections;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.AbstractExecutorTest;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class TryGetProjectByIdExecutorTest extends AbstractExecutorTest
{

    private IMapProjectByIdExecutor mapExecutor;

    @Override
    protected void init()
    {
        mapExecutor = context.mock(IMapProjectByIdExecutor.class);
    }

    @Test
    public void testGetExisting()
    {
        final ProjectPermId permId = new ProjectPermId("TEST_PERM_ID");
        final ProjectPE project = new ProjectPE();
        project.setId(123L);

        context.checking(new Expectations()
            {
                {
                    one(mapExecutor).map(operationContext, Collections.singletonList(permId));
                    will(returnValue(Collections.singletonMap(permId, project)));
                }
            });

        ProjectPE result = execute(permId);
        Assert.assertEquals(Long.valueOf(123L), result.getId());
    }

    @Test
    public void testGetNonexistent()
    {
        final ProjectPermId permId = new ProjectPermId("NONEXISTENT_PROJECT");

        context.checking(new Expectations()
            {
                {
                    one(mapExecutor).map(operationContext, Collections.singletonList(permId));
                    will(returnValue(Collections.emptyMap()));
                }
            });

        ProjectPE result = execute(permId);
        Assert.assertNull(result);
    }

    private ProjectPE execute(IProjectId projectId)
    {
        TryGetProjectByIdExecutor executor = new TryGetProjectByIdExecutor(mapExecutor);
        return executor.tryGet(operationContext, projectId);
    }
}