/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.store;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.OperationExecutionState;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
@Component
public class OperationExecutionDBStoreDAO implements IOperationExecutionDBStoreDAO
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    public PersonPE findPersonById(Long personId)
    {
        return (PersonPE) daoFactory.getSessionFactory().getCurrentSession().load(PersonPE.class, personId);
    }

    @Override
    public OperationExecutionPE findExecutionByCode(String code)
    {
        return daoFactory.getOperationExecutionDAO().tryFindByCode(code);
    }

    @Override
    public void createExecution(OperationExecutionPE executionPE)
    {
        daoFactory.getOperationExecutionDAO().createOrUpdate(executionPE);
    }

    @Override
    public void updateExecutionProgress(String code, String progress)
    {
        Query query = daoFactory.getSessionFactory().getCurrentSession()
                .createQuery(
                        "update OperationExecutionPE set summaryProgress = :progress where code = :code and state in (:states) and summaryAvailability = 'AVAILABLE'");
        query.setParameter("code", code);
        query.setParameter("progress", progress);
        query.setParameterList("states", Arrays.asList(OperationExecutionState.RUNNING, OperationExecutionState.FAILED));
        query.executeUpdate();
    }

    @Override
    public void deleteExecution(OperationExecutionPE executionPE)
    {
        daoFactory.getOperationExecutionDAO().delete(executionPE);
    }

    @Override
    public List<OperationExecutionPE> findAllExecutions()
    {
        return daoFactory.getOperationExecutionDAO().listAllEntities();
    }

    @Override
    public List<OperationExecutionPE> findExecutionsToBeTimeOutPending()
    {
        return daoFactory.getOperationExecutionDAO().getExecutionsToBeTimeOutPending();
    }

    @Override
    public List<OperationExecutionPE> findExecutionsToBeTimedOut()
    {
        return daoFactory.getOperationExecutionDAO().getExecutionsToBeTimedOut();
    }

    @Override
    public List<OperationExecutionPE> findExecutionsToBeDeleted()
    {
        return daoFactory.getOperationExecutionDAO().getExecutionsToBeDeleted();
    }

}
