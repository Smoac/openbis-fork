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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment;

import java.util.ArrayList;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.common.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.common.SetTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.AttachmentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.DataSetTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.MaterialPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.PersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.ProjectTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.SampleTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.TagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * @author pkupczyk
 */
public class ExperimentTranslator extends AbstractCachingTranslator<ExperimentPE, Experiment, ExperimentFetchOptions>
{

    public ExperimentTranslator(TranslationContext translationContext, ExperimentFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
    }

    @Override
    protected boolean shouldTranslate(ExperimentPE input)
    {
        return new ExperimentByIdentiferValidator().doValidation(getTranslationContext().getSession().tryGetPerson(), input);
    }

    @Override
    protected Experiment createObject(ExperimentPE experiment)
    {
        Experiment result = new Experiment();

        result.setCode(experiment.getCode());
        result.setPermId(new ExperimentPermId(experiment.getPermId()));
        result.setIdentifier(new ExperimentIdentifier(experiment.getIdentifier()));
        result.setRegistrationDate(experiment.getRegistrationDate());
        result.setModificationDate(experiment.getModificationDate());
        result.setFetchOptions(new ExperimentFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(ExperimentPE experiment, Experiment result, Relations relations)
    {
        if (getFetchOptions().hasType())
        {
            ExperimentType type =
                    new ExperimentTypeTranslator(getTranslationContext(), getFetchOptions().withType()).translate(experiment.getExperimentType());
            result.setType(type);
            result.getFetchOptions().withTypeUsing(getFetchOptions().withType());
        }

        if (getFetchOptions().hasProperties())
        {
            Map<String, String> properties =
                    new PropertyTranslator(getTranslationContext(), getFetchOptions().withProperties())
                            .translate(experiment);
            result.setProperties(properties);
            result.getFetchOptions().withPropertiesUsing(getFetchOptions().withProperties());
        }

        if (getFetchOptions().hasMaterialProperties())
        {
            result.setMaterialProperties(new MaterialPropertyTranslator(getTranslationContext(), getFetchOptions().withMaterialProperties())
                    .translate(experiment));
            result.getFetchOptions().withMaterialPropertiesUsing(getFetchOptions().withMaterialProperties());
        }

        if (getFetchOptions().hasProject())
        {
            result.setProject(new ProjectTranslator(getTranslationContext(), getFetchOptions().withProject())
                    .translate(experiment.getProject()));
            result.getFetchOptions().withProjectUsing(getFetchOptions().withProject());
        }

        if (getFetchOptions().hasSamples())
        {
            result.setSamples(new ListTranslator().translate(experiment.getSamples(), new SampleTranslator(getTranslationContext(),
                    getFetchOptions().withSamples())));
            result.getFetchOptions().withSamplesUsing(getFetchOptions().withSamples());
        }

        if (getFetchOptions().hasDataSets())
        {
            result.setDataSets(new ListTranslator().translate(experiment.getDataSets(), new DataSetTranslator(getTranslationContext(),
                    getFetchOptions().withDataSets())));
            result.getFetchOptions().withDataSetsUsing(getFetchOptions().withDataSets());
        }

        if (getFetchOptions().hasRegistrator())
        {
            result.setRegistrator(new PersonTranslator(getTranslationContext(), getFetchOptions().withRegistrator()).translate(experiment
                    .getRegistrator()));
            result.getFetchOptions().withRegistratorUsing(getFetchOptions().withRegistrator());
        }

        if (getFetchOptions().hasModifier())
        {
            result.setModifier(new PersonTranslator(getTranslationContext(), getFetchOptions().withModifier()).translate(experiment
                    .getModifier()));
            result.getFetchOptions().withModifierUsing(getFetchOptions().withModifier());
        }

        if (getFetchOptions().hasTags())
        {
            result.setTags(new SetTranslator().translate(experiment.getMetaprojects(), new TagTranslator(getTranslationContext(), getFetchOptions()
                    .withTags())));
            result.getFetchOptions().withTagsUsing(getFetchOptions().withTags());
        }

        if (getFetchOptions().hasAttachments())
        {
            ArrayList<Attachment> attachments =
                    AttachmentTranslator.translate(getTranslationContext(), experiment, getFetchOptions().withAttachments());
            result.setAttachments(attachments);
            result.getFetchOptions().withAttachmentsUsing(getFetchOptions().withAttachments());
        }

    }
}
