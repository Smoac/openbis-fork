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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * @author Tomasz Pylak
 */
public class PlateImage implements IsSerializable
{
    // -------- column headers used in a DSS service to describe images for a plate ------

    public static final String IMAGE = "Thumbnail";

    public static final String CHANNEL = "Channel";

    public static final String TILE = "Tile";

    public static final String COLUMN = "Column";

    public static final String ROW = "Row";

    public static final String DATASET_CODE = "Dataset Code";

    // ----------

    /** @param plateReport report of a screening plugun with images for the whole plate */
    public static final PlateImages createImages(String datasetCode, String downloadUrl,
            TableModel plateReport, TableModel imageParamsReport)
    {
        return new PlateImages(datasetCode, downloadUrl, createImageList(plateReport),
                createImageParams(imageParamsReport));
    }

    private static PlateImageParameters createImageParams(TableModel imageParamsReport)
    {
        PlateImageParameters params = new PlateImageParameters();
        assert imageParamsReport.getRows().size() == 1 : "exactly one row expected in imageParamsReport";
        List<ISerializableComparable> values = imageParamsReport.getRows().get(0).getValues();

        params.setRowsNum(asNum(values.get(0)));
        params.setColsNum(asNum(values.get(1)));
        params.setTileRowsNum(asNum(values.get(2)));
        params.setTileColsNum(asNum(values.get(3)));
        params.setChannelsNum(asNum(values.get(4)));
        return params;
    }

    private static final List<PlateImage> createImageList(TableModel plateReport)
    {
        List<TableModelRow> rows = plateReport.getRows();
        List<PlateImage> images = new ArrayList<PlateImage>();
        for (TableModelRow row : rows)
        {
            images.add(createImage(row));
        }
        return images;
    }

    // creates a DTO for an image from an unstructured report row
    private static PlateImage createImage(TableModelRow row)
    {
        List<ISerializableComparable> values = row.getValues();
        PlateImage image = new PlateImage();
        image.setRow(asNum(values.get(1)));
        image.setColumn(asNum(values.get(2)));
        image.setTile(asNum(values.get(3)));
        image.setChannel(asNum(values.get(4)));
        image.setImagePath(asImagePath(values.get(5)));
        return image;
    }

    private static String asImagePath(ISerializableComparable serializableComparable)
    {
        return ((ImageTableCell) serializableComparable).getPath();
    }

    private static int asNum(ISerializableComparable serializableComparable)
    {
        return (int) ((IntegerTableCell) serializableComparable).getNumber();
    }

    // ------------------------------------------

    private String imagePath;

    private int row;

    private int column;

    private int tile;

    private int channel;

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    public int getRow()
    {
        return row;
    }

    public void setRow(int row)
    {
        this.row = row;
    }

    public int getColumn()
    {
        return column;
    }

    public void setColumn(int column)
    {
        this.column = column;
    }

    /** numbered from 1 */
    public int getTile()
    {
        return tile;
    }

    public void setTile(int tile)
    {
        this.tile = tile;
    }

    /** numbered from 1 */
    public int getChannel()
    {
        return channel;
    }

    public void setChannel(int channel)
    {
        this.channel = channel;
    }
}
