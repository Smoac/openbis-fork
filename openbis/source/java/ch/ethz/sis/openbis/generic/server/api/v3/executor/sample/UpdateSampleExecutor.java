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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleExecutor implements IUpdateSampleExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private IUpdateSampleSpaceExecutor updateSampleSpaceExecutor;

    @Autowired
    private IUpdateSampleExperimentExecutor updateSampleExperimentExecutor;

    @Autowired
    private IUpdateSampleRelatedSamplesExecutor updateSampleRelatedSamplesExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private IUpdateTagForEntityExecutor updateTagForEntityExecutor;

    @Autowired
    private IUpdateAttachmentForEntityExecutor updateAttachmentForEntityExecutor;

    @Autowired
    private IVerifySampleExecutor verifySampleExecutor;

    private UpdateSampleExecutor()
    {
    }

    @Override
    public void update(IOperationContext context, List<SampleUpdate> updates)
    {
        Map<SampleUpdate, SamplePE> samplesAll = new LinkedHashMap<SampleUpdate, SamplePE>();

        int batchSize = 1000;
        for (int batchStart = 0; batchStart < updates.size(); batchStart += batchSize)
        {
            List<SampleUpdate> updatesBatch = updates.subList(batchStart, Math.min(batchStart + batchSize, updates.size()));
            updateSamples(context, updatesBatch, samplesAll);
        }

        reloadSamples(samplesAll);

        updateSampleRelatedSamplesExecutor.update(context, samplesAll);

        verifySamples(context, samplesAll.values());
    }

    private Map<SampleUpdate, SamplePE> getSamplesMap(IOperationContext context, List<SampleUpdate> updates)
    {
        Collection<ISampleId> sampleIds = CollectionUtils.collect(updates, new Transformer<SampleUpdate, ISampleId>()
            {
                @Override
                public ISampleId transform(SampleUpdate input)
                {
                    return input.getSampleId();
                }
            });

        Map<ISampleId, SamplePE> sampleMap = mapSampleByIdExecutor.map(context, sampleIds);

        for (ISampleId sampleId : sampleIds)
        {
            SamplePE sample = sampleMap.get(sampleId);
            if (sample == null)
            {
                throw new ObjectNotFoundException(sampleId);
            }
            if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), sample))
            {
                throw new UnauthorizedObjectAccessException(sampleId);
            }
        }

        Map<SampleUpdate, SamplePE> result = new HashMap<SampleUpdate, SamplePE>();
        for (SampleUpdate update : updates)
        {
            result.put(update, sampleMap.get(update.getSampleId()));
        }

        return result;
    }

    private void updateSamples(IOperationContext context, List<SampleUpdate> updatesBatch, Map<SampleUpdate, SamplePE> samplesAll)
    {
        Map<SampleUpdate, SamplePE> batchMap = getSamplesMap(context, updatesBatch);
        samplesAll.putAll(batchMap);

        daoFactory.setBatchUpdateMode(true);

        Map<IEntityPropertiesHolder, Map<String, String>> entityToPropertiesMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<SampleUpdate, SamplePE> batchEntry : batchMap.entrySet())
        {
            entityToPropertiesMap.put(batchEntry.getValue(), batchEntry.getKey().getProperties());
        }
        updateEntityPropertyExecutor.update(context, entityToPropertiesMap);
        updateSampleSpaceExecutor.update(context, batchMap);
        updateSampleExperimentExecutor.update(context, batchMap);

        for (SampleUpdate update : updatesBatch)
        {
            SamplePE sample = batchMap.get(update);

            updateTagForEntityExecutor.update(context, sample, update.getTagIds());
            updateAttachmentForEntityExecutor.update(context, sample, update.getAttachments());

            RelationshipUtils.updateModificationDateAndModifier(sample, context.getSession().tryGetPerson());
        }

        daoFactory.getSampleDAO().createOrUpdateSamples(new ArrayList<SamplePE>(batchMap.values()), context.getSession().tryGetPerson(), false);

        daoFactory.setBatchUpdateMode(false);
        daoFactory.getSessionFactory().getCurrentSession().flush();
        daoFactory.getSessionFactory().getCurrentSession().clear();
    }

    private void verifySamples(IOperationContext context, Collection<SamplePE> samples)
    {
        Set<Long> techIds = new HashSet<Long>();
        for (SamplePE sample : samples)
        {
            techIds.add(sample.getId());
        }

        daoFactory.getSessionFactory().getCurrentSession().flush();
        daoFactory.getSessionFactory().getCurrentSession().clear();

        Collection<SamplePE> freshSamples = daoFactory.getSampleDAO().listByIDs(techIds);

        verifySampleExecutor.verify(context, freshSamples);
    }

    private void reloadSamples(Map<SampleUpdate, SamplePE> updateToSampleMap)
    {
        Collection<Long> ids = new HashSet<Long>();

        for (SamplePE sample : updateToSampleMap.values())
        {
            ids.add(sample.getId());
        }

        List<SamplePE> samples = daoFactory.getSampleDAO().listByIDs(ids);

        Map<Long, SamplePE> idToSampleMap = new HashMap<Long, SamplePE>();

        for (SamplePE sample : samples)
        {
            idToSampleMap.put(sample.getId(), sample);
        }

        for (Map.Entry<SampleUpdate, SamplePE> entry : updateToSampleMap.entrySet())
        {
            entry.setValue(idToSampleMap.get(entry.getValue().getId()));
        }

    }

}
