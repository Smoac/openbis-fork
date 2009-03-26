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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class VocabularyTermReplacementModel implements ModelData
{
    public static final String ORIGINAL_COLUMN = "original";
    public static final String REPLACEMENT_COLUMN = "replacement";
    
    private static final long serialVersionUID = 1L;

    private final VocabularyTermReplacement termReplacement;

    public VocabularyTermReplacementModel(VocabularyTermReplacement termReplacement)
    {
        this.termReplacement = termReplacement;
    }
    
    public final VocabularyTermReplacement getTermReplacement()
    {
        return termReplacement;
    }

    @SuppressWarnings("unchecked")
    public <X> X get(String property)
    {
        if (ORIGINAL_COLUMN.equals(property))
        {
            return (X) termReplacement.getTerm();
        }
        if (REPLACEMENT_COLUMN.equals(property))
        {
            return (X) termReplacement.getReplacement();
        }
        return null;
    }

    public Map<String, Object> getProperties()
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (String key : getPropertyNames())
        {
            map.put(key, get(key));
        }
        return map;
    }

    public Collection<String> getPropertyNames()
    {
        return Arrays.asList(ORIGINAL_COLUMN, REPLACEMENT_COLUMN);
    }

    public <X> X remove(String property)
    {
        return null;
    }

    public <X> X set(String property, X value)
    {
        X oldValue = get(property);
        if (ORIGINAL_COLUMN.equals(property))
        {
            termReplacement.setTerm((VocabularyTerm) value);
        } else if (REPLACEMENT_COLUMN.equals(property))
        {
            termReplacement.setReplacement((VocabularyTerm) value);
        }
        return oldValue;
    }
}
