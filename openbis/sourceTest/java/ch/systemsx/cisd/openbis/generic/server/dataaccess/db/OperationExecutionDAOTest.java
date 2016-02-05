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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionState;

/**
 * Test cases for {@link OperationExecutionDAO}.
 * 
 * @author pkupczyk
 */
@Test(groups = { "db", "script" })
public final class OperationExecutionDAOTest extends AbstractDAOTest
{

    @Test
    public void testCreateAndUpdateAndFind()
    {
        final String CODE = "testExecution";
        final String DESCRIPTION = "testDescription";
        final String ERROR = "testError";

        OperationExecutionPE execution = new OperationExecutionPE();
        execution.setCode(CODE);
        execution.setState(OperationExecutionState.NEW);
        execution.setDescription(DESCRIPTION);
        execution = createOrUpdateAndReload(execution);

        Assert.assertNotNull(execution.getId());
        Assert.assertEquals(execution.getCode(), CODE);
        Assert.assertEquals(execution.getState(), OperationExecutionState.NEW);
        Assert.assertEquals(execution.getDescription(), DESCRIPTION);
        Assert.assertNotNull(execution.getCreationDate());
        Assert.assertNull(execution.getError());
        Assert.assertNull(execution.getStartDate());
        Assert.assertNull(execution.getFinishDate());

        execution.setState(OperationExecutionState.RUNNING);
        execution.setStartDate(new Date());
        execution = createOrUpdateAndReload(execution);

        Assert.assertNotNull(execution.getId());
        Assert.assertEquals(execution.getCode(), CODE);
        Assert.assertEquals(execution.getState(), OperationExecutionState.RUNNING);
        Assert.assertEquals(execution.getDescription(), DESCRIPTION);
        Assert.assertNotNull(execution.getCreationDate());
        Assert.assertNotNull(execution.getStartDate());
        Assert.assertNull(execution.getError());
        Assert.assertNull(execution.getFinishDate());

        execution.setState(OperationExecutionState.FINISHED);
        execution.setFinishDate(new Date());
        execution = createOrUpdateAndReload(execution);

        Assert.assertNotNull(execution.getId());
        Assert.assertEquals(execution.getCode(), CODE);
        Assert.assertEquals(execution.getState(), OperationExecutionState.FINISHED);
        Assert.assertEquals(execution.getDescription(), DESCRIPTION);
        Assert.assertNotNull(execution.getCreationDate());
        Assert.assertNotNull(execution.getStartDate());
        Assert.assertNotNull(execution.getFinishDate());
        Assert.assertNull(execution.getError());

        execution.setState(OperationExecutionState.FAILED);
        execution.setError(ERROR);
        execution = createOrUpdateAndReload(execution);

        Assert.assertNotNull(execution.getId());
        Assert.assertEquals(execution.getCode(), CODE);
        Assert.assertEquals(execution.getState(), OperationExecutionState.FAILED);
        Assert.assertEquals(execution.getDescription(), DESCRIPTION);
        Assert.assertNotNull(execution.getCreationDate());
        Assert.assertNotNull(execution.getStartDate());
        Assert.assertNotNull(execution.getFinishDate());
        Assert.assertEquals(execution.getError(), ERROR);
    }

    private OperationExecutionPE createOrUpdateAndReload(OperationExecutionPE execution)
    {
        daoFactory.getOperationExecutionDAO().createOrUpdate(execution);
        daoFactory.getSessionFactory().getCurrentSession().flush();
        daoFactory.getSessionFactory().getCurrentSession().clear();
        return daoFactory.getOperationExecutionDAO().tryFindByCode(execution.getCode());
    }

}