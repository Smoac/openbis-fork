/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils.ICollectionMappingFunction;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.collections.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.GroupByMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.utils.IGroupKeyExtractor;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;

/**
 * Loads feature vectors for chosen plates.
 * 
 * @author Tomasz Pylak
 */
public class WellFeatureCollectionLoader extends AbstractContentLoader
{
    public WellFeatureCollectionLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory)
    {
        super(session, businessObjectFactory, daoFactory);
    }

    /** Loads feature vectors for chosen plates and set of features */
    public WellFeatureCollection<FeatureVectorValues> tryLoadWellSingleFeatureVectors(
            Set<PlateIdentifier> plates, List<String> featureCodes,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        StopWatch watch = new StopWatch();
        watch.start();
        FeatureVectorDatasetLoader datasetsRetriever =
                createFeatureVectorDatasetsRetriever(plates, analysisProcedureCriteria);
        List<ExternalData> featureVectorDatasets = datasetsRetriever.getFeatureVectorDatasets();
        if (featureVectorDatasets.isEmpty())
        {
            return null;
        }
        List<DatasetReference> datasetPerPlate = chooseSingleDatasetForPlate(featureVectorDatasets);
        WellFeatureCollection<FeatureVectorValues> features =
                FeatureVectorRetriever.tryFetch(datasetPerPlate, featureCodes,
                        businessObjectFactory);

        operationLog.info(String.format("[%d msec] Fetching %d feature vectors from %d datasets.",
                watch.getTime(), (features == null ? 0 : features.getFeatures().size()),
                featureVectorDatasets.size()));

        return features;
    }

    // TODO 2011-04-04, Tomasz Pylak: here if the plate has more than one dataset assigned, we
    // take the first and ignore the rest. The clean solution would be to introduce analysis
    // runs, where each plate has at most one analysis dataset in each run. {@link
    // UniqueKeyViolationStrategy} could be set to {@link UniqueKeyViolationStrategy.ERROR} in
    // such a case.
    private static List<DatasetReference> chooseSingleDatasetForPlate(List<ExternalData> datasets)
    {
        Collections.sort(datasets);
        TableMap<String, ExternalData> plateToDatasetMap =
                new TableMap<String, ExternalData>(datasets,
                        new IKeyExtractor<String, ExternalData>()
                            {
                                public String getKey(ExternalData externalData)
                                {
                                    Sample plate = externalData.getSample();
                                    return plate != null ? plate.getPermId() : null;
                                }
                            }, UniqueKeyViolationStrategy.KEEP_FIRST);

        List<DatasetReference> datasetPerPlate = new ArrayList<DatasetReference>();
        for (String platePermId : plateToDatasetMap.keySet())
        {
            if (platePermId != null)
            {
                ExternalData dataset = plateToDatasetMap.getOrDie(platePermId);
                DatasetReference datasetReference = ScreeningUtils.createDatasetReference(dataset);
                datasetPerPlate.add(datasetReference);
            }
        }
        return datasetPerPlate;
    }

    /**
     * Fetches feature vectors from different datastores and merges them (assuming that feature
     * codes are the same).
     */
    private static class FeatureVectorRetriever
    {
        public static WellFeatureCollection<FeatureVectorValues> tryFetch(
                Collection<DatasetReference> datasets, List<String> featureCodes,
                IScreeningBusinessObjectFactory businessObjectFactory)
        {
            assert datasets.size() > 0 : "No feature vector datasets specified.";
            return new FeatureVectorRetriever(businessObjectFactory, featureCodes)
                    .tryFetch(datasets);
        }

        private final IScreeningBusinessObjectFactory businessObjectFactory;

        private final List<String> featureCodes;

        public FeatureVectorRetriever(IScreeningBusinessObjectFactory businessObjectFactory,
                List<String> featureCodes)
        {
            this.businessObjectFactory = businessObjectFactory;
            this.featureCodes = featureCodes;
        }

        private WellFeatureCollection<FeatureVectorValues> tryFetch(
                Collection<DatasetReference> datasets)
        {
            GroupByMap<String/* datastore code */, DatasetReference> datastoreToDatasetsMap =
                    GroupByMap.create(datasets, new IGroupKeyExtractor<String, DatasetReference>()
                        {
                            public String getKey(DatasetReference datasetReference)
                            {
                                return datasetReference.getDatastoreCode();
                            }
                        });
            WellFeatureCollection<FeatureVectorValues> allFeatures = null;
            for (String datastoreCode : datastoreToDatasetsMap.getKeys())
            {
                List<DatasetReference> datasetsForDatastore =
                        datastoreToDatasetsMap.getOrDie(datastoreCode);
                WellFeatureCollection<FeatureVectorValues> features =
                        fetchFromDatastore(datastoreCode, datasetsForDatastore);
                if (allFeatures == null)
                {
                    allFeatures = features;
                } else
                {
                    mergeFeatures(allFeatures, features);
                }
            }
            return allFeatures;
        }

        private static void mergeFeatures(WellFeatureCollection<FeatureVectorValues> allFeatures,
                WellFeatureCollection<FeatureVectorValues> features)
        {
            if (allFeatures.getFeatureCodes().equals(features.getFeatureCodes()) == false)
            {
                throw new IllegalStateException(
                        "Cannot merge feature vectors from different datastores because the have different set of features: '"
                                + allFeatures.getFeatureCodes()
                                + "' and '"
                                + features.getFeatureCodes() + "'.");
            }
            allFeatures.getFeatures().addAll(features.getFeatures());
        }

        private WellFeatureCollection<FeatureVectorValues> fetchFromDatastore(String datastoreCode,
                List<DatasetReference> datasets)
        {
            IHCSFeatureVectorLoader loader =
                    businessObjectFactory.createHCSFeatureVectorLoader(datastoreCode);
            return loader.fetchDatasetFeatureValues(extractCodes(datasets), featureCodes);
        }

        private static List<String> extractCodes(List<DatasetReference> datasets)
        {
            return CollectionUtils.map(datasets,
                    new ICollectionMappingFunction<String, DatasetReference>()
                        {
                            public String map(DatasetReference element)
                            {
                                return element.getCode();
                            }
                        });
        }
    }

    // TODO 2011-06-21, Tomasz Pylak: this looks pretty unefficient. Could we do this erlier,
    // avoiding converting these objects?
    public static float[] asFeatureVectorValues(FeatureVectorValues featureVector)
    {
        FeatureValue[] featureValues = featureVector.getFeatureValues();
        float[] values = new float[featureValues.length];
        int i = 0;
        for (FeatureValue featureValue : featureValues)
        {
            values[i++] = asFloat(featureValue);
        }
        return values;
    }

    private static float asFloat(FeatureValue featureValue)
    {
        return featureValue.isFloat() ? featureValue.asFloat() : Float.NaN;
    }
}
