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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Well's metadata: sample, the connected material and optionally the gene inhibited by this
 * material.
 * 
 * @author Tomasz Pylak
 */
public class WellMetadata implements IsSerializable
{
    private Sample wellSample; // with fetched properties

    private WellLocation locationOrNull;

    public Sample getWellSample()
    {
        return wellSample;
    }

    public WellLocation tryGetLocation()
    {
        return locationOrNull;
    }

    public void setWellSample(Sample wellSample, WellLocation locationOrNull)
    {
        this.wellSample = wellSample;
        this.locationOrNull = locationOrNull;
    }

}
