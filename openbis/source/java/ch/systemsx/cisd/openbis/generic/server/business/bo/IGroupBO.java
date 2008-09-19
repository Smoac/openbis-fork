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
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Business object of a project. Holds an instance of {@link GroupPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IGroupBO extends IBusinessObject
{

    /**
     * Defines a new group of specified identifier. After invocation of this method
     * {@link IBusinessObject#save()} should be invoked to store the new group in the <i>Data Access
     * Layer</i>.
     * 
     * @throws UserFailureException if <code>group</code> does already exist.
     */
    public void define(GroupIdentifier groupIdentifier, String descriptionOrNull,
            String groupLeaderOrNull) throws UserFailureException;

    /**
     * Loads a group described by identifier from Database Layer.
     * 
     * @throws UserFailureException if <code>groupIdentifier</code> does not describe existing
     *             group.
     */
    public void load(GroupIdentifier groupIdentifier) throws UserFailureException;

    /**
     * Returns the group or null.
     */
    public GroupPE getGroup() throws UserFailureException;

}
