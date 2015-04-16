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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.attachment.IAttachmentTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.IDataSetTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.history.IHistoryTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.IMaterialPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.project.IProjectTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.IPropertyTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.ISampleTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.tag.ITagTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentTranslator extends AbstractCachingTranslator<ExperimentPE, Experiment, ExperimentFetchOptions> implements
        IExperimentTranslator
{

    @Autowired
    private IExperimentTypeTranslator typeTranslator;

    @Autowired
    private IProjectTranslator projectTranslator;

    @Autowired
    private ISampleTranslator sampleTranslator;

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private IAttachmentTranslator attachmentTranslator;

    @Autowired
    private IPropertyTranslator propertyTranslator;

    @Autowired
    private IMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private IDataSetTranslator dataSetTranslator;

    @Autowired
    private ITagTranslator tagTranslator;

    @Autowired
    private IHistoryTranslator historyTranslator;

    @Override
    protected boolean shouldTranslate(TranslationContext context, ExperimentPE input, ExperimentFetchOptions fetchOptions)
    {
        return new ExperimentByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), input);
    }

    @Override
    protected Experiment createObject(TranslationContext context, ExperimentPE experiment, ExperimentFetchOptions fetchOptions)
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
    protected void updateObject(TranslationContext context, ExperimentPE experiment, Experiment result, Relations relations,
            ExperimentFetchOptions fetchOptions)
    {
        if (fetchOptions.hasType())
        {
            result.setType(typeTranslator.translate(context, experiment.getExperimentType(), fetchOptions.withType()));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(propertyTranslator.translate(context, experiment, fetchOptions.withProperties()));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(materialPropertyTranslator.translate(context, experiment, fetchOptions.withMaterialProperties()));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasProject())
        {
            result.setProject(projectTranslator.translate(context, experiment.getProject(), fetchOptions.withProject()));
            result.getFetchOptions().withProjectUsing(fetchOptions.withProject());
        }

        if (fetchOptions.hasSamples())
        {
            Map<SamplePE, Sample> samples = sampleTranslator.translate(context, experiment.getSamples(), fetchOptions.withSamples());
            result.setSamples(new ArrayList<Sample>(samples.values()));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }

        if (fetchOptions.hasDataSets())
        {
            Map<DataPE, DataSet> dataSets = dataSetTranslator.translate(context, experiment.getDataSets(), fetchOptions.withDataSets());
            result.setDataSets(new ArrayList<DataSet>(dataSets.values()));
            result.getFetchOptions().withDataSetsUsing(fetchOptions.withDataSets());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(personTranslator.translate(context, experiment.getRegistrator(), fetchOptions.withRegistrator()));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(personTranslator.translate(context, experiment.getModifier(), fetchOptions.withModifier()));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

        if (fetchOptions.hasTags())
        {
            Map<MetaprojectPE, Tag> tags = tagTranslator.translate(context, experiment.getMetaprojects(), fetchOptions.withTags());
            result.setTags(new HashSet<Tag>(tags.values()));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasAttachments())
        {
            List<Attachment> attachments = attachmentTranslator.translate(context, experiment, fetchOptions.withAttachments());
            result.setAttachments(attachments);
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(historyTranslator.translate(context, experiment, fetchOptions.withHistory()));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }
    }
}
