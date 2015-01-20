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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
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
        Map<ExperimentUpdate, ExperimentPE> experimentsAll = new LinkedHashMap<ExperimentUpdate, ExperimentPE>();

        int batchSize = 1000;
        for (int batchStart = 0; batchStart < updates.size(); batchStart += batchSize)
        {
            List<ExperimentUpdate> updatesBatch = updates.subList(batchStart, Math.min(batchStart + batchSize, updates.size()));
            updateExperiments(context, updatesBatch, experimentsAll);
        }

        reloadExperiments(experimentsAll);

        verifyExperiments(context, experimentsAll.values());
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

    private void updateExperiments(IOperationContext context, List<ExperimentUpdate> updatesBatch, Map<ExperimentUpdate, ExperimentPE> experimentsAll)
    {
        Map<ExperimentUpdate, ExperimentPE> batchMap = getExperimentsMap(context, updatesBatch);
        experimentsAll.putAll(batchMap);

        daoFactory.setBatchUpdateMode(true);

        Map<IEntityPropertiesHolder, Map<String, String>> entityToPropertiesMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<ExperimentUpdate, ExperimentPE> batchEntry : batchMap.entrySet())
        {
            entityToPropertiesMap.put(batchEntry.getValue(), batchEntry.getKey().getProperties());
        }
        updateEntityPropertyExecutor.update(context, entityToPropertiesMap);
        updateExperimentProjectExecutor.update(context, batchMap);

        for (ExperimentUpdate update : updatesBatch)
        {
            ExperimentPE experiment = batchMap.get(update);

            updateTagForEntityExecutor.update(context, experiment, update.getTagIds());
            updateAttachmentForEntityExecutor.update(context, experiment, update.getAttachments());

            RelationshipUtils.updateModificationDateAndModifier(experiment, context.getSession().tryGetPerson());
        }

        daoFactory.getExperimentDAO().createOrUpdateExperiments(new ArrayList<ExperimentPE>(batchMap.values()), context.getSession().tryGetPerson());

        daoFactory.setBatchUpdateMode(false);
        daoFactory.getSessionFactory().getCurrentSession().flush();
        daoFactory.getSessionFactory().getCurrentSession().clear();
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

    private void reloadExperiments(Map<ExperimentUpdate, ExperimentPE> updateToExperimentMap)
    {
        Collection<Long> ids = new HashSet<Long>();

        for (ExperimentPE experiment : updateToExperimentMap.values())
        {
            ids.add(experiment.getId());
        }

        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByIDs(ids);

        Map<Long, ExperimentPE> idToExperimentMap = new HashMap<Long, ExperimentPE>();

        for (ExperimentPE experiment : experiments)
        {
            idToExperimentMap.put(experiment.getId(), experiment);
        }

        for (Map.Entry<ExperimentUpdate, ExperimentPE> entry : updateToExperimentMap.entrySet())
        {
            entry.setValue(idToExperimentMap.get(entry.getValue().getId()));
        }

    }

}
