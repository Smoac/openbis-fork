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

package ch.systemsx.cisd.openbis.uitest.page.menu;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.page.common.Page;
import ch.systemsx.cisd.openbis.uitest.page.tab.RoleAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.widget.Link;

public class AuthorizationMenu extends Page
{

    @Locate("openbis_top-menu_AUTHORIZATION_MENU_ROLES")
    private Link roles;

    public RoleAssignmentBrowser roles()
    {
        roles.click();
        return get(RoleAssignmentBrowser.class);
    }
}
