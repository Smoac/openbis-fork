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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.WellReplicaSummaryCalculator;

/**
 * Interface to access well feature vector and the material, used by
 * {@link WellReplicaSummaryCalculator}.
 * 
 * @author Tomasz Pylak
 */
public interface IWellData
{
    /** @return feature vector for the well */
    float[] getFeatureVector();

    /** @return id which allows to group all wells which contain replicates */
    long getReplicaId();

    /** Well sample for which the data are provided. */
    Sample getWell();

    /**
     * Material in the well which allowed to find replicates.<br>
     * Note: if the replicaId is the same for two wellData then the returned material is also the
     * same
     */
    Material getMaterial();

}
