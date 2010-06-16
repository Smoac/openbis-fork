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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgFeatureValuesDTO;

/**
 * Image feature vectors stored in a standardized form.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CanonicalFeatureVector
{
    private ImgFeatureDefDTO featureDef;

    private List<ImgFeatureValuesDTO> values = new ArrayList<ImgFeatureValuesDTO>();

    public CanonicalFeatureVector()
    {

    }

    /**
     * The feature def for this feature vector. If the feature def has not yet been commited to the
     * DB, then it has no ID.
     */
    public ImgFeatureDefDTO getFeatureDef()
    {
        return featureDef;
    }

    public void setFeatureDef(ImgFeatureDefDTO featureDef)
    {
        this.featureDef = featureDef;
    }

    /**
     * The feature values for this feature vector. If the feature def has not yet been commited to
     * the DB, then values FK to the feature def will not be valid. If the feature values have not
     * yet been committed, then their ids will not be valid.
     */
    public List<ImgFeatureValuesDTO> getValues()
    {
        return values;
    }

    public void setValues(List<ImgFeatureValuesDTO> values)
    {
        this.values = values;
    }
}
