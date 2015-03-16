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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateSampleExecutor extends AbstractCreateEntityExecutor<SampleCreation, SamplePE, SamplePermId> implements ICreateSampleExecutor
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

    @Override
    protected SamplePE create(IOperationContext context, SampleCreation creation)
    {
        SamplePE sample = new SamplePE();
        sample.setCode(creation.getCode());
        String createdPermId = daoFactory.getPermIdDAO().createPermId();
        sample.setPermId(createdPermId);
        sample.setRegistrator(context.getSession().tryGetPerson());
        RelationshipUtils.updateModificationDateAndModifier(sample, context.getSession().tryGetPerson());
        return sample;
    }

    @Override
    protected SamplePermId createPermId(IOperationContext context, SamplePE entity)
    {
        return new SamplePermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, SampleCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
        SampleIdentifierFactory.assertValidCode(creation.getCode());
    }

    @Override
    protected void checkAccess(IOperationContext context, SamplePE entity)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(new SampleIdentifier(entity.getIdentifier()));
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<SamplePE> entities)
    {
        verifySampleExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<SampleCreation, SamplePE> entitiesMap)
    {
        setSampleSpaceExecutor.set(context, entitiesMap);
        setSampleExperimentExecutor.set(context, entitiesMap);
        setSampleTypeExecutor.set(context, entitiesMap);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<SampleCreation, SamplePE> entry : entitiesMap.entrySet())
        {
            propertyMap.put(entry.getValue(), entry.getKey().getProperties());
        }
        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<SampleCreation, SamplePE> entitiesMap)
    {
        Map<AttachmentHolderPE, Collection<? extends AttachmentCreation>> attachmentMap =
                new HashMap<AttachmentHolderPE, Collection<? extends AttachmentCreation>>();
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<SampleCreation, SamplePE> entry : entitiesMap.entrySet())
        {
            SampleCreation creation = entry.getKey();
            SamplePE entity = entry.getValue();
            attachmentMap.put(entity, creation.getAttachments());
            tagMap.put(entity, creation.getTagIds());
        }

        createAttachmentExecutor.create(context, attachmentMap);
        addTagToEntityExecutor.add(context, tagMap);
        setSampleRelatedSamplesExecutor.set(context, entitiesMap);
    }

    @Override
    protected List<SamplePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getSampleDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<SamplePE> entities, boolean clearCache)
    {
        daoFactory.getSampleDAO().createOrUpdateSamples(entities, context.getSession().tryGetPerson(), clearCache);
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, EntityKind.SAMPLE.getLabel(), EntityKind.SAMPLE);
    }

}
