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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Feature vector for one well.
 * 
 * @author Tomasz Pylak
 */
public class MaterialSingleReplicaFeatureVector implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private int replicaSequenceNumber;

    private float[] featueVectorSummary;

    // GWT only
    @SuppressWarnings("unused")
    private MaterialSingleReplicaFeatureVector()
    {
    }

    public MaterialSingleReplicaFeatureVector(int replicaSequenceNumber, float[] featueVectorSummary)
    {
        this.replicaSequenceNumber = replicaSequenceNumber;
        this.featueVectorSummary = featueVectorSummary;
    }

    public int getReplicaSequenceNumber()
    {
        return replicaSequenceNumber;
    }

    public float[] getFeatueVectorSummary()
    {
        return featueVectorSummary;
    }

}
