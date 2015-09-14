/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.sql;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;

/**
 * @author pkupczyk
 */
@Component
public class ProjectSqlTranslator extends AbstractCachingTranslator<Long, Project, ProjectFetchOptions> implements IProjectSqlTranslator
{

    @Autowired
    private IProjectAuthorizationSqlValidator authorizationValidator;

    @Autowired
    private IProjectBaseSqlTranslator baseTranslator;

    @Autowired
    private IProjectSpaceSqlTranslator spaceTranslator;

    @Autowired
    private IProjectExperimentSqlTranslator experimentTranslator;

    @Autowired
    private IProjectRegistratorSqlTranslator registratorTranslator;

    @Autowired
    private IProjectModifierSqlTranslator modifierTranslator;

    @Autowired
    private IProjectLeaderSqlTranslator leaderTranslator;

    @Autowired
    private IProjectAttachmentSqlTranslator attachmentTranslator;

    @Autowired
    private IProjectHistorySqlTranslator historyTranslator;

    @Override
    protected Collection<Long> shouldTranslate(TranslationContext context, Collection<Long> projectIds, ProjectFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), projectIds);
    }

    @Override
    protected Project createObject(TranslationContext context, Long projectId, ProjectFetchOptions fetchOptions)
    {
        Project project = new Project();
        project.setFetchOptions(new ProjectFetchOptions());
        return project;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> projectIds, ProjectFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IProjectBaseSqlTranslator.class, baseTranslator.translate(context, projectIds, null));

        if (fetchOptions.hasSpace())
        {
            relations.put(IProjectSpaceSqlTranslator.class, spaceTranslator.translate(context, projectIds, fetchOptions.withSpace()));
        }

        if (fetchOptions.hasExperiments())
        {
            relations.put(IProjectExperimentSqlTranslator.class, experimentTranslator.translate(context, projectIds, fetchOptions.withExperiments()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IProjectRegistratorSqlTranslator.class,
                    registratorTranslator.translate(context, projectIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(IProjectModifierSqlTranslator.class, modifierTranslator.translate(context, projectIds, fetchOptions.withModifier()));
        }

        if (fetchOptions.hasLeader())
        {
            relations.put(IProjectLeaderSqlTranslator.class, leaderTranslator.translate(context, projectIds, fetchOptions.withLeader()));
        }

        if (fetchOptions.hasAttachments())
        {
            relations.put(IProjectAttachmentSqlTranslator.class, attachmentTranslator.translate(context, projectIds, fetchOptions.withAttachments()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IProjectHistorySqlTranslator.class, historyTranslator.translate(context, projectIds, fetchOptions.withHistory()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long projectId, Project result, Object objectRelations, ProjectFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ProjectBaseRecord baseRecord = relations.get(IProjectBaseSqlTranslator.class, projectId);

        result.setCode(baseRecord.code);
        result.setPermId(new ProjectPermId(baseRecord.permId));
        result.setIdentifier(new ProjectIdentifier(baseRecord.spaceCode, baseRecord.code));
        result.setDescription(baseRecord.description);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setModificationDate(baseRecord.modificationDate);

        if (fetchOptions.hasSpace())
        {
            result.setSpace(relations.get(IProjectSpaceSqlTranslator.class, projectId));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }

        if (fetchOptions.hasExperiments())
        {
            result.setExperiments((List<Experiment>) relations.get(IProjectExperimentSqlTranslator.class, projectId));
            result.getFetchOptions().withExperimentsUsing(fetchOptions.withExperiments());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IProjectRegistratorSqlTranslator.class, projectId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(IProjectModifierSqlTranslator.class, projectId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

        if (fetchOptions.hasLeader())
        {
            result.setLeader(relations.get(IProjectLeaderSqlTranslator.class, projectId));
            result.getFetchOptions().withLeaderUsing(fetchOptions.withLeader());
        }

        if (fetchOptions.hasAttachments())
        {
            result.setAttachments((List<Attachment>) relations.get(IProjectAttachmentSqlTranslator.class, projectId));
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IProjectHistorySqlTranslator.class, projectId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

    }

}
