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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.IUpdateAttachmentForEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.experiment.ExperimentContextDescription;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateExperimentExecutor implements IUpdateExperimentExecutor
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

    private UpdateExperimentExecutor()
    {
    }

    @Override
    public void update(IOperationContext context, List<ExperimentUpdate> updates)
    {
        Map<ExperimentUpdate, ExperimentPE> experimentsMap = getExperimentsMap(context, updates);

        for (Entry<ExperimentUpdate, ExperimentPE> experimentEntry : experimentsMap.entrySet())
        {
            updateExperiment(context, experimentEntry.getKey(), experimentEntry.getValue());
        }

        verifyExperiments(context, experimentsMap.values());
    }

    private Map<ExperimentUpdate, ExperimentPE> getExperimentsMap(IOperationContext context, List<ExperimentUpdate> updates)
    {
        Collection<IExperimentId> experimentIds = CollectionUtils.collect(updates, new Transformer<ExperimentUpdate, IExperimentId>()
            {
                @Override
                public IExperimentId transform(ExperimentUpdate input)
                {
                    return input.getExperimentId();
                }
            });

        Map<IExperimentId, ExperimentPE> experimentMap = mapExperimentByIdExecutor.map(context, experimentIds);

        for (IExperimentId experimentId : experimentIds)
        {
            context.pushContextDescription(ExperimentContextDescription.updating(experimentId));

            ExperimentPE experiment = experimentMap.get(experimentId);
            if (experiment == null)
            {
                throw new ObjectNotFoundException(experimentId);
            }
            if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
            {
                throw new UnauthorizedObjectAccessException(experimentId);
            }

            context.popContextDescription();
        }

        Map<ExperimentUpdate, ExperimentPE> result = new HashMap<ExperimentUpdate, ExperimentPE>();
        for (ExperimentUpdate update : updates)
        {
            result.put(update, experimentMap.get(update.getExperimentId()));
        }

        return result;
    }

    private void updateExperiment(IOperationContext context, ExperimentUpdate update, ExperimentPE experiment)
    {
        context.pushContextDescription(ExperimentContextDescription.updating(update.getExperimentId()));

        updateExperimentProjectExecutor.update(context, experiment, update.getProjectId());
        updateEntityPropertyExecutor.update(context, experiment, experiment.getEntityType(), update.getProperties());
        RelationshipUtils.updateModificationDateAndModifier(experiment, context.getSession().tryGetPerson());
        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, context.getSession().tryGetPerson());
        updateTagForEntityExecutor.update(context, experiment, update.getTagIds());
        updateAttachmentForEntityExecutor.update(context, experiment, update.getAttachments());

        context.popContextDescription();
    }

    private void verifyExperiments(IOperationContext context, Collection<ExperimentPE> experiments)
    {
        Set<Long> techIds = new HashSet<Long>();
        for (ExperimentPE experiment : experiments)
        {
            techIds.add(experiment.getId());
        }

        daoFactory.getSessionFactory().getCurrentSession().flush();
        daoFactory.getSessionFactory().getCurrentSession().clear();

        Collection<ExperimentPE> freshExperiments = daoFactory.getExperimentDAO().listByIDs(techIds);

        verifyExperimentExecutor.verify(context, freshExperiments);
    }

}
