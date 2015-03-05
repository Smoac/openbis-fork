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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.AttachmentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateExperimentExecutor extends AbstractCreateEntityExecutor<ExperimentCreation, ExperimentPE, ExperimentPermId> implements
        ICreateExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetExperimentTypeExecutor setExperimentTypeExecutor;

    @Autowired
    private ISetExperimentProjectExecutor setExperimentProjectExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @Autowired
    private IVerifyExperimentExecutor verifyExperimentExecutor;

    @Override
    protected EntityKind getKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected ExperimentPE create(IOperationContext context, ExperimentCreation creation)
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(creation.getCode());
        String createdPermId = daoFactory.getPermIdDAO().createPermId();
        experiment.setPermId(createdPermId);
        experiment.setRegistrator(context.getSession().tryGetPerson());
        RelationshipUtils.updateModificationDateAndModifier(experiment, context.getSession().tryGetPerson());
        return experiment;
    }

    @Override
    protected ExperimentPermId createPermId(IOperationContext context, ExperimentPE entity)
    {
        return new ExperimentPermId(entity.getPermId());
    }

    @Override
    protected void checkData(IOperationContext context, ExperimentCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }

        ExperimentIdentifierFactory.assertValidCode(creation.getCode());
    }

    @Override
    protected void checkAccess(IOperationContext context, ExperimentPE entity)
    {
        if (false == new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(new ExperimentIdentifier(entity.getIdentifier()));
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<ExperimentPE> entities)
    {
        verifyExperimentExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<ExperimentCreation, ExperimentPE> entitiesMap)
    {
        setExperimentProjectExecutor.set(context, entitiesMap);
        setExperimentTypeExecutor.set(context, entitiesMap);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<ExperimentCreation, ExperimentPE> entry : entitiesMap.entrySet())
        {
            propertyMap.put(entry.getValue(), entry.getKey().getProperties());
        }
        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<ExperimentCreation, ExperimentPE> entitiesMap)
    {
        Map<AttachmentHolderPE, Collection<? extends AttachmentCreation>> attachmentMap =
                new HashMap<AttachmentHolderPE, Collection<? extends AttachmentCreation>>();
        Map<IEntityWithMetaprojects, Collection<? extends ITagId>> tagMap = new HashMap<IEntityWithMetaprojects, Collection<? extends ITagId>>();

        for (Map.Entry<ExperimentCreation, ExperimentPE> entry : entitiesMap.entrySet())
        {
            ExperimentCreation creation = entry.getKey();
            ExperimentPE entity = entry.getValue();
            attachmentMap.put(entity, creation.getAttachments());
            tagMap.put(entity, creation.getTagIds());
        }

        createAttachmentExecutor.create(context, attachmentMap);
        addTagToEntityExecutor.add(context, tagMap);
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

}
