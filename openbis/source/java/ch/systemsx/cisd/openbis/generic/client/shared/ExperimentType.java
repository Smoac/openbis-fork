/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.shared;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * The <i>GWT</i> equivalent to {@link ExperimentTypePE}.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentType extends EntityType
{
    private List<ExperimentTypePropertyType> experimentTypePropertyTypes;

    public List<ExperimentTypePropertyType> getExperimentTypePropertyTypes()
    {
        return experimentTypePropertyTypes;
    }

    public void setExperimentTypePropertyTypes(
            List<ExperimentTypePropertyType> experimentTypePropertyTypes)
    {
        this.experimentTypePropertyTypes = experimentTypePropertyTypes;
    }
}
