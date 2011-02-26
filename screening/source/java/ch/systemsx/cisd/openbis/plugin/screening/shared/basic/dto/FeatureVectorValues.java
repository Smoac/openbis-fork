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

import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Bean for one feature vector of the well. Contains data set code, well position and a map of
 * feature values. Float.NaN is used for unknown feature value in this array.
 * 
 * @author Tomasz Pylak
 */
@DoNotEscape
public class FeatureVectorValues implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private WellFeatureVectorReference featureVectorReference;

    private Map<String /* feature label */, FeatureValue /* value */> featureMap;

    // GWT only
    @SuppressWarnings("unused")
    private FeatureVectorValues()
    {
    }

    public FeatureVectorValues(String dataSetCode, WellLocation wellLocation,
            Map<String, FeatureValue> featureMap)
    {
        this(new WellFeatureVectorReference(dataSetCode, wellLocation), featureMap);
    }

    public FeatureVectorValues(WellFeatureVectorReference featureVectorReference,
            Map<String, FeatureValue> featureMap)
    {
        this.featureVectorReference = featureVectorReference;
        this.featureMap = featureMap;
    }

    // NOTE: For performance reasons it is better not to call this method multiple times.
    // Use getFeatureMap() instead.
    public FeatureValue[] getFeatureValues()
    {
        return featureMap.values().toArray(new FeatureValue[0]);
    }

    public Map<String, FeatureValue> getFeatureMap()
    {
        return featureMap;
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
