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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleDateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleImageHtmlRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.ComparableCellValueHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GeneratedImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * Definition of dataset report table columns.
 * 
 * @author Tomasz Pylak
 */
public class DataSetReportColumnDefinition implements IColumnDefinition<TableModelRow>
{
    private TableModelColumnHeader columnHeader;

    private String downloadURL;

    private String sessionID;

    public DataSetReportColumnDefinition(TableModelColumnHeader columnHeader, String downloadURL,
            String sessionID)
    {
        this.columnHeader = columnHeader;
        this.downloadURL = downloadURL;
        this.sessionID = sessionID;
    }

    public Comparable<?> tryGetComparableValue(GridRowModel<TableModelRow> rowModel)
    {
        ISerializableComparable value = getCellValue(rowModel);
        return ComparableCellValueHelper.unwrap(value);
    }

    public EntityKind tryGetEntityKind()
    {
        return columnHeader.tryGetEntityKind();
    }

    public String getHeader()
    {
        return columnHeader.getTitle();
    }

    public int getIndex()
    {
        return columnHeader.getIndex();
    }

    public String getIdentifier()
    {
        return "colIndex_" + columnHeader.getIndex();
    }

    public String getValue(GridRowModel<TableModelRow> rowModel)
    {
        ISerializableComparable cell = getCellValue(rowModel);
        if (cell instanceof ImageTableCell)
        {
            ImageTableCell imageCell = (ImageTableCell) cell;
            int width = imageCell.getMaxThumbnailWidth();
            int height = imageCell.getMaxThumbnailHeight();
            String imagePath = imageCell.getPath();
            return SimpleImageHtmlRenderer.createEmbededDatastoreImageHtml(imagePath, width,
                    height, downloadURL, sessionID);
        }
        if (cell instanceof DateTableCell)
        {
            return SimpleDateRenderer.renderDate(((DateTableCell) cell).getDateTime());
        }
        if (cell instanceof GeneratedImageTableCell)
        {
            return ((GeneratedImageTableCell) cell).getHTMLString(downloadURL, sessionID);
        }
        return cell.toString();
    }

    public boolean isNumeric()
    {
        return columnHeader.isNumeric();
    }

    public DataTypeCode getDataType()
    {
        return columnHeader.getDataType();
    }

    private ISerializableComparable getCellValue(GridRowModel<TableModelRow> rowModel)
    {
        int index = columnHeader.getIndex();
        TableModelRow originalObject = rowModel.getOriginalObject();
        ISerializableComparable cell = originalObject.getValues().get(index);
        return cell;
    }

    public String tryToGetProperty(String key)
    {
        return null;
    }

    // GWT only
    @SuppressWarnings("unused")
    private DataSetReportColumnDefinition()
    {
    }
}
