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


/**
 * Common interface of all data structures. Implementations of this interface provide methods to manipulate a data
 * structure. These methods are specific for the version of the data structure. For each version there is a concrete
 * class implementing this interface.
 * <p>
 * A data structure must first be created with {@link #create()} or opened with {@link #open()} before any other method
 * can be invoked. An {@link IllegalStateException} is thrown otherwise. Finally a data structure has to be closed with
 * {@link #close()} in order to commit all changes made since creation or opening.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataStructure extends IHasVersion
{
    /**
     * Creates a new empty data structure.
     */
    public void create();
    
    /**
     * Opens an existing data structure and validates it.
     * 
     * @throws DataStructureException if the data structure is invalid.
     */
    public void open();

    /**
     * Closes the data structure. Before the data structure is closed it will be validated.
     * 
     * @throws DataStructureException if the data structure is invalid.
     * @throws IllegalStateException if called before the first invocation of either {@link #create()} or
     *             {@link #open()}.
     */
    public void close();

}