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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * An basic server.
 * 
 * @author Christian Ribeaud
 */
public interface IServer extends ISessionProvider
{
    /**
     * Every time some public class in this package or subpackage is changed, we should increment
     * the appropriate variable.
     */
    public static final int VERSION = ServiceVersionHolder.VERSION;

    /**
     * Returns the version of this interface.
     */
    public int getVersion();

    /**
     * Tries to authenticate the specified user with given password.
     * 
     * @return <code>null</code> if authentication failed.
     */
    @Transactional
    public SessionContextDTO tryToAuthenticate(final String user, final String password);

    /** @return session for the specified token or null if session has expired */
    public SessionContextDTO tryGetSession(String sessionToken);

    /**
     * Sets the base URL (including "index.html") that the web server is reachable at for this
     * client.
     */
    @Transactional
    public void setBaseIndexURL(String sessionToken, String baseIndexURL);

    @Transactional
    public void saveDisplaySettings(String sessionToken, DisplaySettings displaySettings);

    /**
     * Lists grid custom columns for a given grid id.
     */
    @Transactional
    public List<GridCustomColumn> listGridCustomColumns(String sessionToken, String gridDisplayId);

    /** Changes logged user home group to the one with given technical id. */
    @Transactional
    public void changeUserHomeGroup(String sessionToken, TechId groupIdOrNull);

    /**
     * Logout the session with the specified session token.
     */
    public void logout(final String sessionToken) throws UserFailureException;
}
