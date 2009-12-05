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

package ch.systemsx.cisd.common.net.uniprot;

import java.util.Set;

import ch.systemsx.cisd.common.parser.IParserObjectFactory;
import ch.systemsx.cisd.common.parser.IPropertyMapper;
import ch.systemsx.cisd.common.parser.IPropertyModel;
import ch.systemsx.cisd.common.parser.ParserException;

/**
 * A parser factory for {@link UniprotEntry}s.
 * 
 * @author Bernd Rinn
 */
final class UniprotEntryParserFactory implements IParserObjectFactory<UniprotEntry>
{

    private final UniprotColumn[] columns;

    UniprotEntryParserFactory(IPropertyMapper mapper)
    {
        final Set<String> columnHeaders = mapper.getAllPropertyCodes();
        this.columns = new UniprotColumn[columnHeaders.size()];
        for (String columnHeader : columnHeaders)
        {
            final IPropertyModel model = mapper.getPropertyModel(columnHeader);
            final UniprotColumn col = UniprotColumn.columnMap.get(model.getCode().toLowerCase());
            if (col == null)
            {
                throw new ParserException("Unknown Uniprot column header: '" + model.getCode()
                        + "'");
            }
            columns[model.getColumn()] = col;
        }
    }

    public UniprotEntry createObject(String[] lineTokens) throws ParserException
    {
        assert lineTokens.length == columns.length;
        
        final UniprotEntry result = new UniprotEntry();
        for (int i = 0; i < columns.length; ++i)
        {
            final UniprotColumn column = columns[i];
            result.set(column, lineTokens[i]);
        }
        return result;
    }

}
