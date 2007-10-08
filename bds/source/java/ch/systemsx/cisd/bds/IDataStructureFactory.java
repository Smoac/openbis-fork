/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Factory of {@link IDataStructure}. 
 *
 * @author Franz-Josef Elmer
 */
public interface IDataStructureFactory
{
    /**
     * Returns the subinterface of {@link IDataStructure} for the specified version. 
     * 
     * @throws UserFailureException if this factory can not create a data structure for the specified version.
     */
    public Class<? extends IDataStructure> getDataStructureInterfaceFor(Version version) throws UserFailureException;
    
    /**
     * Creates a new data structure of specified name and version. The return object implements the interface
     * returned by {@link #getDataStructureInterfaceFor(Version)} for the same value of <code>version</code>.
     * 
     * @throws UserFailureException if this factory can not create a data structure for the specified version.
     */
    public IDataStructure createDataStructure(String name, Version version) throws UserFailureException;
}
