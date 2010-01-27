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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.DataTypeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SerializableComparableIDDecorator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class RawDataSampleProvider implements IOriginalDataProvider<GenericTableRow>
{
    @Private static final String PARENT = "PARENT";
    @Private static final String REGISTRATION_DATE = "REGISTRATION_DATE";
    @Private static final String CODE = "CODE";

    private static final class Column
    {
        private final List<ISerializableComparable> values = new ArrayList<ISerializableComparable>();
        private final GenericTableColumnHeader header;

        Column(GenericTableColumnHeader header)
        {
            this.header = header;
        }
        
        GenericTableColumnHeader getHeader()
        {
            return header;
        }
        
        List<ISerializableComparable> getValues()
        {
            return values;
        }
        
        void add(int index, ISerializableComparable value)
        {
            while (values.size() < index)
            {
                values.add(null);
            }
            values.add(value);
        }
        
        void addStringWithID(int index, String string, Long id)
        {
            add(index, new SerializableComparableIDDecorator(new StringTableCell(string), id));
        }
        
        void addDate(int index, Date date)
        {
            add(index, new DateTableCell(date));
        }

        void addPrefixToColumnHeaderCodes(String prefix)
        {
            header.setCode(prefix + header.getCode());
        }

        void setIndex(int index)
        {
            header.setIndex(index);
        }
    }
    
    private static final class PropertyColumns
    {
        private final Map<String, Column> columns = new TreeMap<String, Column>();
        
        void add(int index, IEntityProperty property)
        {
            PropertyType propertyType = property.getPropertyType();
            DataTypeCode dataType = propertyType.getDataType().getCode();
            String key = propertyType.getCode();
            Column column = columns.get(key);
            if (column == null)
            {
                GenericTableColumnHeader header = new GenericTableColumnHeader();
                header.setCode(key);
                header.setIndex(columns.size());
                header.setTitle(propertyType.getLabel());
                header.setType(dataType);
                column = new Column(header);
                columns.put(key, column);
            }
            column.add(index, DataTypeUtils.convertTo(dataType, property.getValue()));
        }
        
        Set<String> getColumnCodes()
        {
            return columns.keySet();
        }

        void addPrefixToColumnHeaderCodes(String prefix)
        {
            for (Map.Entry<String, Column> entry : columns.entrySet())
            {
                entry.getValue().addPrefixToColumnHeaderCodes(prefix);
            }
        }

        int reindexColumns(int startIndex)
        {
            int index = startIndex;
            for (Map.Entry<String, Column> entry : columns.entrySet())
            {
                entry.getValue().setIndex(index++);
            }
            return index;
        }

        Collection<? extends Column> getColumns()
        {
            return columns.values();
        }
    }
    
    private final IRawDataServiceInternal service;
    private final String sessionToken;

    RawDataSampleProvider(IRawDataServiceInternal service, String sessionToken)
    {
        this.service = service;
        this.sessionToken = sessionToken;
    }
    
    public List<GenericTableRow> getOriginalData() throws UserFailureException
    {
        List<Column> columns = getColumns();
        int numberOfRows = columns.get(0).getValues().size();
        List<GenericTableRow> result = new ArrayList<GenericTableRow>(numberOfRows);
        for(int i = 0; i < numberOfRows; i++)
        {
            ISerializableComparable[] row = new ISerializableComparable[columns.size()];
            for (int j = 0; j < row.length; j++)
            {
                Column column = columns.get(j);
                List<ISerializableComparable> values = column.getValues();
                row[j] = i < values.size() ? values.get(i) : null;
            }
            result.add(new GenericTableRow(row));
        }
        return result;
    }
    
    public List<GenericTableColumnHeader> getHeaders()
    {
        List<Column> columns = getColumns();
        List<GenericTableColumnHeader> headers = new ArrayList<GenericTableColumnHeader>(columns.size());
        for (Column column : columns)
        {
            headers.add(column.getHeader());
        }
        return headers;
    }

    private List<Column> getColumns()
    {
        List<Sample> samples = service.listRawDataSamples(sessionToken);
        Column codeColumn = new Column(GenericTableColumnHeader.untitledLinkableStringHeader(0, CODE));
        Column dateColumn = new Column(GenericTableColumnHeader.untitledStringHeader(1, REGISTRATION_DATE));
        Column parentColumn = new Column(GenericTableColumnHeader.untitledStringHeader(2, PARENT));
        List<Column> columns = new ArrayList<Column>();
        columns.add(codeColumn);
        columns.add(dateColumn);
        columns.add(parentColumn);
        int fixedColumns = columns.size();
        PropertyColumns samplePropertyColumns = new PropertyColumns();
        PropertyColumns parentPropertyColumns = new PropertyColumns();
        for (int i = 0; i < samples.size(); i++)
        {
            Sample sample = samples.get(i);
            codeColumn.addStringWithID(i, sample.getCode(), sample.getId());
            dateColumn.addDate(i, sample.getRegistrationDate());
            Sample parent = sample.getGeneratedFrom();
            parentColumn.addStringWithID(i, parent.getIdentifier(), parent.getId());
            addPropertyTypes(samplePropertyColumns, i, sample);
            addPropertyTypes(parentPropertyColumns, i, sample.getGeneratedFrom());
        }
        int nextIndex = samplePropertyColumns.reindexColumns(fixedColumns);
        parentPropertyColumns.reindexColumns(nextIndex);
        HashSet<String> commonColumns = new HashSet<String>(samplePropertyColumns.getColumnCodes());
        commonColumns.retainAll(parentPropertyColumns.getColumnCodes());
        if (commonColumns.isEmpty() == false)
        {
            parentPropertyColumns.addPrefixToColumnHeaderCodes("BIO_");
        }
        columns.addAll(samplePropertyColumns.getColumns());
        columns.addAll(parentPropertyColumns.getColumns());
        return columns;
    }

    private void addPropertyTypes(PropertyColumns columns, int index, Sample sample)
    {
        for (IEntityProperty property : sample.getProperties())
        {
            columns.add(index, property);
        }
    }

}
