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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.ProjectTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.SampleTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SpaceTranslator extends AbstractCachingTranslator<SpacePE, Space, SpaceFetchOptions> implements ISpaceTranslator
{

    @Autowired
    private PersonTranslator personTranslator;

    @Autowired
    private ProjectTranslator projectTranslator;

    @Autowired
    private SampleTranslator sampleTranslator;

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
        result.setFetchOptions(new SpaceFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(TranslationContext context, SpacePE space, Space result, Relations relations, SpaceFetchOptions fetchOptions)
    {
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
            Map<SamplePE, Sample> samples = sampleTranslator.translate(context, space.getSamples(), fetchOptions.withSamples());
            result.setSamples(new ArrayList<Sample>(samples.values()));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }
    }
}
