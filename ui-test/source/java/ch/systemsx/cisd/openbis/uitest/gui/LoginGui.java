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

package ch.systemsx.cisd.openbis.uitest.gui;

import ch.systemsx.cisd.openbis.uitest.functionality.AbstractExecution;
import ch.systemsx.cisd.openbis.uitest.functionality.Login;
import ch.systemsx.cisd.openbis.uitest.page.LoginPage;

/**
 * @author anttil
 */
public class LoginGui extends AbstractExecution<Login, Void>
{
    @Override
    public Void run(Login request)
    {
        load(LoginPage.class).loginAs(request.getUser(), request.getPassword());
        return null;
    }
}
