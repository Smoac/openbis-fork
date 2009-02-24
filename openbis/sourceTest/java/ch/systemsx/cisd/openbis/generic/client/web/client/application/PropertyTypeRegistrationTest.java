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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.PropertyTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.CheckPropertyTypeTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.FillPropertyTypeRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>Property Type Registration</i>.
 * 
 * @author Christian Ribeaud
 */
public class PropertyTypeRegistrationTest extends AbstractGWTTestCase
{
    private static final String PROPERTY_TYPE_CODE = "NUMBER_OF_CELLS";

    private final static FillPropertyTypeRegistrationForm createFillPropertyTypeRegistrationForm()
    {
        final FillPropertyTypeRegistrationForm form =
                new FillPropertyTypeRegistrationForm(PROPERTY_TYPE_CODE, "Number of cells",
                        "The number of cells", DataTypeCode.INTEGER);
        return form;
    }

    public final void testRegisterPropertyType()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.PROPERTY_TYPES,
                CategoriesBuilder.MENU_ELEMENTS.REGISTER));
        remoteConsole.prepare(createFillPropertyTypeRegistrationForm());

        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.PROPERTY_TYPES,
                CategoriesBuilder.MENU_ELEMENTS.BROWSE,
                PropertyTypeRegistrationForm.PropertyTypeRegistrationCallback.class));
        final CheckPropertyTypeTable table = new CheckPropertyTypeTable();
        table.expectedRow(new Row().withCell(PropertyTypeColDefKind.CODE.id(),
                PropertyType.USER_NAMESPACE_CODE_PREPEND + PROPERTY_TYPE_CODE));
        remoteConsole.prepare(table.expectedSize(15));

        launchTest(20000);
    }
}
