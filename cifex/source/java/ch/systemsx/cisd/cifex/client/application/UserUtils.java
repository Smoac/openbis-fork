/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.datepicker.client.CalendarUtil;

import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A class for utility methods regarding user handling.
 * 
 * @author Bernd Rinn
 */
public class UserUtils
{

    private UserUtils()
    {
        // Not to be instantiated
    }

    /**
     * Remove all users from <var>usersByEmail</var> that are <li>not permanent users and are <li>
     * not owner by the <var>requestUser</var>. This way, a new user will be created if all users
     * with a given email are temporary users created by some other permanent user. The rationale is
     * to avoid leakage of file shares with other regular users that by chance exchange files with
     * the same user.
     * <p>
     * This class encodes the same logic as the one in
     * <code>ch.systemsx.cisd.cifex.server.business.FileManager.removeUnsuitableUsersForSharing()</code>
     * , but for {@link UserInfoDTO} instead of
     * {@link ch.systemsx.cisd.cifex.server.business.dto.UserDTO}.
     */
    public static void removeUnsuitableUsersForSharing(UserInfoDTO requestUser,
            List<UserInfoDTO> usersByEmail)
    {
        // For a permanent user, the accepted owner of users to share the file with is the request
        // user itself, for a temporary user it is the registrator of the request user.
        final UserInfoDTO acceptedOwner =
                (requestUser.isPermanent() ? requestUser : requestUser.getRegistrator());
        final Iterator<UserInfoDTO> it = usersByEmail.iterator();
        while (it.hasNext())
        {
            final UserInfoDTO user = it.next();
            if (user.isPermanent() == false && acceptedOwner.equals(user.getRegistrator()) == false)
            {
                it.remove();
            }
        }
    }

    public static Date getDefaultUserExpirationDate(final ViewContext context)
    {
        final Date initialExpirationDate = new Date();
        CalendarUtil.addDaysToDate(initialExpirationDate, context.getModel().getConfiguration()
                .getUserRetention());
        return initialExpirationDate;
    }

}
