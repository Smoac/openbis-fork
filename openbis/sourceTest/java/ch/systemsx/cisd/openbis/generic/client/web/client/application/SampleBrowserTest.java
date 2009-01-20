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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ExportSamplesTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>AMC</i>.
 * 
 * @author Izabela Adamczyk
 */
public class SampleBrowserTest extends AbstractGWTTestCase
{

    public final void testListMasterPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples("CISD", "MASTER_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow("MP001-1").identifier("CISD", "CISD").invalid()
                .noExperiment().property("PLATE_GEOMETRY", "384_WELLS_16X24"));
        table.expectedRow(new SampleRow("MP002-1").identifier("CISD", "CISD").valid()
                .noExperiment().property("PLATE_GEOMETRY", "384_WELLS_16X24"));
        remoteConsole.prepare(table.expectedSize(5));

        launchTest(20000);
    }

    public final void testExportMasterPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples(GroupSelectionWidget.SHARED_GROUP_CODE,
                "MASTER_PLATE"));
        ExportSamplesTestCommand exportCommand = new ExportSamplesTestCommand(client);
        remoteConsole.prepare(exportCommand);
        String header = "Group\tCode\tProject\tExperiment\tPlate Geometry";
        String firstLine = "\tMP\t\t\t384_WELLS_16X24";
        remoteConsole.prepare(exportCommand.createCheckExportCommand(header, firstLine, 2));

        launchTest(20000);
    }

    public final void testListOnlySharedMasterPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples(GroupSelectionWidget.SHARED_GROUP_CODE,
                "MASTER_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        SampleRow expectedRow =
                new SampleRow("MP").identifier("CISD").valid().noExperiment().property(
                        "PLATE_GEOMETRY", "384_WELLS_16X24");
        table.expectedRow(expectedRow);
        remoteConsole.prepare(table.expectedSize(1));

        launchTest(20000);
    }

    public final void testExportCellPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        ExportSamplesTestCommand exportCommand = new ExportSamplesTestCommand(client);
        remoteConsole.prepare(exportCommand);
        String header = "Group\tCode\tProject\tExperiment\tParent 1\tParent 2";
        String firstLine = "CISD\t3VCP1\tNEMO\tEXP1\t3V-123\tMP001-1";
        remoteConsole.prepare(exportCommand.createCheckExportCommand(header, firstLine, 17));

        launchTest(20000);
    }

    public final void testListCellPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow("3VCP1").identifier("CISD", "CISD").invalid().experiment(
                "NEMO", "EXP1").derivedFromAncestor("3V-123", 1).derivedFromAncestor("MP001-1", 2));
        remoteConsole.prepare(table.expectedSize(16));

        launchTest(20000);
    }

    private void loginAndGotoListSamplesTab()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
    }
}
