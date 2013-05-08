/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.server;

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IRowBuilderAdaptor;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class VocabularyBuilder
{
    private final IRowBuilderAdaptor row;
    private final Set<String> terms = new HashSet<String>();
    private final StringBuilder termsBuilder = new StringBuilder();

    VocabularyBuilder(IRowBuilderAdaptor row)
    {
        this.row = row;
        row.setCell(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN, FieldType.VOCABULARY.toString());
    }
    
    public VocabularyBuilder term(String term)
    {
        if (terms.contains(term))
        {
            throw new IllegalArgumentException("There is already a term '" + term + "' defined.");
        }
        if (termsBuilder.length() > 0)
        {
            termsBuilder.append(", ");
        }
        termsBuilder.append(term);
        row.setCell(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN, 
                FieldType.VOCABULARY + ":" + termsBuilder);
        return this;
    }

}
