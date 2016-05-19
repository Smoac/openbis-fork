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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.UnauthorizedObjectAccessException;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdateRelationProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
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
    private IUpdateExperimentPropertyExecutor updateExperimentPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IUpdateExperimentAttachmentExecutor updateExperimentAttachmentExecutor;

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
    protected void checkBusinessRules(IOperationContext context, CollectionBatch<ExperimentPE> batch)
    {
        verifyExperimentExecutor.verify(context, batch);
    }

    @Override
    protected void updateBatch(final IOperationContext context, final MapBatch<ExperimentUpdate, ExperimentPE> batch)
    {
        updateExperimentProjectExecutor.update(context, batch);
        updateExperimentPropertyExecutor.update(context, batch);
        updateTags(context, batch);
        updateAttachments(context, batch);

        PersonPE person = context.getSession().tryGetPerson();
        Date timeStamp = daoFactory.getTransactionTimestamp();

        for (ExperimentPE entity : batch.getObjects().values())
        {
            RelationshipUtils.updateModificationDateAndModifier(entity, person, timeStamp);
        }
    }

    private void updateTags(final IOperationContext context, final MapBatch<ExperimentUpdate, ExperimentPE> batch)
    {
        new MapBatchProcessor<ExperimentUpdate, ExperimentPE>(context, batch)
            {
                @Override
                public void process(ExperimentUpdate update, ExperimentPE entity)
                {
                    if (update.getTagIds() != null && update.getTagIds().hasActions())
                    {
                        updateTagForEntityExecutor.update(context, entity, update.getTagIds());
                    }
                }

                @Override
                public IProgress createProgress(ExperimentUpdate update, ExperimentPE entity, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(update, entity, "experiment-tag", objectIndex, totalObjectCount);
                }
            };
    }

    private void updateAttachments(final IOperationContext context, final MapBatch<ExperimentUpdate, ExperimentPE> batch)
    {
        new MapBatchProcessor<ExperimentUpdate, ExperimentPE>(context, batch)
            {
                @Override
                public void process(ExperimentUpdate update, ExperimentPE entity)
                {
                    if (update.getAttachments() != null && update.getAttachments().hasActions())
                    {
                        updateExperimentAttachmentExecutor.update(context, entity, update.getAttachments());
                    }
                }

                @Override
                public IProgress createProgress(ExperimentUpdate update, ExperimentPE entity, int objectIndex, int totalObjectCount)
                {
                    return new UpdateRelationProgress(update, entity, "experiment-attachment", objectIndex, totalObjectCount);
                }
            };
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<ExperimentUpdate, ExperimentPE> batch)
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
