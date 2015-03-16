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

import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.common.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.ProjectTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.SampleTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class SpaceTranslator extends AbstractCachingTranslator<SpacePE, Space, SpaceFetchOptions>
{
    public SpaceTranslator(TranslationContext translationContext, SpaceFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected boolean shouldTranslate(SpacePE input)
    {
        return new SimpleSpaceValidator().doValidation(getTranslationContext().getSession().tryGetPerson(), input);
    }

    @Override
    protected Space createObject(SpacePE space)
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
    protected void updateObject(SpacePE space, Space result, Relations relations)
    {
        if (getFetchOptions().hasRegistrator())
        {
            result.setRegistrator(new PersonTranslator(getTranslationContext(), getFetchOptions().withRegistrator()).translate(space
                    .getRegistrator()));
            result.getFetchOptions().withRegistratorUsing(getFetchOptions().withRegistrator());
        }

        if (getFetchOptions().hasProjects())
        {
            List<Project> projects =
                    new ListTranslator().translate(space.getProjects(), new ProjectTranslator(getTranslationContext(),
                            getFetchOptions().withProjects()));
            result.setProjects(projects);
            result.getFetchOptions().withProjectsUsing(getFetchOptions().withProjects());
        }

        if (getFetchOptions().hasSamples())
        {
            List<Sample> samples =
                    new ListTranslator().translate(space.getSamples(), new SampleTranslator(getTranslationContext(),
                            getFetchOptions().withSamples()));
            result.setSamples(samples);
            result.getFetchOptions().withSamplesUsing(getFetchOptions().withSamples());
        }
    }
}
