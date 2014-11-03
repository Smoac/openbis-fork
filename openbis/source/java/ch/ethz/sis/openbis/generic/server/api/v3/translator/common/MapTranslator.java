/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;

/**
 * @author pkupczyk
 */
public class MapTranslator<ORIGINAL_KEY, TRANSLATED_KEY, ORIGINAL_VALUE, TRANSLATED_VALUE>
{

    public Map<TRANSLATED_KEY, TRANSLATED_VALUE> translate(Map<ORIGINAL_KEY, ORIGINAL_VALUE> map,
            ITranslator<ORIGINAL_KEY, TRANSLATED_KEY> keyTranslator, ITranslator<ORIGINAL_VALUE, TRANSLATED_VALUE> valueTranslator)
    {
        if (map == null || map.isEmpty())
        {
            return Collections.emptyMap();
        }

        Map<ORIGINAL_KEY, TRANSLATED_KEY> translatedKeyMap = keyTranslator.translate(map.keySet());
        Map<ORIGINAL_VALUE, TRANSLATED_VALUE> translatedValueMap = valueTranslator.translate(map.values());
        Map<TRANSLATED_KEY, TRANSLATED_VALUE> result = new LinkedHashMap<TRANSLATED_KEY, TRANSLATED_VALUE>();

        for (Map.Entry<ORIGINAL_KEY, ORIGINAL_VALUE> entry : map.entrySet())
        {
            ORIGINAL_KEY originalKey = entry.getKey();
            ORIGINAL_VALUE originalValue = entry.getValue();
            TRANSLATED_KEY translatedKey = translatedKeyMap.get(originalKey);
            TRANSLATED_VALUE translatedValue = translatedValueMap.get(originalValue);

            if (originalKey != null && translatedKey == null)
            {
                continue;
            }
            if (originalValue != null && translatedValue == null)
            {
                continue;
            }

            result.put(translatedKey, translatedValue);
        }

        return result;
    }

}
