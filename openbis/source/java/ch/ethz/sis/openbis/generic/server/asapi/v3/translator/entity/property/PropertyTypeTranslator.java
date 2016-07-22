/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.property;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class PropertyTypeTranslator extends AbstractCachingTranslator<Long, PropertyType, PropertyTypeFetchOptions> implements
        IPropertyTypeTranslator
{

    @Autowired
    private IPropertyTypeBaseTranslator baseTranslator;

    @Autowired
    private IPropertyTypeVocabularyTranslator vocabularyTranslator;

    @Autowired
    private IPropertyTypeMaterialTypeTranslator materialTypeTranslator;

    @Autowired
    private IPropertyTypeRegistratorTranslator registratorTranslator;

    @Override
    protected PropertyType createObject(TranslationContext context, Long typeId, PropertyTypeFetchOptions fetchOptions)
    {
        final PropertyType type = new PropertyType();
        type.setFetchOptions(fetchOptions);
        return type;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> typeIds,
            PropertyTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IPropertyTypeBaseTranslator.class, baseTranslator.translate(context, typeIds, null));

        if (fetchOptions.hasVocabulary())
        {
            relations.put(IPropertyTypeVocabularyTranslator.class,
                    vocabularyTranslator.translate(context, typeIds, fetchOptions.withVocabulary()));
        }

        if (fetchOptions.hasMaterialType())
        {
            relations.put(IPropertyTypeMaterialTypeTranslator.class,
                    materialTypeTranslator.translate(context, typeIds, fetchOptions.withMaterialType()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IPropertyTypeRegistratorTranslator.class,
                    registratorTranslator.translate(context, typeIds, fetchOptions.withRegistrator()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long typeId, PropertyType result, Object objectRelations,
            PropertyTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        PropertyTypeRecord baseRecord = relations.get(IPropertyTypeBaseTranslator.class, typeId);

        result.setCode(baseRecord.code);
        result.setLabel(baseRecord.label);
        result.setDescription(baseRecord.description);
        result.setDataType(DataType.valueOf(baseRecord.data_type));
        result.setManagedInternally(baseRecord.is_managed_internally);
        result.setInternalNameSpace(baseRecord.is_internal_namespace);
        result.setSchema(baseRecord.schema);
        result.setTransformation(baseRecord.transformation);
        result.setRegistrationDate(baseRecord.registration_timestamp);

        if (fetchOptions.hasVocabulary())
        {
            result.setVocabulary(relations.get(IPropertyTypeVocabularyTranslator.class, typeId));
        }

        if (fetchOptions.hasMaterialType())
        {
            result.setMaterialType(relations.get(IPropertyTypeMaterialTypeTranslator.class, typeId));
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IPropertyTypeRegistratorTranslator.class, typeId));
        }

    }

}
