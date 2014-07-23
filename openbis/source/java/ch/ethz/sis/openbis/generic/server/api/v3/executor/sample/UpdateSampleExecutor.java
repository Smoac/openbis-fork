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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
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
    private IListSampleByIdExecutor listSampleByIdExecutor;

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
    private IVerifySampleExecutor verifySampleExecutor;

    @SuppressWarnings("unused")
    private UpdateSampleExecutor()
    {
    }

    public UpdateSampleExecutor(IDAOFactory daoFactory, IListSampleByIdExecutor listSampleByIdExecutor,
            IUpdateSampleSpaceExecutor updateSampleSpaceExecutor,
            IUpdateSampleExperimentExecutor updateSampleExperimentExecutor, IUpdateSampleRelatedSamplesExecutor updateSampleRelatedSamplesExecutor,
            IUpdateEntityPropertyExecutor updateEntityPropertyExecutor, IUpdateTagForEntityExecutor updateTagForEntityExecutor,
            IVerifySampleExecutor verifySampleExecutor)
    {
        super();
        this.daoFactory = daoFactory;
        this.listSampleByIdExecutor = listSampleByIdExecutor;
        this.updateSampleSpaceExecutor = updateSampleSpaceExecutor;
        this.updateSampleExperimentExecutor = updateSampleExperimentExecutor;
        this.updateSampleRelatedSamplesExecutor = updateSampleRelatedSamplesExecutor;
        this.updateEntityPropertyExecutor = updateEntityPropertyExecutor;
        this.updateTagForEntityExecutor = updateTagForEntityExecutor;
        this.verifySampleExecutor = verifySampleExecutor;
    }

    @Override
    public void update(IOperationContext context, List<SampleUpdate> updates)
    {
        Map<SampleUpdate, SamplePE> samplesMap = getSamplesMap(context, updates);

        for (Entry<SampleUpdate, SamplePE> sampleEntry : samplesMap.entrySet())
        {
            updateSample(context, sampleEntry.getKey(), sampleEntry.getValue());
        }

        updateSampleRelatedSamplesExecutor.update(context, samplesMap);
        verifySamples(context, samplesMap.values());
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

        List<SamplePE> samples = listSampleByIdExecutor.list(context, sampleIds);

        assert sampleIds.size() == samples.size();

        Map<SampleUpdate, SamplePE> result = new HashMap<SampleUpdate, SamplePE>();

        Iterator<SampleUpdate> it1 = updates.iterator();
        Iterator<SamplePE> it2 = samples.iterator();

        while (it1.hasNext())
        {
            SampleUpdate id = it1.next();
            SamplePE sample = it2.next();
            result.put(id, sample);
        }

        return result;
    }

    private void updateSample(IOperationContext context, SampleUpdate update, SamplePE sample)
    {
        context.pushContextDescription("update sample " + update.getSampleId());

        updateSampleSpaceExecutor.update(context, sample, update.getSpaceId());
        updateSampleExperimentExecutor.update(context, sample, update.getExperimentId());
        updateEntityPropertyExecutor.update(context, sample, sample.getEntityType(), update.getProperties());
        RelationshipUtils.updateModificationDateAndModifier(sample, context.getSession().tryGetPerson());
        daoFactory.getSampleDAO().createOrUpdateSample(sample, context.getSession().tryGetPerson());
        updateTagForEntityExecutor.update(context, sample, update.getTagIds());

        context.popContextDescription();
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

}
