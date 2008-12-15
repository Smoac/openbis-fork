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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.CheckSampleTable;
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
        remoteConsole.prepare(new ListSamples(true, true, "CISD", "MASTER_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow("MP001-1").identifier("CISD", "CISD").invalid()
                .noExperiment().property("PLATE_GEOMETRY", "384_WELLS_16X24"));
        table.expectedRow(new SampleRow("MP002-1").identifier("CISD", "CISD").valid()
                .noExperiment().property("PLATE_GEOMETRY", "384_WELLS_16X24"));
        table.expectedRow(new SampleRow("MP").identifier("CISD").valid().noExperiment().property(
                "PLATE_GEOMETRY", "384_WELLS_16X24"));
        remoteConsole.prepare(table.expectedSize(6));

        startAndWait();
    }

    public final void testListOnlySharedMasterPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples(true, false, null, "MASTER_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        SampleRow expectedRow =
                new SampleRow("MP").identifier("CISD").valid().noExperiment().property(
                        "PLATE_GEOMETRY", "384_WELLS_16X24");
        table.expectedRow(expectedRow);
        remoteConsole.prepare(table.expectedSize(1));

        startAndWait();
    }

    public final void testListCellPlates()
    {
        loginAndGotoListSamplesTab();
        remoteConsole.prepare(new ListSamples(true, true, "CISD", "CELL_PLATE"));
        CheckSampleTable table = new CheckSampleTable();
        table.expectedRow(new SampleRow("3VCP1").identifier("CISD", "CISD").invalid().experiment(
                "NEMO", "EXP1").derivedFromAncestor("3V-123", 1).derivedFromAncestor("MP001-1", 2));
        remoteConsole.prepare(table.expectedSize(16));

        startAndWait();
    }

    private void loginAndGotoListSamplesTab()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
    }

    private void startAndWait()
    {
        remoteConsole.finish(20000);
        client.onModuleLoad();
    }
}
