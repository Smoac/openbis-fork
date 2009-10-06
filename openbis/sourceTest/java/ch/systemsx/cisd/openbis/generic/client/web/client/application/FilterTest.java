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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.CustomGridFilterColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter.AddFilterCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter.ApplyFilterCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter.CheckFiltersTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter.OpenFilterSettingsCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * Tests for filters.
 * 
 * @author Izabela Adamczyk
 */
public class FilterTest extends AbstractGWTTestCase
{

    private static final boolean IS_PUBLIC = true;

    private static final String EXPRESSION = "len(row.col(\"CODE\"))>13";

    private static final String DESCRIPTION = "Length of code greater than 13";

    private static final String NAME = "Long code";

    public final void testFilters()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_TYPES);

        String gridDisplayId = DisplayTypeIDGenerator.TYPE_BROWSER_GRID.createID("-SAMPLE");

        // Open table settings
        String gridId = SampleTypeGrid.GRID_ID;
        OpenFilterSettingsCommand showFilterSettingsCommand =
                new OpenFilterSettingsCommand(gridId, gridDisplayId);
        remoteConsole.prepare(showFilterSettingsCommand.expectedSize(6));

        // Add new filter
        AddFilterCommand addFilterCommand =
                new AddFilterCommand(gridDisplayId, NAME, DESCRIPTION, EXPRESSION, IS_PUBLIC);
        remoteConsole.prepare(addFilterCommand.expectedSize(0));

        // Check new filter
        CheckFiltersTableCommand filtersTable = new CheckFiltersTableCommand(gridDisplayId);
        filtersTable.expectedRow(new Row().withCell(CustomGridFilterColDefKind.NAME.id(), NAME));
        filtersTable
                .expectedRow(new Row().withCell(CustomGridFilterColDefKind.DESCRIPTION.id(), DESCRIPTION));
        filtersTable.expectedRow(new Row().withCell(CustomGridFilterColDefKind.EXPRESSION.id(), EXPRESSION));
        remoteConsole.prepare(filtersTable.expectedSize(1));

        // Apply filter
        ApplyFilterCommand applyFilterCommand = new ApplyFilterCommand(gridId, NAME);
        remoteConsole.prepare(applyFilterCommand);

        // Check results
        CheckTableCommand sampleTypesAfterFilter = new CheckTableCommand(gridId);
        remoteConsole.prepare(sampleTypesAfterFilter.expectedSize(3));

        launchTest(25000);
    }

}
