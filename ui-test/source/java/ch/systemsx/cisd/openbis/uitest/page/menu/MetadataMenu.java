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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ch.systemsx.cisd.openbis.uitest.page.PrivatePage;
import ch.systemsx.cisd.openbis.uitest.page.tab.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeBrowser;

public class MetadataMenu extends PrivatePage
{

    @FindBy(id = "openbis_top-menu_PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES")
    private WebElement browsePropertyTypes;

    @FindBy(id = "openbis_top-menu_PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES")
    private WebElement newPropertyType;

    public PropertyTypeBrowser propertyTypes()
    {
        browsePropertyTypes.click();
        return get(PropertyTypeBrowser.class);
    }

    public AddPropertyType newPropertyType()
    {
        newPropertyType.click();
        return get(AddPropertyType.class);
    }
}
