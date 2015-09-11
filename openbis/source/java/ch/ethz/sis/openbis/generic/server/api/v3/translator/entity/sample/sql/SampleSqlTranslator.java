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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.sample.sql;

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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SampleByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SampleSqlTranslator extends AbstractCachingTranslator<Long, Sample, SampleFetchOptions> implements ISampleSqlTranslator
{

    @Autowired
    private ISampleBaseSqlTranslator baseTranslator;

    @Autowired
    private ISampleTypeRelationSqlTranslator typeTranslator;

    @Autowired
    private ISampleSpaceSqlTranslator spaceTranslator;

    @Autowired
    private ISamplePropertySqlTranslator propertyTranslator;

    @Autowired
    private ISampleMaterialPropertySqlTranslator materialPropertyTranslator;

    @Autowired
    private ISampleExperimentSqlTranslator experimentTranslator;

    @Autowired
    private ISampleParentSqlTranslator parentTranslator;

    @Autowired
    private ISampleContainerSqlTranslator containerTranslator;

    @Autowired
    private ISampleContainedSqlTranslator containedTranslator;

    @Autowired
    private ISampleChildSqlTranslator childTranslator;

    @Autowired
    private ISampleDataSetSqlTranslator dataSetTranslator;

    @Autowired
    private ISampleTagSqlTranslator tagTranslator;

    @Autowired
    private ISampleAttachmentSqlTranslator attachmentTranslator;

    @Autowired
    private ISampleHistorySqlTranslator historyTranslator;

    @Autowired
    private ISampleRegistratorSqlTranslator registratorTranslator;

    @Autowired
    private ISampleModifierSqlTranslator modifierTranslator;

    @Autowired
    // TODO remove it
    private IDAOFactory daoFactory;

    @Override
    protected boolean shouldTranslate(TranslationContext context, Long sampleId, SampleFetchOptions fetchOptions)
    {
        // TODO replace with SQL impl
        SamplePE sample = daoFactory.getSampleDAO().getByTechId(new TechId(sampleId));
        return new SampleByIdentiferValidator().doValidation(context.getSession().tryGetPerson(), sample);
    }

    @Override
    protected Sample createObject(TranslationContext context, Long sampleId, SampleFetchOptions fetchOptions)
    {
        final Sample sample = new Sample();
        sample.setFetchOptions(new SampleFetchOptions());
        return sample;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> sampleIds, SampleFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ISampleBaseSqlTranslator.class, baseTranslator.translate(context, sampleIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(ISampleTypeRelationSqlTranslator.class, typeTranslator.translate(context, sampleIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasSpace())
        {
            relations.put(ISampleSpaceSqlTranslator.class, spaceTranslator.translate(context, sampleIds, fetchOptions.withSpace()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(ISamplePropertySqlTranslator.class, propertyTranslator.translate(context, sampleIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(ISampleMaterialPropertySqlTranslator.class,
                    materialPropertyTranslator.translate(context, sampleIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasExperiment())
        {
            relations.put(ISampleExperimentSqlTranslator.class, experimentTranslator.translate(context, sampleIds, fetchOptions.withExperiment()));
        }

        if (fetchOptions.hasContainer())
        {
            relations.put(ISampleContainerSqlTranslator.class, containerTranslator.translate(context, sampleIds, fetchOptions.withContainer()));
        }

        if (fetchOptions.hasContained())
        {
            relations.put(ISampleContainedSqlTranslator.class, containedTranslator.translate(context, sampleIds, fetchOptions.withContained()));
        }

        if (fetchOptions.hasParents())
        {
            relations.put(ISampleParentSqlTranslator.class, parentTranslator.translate(context, sampleIds, fetchOptions.withParents()));
        }

        if (fetchOptions.hasChildren())
        {
            relations.put(ISampleChildSqlTranslator.class, childTranslator.translate(context, sampleIds, fetchOptions.withChildren()));
        }

        if (fetchOptions.hasDataSets())
        {
            relations.put(ISampleDataSetSqlTranslator.class, dataSetTranslator.translate(context, sampleIds, fetchOptions.withDataSets()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(ISampleTagSqlTranslator.class, tagTranslator.translate(context, sampleIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasAttachments())
        {
            relations.put(ISampleAttachmentSqlTranslator.class, attachmentTranslator.translate(context, sampleIds, fetchOptions.withAttachments()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(ISampleHistorySqlTranslator.class, historyTranslator.translate(context, sampleIds, fetchOptions.withHistory()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(ISampleRegistratorSqlTranslator.class, registratorTranslator.translate(context, sampleIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(ISampleModifierSqlTranslator.class, modifierTranslator.translate(context, sampleIds, fetchOptions.withModifier()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long sampleId, Sample result, Object objectRelations, SampleFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        SampleBaseRecord baseRecord = relations.get(ISampleBaseSqlTranslator.class, sampleId);

        result.setPermId(new SamplePermId(baseRecord.permId));
        result.setCode(baseRecord.code);
        result.setIdentifier(new SampleIdentifier(baseRecord.spaceCode, baseRecord.containerCode, baseRecord.code));
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(ISampleTypeRelationSqlTranslator.class, sampleId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasSpace())
        {
            result.setSpace(relations.get(ISampleSpaceSqlTranslator.class, sampleId));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(ISamplePropertySqlTranslator.class, sampleId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(ISampleMaterialPropertySqlTranslator.class, sampleId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasExperiment())
        {
            result.setExperiment(relations.get(ISampleExperimentSqlTranslator.class, sampleId));
            result.getFetchOptions().withExperimentUsing(fetchOptions.withExperiment());
        }

        if (fetchOptions.hasContainer())
        {
            result.setContainer(relations.get(ISampleContainerSqlTranslator.class, sampleId));
            result.getFetchOptions().withContainerUsing(fetchOptions.withContainer());
        }

        if (fetchOptions.hasContained())
        {
            result.setContained((List<Sample>) relations.get(ISampleContainedSqlTranslator.class, sampleId));
            result.getFetchOptions().withContainedUsing(fetchOptions.withContained());
        }

        if (fetchOptions.hasParents())
        {
            result.setParents((List<Sample>) relations.get(ISampleParentSqlTranslator.class, sampleId));
            result.getFetchOptions().withParentsUsing(fetchOptions.withParents());
        }

        if (fetchOptions.hasChildren())
        {
            result.setChildren((List<Sample>) relations.get(ISampleChildSqlTranslator.class, sampleId));
            result.getFetchOptions().withChildrenUsing(fetchOptions.withChildren());
        }

        if (fetchOptions.hasDataSets())
        {
            result.setDataSets((List<DataSet>) relations.get(ISampleDataSetSqlTranslator.class, sampleId));
            result.getFetchOptions().withDataSetsUsing(fetchOptions.withDataSets());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(ISampleTagSqlTranslator.class, sampleId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasAttachments())
        {
            result.setAttachments((List<Attachment>) relations.get(ISampleAttachmentSqlTranslator.class, sampleId));
            result.getFetchOptions().withAttachmentsUsing(fetchOptions.withAttachments());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(ISampleHistorySqlTranslator.class, sampleId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(ISampleRegistratorSqlTranslator.class, sampleId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(ISampleModifierSqlTranslator.class, sampleId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

    }

}
