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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.sql;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;

/**
 * @author pkupczyk
 */
@Component
public class SpaceSqlTranslator extends AbstractCachingTranslator<Long, Space, SpaceFetchOptions> implements ISpaceSqlTranslator
{

    @Autowired
    private ISpaceAuthorizationSqlValidator authorizationValidator;

    @Autowired
    private ISpaceRegistratorSqlTranslator registratorTranslator;

    @Autowired
    private ISpaceProjectSqlTranslator projectTranslator;

    @Autowired
    private ISpaceSampleSqlTranslator sampleTranslator;

    @Autowired
    private ISpaceBaseSqlTranslator baseTranslator;

    @Override
    protected Collection<Long> shouldTranslate(TranslationContext context, Collection<Long> spaceIds, SpaceFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), spaceIds);
    }

    @Override
    protected Space createObject(TranslationContext context, Long spaceId, SpaceFetchOptions fetchOptions)
    {
        Space space = new Space();
        space.setFetchOptions(new SpaceFetchOptions());
        return space;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> spaceIds, SpaceFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ISpaceBaseSqlTranslator.class, baseTranslator.translate(context, spaceIds, null));

        if (fetchOptions.hasProjects())
        {
            relations.put(ISpaceProjectSqlTranslator.class, projectTranslator.translate(context, spaceIds, fetchOptions.withProjects()));
        }

        if (fetchOptions.hasSamples())
        {
            relations.put(ISpaceSampleSqlTranslator.class, sampleTranslator.translate(context, spaceIds, fetchOptions.withSamples()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(ISpaceRegistratorSqlTranslator.class, registratorTranslator.translate(context, spaceIds, fetchOptions.withRegistrator()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long spaceId, Space result, Object objectRelations, SpaceFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        SpaceBaseRecord baseRecord = relations.get(ISpaceBaseSqlTranslator.class, spaceId);

        result.setCode(baseRecord.code);
        result.setPermId(new SpacePermId(baseRecord.code));
        result.setDescription(baseRecord.description);
        // TODO: add modification date to spaces table
        result.setModificationDate(baseRecord.registrationDate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasProjects())
        {
            result.setProjects((List<Project>) relations.get(ISpaceProjectSqlTranslator.class, spaceId));
            result.getFetchOptions().withProjectsUsing(fetchOptions.withProjects());
        }

        if (fetchOptions.hasSamples())
        {
            result.setSamples((List<Sample>) relations.get(ISpaceSampleSqlTranslator.class, spaceId));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(ISpaceRegistratorSqlTranslator.class, spaceId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

    }

}
