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
import ch.systemsx.cisd.openbis.uitest.page.tab.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.AssignSamplePropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.widget.Link;

public class MetadataMenu extends Page
{

    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES")
    private Link browsePropertyTypes;

    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS")
    private Link browsePropertyTypeAssignments;

    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES")
    private Link newPropertyType;

    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE")
    private Link assignToSampleType;

    public PropertyTypeBrowser propertyTypes()
    {
        browsePropertyTypes.click();
        return get(PropertyTypeBrowser.class);
    }

    public PropertyTypeAssignmentBrowser propertyTypeAssignments()
    {
        browsePropertyTypeAssignments.click();
        return get(PropertyTypeAssignmentBrowser.class);
    }

    public AddPropertyType newPropertyType()
    {
        newPropertyType.click();
        return get(AddPropertyType.class);
    }

    public AssignSamplePropertyType assignToSampleType()
    {
        assignToSampleType.click();
        return get(AssignSamplePropertyType.class);
    }
}
