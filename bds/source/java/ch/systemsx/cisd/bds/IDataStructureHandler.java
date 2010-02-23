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

import ch.systemsx.cisd.bds.exception.DataStructureException;

/**
 * The aim of this interface is to delegate the work that should be done in
 * {@link AbstractDataStructure} to smaller parties.
 * 
 * @author Christian Ribeaud
 */
public interface IDataStructureHandler
{

    /**
     * Validates this data structure and throws {@link DataStructureException} if invalid.
     * <p>
     * This is typically called after {@link #performClosing()} or {@link #performOpening()} has
     * been successfully invoked.
     * </p>
     */
    public void assertValid() throws DataStructureException;

    /**
     * Performs opening specific tasks for the concrete data structure.
     * <p>
     * Will be invoked after the common part of
     * {@link IDataStructure#open(ch.systemsx.cisd.bds.IDataStructure.Mode)} but before validation
     * with {@link #assertValid()}.
     * </p>
     */
    public void performOpening();

    /**
     * Performs closing specific tasks for the concrete data structure.
     * <p>
     * Will be invoked before validation with {@link #assertValid()}.
     * </p>
     */
    public void performClosing();

    /**
     * Performs creating specific tasks for the concrete data structure.
     * <p>
     * Will be invoked after the common part of {@link IDataStructure#create}.
     * </p>
     */
    public void performCreating();
}