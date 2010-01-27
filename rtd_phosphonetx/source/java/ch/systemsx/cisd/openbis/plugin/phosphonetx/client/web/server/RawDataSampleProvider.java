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
import java.util.HashSet;
import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper.Column;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;

/**
 * @author Franz-Josef Elmer
 */
class RawDataSampleProvider implements IOriginalDataProvider<GenericTableRow>
{

    @Private
    static final String PARENT = "PARENT";

    @Private
    static final String REGISTRATION_DATE = "REGISTRATION_DATE";

    @Private
    static final String CODE = "CODE";

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
        for (int i = 0; i < numberOfRows; i++)
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
        List<GenericTableColumnHeader> headers =
                new ArrayList<GenericTableColumnHeader>(columns.size());
        for (Column column : columns)
        {
            headers.add(column.getHeader());
        }
        return headers;
    }

    private List<Column> getColumns()
    {
        List<Sample> samples = service.listRawDataSamples(sessionToken);
        Column codeColumn =
                new Column(GenericTableColumnHeader.untitledLinkableStringHeader(0, CODE));
        Column dateColumn =
                new Column(GenericTableColumnHeader.untitledStringHeader(1, REGISTRATION_DATE));
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
