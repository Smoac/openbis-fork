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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DeletionValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletedDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDeletionTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;

/**
 * @author pkupczyk
 */
@Component
public class ConfirmDeletionExecutor implements IConfirmDeletionExecutor
{

    @Autowired
    private IMapDeletionByIdExecutor mapDeletionByIdExecutor;

    @Autowired
    private IDAOFactory daoFactory;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Override
    public void confirm(IOperationContext context, List<? extends IDeletionId> deletionIds)
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
        Map<IDeletionId, DeletionPE> deletionMap = mapDeletionByIdExecutor.map(context, deletionIds);

        List<Long> deletionTechIds = new LinkedList<Long>();
        for (DeletionPE deletion : deletionMap.values())
        {
            deletionTechIds.add(deletion.getId());
        }

        IDeletionTable table = businessObjectFactory.createDeletionTable(context.getSession());
        table.load(deletionTechIds, true);
        List<Deletion> deletions = table.getDeletions();

        DeletionValidator validator = new DeletionValidator();
        validator.init(new AuthorizationDataProvider(daoFactory));

        for (Deletion deletion : deletions)
        {
            if (false == validator.doValidation(context.getSession().tryGetPerson(), deletion))
            {
                throw new UnauthorizedObjectAccessException(new DeletionTechId(deletion.getId()));
            }
        }

        // NOTE: we can't do bulk deletions to preserve original reasons
        for (IDeletionId deletionId : deletionIds)
        {
            DeletionPE deletion = deletionMap.get(deletionId);

            if (deletion == null)
            {
                throw new ObjectNotFoundException(deletionId);
            }

            deleteDataSets(context, deletion);
            deleteSamples(context, deletion);
            deleteExperiments(context, deletion);

            // WORKAROUND to get the fresh deletion and fix org.hibernate.NonUniqueObjectException
            DeletionPE freshDeletion = deletionDAO.getByTechId(TechId.create(deletion));
            deletionDAO.delete(freshDeletion);
        }

    }

    private void deleteDataSets(IOperationContext context, DeletionPE deletion)
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();

        List<TechId> deletionTechIds = Collections.singletonList(new TechId(deletion.getId()));
        List<String> dataSetCodes = deletionDAO.findTrashedDataSetCodes(deletionTechIds);

        IDeletedDataSetTable deletedDataSetTable =
                businessObjectFactory.createDeletedDataSetTable(context.getSession());
        deletedDataSetTable.loadByDataSetCodes(dataSetCodes);
        deletedDataSetTable.permanentlyDeleteLoadedDataSets(deletion.getReason(), false);
    }

    private void deleteSamples(IOperationContext context, DeletionPE deletion)
    {
        ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        sampleDAO.deletePermanently(deletion, context.getSession().tryGetPerson());
    }

    private void deleteExperiments(IOperationContext context, DeletionPE deletion)
    {
        IDeletionDAO deletionDAO = daoFactory.getDeletionDAO();
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(context.getSession());

        List<TechId> deletionTechIds = Collections.singletonList(new TechId(deletion.getId()));
        List<TechId> experimentTechIds = deletionDAO.findTrashedExperimentIds(deletionTechIds);

        experimentBO.deleteByTechIds(experimentTechIds, deletion.getReason());
    }
}
