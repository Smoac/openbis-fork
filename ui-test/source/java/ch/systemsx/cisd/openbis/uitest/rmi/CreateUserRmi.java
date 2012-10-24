/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.rmi;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.uitest.dsl.Executor;
import ch.systemsx.cisd.openbis.uitest.request.CreateUser;
import ch.systemsx.cisd.openbis.uitest.type.User;

/**
 * @author anttil
 */
public class CreateUserRmi extends Executor<CreateUser, User>
{

    @Override
    public User run(CreateUser request)
    {
        User user = request.getUser();
        String userName = user.getName();
        commonServer.registerPerson(session, userName);
        commonServer.registerInstanceRole(session, RoleCode.ADMIN, Grantee.createPerson(userName));
        return user;
    }
}
