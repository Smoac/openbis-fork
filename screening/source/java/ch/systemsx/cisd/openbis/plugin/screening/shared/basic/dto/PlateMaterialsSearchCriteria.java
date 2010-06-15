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

import java.io.Serializable;
import java.util.Arrays;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes a list of materials for which we search in the Plate Material Reviewer.
 * 
 * @author Tomasz Pylak
 */
public class PlateMaterialsSearchCriteria implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String[] geneSymbols;

    private long experimentId;

    // GWT
    @SuppressWarnings("unused")
    private PlateMaterialsSearchCriteria()
    {
    }

    public PlateMaterialsSearchCriteria(long experimentId, String[] geneSymbols)
    {
        this.geneSymbols = geneSymbols;
        this.experimentId = experimentId;
    }

    public String[] getGeneSymbols()
    {
        return geneSymbols;
    }

    public TechId getExperimentId()
    {
        return new TechId(experimentId);
    }

    @Override
    public String toString()
    {
        return "Experiment id: " + experimentId + ", gene symbols: " + Arrays.toString(geneSymbols);
    }

}
