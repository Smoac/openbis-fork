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

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;

/**
 * A class for fast dataset listing.
 * 
 * @author Tomasz Pylak
 */
public interface IDatasetLister
{
    /** @return datasets connected to the experiment with the specified id */
    List<ExternalData> listByExperimentTechId(TechId experimentId);

    /**
     * @return datasets connected to the sample with the specified id
     * @param showOnlyDirectlyConnected whether to return only directly connected datasets, or also
     *            all descendants in dataset parent-child relationship hierarchy
     */
    List<ExternalData> listBySampleTechId(TechId sampleId, boolean showOnlyDirectlyConnected);

    /** @return datasets that are parents of a dataset with the specified id */
    List<ExternalData> listByChildTechId(TechId childDatasetId);

    /** @return datasets that are parents of a dataset with the specified id */
    List<ExternalData> listByParentTechId(TechId parentDatasetId);

    //

    /** @return datasets with given ids */
    List<ExternalData> listByDatasetIds(Collection<Long> datasetIds);

    /** @return datasets specified by given criteria */
    List<ExternalData> listByTrackingCriteria(TrackingDataSetCriteria criteria);
}
