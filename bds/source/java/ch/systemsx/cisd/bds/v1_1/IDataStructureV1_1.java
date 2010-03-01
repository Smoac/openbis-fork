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

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.v1_0.IDataStructureV1_0;

/**
 * An <code>IDataStructure</code> extension for <i>v1.1</i>.
 * 
 * @author Christian Ribeaud
 */
public interface IDataStructureV1_1 extends IDataStructureV1_0
{

    /**
     * Returns the sample with its owner (a space or a database instance).
     * <p>
     * This is only available in version 1.1. Using this method with data structure version 1.0
     * throws an exception.
     * </p>
     * 
     * @throws DataStructureException if trying to use this method with data structure of version
     *             1.0.
     */
    public SampleWithOwner getSampleWithOwner();

    /**
     * Returns the experiment identifier with the database instance <i>UUID</i>.
     * <p>
     * This is only available in version 1.1. Using this method with data structure version 1.0
     * throws an exception.
     * </p>
     * 
     * @throws DataStructureException if trying to use this method with data structure of version
     *             1.0.
     */
    public ExperimentIdentifierWithUUID getExperimentIdentifierWithUUID();

}
