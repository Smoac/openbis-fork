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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.sql;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;

/**
 * @author pkupczyk
 */
@Component
public class DataSetSqlTranslator extends AbstractCachingTranslator<Long, DataSet, DataSetFetchOptions> implements IDataSetSqlTranslator
{

    @Autowired
    private IDataSetAuthorizationSqlValidator authorizationValidator;

    @Autowired
    private IDataSetBaseSqlTranslator baseTranslator;

    @Autowired
    private IDataSetTypeRelationSqlTranslator typeTranslator;

    @Autowired
    private IDataSetPropertySqlTranslator propertyTranslator;

    @Autowired
    private IDataSetMaterialPropertySqlTranslator materialPropertyTranslator;

    @Autowired
    private IDataSetExternalDataSqlTranslator externalDataTranslator;

    @Autowired
    private IDataSetSampleSqlTranslator sampleTranslator;

    @Autowired
    private IDataSetExperimentSqlTranslator experimentTranslator;

    @Autowired
    private IDataSetParentSqlTranslator parentTranslator;

    @Autowired
    private IDataSetChildSqlTranslator childTranslator;

    @Autowired
    private IDataSetContainerSqlTranslator containerTranslator;

    @Autowired
    private IDataSetContainedSqlTranslator containedTranslator;

    @Autowired
    private IDataSetTagSqlTranslator tagTranslator;

    @Autowired
    private IDataSetHistorySqlTranslator historyTranslator;

    @Autowired
    private IDataSetRegistratorSqlTranslator registratorTranslator;

    @Autowired
    private IDataSetModifierSqlTranslator modifierTranslator;

    @Autowired
    private IDataSetPostRegisteredSqlTranslator postRegisteredTranslator;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        return authorizationValidator.validate(context.getSession().tryGetPerson(), dataSetIds);
    }

    @Override
    protected DataSet createObject(TranslationContext context, Long dataSetId, DataSetFetchOptions fetchOptions)
    {
        final DataSet dataSet = new DataSet();
        dataSet.setFetchOptions(new DataSetFetchOptions());
        return dataSet;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> dataSetIds, DataSetFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IDataSetBaseSqlTranslator.class, baseTranslator.translate(context, dataSetIds, null));
        relations.put(IDataSetPostRegisteredSqlTranslator.class, postRegisteredTranslator.translate(context, dataSetIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(IDataSetTypeRelationSqlTranslator.class, typeTranslator.translate(context, dataSetIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(IDataSetPropertySqlTranslator.class, propertyTranslator.translate(context, dataSetIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(IDataSetMaterialPropertySqlTranslator.class,
                    materialPropertyTranslator.translate(context, dataSetIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasExternalData())
        {
            relations.put(IDataSetExternalDataSqlTranslator.class,
                    externalDataTranslator.translate(context, dataSetIds, fetchOptions.withExternalData()));
        }

        if (fetchOptions.hasSample())
        {
            relations.put(IDataSetSampleSqlTranslator.class, sampleTranslator.translate(context, dataSetIds, fetchOptions.withSample()));
        }

        if (fetchOptions.hasExperiment())
        {
            relations.put(IDataSetExperimentSqlTranslator.class, experimentTranslator.translate(context, dataSetIds, fetchOptions.withExperiment()));
        }

        if (fetchOptions.hasContainers())
        {
            relations.put(IDataSetContainerSqlTranslator.class, containerTranslator.translate(context, dataSetIds, fetchOptions.withContainers()));
        }

        if (fetchOptions.hasContained())
        {
            relations.put(IDataSetContainedSqlTranslator.class, containedTranslator.translate(context, dataSetIds, fetchOptions.withContained()));
        }

        if (fetchOptions.hasParents())
        {
            relations.put(IDataSetParentSqlTranslator.class, parentTranslator.translate(context, dataSetIds, fetchOptions.withParents()));
        }

        if (fetchOptions.hasChildren())
        {
            relations.put(IDataSetChildSqlTranslator.class, childTranslator.translate(context, dataSetIds, fetchOptions.withChildren()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(IDataSetTagSqlTranslator.class, tagTranslator.translate(context, dataSetIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IDataSetHistorySqlTranslator.class, historyTranslator.translate(context, dataSetIds, fetchOptions.withHistory()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations
                    .put(IDataSetRegistratorSqlTranslator.class, registratorTranslator.translate(context, dataSetIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(IDataSetModifierSqlTranslator.class, modifierTranslator.translate(context, dataSetIds, fetchOptions.withModifier()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long dataSetId, DataSet result, Object objectRelations, DataSetFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        DataSetBaseRecord baseRecord = relations.get(IDataSetBaseSqlTranslator.class, dataSetId);

        result.setPermId(new DataSetPermId(baseRecord.code));
        result.setCode(baseRecord.code);
        result.setDerived(baseRecord.isDerived);
        result.setPlaceholder(baseRecord.isPlaceholder);
        result.setAccessDate(baseRecord.accessDate);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setPostRegistered(relations.get(IDataSetPostRegisteredSqlTranslator.class, dataSetId));

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(IDataSetTypeRelationSqlTranslator.class, dataSetId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(IDataSetPropertySqlTranslator.class, dataSetId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(IDataSetMaterialPropertySqlTranslator.class, dataSetId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasExternalData())
        {
            result.setExternalData(relations.get(IDataSetExternalDataSqlTranslator.class, dataSetId));
            result.getFetchOptions().withExternalDataUsing(fetchOptions.withExternalData());
        }

        if (fetchOptions.hasSample())
        {
            result.setSample(relations.get(IDataSetSampleSqlTranslator.class, dataSetId));
            result.getFetchOptions().withSampleUsing(fetchOptions.withSample());
        }

        if (fetchOptions.hasExperiment())
        {
            result.setExperiment(relations.get(IDataSetExperimentSqlTranslator.class, dataSetId));
            result.getFetchOptions().withExperimentUsing(fetchOptions.withExperiment());
        }

        if (fetchOptions.hasContainers())
        {
            result.setContainers((List<DataSet>) relations.get(IDataSetContainerSqlTranslator.class, dataSetId));
            result.getFetchOptions().withContainersUsing(fetchOptions.withContainers());
        }

        if (fetchOptions.hasContained())
        {
            result.setContained((List<DataSet>) relations.get(IDataSetContainedSqlTranslator.class, dataSetId));
            result.getFetchOptions().withContainedUsing(fetchOptions.withContained());
        }

        if (fetchOptions.hasParents())
        {
            result.setParents((List<DataSet>) relations.get(IDataSetParentSqlTranslator.class, dataSetId));
            result.getFetchOptions().withParentsUsing(fetchOptions.withParents());
        }

        if (fetchOptions.hasChildren())
        {
            result.setChildren((List<DataSet>) relations.get(IDataSetChildSqlTranslator.class, dataSetId));
            result.getFetchOptions().withChildrenUsing(fetchOptions.withChildren());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(IDataSetTagSqlTranslator.class, dataSetId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IDataSetHistorySqlTranslator.class, dataSetId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IDataSetRegistratorSqlTranslator.class, dataSetId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(IDataSetModifierSqlTranslator.class, dataSetId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

    }

}
