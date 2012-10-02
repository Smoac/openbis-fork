/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

/**
 * Stores information about a location of a data sets and its components.
 * 
 * @author pkupczyk
 */
public interface IDatasetLocationNode
{

    /**
     * Returns the data set location. Never returns null.
     */
    IDatasetLocation getLocation();

    /**
     * Returns true if the data set is a container.
     */
    boolean isContainer();

    /**
     * Returns a list of component locations. For a data set that is not a container always returns
     * an empty list. Never returns null.
     */
    List<IDatasetLocationNode> getComponents();

}
