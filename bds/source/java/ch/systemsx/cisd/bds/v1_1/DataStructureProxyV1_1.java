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

package ch.systemsx.cisd.bds.v1_1;

import ch.systemsx.cisd.bds.v1_0.DataStructureProxyV1_0;
import ch.systemsx.cisd.bds.v1_0.IDataStructureV1_0;

/**
 * A {@link IDataStructureV1_0} implementation which proxies calls to the encapsulated
 * {@link IDataStructureV1_0}.
 * 
 * @author Christian Ribeaud
 */
public final class DataStructureProxyV1_1 extends DataStructureProxyV1_0 implements
        IDataStructureV1_1
{

    public DataStructureProxyV1_1(final IDataStructureV1_1 dataStructure)
    {
        super(dataStructure);
    }

    private final IDataStructureV1_1 getDataStructure()
    {
        return (IDataStructureV1_1) dataStructure;
    }

    //
    // IDataStructureV1_1
    //

    public final ExperimentIdentifierWithUUID getExperimentIdentifierWithUUID()
    {
        return getDataStructure().getExperimentIdentifierWithUUID();
    }

    public final SampleWithOwner getSampleWithOwner()
    {
        return getDataStructure().getSampleWithOwner();
    }

}
