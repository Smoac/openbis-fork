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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.IUpdateAttachmentForEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateExperimentExecutor extends AbstractUpdateEntityExecutor<ExperimentUpdate, ExperimentPE, IExperimentId> implements
        IUpdateExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

    @Autowired
    private IUpdateExperimentProjectExecutor updateExperimentProjectExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IUpdateAttachmentForEntityExecutor updateAttachmentForEntityExecutor;

    @Autowired
    private IVerifyExperimentExecutor verifyExperimentExecutor;

    @Override
    protected IExperimentId getId(ExperimentUpdate update)
    {
        return update.getExperimentId();
    }

    @Override
    protected void checkData(IOperationContext context, ExperimentUpdate update)
    {
        if (update.getExperimentId() == null)
        {
            throw new UserFailureException("Experiment id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IExperimentId id, ExperimentPE entity)
    {
        if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<ExperimentPE> entities)
    {
        verifyExperimentExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<ExperimentUpdate, ExperimentPE> entitiesMap)
    {
        updateExperimentProjectExecutor.update(context, entitiesMap);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<ExperimentUpdate, ExperimentPE> entry : entitiesMap.entrySet())
        {
            ExperimentUpdate update = entry.getKey();
            ExperimentPE entity = entry.getValue();

            RelationshipUtils.updateModificationDateAndModifier(entity, context.getSession().tryGetPerson());
            updateTagForEntityExecutor.update(context, entity, update.getTagIds());
            updateAttachmentForEntityExecutor.update(context, entity, update.getAttachments());
            propertyMap.put(entity, update.getProperties());
        }

        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<ExperimentUpdate, ExperimentPE> entitiesMap)
    {
    }

    @Override
    protected Map<IExperimentId, ExperimentPE> map(IOperationContext context, Collection<IExperimentId> ids)
    {
        return mapExperimentByIdExecutor.map(context, ids);
    }

    @Override
    protected List<ExperimentPE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getExperimentDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<ExperimentPE> entities, boolean clearCache)
    {
        daoFactory.getExperimentDAO().createOrUpdateExperiments(entities, context.getSession().tryGetPerson(), clearCache);
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.EXPERIMENT.getLabel(), EntityKind.EXPERIMENT);
    }

}
