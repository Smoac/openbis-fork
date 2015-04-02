/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.IAttachmentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.IExperimentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.ISpaceTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ProjectByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
@Component
public class ProjectTranslator extends AbstractCachingTranslator<ProjectPE, Project, ProjectFetchOptions> implements IProjectTranslator
{

    @Autowired
    private ISpaceTranslator spaceTranslator;

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private IExperimentTranslator experimentTranslator;

    @Autowired
    private IAttachmentTranslator attachmentTranslator;

    @Override
    protected boolean shouldTranslate(TranslationContext context, ProjectPE input, ProjectFetchOptions fetchOptions)
    {
        return new ProjectByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), input);
    }

    @Override
    protected Project createObject(TranslationContext context, ProjectPE project, ProjectFetchOptions fetchOptions)
    {
        Project result = new Project();

        result.setCode(project.getCode());
        result.setPermId(new ProjectPermId(project.getPermId()));
        result.setIdentifier(new ProjectIdentifier(project.getIdentifier()));
        result.setDescription(project.getDescription());
        result.setRegistrationDate(project.getRegistrationDate());
        result.setModificationDate(project.getModificationDate());
        result.setFetchOptions(new ProjectFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(TranslationContext context, ProjectPE project, Project result, Relations relations, ProjectFetchOptions fetchOptions)
    {
        if (fetchOptions.hasSpace())
        {
            result.setSpace(spaceTranslator.translate(context, project.getSpace(), fetchOptions.withSpace()));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(personTranslator.translate(context, project.getRegistrator(), fetchOptions.withRegistrator()));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(personTranslator.translate(context, project.getModifier(), fetchOptions.withModifier()));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

        if (fetchOptions.hasLeader())
        {
            result.setLeader(personTranslator.translate(context, project.getProjectLeader(), fetchOptions.withLeader()));
            result.getFetchOptions().withLeaderUsing(fetchOptions.withLeader());
        }

        if (fetchOptions.hasExperiments())
        {
            Map<ExperimentPE, Experiment> experiments =
                    experimentTranslator.translate(context, project.getExperiments(), fetchOptions.withExperiments());
            result.setExperiments(new ArrayList<Experiment>(experiments.values()));
            result.getFetchOptions().withExperimentsUsing(fetchOptions.withExperiments());
        }

        if (fetchOptions.hasAttachments())
        {
            List<Attachment> attachments = attachmentTranslator.translate(context, project, fetchOptions.withAttachments());
            result.setAttachments(attachments);
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

    }
}
