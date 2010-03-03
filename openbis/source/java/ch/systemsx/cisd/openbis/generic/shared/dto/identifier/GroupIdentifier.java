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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Deprecated naming, use {@link SpaceIdentifier} instead. Created to avoid extensive code changes
 * and naming clashes.
 * 
 * @author Tomasz Pylak
 */
public class GroupIdentifier extends SpaceIdentifier
{
    private static final long serialVersionUID = IServer.VERSION;

    public GroupIdentifier(DatabaseInstanceIdentifier databaseInstanceIdentifier, String spaceCode)
    {
        super(databaseInstanceIdentifier, spaceCode);
    }

    public GroupIdentifier(final String databaseInstanceCode, final String spaceCode)
    {
        super(databaseInstanceCode, spaceCode);
    }

    public static GroupIdentifier createHome()
    {
        return new GroupIdentifier(getHomeSpaceCode(), DatabaseInstanceIdentifier.HOME);
    }
}
