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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.experiment.sql;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentSqlTranslator extends AbstractCachingTranslator<Long, Experiment, ExperimentFetchOptions> implements IExperimentSqlTranslator
{

    @Autowired
    private IExperimentAuthorizationSqlValidator authorizationValidator;

    @Autowired
    private IExperimentBaseSqlTranslator baseTranslator;

    @Autowired
    private IExperimentTypeRelationSqlTranslator typeTranslator;

    @Autowired
    private IExperimentProjectSqlTranslator projectTranslator;

    @Autowired
    private IExperimentSampleSqlTranslator sampleTranslator;

    @Autowired
    private IExperimentDataSetSqlTranslator dataSetTranslator;

    @Autowired
    private IExperimentPropertySqlTranslator propertyTranslator;

    @Autowired
    private IExperimentMaterialPropertySqlTranslator materialPropertyTranslator;

    @Autowired
    private IExperimentRegistratorSqlTranslator registratorTranslator;

    @Autowired
    private IExperimentModifierSqlTranslator modifierTranslator;

    @Autowired
    private IExperimentTagSqlTranslator tagTranslator;

    @Autowired
    private IExperimentAttachmentSqlTranslator attachmentTranslator;

    @Autowired
    private IExperimentHistorySqlTranslator historyTranslator;

    @Override
    protected Collection<Long> shouldTranslate(TranslationContext context, Collection<Long> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), experimentIds);
    }

    @Override
    protected Experiment createObject(TranslationContext context, Long experimentId, ExperimentFetchOptions fetchOptions)
    {
        Experiment experiment = new Experiment();
        experiment.setFetchOptions(new ExperimentFetchOptions());
        return experiment;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> experimentIds, ExperimentFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IExperimentBaseSqlTranslator.class, baseTranslator.translate(context, experimentIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(IExperimentTypeRelationSqlTranslator.class, typeTranslator.translate(context, experimentIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProject())
        {
            relations.put(IExperimentProjectSqlTranslator.class, projectTranslator.translate(context, experimentIds, fetchOptions.withProject()));
        }

        if (fetchOptions.hasSamples())
        {
            relations.put(IExperimentSampleSqlTranslator.class, sampleTranslator.translate(context, experimentIds, fetchOptions.withSamples()));
        }

        if (fetchOptions.hasDataSets())
        {
            relations.put(IExperimentDataSetSqlTranslator.class, dataSetTranslator.translate(context, experimentIds, fetchOptions.withDataSets()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(IExperimentPropertySqlTranslator.class,
                    propertyTranslator.translate(context, experimentIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(IExperimentMaterialPropertySqlTranslator.class,
                    materialPropertyTranslator.translate(context, experimentIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IExperimentRegistratorSqlTranslator.class,
                    registratorTranslator.translate(context, experimentIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(IExperimentModifierSqlTranslator.class, modifierTranslator.translate(context, experimentIds, fetchOptions.withModifier()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(IExperimentTagSqlTranslator.class,
                    tagTranslator.translate(context, experimentIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasAttachments())
        {
            relations.put(IExperimentAttachmentSqlTranslator.class,
                    attachmentTranslator.translate(context, experimentIds, fetchOptions.withAttachments()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IExperimentHistorySqlTranslator.class, historyTranslator.translate(context, experimentIds, fetchOptions.withHistory()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long experimentId, Experiment result, Object objectRelations,
            ExperimentFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        ExperimentBaseRecord baseRecord = relations.get(IExperimentBaseSqlTranslator.class, experimentId);

        result.setCode(baseRecord.code);
        result.setPermId(new ExperimentPermId(baseRecord.permId));
        result.setIdentifier(new ExperimentIdentifier(baseRecord.spaceCode, baseRecord.projectCode, baseRecord.code));
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setModificationDate(baseRecord.modificationDate);

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(IExperimentTypeRelationSqlTranslator.class, experimentId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProject())
        {
            result.setProject(relations.get(IExperimentProjectSqlTranslator.class, experimentId));
            result.getFetchOptions().withProjectUsing(fetchOptions.withProject());
        }

        if (fetchOptions.hasSamples())
        {
            result.setSamples((List<Sample>) relations.get(IExperimentSampleSqlTranslator.class, experimentId));
            result.getFetchOptions().withSamplesUsing(fetchOptions.withSamples());
        }

        if (fetchOptions.hasDataSets())
        {
            result.setDataSets((List<DataSet>) relations.get(IExperimentDataSetSqlTranslator.class, experimentId));
            result.getFetchOptions().withDataSetsUsing(fetchOptions.withDataSets());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(IExperimentPropertySqlTranslator.class, experimentId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(IExperimentMaterialPropertySqlTranslator.class, experimentId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IExperimentRegistratorSqlTranslator.class, experimentId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(IExperimentModifierSqlTranslator.class, experimentId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(IExperimentTagSqlTranslator.class, experimentId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasAttachments())
        {
            result.setAttachments((List<Attachment>) relations.get(IExperimentAttachmentSqlTranslator.class, experimentId));
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IExperimentHistorySqlTranslator.class, experimentId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }
    }

}
