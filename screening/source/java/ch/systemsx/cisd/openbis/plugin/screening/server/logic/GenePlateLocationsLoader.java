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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.builder.ToStringBuilder;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.geometry.ConversionUtils;
import ch.systemsx.cisd.common.geometry.Point;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = IScreeningQuery.class)
public class GenePlateLocationsLoader
{

    public static List<WellLocation> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId, ExperimentIdentifier experimentIdentifier)
    {
        return new GenePlateLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(geneMaterialId, experimentIdentifier);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    private final IExternalDataTable externalDataTable;

    private GenePlateLocationsLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
        this.externalDataTable = businessObjectFactory.createExternalDataTable(session);

    }

    private List<WellLocation> getPlateLocations(TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier)
    {
        List<WellLocation> locations = loadLocations(geneMaterialId, experimentIdentifier);
        List<ExternalDataPE> imageDatasets = loadImageDatasets(locations, externalDataTable);
        ImagesWithParams images = tryLoadImagesReport(imageDatasets);
        if (images != null)
        {
            enrichWithImages(locations, imageDatasets, images);
        }
        return locations;
    }

    private static void enrichWithImages(List<WellLocation> locations,
            List<ExternalDataPE> imageDatasets, ImagesWithParams images)
    {
        Map<WellPointer, List<TileImage>> imageMap = asMap(images.getImages());
        Map<Long/* plate id */, String/* datasetCode */> plateToDatasetMap =
                createPlateToDatasetMap(imageDatasets);
        Map<String, ExternalDataPE> datasetMap = asSampleMap(imageDatasets);

        for (WellLocation loc : locations)
        {
            WellPointer pointer = tryCreateWellPointer(plateToDatasetMap, loc);
            if (pointer != null)
            {
                List<TileImage> tiles = imageMap.get(pointer);
                if (tiles != null)
                {
                    ExternalDataPE dataset = datasetMap.get(pointer.getDatasetCode());
                    PlateImageParameters imageParameters =
                            images.getParamsMap().get(dataset.getCode());
                    TileImages wellImages =
                            TileImages.create(createReference(dataset), tiles, imageParameters);
                    loc.setImages(wellImages);
                }
            }
        }
    }

    private static DatasetReference createReference(ExternalDataPE dataset)
    {
        DataStorePE dataStore = dataset.getDataStore();
        return new DatasetReference(dataset.getCode(), dataStore.getCode(), dataStore
                .getDownloadUrl());
    }

    private static WellPointer tryCreateWellPointer(Map<Long, String> plateToDatasetMap,
            WellLocation location)
    {
        String datasetCode = plateToDatasetMap.get(location.getPlate().getId());
        if (datasetCode != null)
        {
            EntityReference well = location.getWell();
            String wellCode = well.getCode();
            Point wellLoc = ConversionUtils.parseSpreadsheetLocation(wellCode);
            return new WellPointer(datasetCode, wellLoc.getX() + 1, wellLoc.getY() + 1);
        } else
        {
            return null;
        }
    }

    private static Map<Long, String> createPlateToDatasetMap(List<ExternalDataPE> datasets)
    {
        Map<Long, String> map = new HashMap<Long, String>();
        for (ExternalDataPE dataset : datasets)
        {
            map.put(dataset.tryGetSample().getId(), dataset.getCode());
        }
        return map;
    }

    private static Map<WellPointer, List<TileImage>> asMap(List<TileImage> images)
    {
        Map<WellPointer, List<TileImage>> map = new HashMap<WellPointer, List<TileImage>>();
        for (TileImage image : images)
        {
            WellPointer pointer = WellPointer.create(image);
            List<TileImage> list = map.get(pointer);
            if (list == null)
            {
                list = new ArrayList<TileImage>();
                map.put(pointer, list);
            }
            list.add(image);
        }
        return map;
    }

    private static class WellPointer
    {
        private static WellPointer create(TileImage image)
        {
            return new WellPointer(image.getDatasetCode(), image.getRow(), image.getColumn());
        }

        private final String datasetCode;

        private final int row;

        private final int column;

        public WellPointer(String datasetCode, int row, int column)
        {
            this.datasetCode = datasetCode;
            this.row = row;
            this.column = column;
        }

        public String getDatasetCode()
        {
            return datasetCode;
        }

        public int getRow()
        {
            return row;
        }

        public int getColumn()
        {
            return column;
        }

        @Override
        public final boolean equals(final Object obj)
        {
            WellPointer that = (WellPointer) obj;
            return row == that.row && column == that.column && datasetCode.equals(that.datasetCode);
        }

        @Override
        public final int hashCode()
        {
            return (column * 2343 + row * 9876) ^ datasetCode.hashCode();
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this,
                    ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        }
    }

    private ImagesWithParams tryLoadImagesReport(List<ExternalDataPE> imageDatasets)
    {
        if (imageDatasets.size() == 0)
        {
            return null;
        }
        List<String> datasetCodes = asCodes(imageDatasets);
        // NOTE: assumes that all datasets are from the same datastore
        String datastoreCode = imageDatasets.get(0).getDataStore().getCode();
        List<TileImage> images =
                DatasetLoader.loadImages(datasetCodes, datastoreCode, externalDataTable);
        List<PlateImageParameters> imageParameters =
                DatasetLoader.loadImageParameters(datasetCodes, datastoreCode, externalDataTable);
        return new ImagesWithParams(images, imageParameters);
    }

    private static class ImagesWithParams extends AbstractHashable
    {
        private List<TileImage> images;

        private Map<String/* dataset code */, PlateImageParameters> paramsMap;

        public ImagesWithParams(List<TileImage> images, List<PlateImageParameters> imageParameters)
        {
            this.images = images;
            this.paramsMap = asMap(imageParameters);
        }

        private static Map<String, PlateImageParameters> asMap(
                List<PlateImageParameters> imageParameters)
        {
            Map<String, PlateImageParameters> map = new HashMap<String, PlateImageParameters>();
            for (PlateImageParameters params : imageParameters)
            {
                map.put(params.getDatasetCode(), params);
            }
            return map;
        }

        public List<TileImage> getImages()
        {
            return images;
        }

        public Map<String, PlateImageParameters> getParamsMap()
        {
            return paramsMap;
        }
    }

    private static List<String> asCodes(List<ExternalDataPE> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (ExternalDataPE dataset : datasets)
        {
            codes.add(dataset.getCode());
        }
        return codes;
    }

    private static Map<String/* dataset code */, ExternalDataPE> asSampleMap(
            List<ExternalDataPE> datasets)
    {
        Map<String, ExternalDataPE> map = new HashMap<String, ExternalDataPE>();
        for (ExternalDataPE dataset : datasets)
        {
            map.put(dataset.getCode(), dataset);
        }
        return map;
    }

    private static List<ExternalDataPE> loadImageDatasets(List<WellLocation> locations,
            IExternalDataTable externalDataTable)
    {
        List<ExternalDataPE> imageDatasets = new ArrayList<ExternalDataPE>();
        List<TechId> plateIds = extractPlateIds(locations);
        for (TechId plateId : plateIds)
        {
            List<ExternalDataPE> datasets =
                    PlateContentLoader.loadDatasets(plateId, externalDataTable);
            imageDatasets.addAll(filterImageDatasets(datasets));
        }
        return imageDatasets;
    }

    private static List<ExternalDataPE> filterImageDatasets(List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> result = new ArrayList<ExternalDataPE>();
        for (ExternalDataPE dataset : datasets)
        {
            if (dataset.getDataSetType().getCode().equalsIgnoreCase(
                    ScreeningConstants.IMAGE_DATASET_TYPE))
            {
                result.add(dataset);
            }
        }
        return result;
    }

    private static List<TechId> extractPlateIds(List<WellLocation> locations)
    {
        Set<Long> ids = new HashSet<Long>();
        for (WellLocation loc : locations)
        {
            ids.add(loc.getPlate().getId());
        }
        return asTechIdList(ids);
    }

    private static List<TechId> asTechIdList(Set<Long> ids)
    {
        List<TechId> result = new ArrayList<TechId>();
        Iterator<Long> iter = ids.iterator();
        while (iter.hasNext())
        {
            result.add(new TechId(iter.next()));
        }
        return result;
    }

    private List<WellLocation> loadLocations(TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier)
    {
        long experimentId = loadExperimentId(experimentIdentifier);

        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellLocation> locations =
                createDAO(daoFactory).getPlateLocations(geneMaterialId.getId(), experimentId);

        return convert(locations);
    }

    private List<WellLocation> convert(
            DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellLocation> locations)
    {
        List<WellLocation> wellLocations = new ArrayList<WellLocation>();
        for (ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellLocation location : locations)
        {
            wellLocations.add(convert(location));
        }
        return wellLocations;
    }

    private static WellLocation convert(
            ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellLocation loc)
    {
        EntityReference well =
                new EntityReference(loc.well_id, loc.well_code, loc.well_type_code,
                        EntityKind.SAMPLE);
        EntityReference plate =
                new EntityReference(loc.plate_id, loc.plate_code, loc.plate_type_code,
                        EntityKind.SAMPLE);
        EntityReference materialContent =
                new EntityReference(loc.material_content_id, loc.material_content_code,
                        loc.material_content_type_code, EntityKind.MATERIAL);
        return new WellLocation(well, plate, materialContent);
    }

    private static IScreeningQuery createDAO(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        return QueryTool.getQuery(connection, IScreeningQuery.class);
    }

    private long loadExperimentId(ExperimentIdentifier experimentIdentifier)
    {
        IExperimentBO experimentBO = businessObjectFactory.createExperimentBO(session);
        experimentBO.loadByExperimentIdentifier(experimentIdentifier);
        return experimentBO.getExperiment().getId();
    }
}
