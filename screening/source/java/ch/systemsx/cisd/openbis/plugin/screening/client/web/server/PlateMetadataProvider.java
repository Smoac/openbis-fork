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
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractOriginalDataProviderWithoutHeaders;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper.Column;
import ch.systemsx.cisd.openbis.generic.client.web.server.GenericColumnsHelper.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMetadataStaticColumns;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * Original data provider for plate content.
 * 
 * @author Izabela Adamczyk
 */
class PlateMetadataProvider extends AbstractOriginalDataProviderWithoutHeaders<TableModelRow>
{

    private final List<WellMetadata> wells;

    public PlateMetadataProvider(IScreeningServer server, String sessionToken, TechId plateId)
    {
        PlateContent plateContent = server.getPlateContent(sessionToken, plateId);
        this.wells = plateContent.getPlateMetadata().getWells();
    }

    public List<TableModelRow> getOriginalData() throws UserFailureException
    {
        return GenericColumnsHelper.createTableRows(getColumns());
    }

    public List<GenericTableColumnHeader> getGenericHeaders()
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
                new Column(GenericTableColumnHeader.untitledLinkableStringHeader(
                        PlateMetadataStaticColumns.WELL.ordinal(),
                        PlateMetadataStaticColumns.WELL.getColumnId()));
        columns.add(codeColumn);

        Column typeColumn =
                new Column(GenericTableColumnHeader.untitledStringHeader(
                        PlateMetadataStaticColumns.TYPE.ordinal(),
                        PlateMetadataStaticColumns.TYPE.getColumnId()));
        columns.add(typeColumn);

        int fixedColumns = columns.size();
        PropertyColumns propertyColumns = new PropertyColumns();
        for (int i = 0; i < wells.size(); i++)
        {
            WellMetadata metadata = wells.get(i);
            Sample well = metadata.getWellSample();
            codeColumn.addStringWithID(i, well.getCode(), well.getId());
            typeColumn.addString(i, well.getSampleType().getCode());

            for (IEntityProperty wellProperty : well.getProperties())
            {
                propertyColumns.add(i, wellProperty);
            }
        }
        propertyColumns.reindexColumns(fixedColumns);
        propertyColumns.addPrefixToColumnHeaderCodes("CONTENT_PROPERTY__");
        columns.addAll(propertyColumns.getColumns());
        return columns;
    }
}
