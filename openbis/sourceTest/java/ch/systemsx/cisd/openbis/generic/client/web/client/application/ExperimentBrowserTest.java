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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser.CheckExperimentTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser.ExperimentRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser.ListExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * A {@link AbstractGWTTestCase} extension to test experiment browser.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentBrowserTest extends AbstractGWTTestCase
{

    public final void testListExperiments()
    {
        loginAndGotoListExperimentsTab();
        remoteConsole.prepare(new ListExperiments("DEFAULT", "SIRNA_HCS"));
        CheckExperimentTable table = new CheckExperimentTable();
        table.expectedRow(new ExperimentRow("EXP-REUSE").valid());
        table.expectedRow(new ExperimentRow("EXP-X").invalid());
        remoteConsole.prepare(table.expectedSize(2));

        startAndWait();
    }

    private void loginAndGotoListExperimentsTab()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.EXPERIMENTS,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
    }

    private void startAndWait()
    {
        remoteConsole.finish(20000);
        client.onModuleLoad();
    }
}
