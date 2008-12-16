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

package ch.systemsx.cisd.openbis.generic.shared.authorization;

import org.testng.AssertJUnit;

import ch.systemsx.cisd.openbis.generic.shared.authorization.Role.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class AuthorizationTestCase extends AssertJUnit
{
    protected static final String INSTANCE_CODE = "DB1";

    /** Identifier with code {@link #INSTANCE_CODE}. */
    protected static final DatabaseInstanceIdentifier INSTANCE_IDENTIFIER =
            new DatabaseInstanceIdentifier(INSTANCE_CODE);

    /**
     * Creates a role with level {@link RoleLevel#GROUP} with specified role code for specified
     * group.
     */
    protected RoleWithIdentifier createGroupRole(RoleCode roleCode,
            GroupIdentifier groupIdentifier)
    {
        GroupPE groupPE = new GroupPE();
        groupPE.setCode(groupIdentifier.getGroupCode());
        DatabaseInstancePE instance = createDatabaseInstancePE(groupIdentifier);
        groupPE.setDatabaseInstance(instance);
        return new RoleWithIdentifier(RoleLevel.GROUP, roleCode, null, groupPE);
    }

    /**
     * Creates a role with level {@link RoleLevel#INSTANCE} with specified role code for specified
     * database instance.
     */
    protected RoleWithIdentifier createInstanceRole(RoleCode roleCode,
            DatabaseInstanceIdentifier instanceIdentifier)
    {
        DatabaseInstancePE instance = createDatabaseInstancePE(instanceIdentifier);
        return new RoleWithIdentifier(RoleLevel.INSTANCE, roleCode, instance, null);
    }

    /**
     * Creates a new instance of {@link DatabaseInstancePE} for the specified identifier.
     * Shortcut for <code>createDatabaseInstance(instanceIdentifier.getDatabaseInstanceCode())</code>.
     */
    protected DatabaseInstancePE createDatabaseInstancePE(
            DatabaseInstanceIdentifier instanceIdentifier)
    {
        return createDatabaseInstance(instanceIdentifier.getDatabaseInstanceCode());
    }

    /**
     * Creates a new instance of {@link DatabaseInstancePE} with code {@link #INSTANCE_CODE}.
     * Shortcut for <code>createDatabaseInstance(INSTANCE_CODE)</code>.
     */
    protected final DatabaseInstancePE createDatabaseInstance()
    {
        return createDatabaseInstance(INSTANCE_CODE);
    }
    
    /**
     * Creates a new instance of {@link DatabaseInstancePE} for the specified code.
     * Only code and UUID will be set.
     */
    protected DatabaseInstancePE createDatabaseInstance(String code)
    {
        DatabaseInstancePE instance = new DatabaseInstancePE();
        instance.setCode(code);
        instance.setUuid("global_" + code);
        return instance;
    }


}
