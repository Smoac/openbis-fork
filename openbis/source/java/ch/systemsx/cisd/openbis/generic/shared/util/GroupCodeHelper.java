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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.exception.UndefinedGroupException;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GroupCodeHelper
{
    private GroupCodeHelper()
    {
    }

    /**
     * Tries to find out the group.
     * <p>
     * If not specified in given {@link GroupIdentifier} must be specified as home group in given
     * {@link PersonPE}.
     * </p>
     * 
     * @throws UndefinedGroupException if no group could be found.
     */
    public final static String getGroupCode(final PersonPE person,
            final GroupIdentifier groupIdentifier) throws UndefinedGroupException
    {
        if (groupIdentifier.isHomeGroup())
        {
            final GroupPE homeGroup = person.getHomeGroup();
            if (homeGroup == null)
            {
                throw new UndefinedGroupException();
            }
            return homeGroup.getCode();
        } else
        {
            return groupIdentifier.getGroupCode();
        }
    }
    
}
