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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;

/**
 * A class for fast dataset listing.
 * 
 * @author Tomasz Pylak
 */
public interface IDatasetLister
{
    /** @return datasets connected to experiments with the specified ids */
    List<ExternalData> listByExperimentTechIds(Collection<TechId> experimentIds);

    /**
     * @return datasets connected to the sample with the specified id
     * @param showOnlyDirectlyConnected whether to return only directly connected datasets, or also
     *            all descendants in dataset parent-child relationship hierarchy
     */
    List<ExternalData> listBySampleTechId(TechId sampleId, boolean showOnlyDirectlyConnected);

    /** @return datasets that are parents of a dataset with the specified id */
    List<ExternalData> listByChildTechId(TechId childDatasetId);

    /** @return datasets that are components of a dataset with the specified id */
    List<ExternalData> listByContainerTechId(TechId containerDatasetId);

    /** @return all datasets that are children of any specified dataset id */
    List<ExternalData> listByParentTechIds(Collection<Long> parentDatasetIds);

    /**
     * Returns a map with all parent data set IDs of specified data set IDs. The keys of the map are
     * IDs from the argument. A value of the map contains at least one element.
     */
    Map<Long, Set<Long>> listParentIds(Collection<Long> dataSetIDs);

    /**
     * Returns a map with all child data set IDs of specified data set IDs. The keys of the map are
     * IDs from the argument. A value of the map contains at least one element.
     */
    Map<Long, Set<Long>> listChildrenIds(Collection<Long> dataSetIDs);

    /**
     * Returns a map with all data sets of specified samples. The sample arguments are the key into
     * the returned map. The returned data sets contains all derived data sets (children, grand
     * children, etc.).
     */
    Map<Sample, List<ExternalData>> listAllDataSetsFor(List<Sample> samples);

    /**
     * Lists all data sets with specified codes. Unenriched data sets will be returned.
     */
    List<ExternalData> listByDatasetCode(Collection<String> datasetCodes);

    /**
     * Lists all data sets of specified data store. Unenriched data sets will be returned.
     */
    List<ExternalData> listByDataStore(long dataStoreID);

    /** @return datasets with given ids */
    List<ExternalData> listByDatasetIds(Collection<Long> datasetIds);

    /** @return datasets specified by given criteria */
    List<ExternalData> listByTrackingCriteria(TrackingDataSetCriteria criteria);

    /** @return datasets specified by given criteria */
    List<ExternalData> listByArchiverCriteria(String dataStoreCode, ArchiverDataSetCriteria criteria);

    /**
     * @return Datasets connected to the samples with the specified ids
     */
    List<ExternalData> listBySampleIds(Collection<Long> sampleIds);

    /**
     * @return properties of given type for given dataset ids
     */
    Map<Long, GenericEntityPropertyRecord> fetchProperties(List<Long> ids, String propertyTypeCode);
}
