/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Provides structured information from one row of the QIAGEN library.
 * 
 * @author Tomasz Pylak
 */
public class QiagenScreeningLibraryColumnExtractor implements IScreeningLibraryColumnExtractor
{
    // ----- column names

    private final static String PLATE_NAME = "barcode";

    private final static String WELL_ROW = "row";

    private final static String WELL_COL = "col";

    // gene

    private final static String GENE_ID = "geneId";

    private final static String GENE_SYMBOL = "symbol";

    private final static String GENE_DESC = "description";

    // oligo

    private final static String RNA_SEQUENCE = "sirna";

    private final static String OLIGO_ID = "productId";

    private final static String[] ALL_COLUMNS = new String[]
        { PLATE_NAME, WELL_ROW, WELL_COL, RNA_SEQUENCE, GENE_ID, GENE_SYMBOL, GENE_DESC, OLIGO_ID };

    // -------------

    private final Map<String/* column name */, Integer/* index in the header table */> columnIndices;

    private final Map<String/* column name */, Integer/* index in the header table */> unknownColumnIndices;

    public QiagenScreeningLibraryColumnExtractor(String[] headerTokens)
    {
        this.columnIndices = createColumnIndex(headerTokens);
        this.unknownColumnIndices = getOmittedIndices(columnIndices, headerTokens);
    }

    public List<String> getAdditionalOligoPropertyNames()
    {
        return new ArrayList<String>(unknownColumnIndices.keySet());
    }

    // ------------

    private static Map<String, Integer> getOmittedIndices(Map<String, Integer> columnIndex,
            String[] headers)
    {
        Map<String, Integer> omittedIndices = new HashMap<String, Integer>();
        Set<Integer> knownIndices = new HashSet<Integer>(columnIndex.values());
        for (int i = 0; i < headers.length; i++)
        {
            if (knownIndices.contains(i) == false)
            {
                omittedIndices.put(headers[i], i);
            }
        }
        return omittedIndices;
    }

    private static Map<String, Integer> createColumnIndex(String[] headers)
    {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (String columnName : ALL_COLUMNS)
        {
            findAndPut(map, headers, columnName);
        }
        return map;
    }

    private static void findAndPut(Map<String, Integer> map, String[] headers, String columnName)
    {
        int ix = findIndexOrDie(headers, columnName);
        map.put(columnName, ix);
    }

    private static int findIndexOrDie(String[] headers, String columnName)
    {
        for (int i = 0; i < headers.length; i++)
        {
            if (headers[i].equalsIgnoreCase(columnName))
            {
                return i;
            }
        }
        throw new UserFailureException("Column " + columnName + " does not exist in "
                + CollectionUtils.abbreviate(headers, -1));
    }

    private String getValue(String[] row, String columnName)
    {
        Integer ix = columnIndices.get(columnName);
        return valueAt(row, ix);
    }

    private static String valueAt(String[] row, Integer ix)
    {
        if (ix >= row.length)
        {
            return "";
        } else
        {
            return row[ix];
        }
    }

    private String asCode(String value)
    {
        String code = "";
        for (int i = 0; i < value.length(); i++)
        {
            char ch = value.charAt(i);
            if (isValidCodeCharacter(ch) == false)
            {
                ch = '_';
            }
            code += ch;
        }
        return code;
    }

    private boolean isValidCodeCharacter(char ch)
    {
        return Character.isLetterOrDigit(ch) || ch == '.' || ch == '-' || ch == '_';
    }

    private String getCodeValue(String[] row, String columnName)
    {
        return asCode(getValue(row, columnName));
    }

    // ------------

    public String getPlateCode(String[] row)
    {
        return getCodeValue(row, PLATE_NAME);
    }

    public String getWellCode(String[] row)
    {
        String wellRow = getValue(row, WELL_ROW);
        String wellCol = getValue(row, WELL_COL);
        return wellRow + wellCol;
    }

    public String getRNASequence(String[] row)
    {
        return getValue(row, RNA_SEQUENCE);
    }

    public String getOligoId(String[] row)
    {
        return getValue(row, OLIGO_ID);
    }

    public String getGeneId(String[] row)
    {
        return getValue(row, GENE_ID);
    }

    public String getGeneCode(String[] row)
    {
        return getCodeValue(row, GENE_SYMBOL);
    }

    public String getGeneDescription(String[] row)
    {
        return getValue(row, GENE_DESC);
    }

    public List<String> getAdditionalOligoPropertyValues(String[] row, List<String> columnNames)
    {
        List<String> values = new ArrayList<String>();
        for (String columnName : columnNames)
        {
            Integer ix = unknownColumnIndices.get(columnName);
            String value = valueAt(row, ix);
            values.add(value);
        }
        return values;
    }
}
