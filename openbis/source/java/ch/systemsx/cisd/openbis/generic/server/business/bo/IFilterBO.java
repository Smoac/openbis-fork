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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.FilterPE;

/**
 * Business object of a filter. Holds an instance of {@link FilterPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IFilterBO extends IEntityBusinessObject
{

    /**
     * Defines a new filter. After invocation of this method {@link IBusinessObject#save()} should
     * be invoked to store the new group in the <i>Data Access Layer</i>.
     */
    public void define(NewFilter filter) throws UserFailureException;

    /**
     * Deletes filter.
     * 
     * @param groupId group technical identifier
     * @throws UserFailureException if filter with given technical identifier is not found.
     */
    public void deleteByTechId(TechId groupId);

}
