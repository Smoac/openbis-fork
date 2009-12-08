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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.bds.DataStructureLoader;
import ch.systemsx.cisd.bds.IDataStructure;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.IHCSImageFormattedData;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.v1_0.IDataStructureV1_0;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImage;

/**
 * Reporting plugin which shows all images in wells of a plate dataset.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningImageReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final long serialVersionUID = 1L;

    public ScreeningImageReportingPlugin(Properties properties, File storeRoot)
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
        builder.addHeader(PlateImage.DATASET_CODE);
        builder.addHeader(PlateImage.ROW);
        builder.addHeader(PlateImage.COLUMN);
        builder.addHeader(PlateImage.TILE);
        builder.addHeader(PlateImage.CHANNEL);
        builder.addHeader(PlateImage.IMAGE);
    }

    private void addReportRows(SimpleTableModelBuilder builder, DatasetDescription dataset,
            IHCSImageFormattedData imageAccessor)
    {
        Geometry plateGeometry = imageAccessor.getPlateGeometry();
        Geometry wellGeometry = imageAccessor.getWellGeometry();
        for (int channel = 1; channel <= imageAccessor.getChannelCount(); channel++)
        {
            for (Location wellLocation : new GeometryIterable(plateGeometry))
            {
                for (Location tileLocation : new GeometryIterable(wellGeometry))
                {
                    INode img =
                            imageAccessor.tryGetStandardNodeAt(channel, wellLocation, tileLocation);
                    if (img != null)
                    {

                        ISerializableComparable image =
                                createImageCell(dataset, new File(img.getPath()));
                        String datasetCode = dataset.getDatasetCode();
                        int tileNumber =
                                tileLocation.getX() + (tileLocation.getY() - 1)
                                        * wellGeometry.getColumns();
                        List<ISerializableComparable> row =
                                Arrays.<ISerializableComparable> asList(new StringTableCell(
                                        datasetCode), asNum(wellLocation.getY()),
                                        asNum(wellLocation.getX()), asNum(tileNumber),
                                        asNum(channel), image);
                        builder.addRow(row);
                    }
                }
            }
        }
    }

    private static ISerializableComparable asNum(int num)
    {
        return new IntegerTableCell(num);
    }

    private static class GeometryIterable implements Iterable<Location>
    {
        private final Geometry geometry;

        public GeometryIterable(Geometry geometry)
        {
            this.geometry = geometry;
        }

        public Iterator<Location> iterator()
        {
            return new Iterator<Location>()
                {
                    private int x = 0;

                    private int y = 1;

                    public boolean hasNext()
                    {
                        return x < geometry.getColumns() || y < geometry.getRows();
                    }

                    public Location next()
                    {
                        if (x < geometry.getColumns())
                        {
                            x++;
                        } else
                        {
                            x = 1;
                            y++;
                        }
                        return new Location(x, y);
                    }

                    public void remove()
                    {
                        throw new NotImplementedException();
                    }
                };
        }
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

    private static ISerializableComparable createImageCell(DatasetDescription dataset, File file)
    {
        if (ImageUtil.isImageFile(file))
        {
            String code = dataset.getDatasetCode();
            String location = dataset.getDataSetLocation();
            return new ImageTableCell(code, location, file.getPath(), 100, 60);
        }
        return new StringTableCell(file.getName());
    }
}
