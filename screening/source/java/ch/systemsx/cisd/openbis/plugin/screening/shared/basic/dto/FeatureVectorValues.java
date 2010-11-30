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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;

/**
 * Bean for one feature vector of the well. Contains data set code, well position and array of
 * feature values. Float.NaN is used for unknown feature value in this array.
 * 
 * @author Tomasz Pylak
 */
@DoNotEscape
public class FeatureVectorValues implements IsSerializable
{
    private WellFeatureVectorReference featureVectorReference;

    private FeatureValue[] featureValues;

    // GWT only
    @SuppressWarnings("unused")
    private FeatureVectorValues()
    {
    }

    public FeatureVectorValues(String dataSetCode, WellLocation wellLocation,
            FeatureValue[] featureValues)
    {
        this(new WellFeatureVectorReference(dataSetCode, wellLocation), featureValues);
    }

    public FeatureVectorValues(WellFeatureVectorReference featureVectorReference,
            FeatureValue[] featureValues)
    {
        this.featureVectorReference = featureVectorReference;
        this.featureValues = featureValues;
    }

    public FeatureValue[] getFeatureValues()
    {
        return featureValues;
    }

    public String getDataSetCode()
    {
        return featureVectorReference.getDatasetCode();
    }

    public WellLocation getWellLocation()
    {
        return featureVectorReference.getWellLocation();
    }

    public WellFeatureVectorReference getFeatureVectorReference()
    {
        return featureVectorReference;
    }

}
