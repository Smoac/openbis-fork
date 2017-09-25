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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class DataSetTranslator extends AbstractCachingTranslator<Long, DataSet, DataSetFetchOptions> implements IDataSetTranslator
{

    @Autowired
    private IDataSetAuthorizationValidator authorizationValidator;

    @Autowired
    private IDataSetBaseTranslator baseTranslator;

    @Autowired
    private IDataSetTypeRelationTranslator typeTranslator;

    @Autowired
    private IDataSetPropertyTranslator propertyTranslator;

    @Autowired
    private IDataSetMaterialPropertyTranslator materialPropertyTranslator;

    @Autowired
    private IDataSetPhysicalDataTranslator physicalDataTranslator;

    @Autowired
    private IDataSetLinkedDataTranslator linkedDataTranslator;

    @Autowired
    private IDataSetDataStoreTranslator dataStoreTranslator;

    @Autowired
    private IDataSetSampleTranslator sampleTranslator;

    @Autowired
    private IDataSetExperimentTranslator experimentTranslator;

    @Autowired
    private IDataSetParentTranslator parentTranslator;

    @Autowired
    private IDataSetChildTranslator childTranslator;

    @Autowired
    private IDataSetContainerTranslator containerTranslator;

    @Autowired
    private IDataSetComponentsTranslator componentsTranslator;

    @Autowired
    private IDataSetTagTranslator tagTranslator;

    @Autowired
    private IDataSetHistoryTranslator historyTranslator;

    @Autowired
    private IDataSetRegistratorTranslator registratorTranslator;

    @Autowired
    private IDataSetModifierTranslator modifierTranslator;

    @Autowired
    private IDataSetPostRegisteredTranslator postRegisteredTranslator;

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

        relations.put(IDataSetBaseTranslator.class, baseTranslator.translate(context, dataSetIds, null));
        relations.put(IDataSetPostRegisteredTranslator.class, postRegisteredTranslator.translate(context, dataSetIds, null));

        if (fetchOptions.hasType())
        {
            relations.put(IDataSetTypeRelationTranslator.class, typeTranslator.translate(context, dataSetIds, fetchOptions.withType()));
        }

        if (fetchOptions.hasProperties())
        {
            relations.put(IDataSetPropertyTranslator.class, propertyTranslator.translate(context, dataSetIds, fetchOptions.withProperties()));
        }

        if (fetchOptions.hasMaterialProperties())
        {
            relations.put(IDataSetMaterialPropertyTranslator.class,
                    materialPropertyTranslator.translate(context, dataSetIds, fetchOptions.withMaterialProperties()));
        }

        if (fetchOptions.hasPhysicalData())
        {
            relations.put(IDataSetPhysicalDataTranslator.class,
                    physicalDataTranslator.translate(context, dataSetIds, fetchOptions.withPhysicalData()));
        }

        if (fetchOptions.hasLinkedData())
        {
            relations.put(IDataSetLinkedDataTranslator.class,
                    linkedDataTranslator.translate(context, dataSetIds, fetchOptions.withLinkedData()));
        }

        if (fetchOptions.hasDataStore())
        {
            relations.put(IDataSetDataStoreTranslator.class, dataStoreTranslator.translate(context, dataSetIds, fetchOptions.withDataStore()));
        }

        if (fetchOptions.hasSample())
        {
            relations.put(IDataSetSampleTranslator.class, sampleTranslator.translate(context, dataSetIds, fetchOptions.withSample()));
        }

        if (fetchOptions.hasExperiment())
        {
            relations.put(IDataSetExperimentTranslator.class, experimentTranslator.translate(context, dataSetIds, fetchOptions.withExperiment()));
        }

        if (fetchOptions.hasContainers())
        {
            relations.put(IDataSetContainerTranslator.class, containerTranslator.translate(context, dataSetIds, fetchOptions.withContainers()));
        }

        if (fetchOptions.hasComponents())
        {
            relations.put(IDataSetComponentsTranslator.class, componentsTranslator.translate(context, dataSetIds, fetchOptions.withComponents()));
        }

        if (fetchOptions.hasParents())
        {
            relations.put(IDataSetParentTranslator.class, parentTranslator.translate(context, dataSetIds, fetchOptions.withParents()));
        }

        if (fetchOptions.hasChildren())
        {
            relations.put(IDataSetChildTranslator.class, childTranslator.translate(context, dataSetIds, fetchOptions.withChildren()));
        }

        if (fetchOptions.hasTags())
        {
            relations.put(IDataSetTagTranslator.class, tagTranslator.translate(context, dataSetIds, fetchOptions.withTags()));
        }

        if (fetchOptions.hasHistory())
        {
            relations.put(IDataSetHistoryTranslator.class, historyTranslator.translate(context, dataSetIds, fetchOptions.withHistory()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations
                    .put(IDataSetRegistratorTranslator.class, registratorTranslator.translate(context, dataSetIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasModifier())
        {
            relations.put(IDataSetModifierTranslator.class, modifierTranslator.translate(context, dataSetIds, fetchOptions.withModifier()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long dataSetId, DataSet result, Object objectRelations, DataSetFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        DataSetBaseRecord baseRecord = relations.get(IDataSetBaseTranslator.class, dataSetId);

        result.setPermId(new DataSetPermId(baseRecord.code));
        result.setCode(baseRecord.code);
        result.setMeasured(baseRecord.isDerived == false);
        result.setDataProducer(baseRecord.dataProducer);
        result.setDataProductionDate(baseRecord.dataProductionDate);
        result.setAccessDate(baseRecord.accessDate);
        result.setModificationDate(baseRecord.modificationDate);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setPostRegistered(relations.get(IDataSetPostRegisteredTranslator.class, dataSetId));
        result.setKind(DataSetKind.valueOf(baseRecord.dataSetKind));

        if (fetchOptions.hasType())
        {
            result.setType(relations.get(IDataSetTypeRelationTranslator.class, dataSetId));
            result.getFetchOptions().withTypeUsing(fetchOptions.withType());
        }

        if (fetchOptions.hasProperties())
        {
            result.setProperties(relations.get(IDataSetPropertyTranslator.class, dataSetId));
            result.getFetchOptions().withPropertiesUsing(fetchOptions.withProperties());
        }

        if (fetchOptions.hasMaterialProperties())
        {
            result.setMaterialProperties(relations.get(IDataSetMaterialPropertyTranslator.class, dataSetId));
            result.getFetchOptions().withMaterialPropertiesUsing(fetchOptions.withMaterialProperties());
        }

        if (fetchOptions.hasPhysicalData())
        {
            result.setPhysicalData(relations.get(IDataSetPhysicalDataTranslator.class, dataSetId));
            result.getFetchOptions().withPhysicalDataUsing(fetchOptions.withPhysicalData());
        }

        if (fetchOptions.hasLinkedData())
        {
            result.setLinkedData(relations.get(IDataSetLinkedDataTranslator.class, dataSetId));
            result.getFetchOptions().withLinkedDataUsing(fetchOptions.withLinkedData());
        }

        if (fetchOptions.hasDataStore())
        {
            result.setDataStore(relations.get(IDataSetDataStoreTranslator.class, dataSetId));
            result.getFetchOptions().withDataStoreUsing(fetchOptions.withDataStore());
        }

        if (fetchOptions.hasSample())
        {
            result.setSample(relations.get(IDataSetSampleTranslator.class, dataSetId));
            result.getFetchOptions().withSampleUsing(fetchOptions.withSample());
        }

        if (fetchOptions.hasExperiment())
        {
            result.setExperiment(relations.get(IDataSetExperimentTranslator.class, dataSetId));
            result.getFetchOptions().withExperimentUsing(fetchOptions.withExperiment());
        }

        if (fetchOptions.hasContainers())
        {
            result.setContainers((List<DataSet>) relations.get(IDataSetContainerTranslator.class, dataSetId));
            result.getFetchOptions().withContainersUsing(fetchOptions.withContainers());
        }

        if (fetchOptions.hasComponents())
        {
            result.setComponents((List<DataSet>) relations.get(IDataSetComponentsTranslator.class, dataSetId));
            result.getFetchOptions().withComponentsUsing(fetchOptions.withComponents());
        }

        if (fetchOptions.hasParents())
        {
            result.setParents((List<DataSet>) relations.get(IDataSetParentTranslator.class, dataSetId));
            result.getFetchOptions().withParentsUsing(fetchOptions.withParents());
        }

        if (fetchOptions.hasChildren())
        {
            result.setChildren((List<DataSet>) relations.get(IDataSetChildTranslator.class, dataSetId));
            result.getFetchOptions().withChildrenUsing(fetchOptions.withChildren());
        }

        if (fetchOptions.hasTags())
        {
            result.setTags((Set<Tag>) relations.get(IDataSetTagTranslator.class, dataSetId));
            result.getFetchOptions().withTagsUsing(fetchOptions.withTags());
        }

        if (fetchOptions.hasHistory())
        {
            result.setHistory(relations.get(IDataSetHistoryTranslator.class, dataSetId));
            result.getFetchOptions().withHistoryUsing(fetchOptions.withHistory());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IDataSetRegistratorTranslator.class, dataSetId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            result.setModifier(relations.get(IDataSetModifierTranslator.class, dataSetId));
            result.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }

    }

}
