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

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder.asNum;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder.asText;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IHCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin which shows parameters of the images acquired for a plate dataset.
 * 
 * @author Tomasz Pylak
 */
// TODO 2010-06-25, Piotr Buczek: remove
public class ScreeningPlateImageParamsReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    // -------- column headers used in a DSS service to describe images for a plate ------

    private static final String DATASET = "Dataset";

    private static final String ROWS = "Rows";

    private static final String COLUMNS = "Columns";

    private static final String TILE_ROWS_NUM = "Tile rows";

    private static final String TILE_COLS_NUM = "Tile columns";

    private static final String CHANNELS_NAMES = "Names of channels";

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
        for (DatasetDescription dataset : datasets)
        {
            File datasetFile = new File(storeRoot, dataset.getDataSetLocation());
            IHCSImageDatasetLoader imageAccessor =
                    HCSImageDatasetLoaderFactory.create(datasetFile, dataset.getDatasetCode());
            addReportRows(builder, dataset, imageAccessor);
            imageAccessor.close();
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
        builder.addHeader(CHANNELS_NAMES);
    }

    private void addReportRows(SimpleTableModelBuilder builder, DatasetDescription dataset,
            IHCSImageDatasetLoader imageAccessor)
    {
        Geometry plateGeometry = imageAccessor.getPlateGeometry();
        Geometry wellGeometry = imageAccessor.getWellGeometry();
        List<String> channelsNames = imageAccessor.getChannelsNames();
        StringBuilder sb = new StringBuilder();
        for (String val : channelsNames)
        {
            if (sb.length() != 0)
            {
                sb.append(",");
            }
            sb.append(StringEscapeUtils.escapeCsv(val));
        }
        List<ISerializableComparable> row =
                Arrays.<ISerializableComparable> asList(asText(dataset.getDatasetCode()),
                        asNum(plateGeometry.getRows()), asNum(plateGeometry.getColumns()),
                        asNum(wellGeometry.getRows()), asNum(wellGeometry.getColumns()), asText(sb
                                .toString()));
        builder.addRow(row);
    }

}
