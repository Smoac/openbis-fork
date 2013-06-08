/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import ch.ethz.sis.hcscld.IFeatureGroup.FeatureGroupDataType;

/**
 * Exception to signal that a feature group has the wrong storage type.
 * 
 * @author Bernd Rinn
 */
public class WrongFeatureGroupStorageTypeException extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    private final String datasetCode;
    
    private final String featureGroupId;
    
    private final FeatureGroupDataType expectedStorageDataType;

    private final FeatureGroupDataType foundStorageDataType;

    WrongFeatureGroupStorageTypeException(String datasetCode, String featureGroupId, FeatureGroupDataType expectedStorageDataType,
            FeatureGroupDataType foundStorageDataType)
    {
        super(String.format("Dataset '%s', FeatureGroup '%s' [type expected: %s, type found: %s]", datasetCode, featureGroupId,
                expectedStorageDataType, foundStorageDataType));
        this.datasetCode = datasetCode;
        this.featureGroupId = featureGroupId;
        this.expectedStorageDataType = expectedStorageDataType;
        this.foundStorageDataType = foundStorageDataType;
    }

    /**
     * Returns the dataset code of the exception.
     */
    public String getDatasetCode()
    {
        return datasetCode;
    }

    /**
     * Returns the id of the feature group.
     */
    public String getFeatureGroupId()
    {
        return featureGroupId;
    }

    /**
     * Returns the feature group data type expected.
     */
    public FeatureGroupDataType getExpectedStorageDataType()
    {
        return expectedStorageDataType;
    }

    /**
     * Returns the feature group data type found.
     */
    public FeatureGroupDataType getFoundStorageDataType()
    {
        return foundStorageDataType;
    }
}
