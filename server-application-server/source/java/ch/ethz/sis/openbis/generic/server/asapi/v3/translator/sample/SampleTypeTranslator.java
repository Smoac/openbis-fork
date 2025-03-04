/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class SampleTypeTranslator extends AbstractCachingTranslator<Long, SampleType, SampleTypeFetchOptions> implements
        ISampleTypeTranslator
{

    @Autowired
    private ISampleTypeBaseTranslator baseTranslator;

    @Autowired
    private ISamplePropertyAssignmentTranslator assignmentTranslator;

    @Autowired
    private ISampleTypeValidationPluginTranslator validationPluginTranslator;
    
    @Autowired
    private ISampleTypeSemanticAnnotationTranslator annotationTranslator;

    @Override
    protected SampleType createObject(TranslationContext context, Long typeId, SampleTypeFetchOptions fetchOptions)
    {
        final SampleType type = new SampleType();
        type.setFetchOptions(fetchOptions);
        return type;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> typeIds,
            SampleTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ISampleTypeBaseTranslator.class, baseTranslator.translate(context, typeIds, null));

        if (fetchOptions.hasPropertyAssignments())
        {
            relations.put(ISamplePropertyAssignmentTranslator.class,
                    assignmentTranslator.translate(context, typeIds, fetchOptions.withPropertyAssignments()));
        }
        if (fetchOptions.hasValidationPlugin())
        {
            relations.put(ISampleTypeValidationPluginTranslator.class,
                    validationPluginTranslator.translate(context, typeIds, fetchOptions.withValidationPlugin()));
        }
        if (fetchOptions.hasSemanticAnnotations())
        {
            relations.put(ISampleTypeSemanticAnnotationTranslator.class,
                    annotationTranslator.translate(context, typeIds, fetchOptions.withSemanticAnnotations()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long typeId, SampleType result, Object objectRelations,
            SampleTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        SampleTypeBaseRecord baseRecord = relations.get(ISampleTypeBaseTranslator.class, typeId);

        result.setPermId(new EntityTypePermId(baseRecord.code, EntityKind.SAMPLE));
        result.setCode(baseRecord.code);
        result.setDescription(baseRecord.description);
        result.setAutoGeneratedCode(baseRecord.autoGeneratedCode);
        result.setGeneratedCodePrefix(baseRecord.generatedCodePrefix);
        result.setListable(baseRecord.listable);
        result.setShowContainer(baseRecord.partOfDepth > 0);
        result.setShowParents(baseRecord.generatedFromDepth > 0);
        result.setShowParentMetadata(baseRecord.showParentMetadata);
        result.setSubcodeUnique(baseRecord.subcodeUnique);
        result.setModificationDate(baseRecord.modificationDate);
        result.setMetaData(CommonUtils.asMap(baseRecord.metaData));
        result.setManagedInternally(baseRecord.managedInternally);

        if (fetchOptions.hasPropertyAssignments())
        {
            Collection<PropertyAssignment> assignments = relations.get(ISamplePropertyAssignmentTranslator.class, typeId);
            List<PropertyAssignment> propertyAssignments = new ArrayList<>(assignments);
            result.setPropertyAssignments(new SortAndPage().sortAndPage(propertyAssignments, null, fetchOptions.withPropertyAssignments()));
        }
        if (fetchOptions.hasValidationPlugin())
        {
            result.setValidationPlugin(relations.get(ISampleTypeValidationPluginTranslator.class, typeId));
        }
        if (fetchOptions.hasSemanticAnnotations())
        {
            result.setSemanticAnnotations((List<SemanticAnnotation>) relations.get(ISampleTypeSemanticAnnotationTranslator.class, typeId));
            result.getFetchOptions().withSemanticAnnotationsUsing(fetchOptions.withSemanticAnnotations());
        }
    }

}
