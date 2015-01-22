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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.IUpdateAttachmentForEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IUpdateTagForEntityExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class UpdateSampleExecutor extends AbstractUpdateEntityExecutor<SampleUpdate, SamplePE, ISampleId> implements IUpdateSampleExecutor
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

    @Override
    protected EntityKind getKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected ISampleId getId(SampleUpdate update)
    {
        return update.getSampleId();
    }

    @Override
    protected void checkData(IOperationContext context, SampleUpdate update)
    {
        if (update.getSampleId() == null)
        {
            throw new UserFailureException("Sample id cannot be null.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, ISampleId id, SamplePE entity)
    {
        if (false == new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), entity))
        {
            throw new UnauthorizedObjectAccessException(id);
        }
    }

    @Override
    protected void checkBusinessRules(IOperationContext context, Collection<SamplePE> entities)
    {
        verifySampleExecutor.verify(context, entities);
    }

    @Override
    protected void updateBatch(IOperationContext context, Map<SampleUpdate, SamplePE> entitiesMap)
    {
        updateSampleSpaceExecutor.update(context, entitiesMap);
        updateSampleExperimentExecutor.update(context, entitiesMap);

        Map<IEntityPropertiesHolder, Map<String, String>> propertyMap = new HashMap<IEntityPropertiesHolder, Map<String, String>>();
        for (Map.Entry<SampleUpdate, SamplePE> entry : entitiesMap.entrySet())
        {
            SampleUpdate update = entry.getKey();
            SamplePE entity = entry.getValue();

            RelationshipUtils.updateModificationDateAndModifier(entity, context.getSession().tryGetPerson());
            updateTagForEntityExecutor.update(context, entity, update.getTagIds());
            updateAttachmentForEntityExecutor.update(context, entity, update.getAttachments());
            propertyMap.put(entity, update.getProperties());
        }

        updateEntityPropertyExecutor.update(context, propertyMap);
    }

    @Override
    protected void updateAll(IOperationContext context, Map<SampleUpdate, SamplePE> entitiesMap)
    {
        updateSampleRelatedSamplesExecutor.update(context, entitiesMap);
    }

    @Override
    protected Map<ISampleId, SamplePE> map(IOperationContext context, Collection<ISampleId> ids)
    {
        return mapSampleByIdExecutor.map(context, ids);
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

}
