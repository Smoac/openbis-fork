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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
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
    private IListExperimentByIdExecutor listExperimentByIdExecutor;

    @Autowired
    private IUpdateExperimentProjectExecutor updateExperimentProjectExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IVerifyExperimentExecutor verifyExperimentExecutor;

    @SuppressWarnings("unused")
    private UpdateExperimentExecutor()
    {
    }

    public UpdateExperimentExecutor(IDAOFactory daoFactory, IListExperimentByIdExecutor listExperimentByIdExecutor,
            IUpdateExperimentProjectExecutor updateExperimentProjectExecutor,
            IUpdateTagForEntityExecutor updateTagForEntityExecutor, IUpdateEntityPropertyExecutor updateEntityPropertyExecutor,
            IVerifyExperimentExecutor verifyExperimentExecutor)
    {
        super();
        this.listExperimentByIdExecutor = listExperimentByIdExecutor;
        this.updateExperimentProjectExecutor = updateExperimentProjectExecutor;
        this.updateTagForEntityExecutor = updateTagForEntityExecutor;
        this.updateEntityPropertyExecutor = updateEntityPropertyExecutor;
        this.verifyExperimentExecutor = verifyExperimentExecutor;
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

        List<ExperimentPE> experiments = listExperimentByIdExecutor.list(context, experimentIds);

        assert experimentIds.size() == experiments.size();

        for (ExperimentPE experiment : experiments)
        {
            if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), experiment))
            {
                throw new AuthorizationFailureException("Cannot access experiment " + experiment.getIdentifier());
            }
        }

        Map<ExperimentUpdate, ExperimentPE> result = new HashMap<ExperimentUpdate, ExperimentPE>();

        Iterator<ExperimentUpdate> it1 = updates.iterator();
        Iterator<ExperimentPE> it2 = experiments.iterator();

        while (it1.hasNext())
        {
            ExperimentUpdate id = it1.next();
            ExperimentPE experiment = it2.next();
            result.put(id, experiment);
        }

        return result;
    }

    private void updateExperiment(IOperationContext context, ExperimentUpdate update, ExperimentPE experiment)
    {
        context.pushContextDescription("update experiment " + update.getExperimentId());

        updateExperimentProjectExecutor.update(context, experiment, update.getProjectId());
        updateEntityPropertyExecutor.update(context, experiment, experiment.getEntityType(), update.getProperties());
        RelationshipUtils.updateModificationDateAndModifier(experiment, context.getSession().tryGetPerson());
        daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, context.getSession().tryGetPerson());
        updateTagForEntityExecutor.update(context, experiment, update.getTagIds());

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
