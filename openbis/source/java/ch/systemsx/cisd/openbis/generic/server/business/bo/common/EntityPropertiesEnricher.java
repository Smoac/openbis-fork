/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermEntityProperty;

/**
 * A class that can enrich a set of entities with its entity properties.
 * 
 * @author Bernd Rinn
 */
public final class EntityPropertiesEnricher implements IEntityPropertiesEnricher
{
    private final IPropertyListingQuery query;

    private final IEntityPropertySetListingQuery propertySetQuery;

    public EntityPropertiesEnricher(final IPropertyListingQuery query,
            final IEntityPropertySetListingQuery setQuery)
    {
        this.query = query;
        this.propertySetQuery = createEfficientIterator(setQuery);
    }

    private IEntityPropertySetListingQuery createEfficientIterator(
            final IEntityPropertySetListingQuery setQuery)
    {
        return new IEntityPropertySetListingQuery()
            {
                private final static int BATCH_SIZE = 50000;

                @Override
                public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
                        LongSet entityIDs)
                {
                    return new AbstractBatchIterator<GenericEntityPropertyRecord>(entityIDs,
                            BATCH_SIZE)
                        {
                            @Override
                            protected Iterable<GenericEntityPropertyRecord> createUnefficientIterator(
                                    LongSet ids)
                            {
                                return setQuery.getEntityPropertyGenericValues(ids);
                            }
                        };
                }

                @Override
                public Iterable<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
                        LongSet entityIDs)
                {
                    return new AbstractBatchIterator<VocabularyTermRecord>(entityIDs, BATCH_SIZE)
                        {
                            @Override
                            protected Iterable<VocabularyTermRecord> createUnefficientIterator(
                                    LongSet ids)
                            {
                                return setQuery.getEntityPropertyVocabularyTermValues(ids);
                            }
                        };
                }

                @Override
                public Iterable<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
                        LongSet entityIDs)
                {
                    return new AbstractBatchIterator<MaterialEntityPropertyRecord>(entityIDs,
                            BATCH_SIZE)
                        {
                            @Override
                            protected Iterable<MaterialEntityPropertyRecord> createUnefficientIterator(
                                    LongSet ids)
                            {
                                return setQuery.getEntityPropertyMaterialValues(ids);
                            }
                        };
                }

            };
    }

    /**
     * Enriches the entities with given <var>entityIDs</var> with its properties. The entities will
     * be resolved by the {@link IEntityPropertiesHolderResolver} and will be enriched in place.
     */
    @Override
    public void enrich(final LongSet entityIDs, final IEntityPropertiesHolderResolver entities)
    {
        final Long2ObjectMap<PropertyType> propertyTypes = getPropertyTypes();
        // Generic properties
        for (GenericEntityPropertyRecord val : propertySetQuery
                .getEntityPropertyGenericValues(entityIDs))
        {
            final IEntityPropertiesHolder entity = entities.get(val.entity_id);
            final AbstractEntityProperty property = new GenericEntityProperty();
            property.setValue(val.value);
            property.setPropertyType(propertyTypes.get(val.prty_id));
            property.setScriptable(val.script_id != null);
            property.setDynamic(ScriptType.DYNAMIC_PROPERTY.name().equals(val.script_type));
            property.setOrdinal(val.ordinal);
            entity.getProperties().add(property);
        }

        // Controlled vocabulary properties
        Long2ObjectMap<String> vocabularyURLMap = null;
        Long2ObjectMap<VocabularyTerm> terms = new Long2ObjectOpenHashMap<VocabularyTerm>();
        for (VocabularyTermRecord val : propertySetQuery
                .getEntityPropertyVocabularyTermValues(entityIDs))
        {
            if (vocabularyURLMap == null)
            {
                vocabularyURLMap = getVocabularyURLs();
            }
            final IEntityPropertiesHolder entity = entities.get(val.entity_id);
            final IEntityProperty property = new VocabularyTermEntityProperty();
            VocabularyTerm vocabularyTerm = terms.get(val.id);
            if (vocabularyTerm == null)
            {
                vocabularyTerm = new VocabularyTerm();
                vocabularyTerm.setCode(val.code);
                vocabularyTerm.setLabel(val.label);
                vocabularyTerm.setDescription(val.description);
                final String template = vocabularyURLMap.get(val.covo_id);
                if (template != null)
                {
                    String url =
                            template.replaceAll(
                                    BasicConstant.DEPRECATED_VOCABULARY_URL_TEMPLATE_TERM_PATTERN,
                                    val.code);
                    url =
                            url.replaceAll(BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PATTERN,
                                    val.code);
                    vocabularyTerm.setUrl(url);
                }
                terms.put(val.id, vocabularyTerm);
            }
            property.setVocabularyTerm(vocabularyTerm);
            property.setPropertyType(propertyTypes.get(val.prty_id));
            property.setOrdinal(val.ordinal);
            entity.getProperties().add(property);
        }

        // Material-type properties
        Long2ObjectMap<MaterialType> materialTypes = null;
        Long2ObjectMap<Material> materials = new Long2ObjectOpenHashMap<Material>();
        for (MaterialEntityPropertyRecord val : propertySetQuery
                .getEntityPropertyMaterialValues(entityIDs))
        {
            if (materialTypes == null)
            {
                materialTypes = getMaterialTypes();
            }
            final IEntityPropertiesHolder entity = entities.get(val.entity_id);
            final IEntityProperty property = new MaterialEntityProperty();
            Material material = materials.get(val.id);
            if (material == null)
            {
                material = new Material();
                material.setCode(val.code);
                material.setMaterialType(materialTypes.get(val.maty_id));
                material.setId(val.id);
                materials.put(val.id, material);
            }
            property.setMaterial(material);
            property.setPropertyType(propertyTypes.get(val.prty_id));
            property.setOrdinal(val.ordinal);
            entity.getProperties().add(property);
        }

    }

    private Long2ObjectMap<PropertyType> getPropertyTypes()
    {
        final PropertyType[] types = query.getPropertyTypes();
        final Long2ObjectOpenHashMap<PropertyType> propertyTypeMap =
                new Long2ObjectOpenHashMap<PropertyType>(types.length);
        for (PropertyType t : types)
        {
            propertyTypeMap.put(t.getId(), t);
        }
        propertyTypeMap.trim();
        return propertyTypeMap;
    }

    private Long2ObjectMap<String> getVocabularyURLs()
    {
        final CodeRecord[] vocabURLs = query.getVocabularyURLTemplates();
        final Long2ObjectOpenHashMap<String> vocabularyURLMap =
                new Long2ObjectOpenHashMap<String>(vocabURLs.length);
        for (CodeRecord vocabURL : vocabURLs)
        {
            vocabularyURLMap.put(vocabURL.id, vocabURL.code);
        }
        vocabularyURLMap.trim();
        return vocabularyURLMap;
    }

    private Long2ObjectMap<MaterialType> getMaterialTypes()
    {
        final CodeRecord[] typeCodes = query.getMaterialTypes();
        final Long2ObjectOpenHashMap<MaterialType> materialTypeMap =
                new Long2ObjectOpenHashMap<MaterialType>(typeCodes.length);
        for (CodeRecord t : typeCodes)
        {
            final MaterialType type = new MaterialType();
            type.setCode(t.code);
            materialTypeMap.put(t.id, type);
        }
        materialTypeMap.trim();
        return materialTypeMap;
    }

}
