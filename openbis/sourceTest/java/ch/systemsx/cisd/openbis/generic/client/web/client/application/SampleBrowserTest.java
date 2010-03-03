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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind.DEFAULT_COLUMN_WIDTH;

import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.InvokeActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Logout;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ExportSamplesTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.ChangeTableColumnSettingsCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>AMC</i>.
 * 
 * @author Izabela Adamczyk
 */
public class SampleBrowserTest extends AbstractGWTTestCase
{

    private static final String DEFAULT_PLATE_GEOMETRY_VALUE = "384_WELLS_16X24";

    @DoNotRunWith(Platform.HtmlUnit)
    public final void testChangeColumnSettings()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples("CISD", "MASTER_PLATE"));
        ChangeTableColumnSettingsCommand settingsCommand =
                new ChangeTableColumnSettingsCommand(SampleBrowserGrid.GRID_ID);
        settingsCommand.hiddenChangeEvent(CommonSampleColDefKind.CODE.name(), true);
        settingsCommand.widthChangeEvent(CommonSampleColDefKind.REGISTRATOR.name(),
                2 * DEFAULT_COLUMN_WIDTH);
        remoteConsole.prepare(settingsCommand);
        remoteConsole.prepare(new Logout(SampleBrowserGrid.GRID_ID));
        Login login = new Login("test", "a");
        remoteConsole.prepare(login);
        remoteConsole.prepare(new InvokeActionMenu(ActionMenuKind.SAMPLE_MENU_BROWSE));
        remoteConsole.prepare(new ListSamples("CISD", "MASTER_PLATE"));
        CheckSampleTable checkCommand = new CheckSampleTable();
        checkCommand.expectedColumnHidden(CommonSampleColDefKind.CODE.name(), true);
        checkCommand.expectedColumnWidth(CommonSampleColDefKind.REGISTRATOR.name(),
                2 * DEFAULT_COLUMN_WIDTH);
        checkCommand.expectedColumnsNumber(17);
        remoteConsole.prepare(checkCommand);

        launchTest();
    }

    public final void testListAllSamples()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
        // samples of all types in home group should be automatically displayed
        CheckSampleTable table = new CheckSampleTable();

        // Test that there are two samples displayed that have different types, and a proper
        // value is displayed in property columns that are assigned only to one of these types
        // (union of property values is displayed).

        // 'ORGANISM' is assigned only to 'CELL_PLATE' sample type
        table.expectedRow(new SampleRow("CP-TEST-1", "CELL_PLATE").identifier("CISD", "CISD")
                .withUserPropertyCell("ORGANISM", "HUMAN"));
        // 'PLATE_GEOMETRY' is assigned only to 'CONTROL_LAYOUT' and 'MASTER PLATE' sample types
        table.expectedRow(new SampleRow("C1", "CONTROL_LAYOUT").identifier("CISD", "CISD")
                .withInternalPropertyCell("PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE));

        // test that 3 parents of 'REINFECT_PLATE' are displayed
        table.expectedRow(new SampleRow("RP1-A2X", "REINFECT_PLATE").identifier("CISD", "CISD")
                .derivedFromAncestors("CISD:/CISD/CP1-A2", "CISD:/CISD/DP1-A",
                        "CISD:/CISD/MP1-MIXED"));

        table.expectedColumnsNumber(25);
        remoteConsole.prepare(table.expectedSize(40));

        launchTest();
    }

    public final void testListMasterPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples("CISD", "MASTER_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedColumnHidden(CommonSampleColDefKind.CODE.name(), false);
        table.expectedColumnWidth(CommonSampleColDefKind.REGISTRATOR.name(), DEFAULT_COLUMN_WIDTH);
        table.expectedRow(new SampleRow("MP001-1").identifier("CISD", "CISD").invalid()
                .noExperiment().withInternalPropertyCell("PLATE_GEOMETRY",
                        DEFAULT_PLATE_GEOMETRY_VALUE));
        table.expectedRow(new SampleRow("MP002-1").identifier("CISD", "CISD").valid()
                .noExperiment().withInternalPropertyCell("PLATE_GEOMETRY",
                        DEFAULT_PLATE_GEOMETRY_VALUE));
        remoteConsole.prepare(table.expectedSize(5));

        launchTest();
    }

    public final void testExportMasterPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples(GroupSelectionWidget.SHARED_SPACE_CODE,
                "MASTER_PLATE"));
        ExportSamplesTestCommand exportCommand = new ExportSamplesTestCommand(client);
        remoteConsole.prepare(exportCommand);
        String header = "Code\tExperiment\tProject\tRegistrator\tRegistration Date";
        String firstLine = "MP\t\t\tDoe, John\t2008-11-05 09:20:47 GMT+01:00";
        remoteConsole.prepare(exportCommand.createCheckExportCommand(header, firstLine, 2));

        launchTest();
    }

    public final void testListOnlySharedMasterPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples(GroupSelectionWidget.SHARED_SPACE_CODE,
                "MASTER_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        Row expectedRow =
                new SampleRow("MP").identifier("CISD").valid().noExperiment()
                        .withInternalPropertyCell("PLATE_GEOMETRY", DEFAULT_PLATE_GEOMETRY_VALUE);
        table.expectedRow(expectedRow);
        remoteConsole.prepare(table.expectedSize(1));

        launchTest();
    }

    public final void testExportCellPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        ExportSamplesTestCommand exportCommand = new ExportSamplesTestCommand(client);
        remoteConsole.prepare(exportCommand);
        String header =
                "Code\tExperiment\tProject\tRegistrator\tRegistration Date\tParent 1\tParent 2";
        String firstLine =
                "3VCP1\tEXP1\tNEMO\tDoe, John\t2008-11-05 09:21:46 GMT+01:00\tCISD:/CISD/3V-123\tCISD:/CISD/MP001-1";
        remoteConsole.prepare(exportCommand.createCheckExportCommand(header, firstLine, 16));

        launchTest();
    }

    public final void testListCellPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow("3VCP1").identifier("CISD", "CISD").invalid().experiment(
                "CISD", "NEMO", "EXP1").derivedFromAncestors("CISD:/CISD/3V-123",
                "CISD:/CISD/MP001-1"));
        table.expectedColumnsNumber(22);
        remoteConsole.prepare(table.expectedSize(15));

        launchTest();
    }

    private void loginAndGotoListSamplesTab()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
    }
}
