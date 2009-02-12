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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FillSearchCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion.DataSetSearchFieldKind;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>Data Set Search</i>.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchTest extends AbstractGWTTestCase
{

    public final void testSearch()
    {
        loginAndGotoTab();
        remoteConsole.prepare(new FillSearchCriteria().addSimpleCriterion(
                DataSetSearchFieldKind.EXPERIMENT, "exp1").addExperimentPropertyCriterion(
                "Description", "*"));
        remoteConsole.prepare(createCheckTable());
        launchTest(20000);
    }

    private final static CheckTableCommand createCheckTable()
    {
        final CheckTableCommand table =
                new CheckTableCommand(DataSetSearchHitGrid.GRID_ID,
                        DataSetSearchHitGrid.ListEntitiesCallback.class);
        table.expectedSize(0); // FIXME: Adjust after test database updated
        return table;
    }

    private void loginAndGotoTab()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.DATA_SETS,
                CategoriesBuilder.MENU_ELEMENTS.SEARCH));
    }
}
