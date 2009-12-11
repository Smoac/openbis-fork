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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Well's metadata: sample, the connected material and optionally the gene inhibited by this
 * material.
 * 
 * @author Tomasz Pylak
 */
public class WellMetadata implements IsSerializable
{
    private Sample wellSample;

    private Material contentOrNull; // with fetched properties

    // if the well content is connected to a gene material, this field is filled
    private Material geneOrNull;

    public Sample getWellSample()
    {
        return wellSample;
    }

    public void setWellSample(Sample wellSample)
    {
        this.wellSample = wellSample;
    }

    /** can be null */
    public Material tryGetContent()
    {
        return contentOrNull;
    }

    public void setContent(Material contentOrNull)
    {
        this.contentOrNull = contentOrNull;
    }

    /** can be null */
    public Material tryGetGene()
    {
        return geneOrNull;
    }

    public void setGene(Material geneOrNull)
    {
        this.geneOrNull = geneOrNull;
    }

}
