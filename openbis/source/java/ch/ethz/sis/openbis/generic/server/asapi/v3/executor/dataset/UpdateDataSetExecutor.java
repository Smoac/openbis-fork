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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.UpdateUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateDataSetExecutor extends AbstractUpdateEntityExecutor<DataSetUpdate, DataPE, IDataSetId> implements IUpdateDataSetExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapDataSetByIdExecutor mapDataSetByIdExecutor;

    @Autowired
    private IUpdateDataSetExperimentExecutor updateDataSetExperimentExecutor;

    @Autowired
    private IUpdateDataSetSampleExecutor updateDataSetSampleExecutor;

    @Autowired
    private IUpdateDataSetPhysicalDataExecutor updateDataSetPhysicalDataExecutor;

    @Autowired
    private IUpdateDataSetLinkedDataExecutor updateDataSetLinkedDataExecutor;

    @Autowired
    private IUpdateDataSetRelatedDataSetsExecutor updateDataSetRelatedDataSetsExecutor;

    @Autowired
    private IUpdateDataSetPropertyExecutor updateDataSetPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IVerifyDataSetExecutor verifyDataSetExecutor;

    @Override
    protected IDataSetId getId(DataSetUpdate update)
    {
        return update.getDataSetId();
    }

    @Override
    protected void checkData(IOperationContext context, DataSetUpdate update)
    {
        if (update.getDataSetId() == null)
        {
            throw new UserFailureException("Data set id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IDataSetId id, DataPE entity)
    {
        if (false == new DataSetPEByExperimentOrSampleIdentifierValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<DataPE> entities)
    {
        verifyDataSetExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<DataSetUpdate, DataPE> entitiesMap)
    {
        updateDataSetPhysicalDataExecutor.update(context, entitiesMap);
        updateDataSetLinkedDataExecutor.update(context, entitiesMap);
        updateDataSetExperimentExecutor.update(context, entitiesMap);
        updateDataSetSampleExecutor.update(context, entitiesMap);

        PersonPE person = context.getSession().tryGetPerson();
        Date timeStamp = UpdateUtils.getTransactionTimeStamp(daoFactory);
        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<DataSetUpdate, DataPE> entry : entitiesMap.entrySet())
        {
            DataSetUpdate update = entry.getKey();
            DataPE entity = entry.getValue();

            RelationshipUtils.updateModificationDateAndModifier(entity, person, timeStamp);
            updateTagForEntityExecutor.update(context, entity, update.getTagIds());

            if (update.getProperties() != null && false == update.getProperties().isEmpty())
            {
                propertyMap.put(entity, update.getProperties());
            }
        }

        if (false == propertyMap.isEmpty())
        {
            updateDataSetPropertyExecutor.update(context, propertyMap);
        }
    }

    @Override
    protected void updateAll(IOperationContext context, Map<DataSetUpdate, DataPE> entitiesMap)
    {
        updateDataSetRelatedDataSetsExecutor.update(context, entitiesMap);
    }

    @Override
    protected Map<IDataSetId, DataPE> map(IOperationContext context, Collection<IDataSetId> ids)
    {
        return mapDataSetByIdExecutor.map(context, ids);
    }

    @Override
    protected List<DataPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getDataDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<DataPE> entities, boolean clearCache)
    {
        daoFactory.getDataDAO().updateDataSets(entities, context.getSession().tryGetPerson());
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.DATA_SET.getLabel(), EntityKind.DATA_SET);
    }

}
