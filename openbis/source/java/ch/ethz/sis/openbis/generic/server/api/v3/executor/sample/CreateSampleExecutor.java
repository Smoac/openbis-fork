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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IMapEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IMapExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
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
    private IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor;

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Autowired
    private IMapExperimentByIdExecutor mapExperimentByIdExecutor;

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

    public CreateSampleExecutor(IDAOFactory daoFactory, IMapEntityTypeByIdExecutor mapEntityTypeByIdExecutor,
            IMapSpaceByIdExecutor mapSpaceByIdExecutor, IMapExperimentByIdExecutor mapExperimentByIdExecutor,
            ISetSampleRelatedSamplesExecutor setSampleRelatedSamplesExecutor, IUpdateEntityPropertyExecutor updateEntityPropertyExecutor,
            ICreateAttachmentExecutor createAttachmentExecutor, IAddTagToEntityExecutor addTagToEntityExecutor,
            IVerifySampleExecutor verifySampleExecutor)
    {
        this.daoFactory = daoFactory;
        this.mapEntityTypeByIdExecutor = mapEntityTypeByIdExecutor;
        this.mapSpaceByIdExecutor = mapSpaceByIdExecutor;
        this.mapExperimentByIdExecutor = mapExperimentByIdExecutor;
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
        HashMap<SampleCreation, SamplePE> samplesAll = new HashMap<SampleCreation, SamplePE>();

        int batchSize = 1000;
        int batchStart = 0;

        while (batchStart < creations.size())
        {
            List<SampleCreation> creationsBatch = creations.subList(batchStart, Math.min(batchStart + batchSize, creations.size()));
            List<SamplePE> samplesBatch = new LinkedList<SamplePE>();

            daoFactory.setBatchUpdateMode(true);

            Map<IEntityTypeId, EntityTypePE> typeMap = getTypeMap(context, creationsBatch);
            Map<ISpaceId, SpacePE> spaceMap = getSpaceMap(context, creationsBatch);
            Map<IExperimentId, ExperimentPE> experimentMap = getExperimentMap(context, creationsBatch);

            for (SampleCreation creation : creationsBatch)
            {
                context.pushContextDescription("register sample " + creation.getCode());

                SamplePE sample = createSamplePE(context, creation, typeMap, spaceMap, experimentMap);

                permIdsAll.add(new SamplePermId(sample.getPermId()));
                samplesAll.put(creation, sample);
                samplesBatch.add(sample);

                context.popContextDescription();
            }

            daoFactory.getSampleDAO().createOrUpdateSamples(samplesBatch, context.getSession().tryGetPerson(), false);
            daoFactory.setBatchUpdateMode(false);

            for (SampleCreation creation : creationsBatch)
            {
                SamplePE sample = samplesAll.get(creation);
                createAttachmentExecutor.create(context, sample, creation.getAttachments());
                addTagToEntityExecutor.add(context, sample, creation.getTagIds());
            }

            daoFactory.getSessionFactory().getCurrentSession().flush();
            daoFactory.getSessionFactory().getCurrentSession().clear();

            batchStart += batchSize;
        }

        setSampleRelatedSamplesExecutor.set(context, samplesAll);
        verifySamples(context, samplesAll.values());

        return permIdsAll;
    }

    private Map<IEntityTypeId, EntityTypePE> getTypeMap(IOperationContext context, List<SampleCreation> creations)
    {
        Set<IEntityTypeId> ids = new HashSet<IEntityTypeId>();
        for (SampleCreation creation : creations)
        {
            if (creation.getTypeId() != null)
            {
                ids.add(creation.getTypeId());
            }
        }
        return mapEntityTypeByIdExecutor.map(context, EntityKind.SAMPLE, ids);
    }

    private Map<ISpaceId, SpacePE> getSpaceMap(IOperationContext context, List<SampleCreation> creations)
    {
        Set<ISpaceId> ids = new HashSet<ISpaceId>();
        for (SampleCreation creation : creations)
        {
            if (creation.getSpaceId() != null)
            {
                ids.add(creation.getSpaceId());
            }
        }
        return mapSpaceByIdExecutor.map(context, ids);
    }

    private Map<IExperimentId, ExperimentPE> getExperimentMap(IOperationContext context, List<SampleCreation> creations)
    {
        Set<IExperimentId> ids = new HashSet<IExperimentId>();
        for (SampleCreation creation : creations)
        {
            if (creation.getExperimentId() != null)
            {
                ids.add(creation.getExperimentId());
            }
        }
        return mapExperimentByIdExecutor.map(context, ids);
    }

    private SamplePE createSamplePE(IOperationContext context, SampleCreation sampleCreation, Map<IEntityTypeId, EntityTypePE> typeMap,
            Map<ISpaceId, SpacePE> spaceMap, Map<IExperimentId, ExperimentPE> experimentMap)
    {
        SamplePE sample = new SamplePE();

        if (sampleCreation.getCode() != null)
        {
            sample.setCode(sampleCreation.getCode());
        } else
        {
            throw new UserFailureException("No code for sample provided");
        }

        String createdPermId = daoFactory.getPermIdDAO().createPermId();
        sample.setPermId(createdPermId);

        IEntityTypeId typeId = sampleCreation.getTypeId();
        EntityTypePE entityType = typeMap.get(typeId);
        sample.setSampleType((SampleTypePE) entityType);

        IExperimentId experimentId = sampleCreation.getExperimentId();
        if (experimentId != null)
        {
            sample.setExperiment(experimentMap.get(experimentId));
        }

        ISpaceId spaceId = sampleCreation.getSpaceId();
        if (spaceId != null)
        {
            SpacePE space = spaceMap.get(spaceId);
            sample.setSpace(space);
        }

        updateEntityPropertyExecutor.update(context, sample, entityType, sampleCreation.getProperties());
        sample.setRegistrator(context.getSession().tryGetPerson());
        RelationshipUtils.updateModificationDateAndModifier(sample, context.getSession().tryGetPerson());

        return sample;
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
