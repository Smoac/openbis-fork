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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.api;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.structured.Element;

/**
 * Abstracts the conversion of {@link Element} objects to/from database strings.
 * 
 * @author Kaloyan Enimanev
 */
public interface IStructuredPropertyConverter
{
    /**
     * Converts the values of specified property into a list of elements.
     * 
     * @return an empty list if the value is undefined or special.
     */
    IElement[] convertToElements(IManagedProperty property);
    
    /**
     * @return a {@link String} representation of the specified elements that can be persisted in
     *         the database. 
     */
    String convertToString(IElement[] elements);

}