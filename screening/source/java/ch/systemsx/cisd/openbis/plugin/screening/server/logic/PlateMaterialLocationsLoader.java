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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.ArrayUtils;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.MaterialSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.SingleExperimentSearchCriteria;

/**
 * Loades selected wells content: metadata and (if available) image dataset and feature vectors.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = IScreeningQuery.class)
public class PlateMaterialLocationsLoader
{
    /**
     * Finds wells containing the specified material and belonging to the specified experiment.
     * Loads wells content: metadata and (if available) image dataset and feature vectors.
     */
    public static List<WellContent> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId, String experimentPermId, boolean enrichWithImages)
    {
        return new PlateMaterialLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(geneMaterialId, experimentPermId, enrichWithImages);
    }

    public static List<WellContent> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            PlateMaterialsSearchCriteria materialCriteria, boolean enrichWithImages)
    {
        return new PlateMaterialLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(materialCriteria, enrichWithImages);
    }

    /** loads wells metadata, but no information about image or image analysis datasets */
    public static List<WellContent> loadOnlyMetadata(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId)
    {
        return new PlateMaterialLocationsLoader(session, businessObjectFactory, daoFactory)
                .getPlateLocations(geneMaterialId);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    private PlateMaterialLocationsLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
    }

    private List<WellContent> getPlateLocations(PlateMaterialsSearchCriteria materialCriteria,
            boolean enrichWithImages)
    {
        List<WellContent> locations = loadLocations(materialCriteria);
        if (enrichWithImages)
        {
            return enrichWithDatasets(locations);
        } else
        {
            return locations;
        }
    }

    private List<WellContent> getPlateLocations(TechId geneMaterialId, String experimentPermId,
            boolean enrichWithImages)
    {
        List<WellContent> locations = loadLocations(geneMaterialId, experimentPermId);
        if (enrichWithImages)
        {
            return enrichWithDatasets(locations);
        } else
        {
            return locations;
        }
    }

    private List<WellContent> getPlateLocations(TechId geneMaterialId)
    {
        return loadLocations(geneMaterialId);
    }

    private List<WellContent> enrichWithDatasets(List<WellContent> locations)
    {
        List<ExternalData> imageDatasets = loadImageDatasets(locations);
        Map<Long/* plate id */, List<ExternalData>> plateToDatasetMap =
                createPlateToDatasetMap(imageDatasets);
        Map<String, PlateImageParameters> imageParams = loadImagesReport(imageDatasets);
        return enrichWithImages(locations, plateToDatasetMap, imageParams);
    }

    private static List<WellContent> enrichWithImages(List<WellContent> wellContents,
            Map<Long/* plate id */, List<ExternalData>> plateToDatasetMap,
            Map<String, PlateImageParameters> imageParams)
    {
        List<WellContent> wellsWithImages = new ArrayList<WellContent>();
        for (WellContent wellContent : wellContents)
        {
            List<ExternalData> datasets = plateToDatasetMap.get(wellContent.getPlate().getId());
            boolean imagesExist = false;
            // there can be more than one dataset with images for each well - in such a case we will
            // have one well content duplicated for each dataset
            if (datasets != null)
            {
                for (ExternalData dataset : datasets)
                {
                    PlateImageParameters imageParameters = imageParams.get(dataset.getCode());
                    if (imageParameters != null)
                    {
                        DatasetReference datasetReference =
                                ScreeningUtils.createDatasetReference(dataset);
                        DatasetImagesReference wellImages =
                                DatasetImagesReference.create(datasetReference, imageParameters);
                        WellContent wellWithImages = wellContent.cloneWithImages(wellImages);
                        wellsWithImages.add(wellWithImages);
                        imagesExist = true;
                    }
                }
            }
            // if there are no datasets for the well content, we add it without images
            if (imagesExist == false)
            {
                wellsWithImages.add(wellContent);
            }
        }
        return wellsWithImages;
    }

    private static Map<Long/* sample id */, List<ExternalData>> createPlateToDatasetMap(
            List<ExternalData> datasets)
    {
        Map<Long, List<ExternalData>> map = new HashMap<Long, List<ExternalData>>();
        for (ExternalData dataset : datasets)
        {
            Sample sample = dataset.getSample();
            if (sample != null)
            {
                Long sampleId = sample.getId();

                List<ExternalData> plateDatasets = map.get(sampleId);
                if (plateDatasets == null)
                {
                    plateDatasets = new ArrayList<ExternalData>();
                    map.put(sampleId, plateDatasets);
                }
                plateDatasets.add(dataset);
            }
        }
        return map;
    }

    private Map<String/* dataset code */, PlateImageParameters> loadImagesReport(
            List<ExternalData> imageDatasets)
    {
        List<PlateImageParameters> imageParameters = new ArrayList<PlateImageParameters>();
        for (ExternalData dataSet : imageDatasets)
        {
            imageParameters.add(ScreeningUtils.loadImageParameters(dataSet, businessObjectFactory));
        }
        return asDatasetToParamsMap(imageParameters);
    }

    private static Map<String/* dataset code */, PlateImageParameters> asDatasetToParamsMap(
            List<PlateImageParameters> imageParameters)
    {
        Map<String, PlateImageParameters> map = new HashMap<String, PlateImageParameters>();
        for (PlateImageParameters params : imageParameters)
        {
            map.put(params.getDatasetCode(), params);
        }
        return map;
    }

    private List<ExternalData> loadImageDatasets(List<WellContent> locations)
    {
        Set<Long> plateIds = extractPlateIds(locations);

        IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        List<ExternalData> imageDatasets = datasetLister.listBySampleIds(plateIds);
        imageDatasets =
                ScreeningUtils.filterExternalDataByType(imageDatasets,
                        ScreeningConstants.IMAGE_DATASET_TYPE);
        return imageDatasets;
    }

    private static Set<Long> extractPlateIds(List<WellContent> locations)
    {
        Set<Long> ids = new HashSet<Long>();
        for (WellContent loc : locations)
        {
            ids.add(loc.getPlate().getId());
        }
        return ids;
    }

    private List<WellContent> loadLocations(PlateMaterialsSearchCriteria materialCriteria)
    {
        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations;
        MaterialSearchCriteria materialSearchCriteria =
                materialCriteria.getMaterialSearchCriteria();
        ExperimentSearchCriteria experiment = materialCriteria.getExperimentCriteria();
        IScreeningQuery dao = createDAO(daoFactory);
        if (materialSearchCriteria.tryGetMaterialCodesOrProperties() != null)
        {
            MaterialSearchCodesCriteria codesCriteria =
                    materialSearchCriteria.tryGetMaterialCodesOrProperties();

            Long expId = tryGetExperimentId(experiment);
            long[] ids = findMaterialIds(codesCriteria);
            if (expId == null)
            {
                locations =
                        dao.getPlateLocationsForMaterialCodes(ids,
                                codesCriteria.getMaterialTypeCodes());
            } else
            {
                locations =
                        dao.getPlateLocationsForMaterialCodes(ids,
                                codesCriteria.getMaterialTypeCodes(), expId);
            }

        } else if (materialSearchCriteria.tryGetMaterialId() != null)
        {
            long materialId = materialSearchCriteria.tryGetMaterialId().getId();
            Long expId = tryGetExperimentId(experiment);
            if (expId == null)
            {
                locations = dao.getPlateLocationsForMaterialId(materialId);
            } else
            {
                locations = dao.getPlateLocationsForMaterialId(materialId, expId);
            }
        } else
        {
            throw new IllegalStateException("unhandled materia search criteria: "
                    + materialSearchCriteria);
        }

        return convert(locations);
    }

    private long[] findMaterialIds(MaterialSearchCodesCriteria codesCriteria)
    {
        List<String> materialTypeCodes = Arrays.asList(codesCriteria.getMaterialTypeCodes());
        List<MaterialTypePE> types =
                daoFactory.getEntityTypeDAO(
                        ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL)
                        .listEntityTypes();
        Set<String> propertyCodes = new HashSet<String>();
        for (MaterialTypePE mt : types)
        {
            if (materialTypeCodes.contains(mt.getCode()))
            {
                for (MaterialTypePropertyTypePE mtpt : mt.getMaterialTypePropertyTypes())
                {
                    propertyCodes.add(mtpt.getPropertyType().getCode());
                }
            }
        }
        DetailedSearchCriteria criteria = new DetailedSearchCriteria();
        ArrayList<DetailedSearchCriterion> listOfCriteria =
                new ArrayList<DetailedSearchCriterion>();
        for (String value : codesCriteria.getMaterialCodesOrProperties())
        {
            listOfCriteria.add(createCodeCriterion(value));
            listOfCriteria.add(createPropertyCriterion(value, propertyCodes));
        }
        criteria.setCriteria(listOfCriteria);
        criteria.setConnection(SearchCriteriaConnection.MATCH_ANY);
        return ArrayUtils.toPrimitive(daoFactory
                .getHibernateSearchDAO()
                .searchForEntityIds(criteria,
                        ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL)
                .toArray(new Long[0]));
    }

    private DetailedSearchCriterion createPropertyCriterion(String value,
            Set<String> allEntityPropertyCodes)
    {
        DetailedSearchCriterion criterion = new DetailedSearchCriterion();
        criterion.setField(DetailedSearchField.createAnyPropertyField(new ArrayList<String>(
                allEntityPropertyCodes)));
        criterion.setValue(value);
        return criterion;
    }

    private DetailedSearchCriterion createCodeCriterion(String code)
    {
        DetailedSearchCriterion criterion = new DetailedSearchCriterion();
        criterion.setField(DetailedSearchField
                .createAttributeField(MaterialAttributeSearchFieldKind.CODE));
        criterion.setValue(code);
        return criterion;
    }

    private Long tryGetExperimentId(ExperimentSearchCriteria experiment)
    {
        SingleExperimentSearchCriteria exp = experiment.tryGetExperiment();
        return exp == null ? null : exp.getExperimentId().getId();
    }

    private List<WellContent> loadLocations(TechId geneMaterialId, String experimentPermId)
    {
        final long experimentId = loadExperimentIdByPermId(experimentPermId);

        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations =
                createDAO(daoFactory).getPlateLocationsForMaterialId(geneMaterialId.getId(),
                        experimentId);

        return convert(locations);
    }

    private List<WellContent> loadLocations(TechId geneMaterialId)
    {
        DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations =
                createDAO(daoFactory).getPlateLocationsForMaterialId(geneMaterialId.getId());
        return convert(locations);
    }

    private List<WellContent> convert(
            DataIterator<ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent> locations)
    {
        List<WellContent> wellLocations = new ArrayList<WellContent>();
        for (ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent location : locations)
        {
            wellLocations.add(convert(location));
        }
        sortByMaterialName(wellLocations);
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        List<Material> containedMaterials = getMaterials(wellLocations);
        materialLister.enrichWithProperties(containedMaterials);
        return wellLocations;
    }

    private List<Material> getMaterials(List<WellContent> wellLocations)
    {
        List<Material> materials = new ArrayList<Material>();
        for (WellContent wc : wellLocations)
        {
            materials.add(wc.getMaterialContent());
        }
        return materials;
    }

    private static void sortByMaterialName(List<? extends WellContent> wellLocations)
    {
        Collections.sort(wellLocations, new Comparator<WellContent>()
            {
                public int compare(WellContent o1, WellContent o2)
                {
                    Material m1 = o1.getMaterialContent();
                    Material m2 = o2.getMaterialContent();
                    return m1.getCode().compareTo(m2.getCode());
                }
            });
    }

    private static WellContent convert(
            ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent loc)
    {
        WellLocation location = ScreeningUtils.tryCreateLocationFromMatrixCoordinate(loc.well_code);
        EntityReference well =
                new EntityReference(loc.well_id, loc.well_code, loc.well_type_code,
                        EntityKind.SAMPLE, loc.well_perm_id);
        EntityReference plate =
                new EntityReference(loc.plate_id, loc.plate_code, loc.plate_type_code,
                        EntityKind.SAMPLE, loc.plate_perm_id);
        Material materialContent = new Material();
        materialContent.setCode(loc.material_content_code);
        materialContent.setId(loc.material_content_id);
        MaterialType type = new MaterialType();
        type.setCode(loc.material_content_type_code);
        materialContent.setMaterialType(type);
        return new WellContent(location, well, plate, convertExperiment(loc), materialContent);
    }

    private static ExperimentReference convertExperiment(
            ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.WellContent loc)
    {
        return new ExperimentReference(loc.exp_id, loc.exp_perm_id, loc.exp_code,
                loc.exp_type_code, loc.proj_code, loc.space_code);
    }

    private static IScreeningQuery createDAO(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        return QueryTool.getQuery(connection, IScreeningQuery.class);
    }

    private long loadExperimentIdByPermId(String experimentPermId)
    {
        final ExperimentPE experiment =
                daoFactory.getExperimentDAO().tryGetByPermID(experimentPermId);
        if (experiment == null)
        {
            throw new UserFailureException("Unkown experiment for permId '" + experimentPermId
                    + "'.");
        }
        return experiment.getId();
    }
}
