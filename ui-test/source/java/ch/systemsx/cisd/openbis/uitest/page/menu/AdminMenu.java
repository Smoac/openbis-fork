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
import ch.systemsx.cisd.openbis.uitest.widget.Link;

public class AdminMenu
{

    @Locate("openbis_top-menu_ADMINISTRATION_MENU_MANAGE_GROUPS")
    private Link spaces;

    @Locate("openbis_top-menu_VOCABULARY_MENU_BROWSE")
    private Link vocabularies;

    @Locate("ADMINISTRATION_MENU_MANAGE_TYPES")
    private Link types;

    @Locate("ADMINISTRATION_MENU_MANAGE_PROPERTY_TYPES")
    private Link metadata;

    @Locate("ADMINISTRATION_MENU_MANAGE_AUTHORIZATION")
    private Link authorization;

    public void spaces()
    {
        spaces.click();
    }

    public void vocabularies()
    {
        vocabularies.click();
    }

    public void types()
    {
        types.highlight();
    }

    public void metadata()
    {
        metadata.highlight();
    }

    public void authorization()
    {
        authorization.highlight();
    }
}
