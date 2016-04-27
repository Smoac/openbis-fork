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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSequence;

/**
 * Criteria for listing {@link ProteinSequence} instances.
 *
 * @author Franz-Josef Elmer
 */
public class ListProteinSequenceCriteria extends
        DefaultResultSetConfig<String, TableModelRowWithObject<ProteinSequence>> implements
        IsSerializable
{
    private TechId proteinReferenceID;

    private TechId experimentID;

    public final TechId getProteinReferenceID()
    {
        return proteinReferenceID;
    }

    public final void setProteinReferenceID(TechId proteinReferenceID)
    {
        this.proteinReferenceID = proteinReferenceID;
    }

    public void setExperimentID(TechId experimentID)
    {
        this.experimentID = experimentID;
    }

    public TechId getExperimentID()
    {
        return experimentID;
    }

}
