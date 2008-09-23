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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Business object of a person. Holds an instance of {@link PersonPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IPersonBO extends IBusinessObject
{

    /**
     * Loads the person from the <i>Data Access Layer</i> which has <var>userId</var> specified by
     * the session or defines a new person (based on the information found in the session).
     * <p>
     * Finally sets the found {@link PersonPE} to the session.
     * </p>
     */
    public void enrichSessionWithPerson();

    /**
     * Returns the id of person which is currently logged in (a.k.a. registrator id).
     */
    public Long getLoggedInUserId();

    /**
     * Loads the person from the <i>Data Access Layer</i> which has given <var>userId</var>.
     * 
     * @throws UserFailureException if given <var>userId</var> could not be found.
     */
    public void load(final String userId) throws UserFailureException;

    /**
     * Sets home group on loaded {@link PersonPE}.
     */
    public void setHomeGroup(GroupPE group);

    /**
     * Registers person with given code.
     */
    public void registerPerson(String code);
}