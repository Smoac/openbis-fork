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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.CodeVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.GenericSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.MaterialSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.SampleListingWorker.ISampleResolver;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;

/**
 * A class that can enrich a set of samples with its sample properties.
 * 
 * @author Bernd Rinn
 */
final class SamplePropertiesEnrichmentWorker implements
        SampleListingWorker.ISamplePropertiesEnricher
{
    private final ISampleListingQuery query;
    
    private final ISampleSetListingQuery setQuery;

    SamplePropertiesEnrichmentWorker(final ISampleListingQuery query,
            final ISampleSetListingQuery setQuery)
    {
        this.query = query;
        this.setQuery = setQuery;
    }

    /**
     * Enriches the samples with given <var>sampleIds</var> with its properties. The samples will be
     * resolved by the {@link ISampleResolver} and will be enriched in place.
     */
    public void enrich(final LongSet sampleIds, final ISampleResolver samples)
    {
        final Long2ObjectMap<PropertyType> propertyTypes = getPropertyTypes();
        // Generic properties
        for (GenericSamplePropertyVO val : setQuery.getSamplePropertyGenericValues(sampleIds))
        {
            final Sample sample = samples.get(val.samp_id);
            final IEntityProperty property = new GenericValueEntityProperty();
            property.setValue(StringEscapeUtils.escapeHtml(val.value));
            property.setPropertyType(propertyTypes.get(val.prty_id));
            sample.getProperties().add(property);
        }
        // Controlled vocabulary properties
        Long2ObjectMap<String> vocabularyURLMap = null;
        Long2ObjectMap<VocabularyTerm> terms = new Long2ObjectOpenHashMap<VocabularyTerm>();
        for (CoVoSamplePropertyVO val : setQuery.getSamplePropertyVocabularyTermValues(sampleIds))
        {
            if (vocabularyURLMap == null)
            {
                vocabularyURLMap = getVocabularyURLs();
            }
            final Sample sample = samples.get(val.samp_id);
            final IEntityProperty property = new VocabularyTermValueEntityProperty();
            VocabularyTerm vocabularyTerm = terms.get(val.id);
            if (vocabularyTerm == null)
            {
                vocabularyTerm = new VocabularyTerm();
                vocabularyTerm.setCode(StringEscapeUtils.escapeHtml(val.code));
                vocabularyTerm.setLabel(StringEscapeUtils.escapeHtml(val.label));
                final String template = vocabularyURLMap.get(val.covo_id);
                if (template != null)
                {
                    vocabularyTerm.setUrl(StringEscapeUtils.escapeHtml(template.replaceAll(
                            BasicConstant.VOCABULARY_URL_TEMPLATE_TERM_PATTERN, val.code)));
                }
                terms.put(val.id, vocabularyTerm);
            }
            property.setVocabularyTerm(vocabularyTerm);
            property.setPropertyType(propertyTypes.get(val.prty_id));
            sample.getProperties().add(property);
        }
        // Material-type properties
        Long2ObjectMap<MaterialType> materialTypes = null;
        Long2ObjectMap<Material> materials = new Long2ObjectOpenHashMap<Material>();
        for (MaterialSamplePropertyVO val : setQuery.getSamplePropertyMaterialValues(sampleIds))
        {
            if (materialTypes == null)
            {
                materialTypes = getMaterialTypes();
            }
            final Sample sample = samples.get(val.samp_id);
            final IEntityProperty property = new MaterialValueEntityProperty();
            Material material = materials.get(val.id);
            if (material == null)
            {
                material = new Material();
                material.setCode(StringEscapeUtils.escapeHtml(val.code));
                material.setMaterialType(materialTypes.get(val.maty_id));
                materials.put(val.id, material);
            }
            property.setMaterial(material);
            property.setPropertyType(propertyTypes.get(val.prty_id));
            sample.getProperties().add(property);
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
        final CodeVO[] vocabURLs = query.getVocabularyURLTemplates();
        final Long2ObjectOpenHashMap<String> vocabularyURLMap =
                new Long2ObjectOpenHashMap<String>(vocabURLs.length);
        for (CodeVO vocabURL : vocabURLs)
        {
            vocabularyURLMap.put(vocabURL.id, vocabURL.code);
        }
        vocabularyURLMap.trim();
        return vocabularyURLMap;
    }

    private Long2ObjectMap<MaterialType> getMaterialTypes()
    {
        final CodeVO[] typeCodes = query.getMaterialTypes();
        final Long2ObjectOpenHashMap<MaterialType> materialTypeMap =
                new Long2ObjectOpenHashMap<MaterialType>(typeCodes.length);
        for (CodeVO t : typeCodes)
        {
            final MaterialType type = new MaterialType();
            type.setCode(StringEscapeUtils.escapeHtml(t.code));
            materialTypeMap.put(t.id, type);
        }
        materialTypeMap.trim();
        return materialTypeMap;
    }

}
