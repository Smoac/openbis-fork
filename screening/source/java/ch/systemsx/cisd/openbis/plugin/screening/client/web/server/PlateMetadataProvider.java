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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper.Column;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.StaticColumns;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Original data provider for plate content.
 *
 * @author Izabela Adamczyk
 */
class PlateMetadataProvider implements IOriginalDataProvider<GenericTableRow>
{

    private final List<WellMetadata> wells;

    private final PlateContent plateContent;

    public PlateMetadataProvider(IScreeningServer server, String sessionToken, TechId plateId)
    {
        plateContent = server.getPlateContent(sessionToken, plateId);
        wells = plateContent.getWells();
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
        List<Column> columns = new ArrayList<Column>();
        Column codeColumn =
                new Column(GenericTableColumnHeader.untitledLinkableStringHeader(StaticColumns.WELL
                        .ordinal(), StaticColumns.WELL.colId()));
        columns.add(codeColumn);

        Column typeColumn =
                new Column(GenericTableColumnHeader.untitledStringHeader(StaticColumns.TYPE
                        .ordinal(), StaticColumns.TYPE.colId()));
        columns.add(typeColumn);

        Column contentColumn =
                new Column(GenericTableColumnHeader.untitledLinkableStringHeader(
                        StaticColumns.CONTENT.ordinal(), StaticColumns.CONTENT.colId()));
        columns.add(contentColumn);

        Column contentTypeColumn =
                new Column(GenericTableColumnHeader.untitledStringHeader(StaticColumns.CONTENT_TYPE
                        .ordinal(), StaticColumns.CONTENT_TYPE.colId()));
        columns.add(contentTypeColumn);

        Column geneCodeColumn =
                new Column(GenericTableColumnHeader.untitledLinkableStringHeader(
                        StaticColumns.INHIBITED_GENE.ordinal(), StaticColumns.INHIBITED_GENE
                                .colId()));
        columns.add(geneCodeColumn);

        Column showGeneColumn =
                new Column(GenericTableColumnHeader.untitledLinkableStringHeader(
                        StaticColumns.GENE_DETAILS.ordinal(), StaticColumns.GENE_DETAILS.colId()));
        columns.add(showGeneColumn);
        int fixedColumns = columns.size();
        PropertyColumns contentPropertyColumns = new PropertyColumns();
        PropertyColumns genePropertyColumns = new PropertyColumns();
        for (int i = 0; i < wells.size(); i++)
        {
            WellMetadata metadata = wells.get(i);
            Sample well = metadata.getWellSample();
            codeColumn.addStringWithID(i, well.getCode(), well.getId());
            typeColumn.addString(i, well.getSampleType().getCode());
            Material content = metadata.tryGetContent();
            if (content != null)
            {
                contentColumn.addStringWithID(i, content.getCode(), content.getId());
                contentTypeColumn.addString(i, content.getMaterialType().getCode());
                addPropertyTypes(contentPropertyColumns, i, content);
            }
            Material gene = metadata.tryGetGene();
            if (gene != null)
            {
                geneCodeColumn.addStringWithID(i, gene.getCode(), gene.getId());
                // NOTE: If we want to include the gene library url in exported data,
                // we must configure it outside the dictionary (see PlateMetadataBrowser).
                showGeneColumn.addString(i, "Show");
                addPropertyTypes(genePropertyColumns, i, gene);
            }
        }
        int nextIndex = contentPropertyColumns.reindexColumns(fixedColumns);
        contentPropertyColumns.addPrefixToColumnHeaderCodes("CONTENT_PROPERTY__");
        columns.addAll(contentPropertyColumns.getColumns());
        genePropertyColumns.reindexColumns(nextIndex);
        genePropertyColumns.addPrefixToColumnHeaderCodes("GENE_PROPERTY__");
        columns.addAll(genePropertyColumns.getColumns());
        return columns;
    }

    private void addPropertyTypes(PropertyColumns columns, int index,
            IEntityPropertiesHolder propertiesHolder)
    {
        for (IEntityProperty property : propertiesHolder.getProperties())
        {
            columns.add(index, property);
        }
    }
}
