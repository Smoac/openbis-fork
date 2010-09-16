/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.featurevectors;

import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * References a feature vector of one well
 * 
 * @author Tomasz Pylak
 */
public class WellFeatureVectorReference extends AbstractHashable
{
    private String dataSetCode; // dataset with feature vectors

    private WellPosition wellPosition;

    public WellFeatureVectorReference(String dataSetCode, WellPosition wellPosition)
    {
        this.dataSetCode = dataSetCode;
        this.wellPosition = wellPosition;
    }

    public final String getDatasetCode()
    {
        return dataSetCode;
    }

    public final WellPosition getWellPosition()
    {
        return wellPosition;
    }
}
