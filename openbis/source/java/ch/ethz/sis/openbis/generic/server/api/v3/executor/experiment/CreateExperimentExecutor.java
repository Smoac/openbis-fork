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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.attachment.ICreateAttachmentExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.entity.IGetEntityTypeByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.project.IGetProjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.property.IUpdateEntityPropertyExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.tag.IAddTagToEntityExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.experiment.ExperimentContextDescription;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * @author pkupczyk
 */
@Component
public class CreateExperimentExecutor implements ICreateExperimentExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IGetEntityTypeByIdExecutor getEntityTypeByIdExecutor;

    @Autowired
    private IGetProjectByIdExecutor getProjectByIdExecutor;

    @Autowired
    private IUpdateEntityPropertyExecutor updateEntityPropertyExecutor;

    @Autowired
    private ICreateAttachmentExecutor createAttachmentExecutor;

    @Autowired
    private IAddTagToEntityExecutor addTagToEntityExecutor;

    @SuppressWarnings("unused")
    private CreateExperimentExecutor()
    {
    }

    public CreateExperimentExecutor(IDAOFactory daoFactory, IGetProjectByIdExecutor getProjectByIdExecutor,
            IGetEntityTypeByIdExecutor getEntityTypeByIdExecutor, IUpdateEntityPropertyExecutor updateEntityPropertyExecutor,
            ICreateAttachmentExecutor createAttachmentExecutor, IAddTagToEntityExecutor addTagToEntityExecutor)
    {
        this.daoFactory = daoFactory;
        this.getProjectByIdExecutor = getProjectByIdExecutor;
        this.getEntityTypeByIdExecutor = getEntityTypeByIdExecutor;
        this.updateEntityPropertyExecutor = updateEntityPropertyExecutor;
        this.createAttachmentExecutor = createAttachmentExecutor;
        this.addTagToEntityExecutor = addTagToEntityExecutor;
    }

    @Override
    public List<ExperimentPermId> create(IOperationContext context, List<ExperimentCreation> creations)
    {
        List<ExperimentPermId> result = new LinkedList<ExperimentPermId>();

        for (ExperimentCreation creation : creations)
        {
            context.pushContextDescription(ExperimentContextDescription.creating(creation));

            ExperimentPE experiment = createExperimentPE(context, creation);
            daoFactory.getExperimentDAO().createOrUpdateExperiment(experiment, context.getSession().tryGetPerson());
            createAttachmentExecutor.create(context, experiment, creation.getAttachments());
            addTagToEntityExecutor.add(context, experiment, creation.getTagIds());
            result.add(new ExperimentPermId(experiment.getPermId()));

            context.popContextDescription();
        }

        return result;
    }

    private ExperimentPE createExperimentPE(IOperationContext context, ExperimentCreation experimentCreation)
    {
        ExperimentPE experiment = new ExperimentPE();

        if (StringUtils.isEmpty(experimentCreation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }
        if (experimentCreation.getTypeId() == null)
        {
            throw new UserFailureException("Type id cannot be null.");
        }
        if (experimentCreation.getProjectId() == null)
        {
            throw new UserFailureException("Project id cannot be null.");
        }

        ExperimentIdentifierFactory.assertValidCode(experimentCreation.getCode());
        experiment.setCode(experimentCreation.getCode());
        experiment.setRegistrator(context.getSession().tryGetPerson());

        IEntityTypeId typeId = experimentCreation.getTypeId();
        EntityTypePE entityType = getEntityTypeByIdExecutor.get(context, EntityKind.EXPERIMENT, typeId);
        experiment.setExperimentType((ExperimentTypePE) entityType);

        updateEntityPropertyExecutor.update(context, experiment, entityType, experimentCreation.getProperties());

        ProjectPE project = getProjectByIdExecutor.get(context, experimentCreation.getProjectId());

        if (false == new ProjectByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), project))
        {
            throw new UnauthorizedObjectAccessException(experimentCreation.getProjectId());
        }

        experiment.setProject(project);

        String createdPermId = daoFactory.getPermIdDAO().createPermId();
        experiment.setPermId(createdPermId);

        RelationshipUtils.updateModificationDateAndModifier(experiment, context.getSession().tryGetPerson());

        return experiment;
    }

}
