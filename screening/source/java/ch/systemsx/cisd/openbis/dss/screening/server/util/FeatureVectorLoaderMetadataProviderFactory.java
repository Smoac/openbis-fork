/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.screening.server.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.IMetadataProvider;

/**
 * Creates a metadata provider for feature data set loader based on the IEncapsulatedOpenBISService
 * 
 * @author Jakub Straszewski
 */
public class FeatureVectorLoaderMetadataProviderFactory
{

    private static HashMap<String, List<String>> createContainedDatasetMapFromFeatureVectors(
            IEncapsulatedOpenBISService service,
            List<? extends FeatureVectorDatasetReference> featureDatasets)
    {

        List<String> dsCodes = new LinkedList<String>();

        for (FeatureVectorDatasetReference ds : featureDatasets)
        {
            dsCodes.add(ds.getDatasetCode());
        }

        return createContainedDatasetMap(service, dsCodes);
    }

    private static HashMap<String, List<String>> createContainedDatasetMap(
            IEncapsulatedOpenBISService service, List<String> dsCodes)
    {
        List<AbstractExternalData> dataSets = service.listDataSetsByCode(dsCodes);

        HashMap<String, List<String>> containedDataSetsMap = new HashMap<String, List<String>>();

        for (AbstractExternalData dataSet : dataSets)
        {
            List<String> list = getContainedDatasets(dataSet);
            containedDataSetsMap.put(dataSet.getCode(), list);
        }
        return containedDataSetsMap;
    }

    private static List<String> getContainedDatasets(AbstractExternalData dataSet)
    {
        List<String> list;
        ContainerDataSet container = dataSet.tryGetAsContainerDataSet();
        if (container != null)
        {
            list = new LinkedList<String>();
            for (AbstractExternalData contained : container.getContainedDataSets())
            {
                list.add(contained.getCode());
            }
        } else
        {
            list = Collections.<String> emptyList();
        }
        return list;
    }

    /**
     * Metadata provider that get's the request for dataset only once for all datasets at the moment
     * of creation.
     */
    public static IMetadataProvider createMetadataProvider(
            final IEncapsulatedOpenBISService openBISService, final List<String> dataSetCodes)
    {

        return createMetadataProvider(openBISService,
                createContainedDatasetMap(openBISService, dataSetCodes));
    }

    /**
     * Creates a metadata provider that will always ask openbis service about the required
     * information
     */
    public static IMetadataProvider createMetadataProvider(
            final IEncapsulatedOpenBISService openBISService)
    {
        return new IMetadataProvider()
            {

                @Override
                public SampleIdentifier tryGetSampleIdentifier(String samplePermId)
                {
                    return openBISService.tryGetSampleIdentifier(samplePermId);
                }

                @Override
                public List<String> tryGetContainedDatasets(String datasetCode)
                {
                    AbstractExternalData dataSet = openBISService.tryGetDataSet(datasetCode);
                    return getContainedDatasets(dataSet);
                }
            };
    }

    /**
     * Metadata provider that get's the request for dataset only once for all datasets at the moment
     * of creation
     */
    public static IMetadataProvider createMetadataProviderFromFeatureVectors(
            final IEncapsulatedOpenBISService openBISService,
            final List<? extends FeatureVectorDatasetReference> featureDatasets)
    {

        return createMetadataProvider(openBISService,
                createContainedDatasetMapFromFeatureVectors(openBISService, featureDatasets));
    }

    private static IMetadataProvider createMetadataProvider(
            final IEncapsulatedOpenBISService openBISService,
            final HashMap<String, List<String>> containedDataSetsMap)
    {

        return new IMetadataProvider()
            {
                @Override
                public SampleIdentifier tryGetSampleIdentifier(String samplePermId)
                {
                    return openBISService.tryGetSampleIdentifier(samplePermId);
                }

                @Override
                public List<String> tryGetContainedDatasets(String datasetCode)
                {
                    if (containedDataSetsMap.containsKey(datasetCode))
                    {
                        return containedDataSetsMap.get(datasetCode);
                    } else
                    {
                        throw new IllegalArgumentException(
                                "Data set code unknown to the provider. " + datasetCode);
                    }
                }
            };
    }
}
