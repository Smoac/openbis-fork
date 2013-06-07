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

import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IRowBuilderAdaptor;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ParameterDescriptionBuilder
{
    private final IRowBuilderAdaptor row;

    ParameterDescriptionBuilder(IRowBuilderAdaptor row)
    {
        this.row = row;
    }
    
    public void text()
    {
        row.setCell(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN, FieldType.VARCHAR.toString());
    }
    
    public void experiment()
    {
        row.setCell(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN, FieldType.EXPERIMENT.toString());
    }
    
    public void sample()
    {
        row.setCell(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN, FieldType.SAMPLE.toString());
    }

    public void dataSet()
    {
        row.setCell(Constants.PARAMETER_DESCRIPTION_TYPE_COLUMN, FieldType.DATA_SET.toString());
    }
    
    public VocabularyBuilder vocabulary(String...terms)
    {
        VocabularyBuilder vocabularyBuilder = new VocabularyBuilder(row);
        for (String term : terms)
        {
            vocabularyBuilder.term(term);
        }
        return vocabularyBuilder;
    }
    
}
