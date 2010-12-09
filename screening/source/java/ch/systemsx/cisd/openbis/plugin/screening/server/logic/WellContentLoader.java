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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
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
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.NamedFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellFeatureVectorReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;

/**
 * Loades selected wells content: metadata and (if available) image dataset and feature vectors.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = IScreeningQuery.class)
public class WellContentLoader
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            WellContentLoader.class);

    /**
     * Finds wells containing the specified material and belonging to the specified experiment.
     * Loads wells metadata, but no information about connected datasets.
     */
    public static List<WellContent> loadOnlyMetadata(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId, String experimentPermId)
    {
        return new WellContentLoader(session, businessObjectFactory, daoFactory).loadLocations(
                geneMaterialId, experimentPermId);
    }

    /** loads wells metadata, but no information about image or image analysis datasets */
    public static List<WellContent> loadOnlyMetadata(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId geneMaterialId)
    {
        return new WellContentLoader(session, businessObjectFactory, daoFactory)
                .loadLocations(geneMaterialId);
    }

    /**
     * Finds wells matching the specified criteria. containing the specified materials and belonging
     * to the specified experiment. Loads wells content: metadata and (if available) image dataset
     * and feature vectors.
     */
    public static List<WellContent> load(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            WellSearchCriteria materialCriteria)
    {
        return new WellContentLoader(session, businessObjectFactory, daoFactory)
                .loadLocationsAndEnrich(materialCriteria);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    private final IDAOFactory daoFactory;

    private WellContentLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
        this.daoFactory = daoFactory;
    }

    private List<WellContent> loadLocationsAndEnrich(WellSearchCriteria materialCriteria)
    {
        List<WellContent> locations = loadLocations(materialCriteria);
        List<WellContent> enriched = enrichWithDatasets(locations);
        return enrichWithFeatureVectors(enriched);
    }

    private List<WellContent> enrichWithDatasets(List<WellContent> locations)
    {
        Collection<PlateIdentifier> plates = extractPlates(locations);
        FeatureVectorDatasetLoader datasetsRetriever =
                new FeatureVectorDatasetLoader(session, businessObjectFactory, null, plates);
        Collection<ExternalData> imageDatasets = datasetsRetriever.getImageDatasets();
        Map<String, ImageDatasetParameters> imageParams = loadImagesReport(imageDatasets);

        Collection<ExternalData> featureVectorDatasets =
                datasetsRetriever.getFeatureVectorDatasets();
        Collection<ExternalData> childlessImageDatasets =
                selectChildlessImageDatasets(imageDatasets, featureVectorDatasets);

        Map<Long/* plate id */, List<ExternalData>> plateToChildlessImageDatasetMap =
                createPlateToDatasetMap(childlessImageDatasets);
        Map<Long/* plate id */, List<ExternalData>> plateToFeatureVectoreDatasetMap =
                createPlateToDatasetMap(featureVectorDatasets);

        return enrichWithDatasets(locations, plateToChildlessImageDatasetMap,
                plateToFeatureVectoreDatasetMap, imageParams);
    }

    private static Collection<ExternalData> selectChildlessImageDatasets(
            Collection<ExternalData> imageDatasets, Collection<ExternalData> featureVectorDatasets)
    {
        Collection<ExternalData> childlessImageDatasets = new ArrayList<ExternalData>();
        Set<String> parentImageDatasetCodes = extractParentDatasetCodes(featureVectorDatasets);
        for (ExternalData imageDataset : imageDatasets)
        {
            if (parentImageDatasetCodes.contains(imageDataset.getCode()) == false)
            {
                childlessImageDatasets.add(imageDataset);
            }
        }
        return childlessImageDatasets;
    }

    private static Set<String> extractParentDatasetCodes(Collection<ExternalData> datasets)
    {
        Set<String> codes = new HashSet<String>();
        for (ExternalData dataset : datasets)
        {
            Collection<ExternalData> parents = dataset.getParents();
            if (parents != null)
            {
                for (ExternalData parent : parents)
                {
                    codes.add(parent.getCode());
                }
            }
        }
        return codes;
    }

    private static Collection<PlateIdentifier> extractPlates(List<WellContent> locations)
    {
        Collection<PlateIdentifier> plates = new ArrayList<PlateIdentifier>();
        for (WellContent location : locations)
        {
            plates.add(PlateIdentifier.createFromPermId(location.getPlate().getPermId()));
        }
        return plates;
    }

    /**
     * Connects wells with datasets.
     */
    private static List<WellContent> enrichWithDatasets(List<WellContent> wellContents,
            Map<Long/* plate id */, List<ExternalData>> plateToChildlessImageDatasetMap,
            Map<Long/* plate id */, List<ExternalData>> plateToFeatureVectoreDatasetMap,
            Map<String, ImageDatasetParameters> imageParams)
    {
        List<WellContent> wellsWithDatasets = new ArrayList<WellContent>();
        for (WellContent wellContent : wellContents)
        {
            List<WellContent> clonedWellContents =
                    enrichWithDatasetReferences(wellContent, plateToChildlessImageDatasetMap,
                            plateToFeatureVectoreDatasetMap, imageParams);
            // if there are no datasets for the well content, we add it without images
            if (clonedWellContents.isEmpty())
            {
                wellsWithDatasets.add(wellContent);
            } else
            {
                wellsWithDatasets.addAll(clonedWellContents);
            }
        }
        return wellsWithDatasets;
    }

    private List<WellContent> enrichWithFeatureVectors(List<WellContent> wellsWithDatasets)
    {
        List<WellContent> enrichedWellContents = new ArrayList<WellContent>();
        Map<String/* dss code */, List<WellContent>> datastoreToWellContentsMap =
                createAnalysisDatastoreToWellContentsMap(wellsWithDatasets);
        for (Entry<String, List<WellContent>> entry : datastoreToWellContentsMap.entrySet())
        {
            String datastoreCode = entry.getKey();
            List<WellContent> oneDatastoreWellContents = entry.getValue();

            if (datastoreCode != null)
            {
                IHCSFeatureVectorLoader loader =
                        businessObjectFactory.createHCSFeatureVectorLoader(datastoreCode);
                oneDatastoreWellContents =
                        enrichWithFeatureVectors(oneDatastoreWellContents, loader);
            }
            enrichedWellContents.addAll(oneDatastoreWellContents);
        }
        return enrichedWellContents;
    }

    // Groups elements according to the datastore server to which the connected analysis dataset
    // belongs. At null key we store all elements which do not have the dataset assigned.
    private Map<String, List<WellContent>> createAnalysisDatastoreToWellContentsMap(
            List<WellContent> wellContents)
    {
        Map<String/* dss code */, List<WellContent>> map =
                new HashMap<String/* dss code */, List<WellContent>>();
        for (WellContent wellContent : wellContents)
        {
            DatasetReference featureVectorDataset = wellContent.tryGetFeatureVectorDataset();
            String datastoreCode =
                    (featureVectorDataset == null ? null : featureVectorDataset.getDatastoreCode());
            List<WellContent> datastoreWellContents = map.get(datastoreCode);
            if (datastoreWellContents == null)
            {
                datastoreWellContents = new ArrayList<WellContent>();
            }
            datastoreWellContents.add(wellContent);
            map.put(datastoreCode, datastoreWellContents);
        }
        return map;
    }

    // should be called for wells which have feature vector datasets from the same DSS
    private static List<WellContent> enrichWithFeatureVectors(List<WellContent> wellsWithDatasets,
            IHCSFeatureVectorLoader loader)
    {
        List<WellFeatureVectorReference> wellReferences = extractWellReferences(wellsWithDatasets);
        WellFeatureCollection<FeatureVectorValues> featureVectors =
                loader.fetchWellFeatureValuesIfPossible(wellReferences);
        return enrichWithFeatureVectors(wellsWithDatasets, featureVectors);
    }

    private static class FeaturesMetadata
    {
        private final String[] featureCodes;

        private final String[] featureLabels;

        public FeaturesMetadata(String[] featureCodes, String[] featureLabels)
        {
            this.featureCodes = featureCodes;
            this.featureLabels = featureLabels;
        }
    }

    private static List<WellContent> enrichWithFeatureVectors(List<WellContent> wellsWithDatasets,
            WellFeatureCollection<FeatureVectorValues> featureVectors)
    {
        FeaturesMetadata featureMetadata = extractFeaturesMetadata(featureVectors);
        Map<WellFeatureVectorReference, FeatureVectorValues> refsToFeaturesMap =
                createReferencesToFeaturesMap(featureVectors);
        List<WellContent> enrichedWellContents = new ArrayList<WellContent>();
        for (WellContent wellContent : wellsWithDatasets)
        {
            WellContent enrichedWellContent =
                    enrichWithFeatureVectors(wellContent, refsToFeaturesMap, featureMetadata);
            enrichedWellContents.add(enrichedWellContent);
        }
        return enrichedWellContents;
    }

    private static FeaturesMetadata extractFeaturesMetadata(
            WellFeatureCollection<FeatureVectorValues> featureVectors)
    {
        int size = featureVectors.getFeatureCodesAndLabels().size();
        String[] featureCodes = new String[size];
        featureVectors.getFeatureCodes().toArray(featureCodes);

        String[] featureLabels = new String[size];
        featureVectors.getFeatureLabels().toArray(featureLabels);

        return new FeaturesMetadata(featureCodes, featureLabels);
    }

    private static WellContent enrichWithFeatureVectors(WellContent wellsWithDatasets,
            Map<WellFeatureVectorReference, FeatureVectorValues> refsToFeaturesMap,
            FeaturesMetadata featureMetadata)
    {
        WellFeatureVectorReference ref = tryAsWellReference(wellsWithDatasets);
        if (ref != null)
        {
            FeatureVectorValues featureVectorValues = refsToFeaturesMap.get(ref);
            if (featureVectorValues != null)
            {
                NamedFeatureVector fv =
                        new NamedFeatureVector(featureVectorValues.getFeatureValues(),
                                featureMetadata.featureCodes, featureMetadata.featureLabels);
                return wellsWithDatasets.cloneWithFeatureVector(fv);
            }
        }
        return wellsWithDatasets;
    }

    private static Map<WellFeatureVectorReference, FeatureVectorValues> createReferencesToFeaturesMap(
            WellFeatureCollection<FeatureVectorValues> featureVectors)
    {
        Map<WellFeatureVectorReference, FeatureVectorValues> map =
                new HashMap<WellFeatureVectorReference, FeatureVectorValues>();
        for (FeatureVectorValues featureVector : featureVectors.getFeatures())
        {
            map.put(featureVector.getFeatureVectorReference(), featureVector);
        }
        return map;
    }

    private static List<WellFeatureVectorReference> extractWellReferences(
            List<WellContent> wellsWithDatasets)
    {
        List<WellFeatureVectorReference> wellRefs = new ArrayList<WellFeatureVectorReference>();
        for (WellContent wellContent : wellsWithDatasets)
        {
            WellFeatureVectorReference ref = tryAsWellReference(wellContent);
            if (ref != null)
            {
                wellRefs.add(ref);
            }
        }
        return wellRefs;
    }

    private static WellFeatureVectorReference tryAsWellReference(WellContent wellContent)
    {
        WellFeatureVectorReference ref = null;
        WellLocation location = wellContent.tryGetLocation();
        DatasetReference featureVectorDataset = wellContent.tryGetFeatureVectorDataset();
        if (location != null && featureVectorDataset != null)
        {
            WellLocation pos = new WellLocation(location.getRow(), location.getColumn());
            ref = new WellFeatureVectorReference(featureVectorDataset.getCode(), pos);

        }
        return ref;
    }

    /**
     * Connects one WellContent with dataset references.<br>
     * We want to present all the data to the user, so if a well has several feature vector
     * datasets, it will be cloned several times. By connecting to feature vector datasets we are
     * possibly connecting to image datasets as well.<br>
     * Additionally a join with childless image datasets has to be performed.
     */
    private static List<WellContent> enrichWithDatasetReferences(WellContent wellContent,
            Map<Long, List<ExternalData>> plateToChildlessImageDatasetMap,
            Map<Long, List<ExternalData>> plateToFeatureVectoreDatasetMap,
            Map<String, ImageDatasetParameters> imageParams)
    {
        Long plateId = wellContent.getPlate().getId();
        List<WellContent> clonedWellContents = new ArrayList<WellContent>();

        List<ExternalData> featureVectoreDatasets = plateToFeatureVectoreDatasetMap.get(plateId);
        List<ExternalData> childlessImageDatasets = plateToChildlessImageDatasetMap.get(plateId);
        DatasetImagesReference singleImageDatasetOrNull =
                tryGetSingleImageDataset(childlessImageDatasets, imageParams);
        boolean singleImageAlreadyUsed = false;

        if (featureVectoreDatasets != null)
        {
            for (ExternalData featureVectoreDataset : featureVectoreDatasets)
            {
                DatasetReference featureVectoreDatasetReference =
                        ScreeningUtils.createDatasetReference(featureVectoreDataset);
                DatasetImagesReference imagesDatasetReference =
                        tryGetImageDatasetReference(featureVectoreDataset, imageParams);
                if (imagesDatasetReference == null && singleImageDatasetOrNull != null)
                {
                    // If the plate has only one childless image dataset, then we assume that it
                    // must have been the one which has been analysed. We need such a heuristic
                    // because some analysis dataset may have no parent dataset assigned.
                    imagesDatasetReference = singleImageDatasetOrNull;
                    singleImageAlreadyUsed = true;
                }
                clonedWellContents.add(wellContent.cloneWithDatasets(imagesDatasetReference,
                        featureVectoreDatasetReference));
            }
        }

        // there can be more than one dataset with images for each well - in such a case we will
        // have one well content duplicated for each dataset
        if (childlessImageDatasets != null && singleImageAlreadyUsed == false)
        {
            for (ExternalData childlessImageDataset : childlessImageDatasets)
            {
                DatasetImagesReference imagesDatasetReference =
                        createDatasetImagesReference(childlessImageDataset, imageParams);
                clonedWellContents.add(wellContent.cloneWithDatasets(imagesDatasetReference, null));
            }
        }
        return clonedWellContents;
    }

    private static DatasetImagesReference tryGetSingleImageDataset(
            List<ExternalData> childlessImageDatasets, Map<String, ImageDatasetParameters> imageParams)
    {
        if (childlessImageDatasets != null && childlessImageDatasets.size() == 1)
        {
            ExternalData singleImageDataset = childlessImageDatasets.get(0);
            return createDatasetImagesReference(singleImageDataset, imageParams);
        } else
        {
            return null;
        }
    }

    private static DatasetImagesReference tryGetImageDatasetReference(
            ExternalData featureVectoreDataset, Map<String, ImageDatasetParameters> imageParams)
    {
        Collection<ExternalData> parents = featureVectoreDataset.getParents();
        if (parents != null && parents.size() == 1)
        {
            ExternalData imageDataset = parents.iterator().next();
            return createDatasetImagesReference(imageDataset, imageParams);
        } else
        {
            return null;
        }
    }

    private static DatasetImagesReference createDatasetImagesReference(ExternalData imageDataset,
            Map<String, ImageDatasetParameters> imageParams)
    {
        ImageDatasetParameters imageParameters = imageParams.get(imageDataset.getCode());
        if (imageParameters != null)
        {
            return DatasetImagesReference.create(
                    ScreeningUtils.createDatasetReference(imageDataset), imageParameters);
        } else
        {
            operationLog.error("Cannot find image parameters for dataset: "
                    + imageDataset.getCode() + ". It will not be displayed");
            return null;
        }
    }

    private static Map<Long/* sample id */, List<ExternalData>> createPlateToDatasetMap(
            Collection<ExternalData> datasets)
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

    private Map<String/* dataset code */, ImageDatasetParameters> loadImagesReport(
            Collection<ExternalData> imageDatasets)
    {
        List<ImageDatasetParameters> imageParameters = new ArrayList<ImageDatasetParameters>();
        for (ExternalData dataSet : imageDatasets)
        {
            imageParameters.add(ScreeningUtils.loadImageParameters(dataSet, businessObjectFactory));
        }
        return asDatasetToParamsMap(imageParameters);
    }

    private static Map<String/* dataset code */, ImageDatasetParameters> asDatasetToParamsMap(
            List<ImageDatasetParameters> imageParameters)
    {
        Map<String, ImageDatasetParameters> map = new HashMap<String, ImageDatasetParameters>();
        for (ImageDatasetParameters params : imageParameters)
        {
            map.put(params.getDatasetCode(), params);
        }
        return map;
    }

    private List<WellContent> loadLocations(WellSearchCriteria materialCriteria)
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
        criteria.setUseWildcardSearchMode(codesCriteria.isExactMatchOnly());
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

    private static List<Material> getMaterials(List<WellContent> wellLocations)
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
