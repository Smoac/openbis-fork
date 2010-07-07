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

package ch.systemsx.cisd.openbis.plugin.query.shared;

import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;

/**
 * @author Franz-Josef Elmer
 */
public class DatabaseDefinition
{
    private final String key;

    private final String label;

    private final GroupPE dataSpaceOrNull;

    private final RoleSet creatorMinimalRole;

    private final SimpleDatabaseConfigurationContext configurationContext;

    public DatabaseDefinition(SimpleDatabaseConfigurationContext configurationContext, String key,
            String label, RoleSet creatorMinimalRole, GroupPE dataSpaceOrNull)
    {
        assert key != null;
        assert label != null;
        assert creatorMinimalRole != null;
        assert configurationContext != null;
        this.key = key;
        this.label = label;
        this.dataSpaceOrNull = dataSpaceOrNull;
        this.creatorMinimalRole = creatorMinimalRole;
        this.configurationContext = configurationContext;
    }

    public String getKey()
    {
        return key;
    }

    public String getLabel()
    {
        return label;
    }

    public RoleSet getCreatorMinimalRole()
    {
        return creatorMinimalRole;
    }

    public GroupPE tryGetDataSpace()
    {
        return dataSpaceOrNull;
    }

    public SimpleDatabaseConfigurationContext getConfigurationContext()
    {
        return configurationContext;
    }

}
