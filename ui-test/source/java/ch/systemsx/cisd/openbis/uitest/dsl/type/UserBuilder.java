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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateUserRmi;
import ch.systemsx.cisd.openbis.uitest.type.User;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
public class UserBuilder implements Builder<User>
{

    private String name;

    public UserBuilder(UidGenerator uid)
    {
        this.name = uid.uid();
        if (name.length() > 50)
        {
            name = name.substring(0, 50);
        }
    }

    @SuppressWarnings("hiding")
    public UserBuilder withName(String name)
    {
        this.name = name;
        return this;
    }

    @Override
    public User build(Application openbis, Ui ui)
    {
        if (Ui.PUBLIC_API.equals(ui))
        {
            return openbis.execute(new CreateUserRmi(new UserDsl(name)));
        } else if (Ui.DUMMY.equals(ui))
        {
            return new UserDsl(name);
        } else
        {
            throw new UnsupportedOperationException();
        }
    }
}
