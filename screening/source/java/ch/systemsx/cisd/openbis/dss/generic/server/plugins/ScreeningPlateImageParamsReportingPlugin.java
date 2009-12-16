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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.IDataStructure;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.IHCSImageFormattedData;
import ch.systemsx.cisd.bds.v1_0.IDataStructureV1_0;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin which shows parameters of the images acquired for a plate dataset.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningPlateImageParamsReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    // -------- column headers used in a DSS service to describe images for a plate ------

    private static final String DATASET = "Dataset";

    private static final String ROWS = "Rows";

    private static final String COLUMNS = "Columns";

    private static final String TILE_ROWS_NUM = "Tile rows";

    private static final String TILE_COLS_NUM = "Tile columns";

    private static final String CHANNELS_NUM = "Number of channels";

    // ----------

    private static final long serialVersionUID = 1L;

    public ScreeningPlateImageParamsReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        addReportHeaders(builder);
        DataStructureLoader loader = new DataStructureLoader(storeRoot);
        for (DatasetDescription dataset : datasets)
        {
            IDataStructureV1_0 structure = createDatasetAccessor(loader, dataset);
            IHCSImageFormattedData imageAccessor = getImageAccessor(structure);
            addReportRows(builder, dataset, imageAccessor);
            structure.close();
        }
        return builder.getTableModel();
    }

    private void addReportHeaders(SimpleTableModelBuilder builder)
    {
        // Note: we rely on that column order at the openBIS server side!
        builder.addHeader(DATASET);
        builder.addHeader(ROWS);
        builder.addHeader(COLUMNS);
        builder.addHeader(TILE_ROWS_NUM);
        builder.addHeader(TILE_COLS_NUM);
        builder.addHeader(CHANNELS_NUM);
    }

    private void addReportRows(SimpleTableModelBuilder builder, DatasetDescription dataset,
            IHCSImageFormattedData imageAccessor)
    {
        Geometry plateGeometry = imageAccessor.getPlateGeometry();
        Geometry wellGeometry = imageAccessor.getWellGeometry();
        int channels = imageAccessor.getChannelCount();
        List<ISerializableComparable> row =
                Arrays.<ISerializableComparable> asList(asText(dataset.getDatasetCode()),
                        asNum(plateGeometry.getRows()), asNum(plateGeometry.getColumns()),
                        asNum(wellGeometry.getRows()), asNum(wellGeometry.getColumns()),
                        asNum(channels));
        builder.addRow(row);
    }

    private static ISerializableComparable asText(String text)
    {
        return new StringTableCell(text);
    }

    private static ISerializableComparable asNum(int num)
    {
        return new IntegerTableCell(num);
    }

    private IHCSImageFormattedData getImageAccessor(IDataStructureV1_0 structure)
    {
        return (IHCSImageFormattedData) structure.getFormattedData();
    }

    private IDataStructureV1_0 createDatasetAccessor(DataStructureLoader loader,
            DatasetDescription dataset)
    {
        IDataStructure dataStructure = loader.load(dataset.getDataSetLocation(), false);
        return (IDataStructureV1_0) dataStructure;
    }
}
