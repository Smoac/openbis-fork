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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityPropertyHistory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AbstractEntityPropertyHistoryPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * @author Franz-Josef Elmer
 */
public class EntityPropertyHistoryTranslator
{
    public static List<EntityPropertyHistory> translate(
            List<AbstractEntityPropertyHistoryPE> history)
    {
        List<EntityPropertyHistory> result = new ArrayList<EntityPropertyHistory>();
        HashMap<PropertyTypePE, PropertyType> cache = new HashMap<PropertyTypePE, PropertyType>();
        for (AbstractEntityPropertyHistoryPE entityPropertyHistory : history)
        {
            result.add(translate(entityPropertyHistory, cache));
        }
        return result;
    }

    private static EntityPropertyHistory translate(
            AbstractEntityPropertyHistoryPE entityPropertyHistory,
            Map<PropertyTypePE, PropertyType> cache)
    {
        EntityPropertyHistory result = new EntityPropertyHistory();
        result.setAuthor(PersonTranslator.translate(entityPropertyHistory.getAuthor()));
        result.setValidFromDate(entityPropertyHistory.getValidFromDate());
        result.setValidUntilDate(entityPropertyHistory.getValidUntilDate());
        result.setValue(entityPropertyHistory.getValue());
        result.setMaterial(entityPropertyHistory.getMaterial());
        result.setVocabularyTerm(entityPropertyHistory.getVocabularyTerm());
        result.setPropertyType(PropertyTypeTranslator.translate(entityPropertyHistory
                .getEntityTypePropertyType().getPropertyType(), cache));
        return result;
    }
}
