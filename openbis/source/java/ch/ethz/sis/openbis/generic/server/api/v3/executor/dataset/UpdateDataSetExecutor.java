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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
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
    private IUpdateDataSetFileFormatTypeExecutor updateDataSetFileFormatTypeExecutor;

    @Autowired
    private IUpdateDataSetRelatedDataSetsExecutor updateDataSetRelatedDataSetsExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IVerifyDataSetExecutor verifyDataSetExecutor;

    @Override
    protected EntityKind getKind()
    {
        return EntityKind.DATA_SET;
    }

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
        if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity.getExperiment()))
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
        updateDataSetExperimentExecutor.update(context, entitiesMap);
        updateDataSetSampleExecutor.update(context, entitiesMap);
        updateDataSetFileFormatTypeExecutor.update(context, entitiesMap);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<DataSetUpdate, DataPE> entry : entitiesMap.entrySet())
        {
            DataSetUpdate update = entry.getKey();
            DataPE entity = entry.getValue();

            RelationshipUtils.updateModificationDateAndModifier(entity, context.getSession().tryGetPerson());
            updateTagForEntityExecutor.update(context, entity, update.getTagIds());
            propertyMap.put(entity, update.getProperties());
        }

        updateEntityPropertyExecutor.update(context, propertyMap);
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

}
