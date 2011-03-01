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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Extends {@link DataSetInformation} with information about images analysis on the well level
 * (relevant for HCS).
 * 
 * @author Tomasz Pylak
 */
public class FeatureVectorDataSetInformation extends DataSetInformation
{
    private static final long serialVersionUID = IServer.VERSION;

    private List<FeatureDefinitionValues> features;

    public FeatureVectorDataSetInformation()
    {
    }

    public List<FeatureDefinitionValues> getFeatures()
    {
        return features;
    }

    public void setFeatures(List<FeatureDefinitionValues> features)
    {
        this.features = features;
    }

    /** are all necessary fields filled? */
    public boolean isValid()
    {
        return features != null && features.size() > 0;
    }

}
