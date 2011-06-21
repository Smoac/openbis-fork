/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Piotr Buczek
 */
public class InvalidationBO extends AbstractBusinessObject implements IInvalidationBO
{

    private final ICommonBusinessObjectFactory boFactory;

    private InvalidationPE invalidation;

    public InvalidationBO(IDAOFactory daoFactory, Session session,
            ICommonBusinessObjectFactory boFactory)
    {
        super(daoFactory, session);
        this.boFactory = boFactory;
    }

    @Override
    public void createInvalidation(String reason)
    {
        try
        {
            invalidation = new InvalidationPE();
            invalidation.setReason(reason);
            invalidation.setRegistrator(session.tryGetPerson());
            getInvalidationDAO().create(invalidation);
        } catch (final DataAccessException ex)
        {
            throwException(ex, "Invalidation");
        }
    }

    @Override
    public void invalidateSamples(List<TechId> sampleIds)
    {
        assert invalidation != null;
        ISampleTable sampleTableBO = boFactory.createSampleTable(session);
        sampleTableBO.invalidateByTechIds(sampleIds, invalidation);
    }

    @Override
    public void invalidateExperiments(List<TechId> experimentIds)
    {
        assert invalidation != null;
        IExperimentBO experimentBO = boFactory.createExperimentBO(session);
        experimentBO.invalidateByTechIds(experimentIds, invalidation);
    }

    @Override
    public void invalidateDataSets(List<DataPE> dataSets)
    {
        assert invalidation != null;
        IDataSetTable dataSetTable = boFactory.createDataSetTable(session);
        dataSetTable.setDataSets(dataSets);
        dataSetTable.invalidateLoadedDataSets(invalidation);
    }

}
