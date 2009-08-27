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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class AccessionNumberURLCreator
{
    private static final String UNIPROT_URL_TEMPLATE = "http://www.uniprot.org/uniprot/$id";
    private static final Map<String, String> URL_TEMPLATES = createURLTemplates();
    
    private static final Map<String, String> createURLTemplates()
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("sp", UNIPROT_URL_TEMPLATE);
        map.put("tr", UNIPROT_URL_TEMPLATE);
        map.put("gi", "http://www.ncbi.nlm.nih.gov/protein/$id");
        map.put("isb", "http://www.ebi.ac.uk/ebisearch/search.ebi?db=proteinSequences&t=$id");
        return map;
    }

    static String tryToCreateURL(String typeOrNull, String accessionNumber)
    {
        if (typeOrNull == null)
        {
            return eval(UNIPROT_URL_TEMPLATE, accessionNumber);
        }
        String template = URL_TEMPLATES.get(typeOrNull);
        return template == null ? null : eval(template, accessionNumber);
    }
    
    private static String eval(String template, String value)
    {
        return template.replaceAll("\\$id", value);
    }
}
