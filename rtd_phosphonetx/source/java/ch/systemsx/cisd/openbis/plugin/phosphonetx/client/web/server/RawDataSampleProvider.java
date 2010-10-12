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
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractOriginalDataProviderWithoutHeaders;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper.Column;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
class RawDataSampleProvider extends AbstractOriginalDataProviderWithoutHeaders<TableModelRow>
{

    @Private
    static final String EXPERIMENT = "EXPERIMENT";
    
    @Private
    static final String PARENT = "PARENT";

    @Private
    static final String REGISTRATION_DATE = "REGISTRATION_DATE";

    @Private
    static final String CODE = "CODE";

    private final IProteomicsDataServiceInternal service;

    private final String sessionToken;

    RawDataSampleProvider(IProteomicsDataServiceInternal service, String sessionToken)
    {
        this.service = service;
        this.sessionToken = sessionToken;
    }

    public List<TableModelRow> getOriginalData() throws UserFailureException
    {
        return GenericColumnsHelper.createTableRows(getColumns());
    }

    public List<TableModelColumnHeader> getGenericHeaders()
    {
        List<Column> columns = getColumns();
        List<TableModelColumnHeader> headers =
                new ArrayList<TableModelColumnHeader>(columns.size());
        for (Column column : columns)
        {
            headers.add(column.getHeader());
        }
        return headers;
    }

    private List<Column> getColumns()
    {
        List<MsInjectionSample> samples = service.listRawDataSamples(sessionToken);
        Column codeColumn =
                new Column(TableModelColumnHeader.untitledLinkableStringHeader(0, CODE));
        Column dateColumn =
                new Column(TableModelColumnHeader.untitledStringHeader(1, REGISTRATION_DATE));
        Column parentColumn = new Column(TableModelColumnHeader.untitledStringHeader(2, PARENT));
        Column experimentColumn = new Column(TableModelColumnHeader.untitledStringHeader(3, EXPERIMENT));
        List<Column> columns = new ArrayList<Column>();
        columns.add(codeColumn);
        columns.add(dateColumn);
        columns.add(parentColumn);
        columns.add(experimentColumn);
        int fixedColumns = columns.size();
        PropertyColumns samplePropertyColumns = new PropertyColumns();
        PropertyColumns parentPropertyColumns = new PropertyColumns();
        for (int i = 0; i < samples.size(); i++)
        {
            Sample sample = samples.get(i).getSample();
            codeColumn.addStringWithID(i, sample.getCode(), sample.getId());
            dateColumn.addDate(i, sample.getRegistrationDate());
            Sample parent = sample.getGeneratedFrom();
            parentColumn.addStringWithID(i, parent.getIdentifier(), parent.getId());
            Experiment experiment = parent.getExperiment();
            if (experiment != null)
            {
                experimentColumn.addStringWithID(i, experiment.getIdentifier(), experiment.getId());
            }
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
