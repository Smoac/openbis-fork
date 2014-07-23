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
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IGetEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.experiment.IGetExperimentByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IGetSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
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
    private IGetEntityTypeByIdExecutor getEntityTypeByIdExecutor;

    @Autowired
    private IGetSpaceByIdExecutor getSpaceByIdExecutor;

    @Autowired
    private IGetExperimentByIdExecutor getExperimentByIdExecutor;

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

    public CreateSampleExecutor(IDAOFactory daoFactory, IGetEntityTypeByIdExecutor getEntityTypeByIdExecutor,
            IGetSpaceByIdExecutor getSpaceByIdExecutor, IGetExperimentByIdExecutor getExperimentByIdExecutor,
            ISetSampleRelatedSamplesExecutor setSampleRelatedSamplesExecutor, IUpdateEntityPropertyExecutor updateEntityPropertyExecutor,
            ICreateAttachmentExecutor createAttachmentExecutor, IAddTagToEntityExecutor addTagToEntityExecutor,
            IVerifySampleExecutor verifySampleExecutor)
    {
        this.daoFactory = daoFactory;
        this.getEntityTypeByIdExecutor = getEntityTypeByIdExecutor;
        this.getSpaceByIdExecutor = getSpaceByIdExecutor;
        this.getExperimentByIdExecutor = getExperimentByIdExecutor;
        this.setSampleRelatedSamplesExecutor = setSampleRelatedSamplesExecutor;
        this.updateEntityPropertyExecutor = updateEntityPropertyExecutor;
        this.createAttachmentExecutor = createAttachmentExecutor;
        this.addTagToEntityExecutor = addTagToEntityExecutor;
        this.verifySampleExecutor = verifySampleExecutor;
    }

    @Override
    public List<SamplePermId> create(IOperationContext context, List<SampleCreation> creations)
    {
        List<SamplePermId> result = new LinkedList<SamplePermId>();
        HashMap<SampleCreation, SamplePE> createdSamples = new HashMap<SampleCreation, SamplePE>();

        for (SampleCreation sampleCreation : creations)
        {
            context.pushContextDescription("register sample " + sampleCreation.getCode());

            SamplePE sample = createSamplePE(context, sampleCreation);
            daoFactory.getSampleDAO().createOrUpdateSample(sample, context.getSession().tryGetPerson());
            createAttachmentExecutor.create(context, sample, sampleCreation.getAttachments());
            addTagToEntityExecutor.add(context, sample, sampleCreation.getTagIds());
            result.add(new SamplePermId(sample.getPermId()));
            createdSamples.put(sampleCreation, sample);

            context.popContextDescription();
        }

        setSampleRelatedSamplesExecutor.set(context, createdSamples);
        verifySamples(context, createdSamples.values());

        return result;
    }

    private SamplePE createSamplePE(IOperationContext context, SampleCreation sampleCreation)
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
        EntityTypePE entityType = getEntityTypeByIdExecutor.get(context, EntityKind.SAMPLE, typeId);
        sample.setSampleType((SampleTypePE) entityType);

        IExperimentId experimentId = sampleCreation.getExperimentId();
        if (experimentId != null)
        {
            sample.setExperiment(getExperimentByIdExecutor.get(context, experimentId));
        }

        ISpaceId spaceId = sampleCreation.getSpaceId();
        if (spaceId != null)
        {
            SpacePE space = getSpaceByIdExecutor.get(context, sampleCreation.getSpaceId());
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
