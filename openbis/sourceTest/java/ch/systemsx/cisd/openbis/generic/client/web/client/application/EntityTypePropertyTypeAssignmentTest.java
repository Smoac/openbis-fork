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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.CheckPropertyTypeAssignmentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.FillPropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test Entity Property Type Assignment.
 * 
 * @author Izabela Adamczyk
 */
public class EntityTypePropertyTypeAssignmentTest extends AbstractGWTTestCase
{

    private static final String EXPERIMENT = "EXPERIMENT";

    private static final String SIRNA_HCS = "SIRNA_HCS";

    private static final String USER_DESCRIPTION = "USER.DESCRIPTION";

    private final void prepareListingAfterAssignment(String propertyTypeCode,
            String entityTypeCode, String entityKindName)
    {
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.PROPERTY_TYPES,
                CategoriesBuilder.MENU_ELEMENTS.LIST_ASSIGNMENTS,
                PropertyTypeAssignmentForm.AssignPropertyTypeCallback.class));
        CheckPropertyTypeAssignmentTable table = new CheckPropertyTypeAssignmentTable();
        table.expectedRow(new Row().withCell(ModelDataPropertyNames.PROPERTY_TYPE_CODE,
                propertyTypeCode).withCell(ModelDataPropertyNames.ENTITY_TYPE_CODE, entityTypeCode)
                .withCell(ModelDataPropertyNames.ENTITY_KIND, entityKindName));
        remoteConsole.prepare(table.expectedSize(24));
    }

    public final void testAssignmenExperimentPropertyType()
    {
        // TODO 2008-12-17, IA: Finish after server side of entity property assignment is ready
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.PROPERTY_TYPES,
                CategoriesBuilder.MENU_ELEMENTS.ASSIGN_ETPT));
        remoteConsole.prepare(new FillPropertyTypeAssignmentForm(false, USER_DESCRIPTION,
                SIRNA_HCS, null, EXPERIMENT));
        prepareListingAfterAssignment(USER_DESCRIPTION, SIRNA_HCS, EXPERIMENT);

        launchTest(20000);
    }
}
