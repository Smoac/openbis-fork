/*
 * Copyright 2016 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataTypeCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.vocabulary.IVocabularyTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Franz-Josef Elmer
 */
public abstract class PropertyAssignmentTranslator extends ObjectToManyRelationTranslator<PropertyAssignment, PropertyAssignmentFetchOptions>
{
    @Autowired
    private IVocabularyTranslator vocabularyTranslator;

    protected Map<Long, PropertyAssignment> getAssignments(TranslationContext context, 
            Collection<PropertyAssignmentRecord> assignmentRecords, 
            PropertyAssignmentFetchOptions assignmentFetchOptions)
    {
        Map<Long, PropertyAssignment> assignments = new HashMap<>();
        Map<Long, List<PropertyAssignment>> assignmentsByPropertyTypeId = new HashMap<>();
        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            PropertyAssignment assignment = new PropertyAssignment();
            assignment.setMandatory(assignmentRecord.is_mandatory);
            assignments.put(assignmentRecord.id, assignment);
            List<PropertyAssignment> list = assignmentsByPropertyTypeId.get(assignmentRecord.prty_id);
            if (list == null)
            {
                list = new ArrayList<>();
                assignmentsByPropertyTypeId.put(assignmentRecord.prty_id, list);
            }
            list.add(assignment);
        }
        VocabularyFetchOptions vocabularyFetchOptions = getVocabularyFetchOptions(assignmentFetchOptions);
        Map<Long, PropertyType> propertyTypesByVocabularyId = injectPropertyTypes(assignmentsByPropertyTypeId, vocabularyFetchOptions);
        injectVocabularies(context, vocabularyFetchOptions, propertyTypesByVocabularyId);
        return assignments;
    }

    private void injectVocabularies(TranslationContext context, VocabularyFetchOptions vocabularyFetchOptions,
            Map<Long, PropertyType> propertyTypesByVocabularyId)
    {
        if (vocabularyFetchOptions != null)
        {
            Set<Long> ids = propertyTypesByVocabularyId.keySet();
            Map<Long, Vocabulary> map = vocabularyTranslator.translate(context, ids, vocabularyFetchOptions);
            Set<Entry<Long, Vocabulary>> entrySet = map.entrySet();
            for (Entry<Long, Vocabulary> entry : entrySet)
            {
                Long vocabularyId = entry.getKey();
                Vocabulary vocabulary = entry.getValue();
                PropertyType propertyType = propertyTypesByVocabularyId.get(vocabularyId);
                if (propertyType != null)
                {
                    propertyType.setVocabulary(vocabulary);
                }
            }
        }
    }

    private Map<Long, PropertyType> injectPropertyTypes(Map<Long, List<PropertyAssignment>> assignmentsByPropertyTypeId,
            VocabularyFetchOptions vocabularyFetchOptions)
    {
        PropertyTypeQuery query = QueryTool.getManagedQuery(PropertyTypeQuery.class);
        List<PropertyTypeRecord> propertyTypeRecords = query.getPropertyTypes(new LongOpenHashSet(assignmentsByPropertyTypeId.keySet()));
        Map<Long, PropertyType> propertyTypesByVocabularyId = new HashMap<>();
        for (PropertyTypeRecord propertyTypeRecord : propertyTypeRecords)
        {
            Long propertyTypeId = propertyTypeRecord.id;
            PropertyType propertyType = new PropertyType();
            propertyType.setCode(propertyTypeRecord.code);
            propertyType.setLabel(propertyTypeRecord.label);
            propertyType.setDescription(propertyTypeRecord.description);
            propertyType.setDataTypeCode(DataTypeCode.valueOf(propertyTypeRecord.dataSetTypeCode));
            propertyType.setInternalNameSpace(propertyTypeRecord.is_internal_namespace);
            propertyType.setVocabularyFetchOptions(vocabularyFetchOptions);
            if (propertyTypeRecord.covo_id != null)
            {
                propertyTypesByVocabularyId.put(propertyTypeRecord.covo_id, propertyType);
            }
            for (PropertyAssignment assignment : assignmentsByPropertyTypeId.get(propertyTypeId))
            {
                assignment.setPropertyType(propertyType);
            }
        }
        return propertyTypesByVocabularyId;
    }

    private VocabularyFetchOptions getVocabularyFetchOptions(PropertyAssignmentFetchOptions assignmentFetchOptions)
    {
        if (assignmentFetchOptions.hasVocabulary())
        {
            return assignmentFetchOptions.withVocabulary();
        }
        return null;
    }

    @Override
    protected Collection<PropertyAssignment> createCollection()
    {
        return new ArrayList<>();
    }
}
