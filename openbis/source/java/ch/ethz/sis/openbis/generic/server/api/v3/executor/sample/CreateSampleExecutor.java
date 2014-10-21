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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateSampleExecutor implements ICreateSampleExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetSampleTypeExecutor setSampleTypeExecutor;

    @Autowired
    private ISetSampleSpaceExecutor setSampleSpaceExecutor;

    @Autowired
    private ISetSampleExperimentExecutor setSampleExperimentExecutor;

    @Autowired
    private ISetSampleRelatedSamplesExecutor setSampleRelatedSamplesExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Autowired
    private IVerifySampleExecutor verifySampleExecutor;

    @SuppressWarnings("unused")
    private CreateSampleExecutor()
    {
    }

    public CreateSampleExecutor(IDAOFactory daoFactory, ISetSampleTypeExecutor setSampleTypeExecutor,
            ISetSampleSpaceExecutor setSampleSpaceExecutor, ISetSampleExperimentExecutor setSampleExperimentExecutor,
            ISetSampleRelatedSamplesExecutor setSampleRelatedSamplesExecutor, IUpdateEntityPropertyExecutor updateEntityPropertyExecutor,
            ICreateAttachmentExecutor createAttachmentExecutor, IAddTagToEntityExecutor addTagToEntityExecutor,
            IVerifySampleExecutor verifySampleExecutor)
    {
        this.daoFactory = daoFactory;
        this.setSampleTypeExecutor = setSampleTypeExecutor;
        this.setSampleSpaceExecutor = setSampleSpaceExecutor;
        this.setSampleExperimentExecutor = setSampleExperimentExecutor;
        this.setSampleRelatedSamplesExecutor = setSampleRelatedSamplesExecutor;
        this.updateEntityPropertyExecutor = updateEntityPropertyExecutor;
        this.createAttachmentExecutor = createAttachmentExecutor;
        this.addTagToEntityExecutor = addTagToEntityExecutor;
        this.verifySampleExecutor = verifySampleExecutor;
    }

    @Override
    public List<SamplePermId> create(IOperationContext context, List<SampleCreation> creations)
    {
        List<SamplePermId> permIdsAll = new LinkedList<SamplePermId>();
        Map<SampleCreation, SamplePE> samplesAll = new LinkedHashMap<SampleCreation, SamplePE>();

        int batchSize = 1000;
        for (int batchStart = 0; batchStart < creations.size(); batchStart += batchSize)
        {
            List<SampleCreation> creationsBatch = creations.subList(batchStart, Math.min(batchStart + batchSize, creations.size()));
            createSamples(context, creationsBatch, permIdsAll, samplesAll);

            for (SampleCreation creation : creationsBatch)
            {
                SamplePE sample = samplesAll.get(creation);
                createAttachmentExecutor.create(context, sample, creation.getAttachments());
                addTagToEntityExecutor.add(context, sample, creation.getTagIds());
            }

            daoFactory.getSessionFactory().getCurrentSession().flush();
            daoFactory.getSessionFactory().getCurrentSession().clear();
        }

        reloadSamples(samplesAll);

        setSampleRelatedSamplesExecutor.set(context, samplesAll);
        verifySampleExecutor.verify(context, samplesAll.values());

        daoFactory.getSessionFactory().getCurrentSession().flush();
        daoFactory.getSessionFactory().getCurrentSession().clear();

        return permIdsAll;
    }

    private void createSamples(IOperationContext context, List<SampleCreation> creationsBatch, 
            List<SamplePermId> permIdsAll, Map<SampleCreation, SamplePE> samplesAll)
    {
        Map<SampleCreation, SamplePE> batchMap = new LinkedHashMap<SampleCreation, SamplePE>();

        daoFactory.setBatchUpdateMode(true);

        for (SampleCreation creation : creationsBatch)
        {
            context.pushContextDescription("register sample " + creation.getCode());

            SamplePE sample = createSamplePE(context, creation);

            permIdsAll.add(new SamplePermId(sample.getPermId()));
            samplesAll.put(creation, sample);
            batchMap.put(creation, sample);

            context.popContextDescription();
        }

        setSampleSpaceExecutor.set(context, batchMap);
        setSampleExperimentExecutor.set(context, batchMap);
        setSampleTypeExecutor.set(context, batchMap);

        for (Map.Entry<SampleCreation, SamplePE> batchEntry : batchMap.entrySet())
        {
            SamplePE sample = batchEntry.getValue();
            Map<String, String> properties = batchEntry.getKey().getProperties();
            updateEntityPropertyExecutor.update(context, sample, sample.getEntityType(), properties);
        }

        PersonPE modifier = context.getSession().tryGetPerson();
        daoFactory.getSampleDAO().createOrUpdateSamples(new ArrayList<SamplePE>(batchMap.values()), modifier, false);
        daoFactory.setBatchUpdateMode(false);
    }

    private SamplePE createSamplePE(IOperationContext context, SampleCreation sampleCreation)
    {
        if (sampleCreation.getCode() == null)
        {
            throw new UserFailureException("No code for sample provided");
        }
        
        SamplePE sample = new SamplePE();
        sample.setCode(sampleCreation.getCode());
        String createdPermId = daoFactory.getPermIdDAO().createPermId();
        sample.setPermId(createdPermId);
        sample.setRegistrator(context.getSession().tryGetPerson());
        RelationshipUtils.updateModificationDateAndModifier(sample, context.getSession().tryGetPerson());

        return sample;
    }

    private void reloadSamples(Map<SampleCreation, SamplePE> creationToSampleMap)
    {
        Collection<Long> ids = new HashSet<Long>();

        for (SamplePE sample : creationToSampleMap.values())
        {
            ids.add(sample.getId());
        }

        List<SamplePE> samples = daoFactory.getSampleDAO().listByIDs(ids);

        Map<Long, SamplePE> idToSampleMap = new HashMap<Long, SamplePE>();

        for (SamplePE sample : samples)
        {
            idToSampleMap.put(sample.getId(), sample);
        }

        for (Map.Entry<SampleCreation, SamplePE> entry : creationToSampleMap.entrySet())
        {
            entry.setValue(idToSampleMap.get(entry.getValue().getId()));
        }

    }

}
