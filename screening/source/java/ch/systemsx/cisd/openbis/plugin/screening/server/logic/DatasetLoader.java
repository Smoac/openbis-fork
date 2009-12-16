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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImage;

/**
 * Loads content of datasets by contacting DSS.
 * 
 * @author Tomasz Pylak
 */
public class DatasetLoader
{
    public static List<TileImage> loadImages(List<String> datasets, String datastoreCode,
            IExternalDataTable externalDataTable)
    {
        TableModel plateReport =
                externalDataTable.createReportFromDatasets(
                        ScreeningConstants.PLATE_VIEWER_REPORT_KEY, datastoreCode, datasets);
        return asImageList(plateReport);
    }

    public static List<PlateImageParameters> loadImageParameters(List<String> datasets,
            String datastoreCode, IExternalDataTable externalDataTable)
    {
        TableModel imageParamsReport =
                externalDataTable.createReportFromDatasets(
                        ScreeningConstants.PLATE_IMAGE_PARAMS_REPORT_KEY, datastoreCode, datasets);
        return asImageParams(imageParamsReport);
    }

    private static List<PlateImageParameters> asImageParams(TableModel imageParamsReport)
    {
        List<PlateImageParameters> paramsList = new ArrayList<PlateImageParameters>();
        for (TableModelRow tableModelRow : imageParamsReport.getRows())
        {
            paramsList.add(asImageParams(tableModelRow));
        }
        return paramsList;
    }

    private static PlateImageParameters asImageParams(TableModelRow tableModelRow)
    {
        PlateImageParameters params = new PlateImageParameters();
        List<ISerializableComparable> values = tableModelRow.getValues();
        params.setDatasetCode(asText(values.get(0)));
        params.setRowsNum(asNum(values.get(1)));
        params.setColsNum(asNum(values.get(2)));
        params.setTileRowsNum(asNum(values.get(3)));
        params.setTileColsNum(asNum(values.get(4)));
        params.setChannelsNum(asNum(values.get(5)));
        return params;
    }

    /**
     * @param plateImages report of a screening plugun with images for the whole plate
     */
    private static final List<TileImage> asImageList(TableModel plateReport)
    {
        List<TableModelRow> rows = plateReport.getRows();
        List<TileImage> images = new ArrayList<TileImage>();
        for (TableModelRow row : rows)
        {
            images.add(createImage(row));
        }
        return images;
    }

    // creates a DTO for an image from an unstructured report row
    private static TileImage createImage(TableModelRow row)
    {
        List<ISerializableComparable> values = row.getValues();
        TileImage image = new TileImage();
        image.setDatasetCode(asText(values.get(0)));
        image.setRow(asNum(values.get(1)));
        image.setColumn(asNum(values.get(2)));
        image.setTile(asNum(values.get(3)));
        image.setChannel(asNum(values.get(4)));
        image.setImagePath(asImagePath(values.get(5)));
        return image;
    }

    private static String asText(ISerializableComparable serializableComparable)
    {
        return serializableComparable.toString();
    }

    private static String asImagePath(ISerializableComparable serializableComparable)
    {
        return ((ImageTableCell) serializableComparable).getPath();
    }

    private static int asNum(ISerializableComparable serializableComparable)
    {
        return (int) ((IntegerTableCell) serializableComparable).getNumber();
    }
}
