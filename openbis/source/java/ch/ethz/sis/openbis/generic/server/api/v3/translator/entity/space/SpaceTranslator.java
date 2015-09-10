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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.IProjectTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.sql.ISpaceSampleSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SpaceTranslator extends AbstractCachingTranslator<SpacePE, Space, SpaceFetchOptions> implements ISpaceTranslator
{

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private IProjectTranslator projectTranslator;

    @Autowired
    private ISpaceSampleSqlTranslator sampleTranslator;

    @Override
    protected boolean shouldTranslate(TranslationContext context, SpacePE input, SpaceFetchOptions fetchOptions)
    {
        return new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), input);
    }

    @Override
    protected Space createObject(TranslationContext context, SpacePE space, SpaceFetchOptions fetchOptions)
    {
        Space result = new Space();

        result.setCode(space.getCode());
        result.setPermId(new SpacePermId(space.getCode()));
        result.setDescription(space.getDescription());
        result.setRegistrationDate(space.getRegistrationDate());
        result.setModificationDate(space.getRegistrationDate());
        result.setFetchOptions(new SpaceFetchOptions());

        return result;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<SpacePE> spaces, SpaceFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        Set<Long> spaceIds = new HashSet<Long>();
        for (SpacePE space : spaces)
        {
            spaceIds.add(space.getId());
        }

        if (fetchOptions.hasSamples())
        {
            relations.put(ISpaceSampleSqlTranslator.class, sampleTranslator.translate(context, spaceIds, fetchOptions.withSamples()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, SpacePE space, Space result, Object objectRelations, SpaceFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(personTranslator.translate(context, space.getRegistrator(), fetchOptions.withRegistrator()));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasProjects())
        {
            Map<ProjectPE, Project> projects = projectTranslator.translate(context, space.getProjects(), fetchOptions.withProjects());
            result.setProjects(new ArrayList<Project>(projects.values()));
            result.getFetchOptions().withProjectsUsing(fetchOptions.withProjects());
        }

        if (fetchOptions.hasSamples())
        {
            result.setSamples((List<Sample>) relations.get(ISpaceSampleSqlTranslator.class, space.getId()));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }
    }
}
