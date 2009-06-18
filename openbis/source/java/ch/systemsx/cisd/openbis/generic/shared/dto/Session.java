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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import org.apache.commons.lang.time.DateFormatUtils;

import ch.systemsx.cisd.authentication.BasicSession;
import ch.systemsx.cisd.authentication.Principal;

/**
 * A small object that encapsulates information related to a session.
 * 
 * @author Christian Ribeaud
 */
public final class Session extends BasicSession implements IAuthSession
{
    final private static long serialVersionUID = 1L;

    /**
     * The {@link PersonPE} represented by this <code>Session</code> or <code>null</code> if it is
     * not defined.
     */
    private PersonPE personOrNull;

    @Deprecated
    public Session()
    {
        super();
    }

    public Session(final String user, final String sessionToken, final Principal principal,
            final String remoteHost, final long sessionStart)
    {
        this(user, sessionToken, principal, remoteHost, sessionStart, 0);
    }

    public Session(String userName, String sessionToken, Principal principal, String remoteHost,
            long sessionStart, int expirationTime)
    {
        super(sessionToken, userName, principal, remoteHost, sessionStart, expirationTime);
    }

    public final void setPerson(final PersonPE person)
    {
        this.personOrNull = person;
    }

    /**
     * Returns the {@link PersonPE} associated to this session or <code>null</code>.
     */
    public final PersonPE tryGetPerson()
    {
        return personOrNull;
    }

    /** Returns the home group or <code>null</code>. */
    public final GroupPE tryGetHomeGroup()
    {
        if (personOrNull == null)
        {
            return null;
        }
        return personOrNull.getHomeGroup();
    }

    /** Returns home group code or <code>null</code>. */
    public final String tryGetHomeGroupCode()
    {
        final GroupPE homeGroup = tryGetHomeGroup();
        if (homeGroup == null)
        {
            return null;
        }
        return homeGroup.getCode();
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return "Session{user=" + getUserName() + ",group=" + tryGetHomeGroupCode()
                + ",sessionstart=" + DateFormatUtils.format(getSessionStart(), DATE_FORMAT_PATTERN)
                + "}";
    }

	@Deprecated
    public PersonPE getPerson()
    {
        return personOrNull;
    }
}
