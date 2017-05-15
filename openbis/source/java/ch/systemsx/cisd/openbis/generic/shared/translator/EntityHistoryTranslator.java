/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityPropertyHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author Franz-Josef Elmer
 */
public class EntityHistoryTranslator
{
    public static List<EntityHistory> translate(List<AbstractEntityPropertyHistoryPE> history,
            String baseIndexURL, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        List<EntityHistory> result = new ArrayList<EntityHistory>();
        HashMap<PropertyTypePE, PropertyType> cache = new HashMap<PropertyTypePE, PropertyType>();
        HashMap<MaterialTypePE, MaterialType> materialTypesCache = new HashMap<MaterialTypePE, MaterialType>();
        for (AbstractEntityPropertyHistoryPE entityPropertyHistory : history)
        {
            result.add(translate(entityPropertyHistory, materialTypesCache, cache, baseIndexURL,
                    managedPropertyEvaluatorFactory));
        }
        return result;
    }

    private static EntityHistory translate(AbstractEntityPropertyHistoryPE entityPropertyHistory,
            Map<MaterialTypePE, MaterialType> materialTypeCache, 
            Map<PropertyTypePE, PropertyType> cache, String baseIndexURL,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        EntityHistory result = new EntityHistory();
        result.setAuthor(PersonTranslator.translate(entityPropertyHistory.getAuthor()));
        result.setValidFromDate(entityPropertyHistory.getValidFromDate());
        result.setValidUntilDate(entityPropertyHistory.getValidUntilDate());
        result.setValue(entityPropertyHistory.getValue());
        result.setMaterial(entityPropertyHistory.getMaterial());
        result.setVocabularyTerm(entityPropertyHistory.getVocabularyTerm());
        if (entityPropertyHistory.getEntityTypePropertyType() != null)
        {
            result.setPropertyType(PropertyTypeTranslator.translate(entityPropertyHistory
                    .getEntityTypePropertyType().getPropertyType(), materialTypeCache, cache));
        }

        if (entityPropertyHistory instanceof AbstractEntityHistoryPE)
        {
            AbstractEntityHistoryPE entityHistory = (AbstractEntityHistoryPE) entityPropertyHistory;
            result.setRelatedEntityPermId(entityHistory.getEntityPermId());
            String entityType = null;
            if (entityHistory.getRelatedEntity() != null)
            {
                switch (entityHistory.getRelatedEntity().getEntityKind())
                {
                    case DATA_SET:
                        entityType = EntityKind.DATA_SET.getDescription();
                        result.setRelatedEntity(DataSetTranslator
                                .translateBasicProperties((DataPE) entityHistory.getRelatedEntity()));
                        break;
                    case EXPERIMENT:
                        entityType = EntityKind.EXPERIMENT.getDescription();
                        result.setRelatedEntity(ExperimentTranslator.translate(
                                (ExperimentPE) entityHistory.getRelatedEntity(), baseIndexURL,
                                null, managedPropertyEvaluatorFactory));
                        break;
                    case SAMPLE:
                        entityType = EntityKind.SAMPLE.getDescription();
                        result.setRelatedEntity(SampleTranslator.translate(
                                (SamplePE) entityHistory.getRelatedEntity(), baseIndexURL, null,
                                managedPropertyEvaluatorFactory));
                        break;
                    case MATERIAL:
                }
            }
            if (entityHistory.getSpace() != null)
            {
                entityType = "Space";
                result.setRelatedSpace(SpaceTranslator.translate(entityHistory.getSpace()));
            }
            if (entityHistory.getProject() != null)
            {
                entityType = "Project";
                result.setRelatedProject(ProjectTranslator.translate(entityHistory.getProject()));
            }
            if (entityHistory.getRelationType() != null)
            {
                result.setRelationType(entityHistory.getRelationType().getDescrption(entityType));
            }
        }
        return result;
    }
}
