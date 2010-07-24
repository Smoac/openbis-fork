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

    private String[] materialCodes;

    private String[] materialTypeCodes;

    private long experimentId;

    // GWT
    @SuppressWarnings("unused")
    private PlateMaterialsSearchCriteria()
    {
    }

    /**
     * We look for wells containing materials which have a code contained in the specified list of
     * codes and type contained in the specified list of types. Additionally all wells should belong
     * to the plate in the specified experiment.
     */
    public PlateMaterialsSearchCriteria(long experimentId, String[] materialCodes,
            String[] materialTypeCodes)
    {
        this.materialCodes = materialCodes;
        this.materialTypeCodes = materialTypeCodes;
        this.experimentId = experimentId;
        for (int i = 0; i < materialCodes.length; i++)
        {
            materialCodes[i] = materialCodes[i].toUpperCase();
        }
    }

    public String[] getMaterialCodes()
    {
        return materialCodes;
    }

    public String[] getMaterialTypeCodes()
    {
        return materialTypeCodes;
    }

    public TechId getExperimentId()
    {
        return new TechId(experimentId);
    }

    @Override
    public String toString()
    {
        return "Experiment id: " + experimentId + ", material types: "
                + Arrays.toString(materialTypeCodes) + ", material codes: " + printMaterialCodes();
    }

    private String printMaterialCodes()
    {
        StringBuffer sb = new StringBuffer();
        for (String gene : materialCodes)
        {
            if (sb.length() > 0)
            {
                sb.append(",");
            }
            sb.append(gene);
        }
        return sb.toString();
    }

}
