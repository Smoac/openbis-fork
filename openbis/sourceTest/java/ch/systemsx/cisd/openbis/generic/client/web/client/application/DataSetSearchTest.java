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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetSearchHitColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FillSearchCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchFieldKind;

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
        FillSearchCriteria fillCriteriaCmd = new FillSearchCriteria();
        fillCriteriaCmd.addSimpleCriterion(DataSetSearchFieldKind.EXPERIMENT, "exp");
        fillCriteriaCmd.addSimpleCriterion(DataSetSearchFieldKind.PROJECT, "nemo");
        fillCriteriaCmd.addSamplePropertyCriterion("Comment", "*stuff*");
        remoteConsole.prepare(fillCriteriaCmd);

        final CheckTableCommand checkResultTableCmd = createCheckSearchGridCmd();
        checkResultTableCmd.expectedSize(2);
        DataSetSearchRow row = new DataSetSearchRow();
        row.withCell(DataSetSearchHitColDefKind.LOCATION.id(), "a/3");
        row.withPropertyCell("comment", "no comment");
        checkResultTableCmd.expectedRow(row);
        checkResultTableCmd.expectedColumnsNumber(25);
        remoteConsole.prepare(checkResultTableCmd);

        launchTest(30000);
    }

    public final void testSearchByDataSetProperty()
    {
        loginAndGotoTab();
        FillSearchCriteria fillCriteriaCmd = new FillSearchCriteria();
        fillCriteriaCmd.addDataSetPropertyCriterion("Comment", "no comment");
        remoteConsole.prepare(fillCriteriaCmd);

        final CheckTableCommand checkResultTableCmd = createCheckSearchGridCmd();
        checkResultTableCmd.expectedSize(5);
        DataSetSearchRow row = new DataSetSearchRow();
        row.withCell(DataSetSearchHitColDefKind.LOCATION.id(), "a/1");
        row.withPropertyCell("comment", "no comment");
        checkResultTableCmd.expectedRow(row);
        checkResultTableCmd.expectedColumnsNumber(25);
        remoteConsole.prepare(checkResultTableCmd);

        launchTest(20000);
    }

    public final void testSearchUnknownGroup()
    {
        loginAndGotoTab();
        FillSearchCriteria fillCriteriaCmd = new FillSearchCriteria();
        fillCriteriaCmd.addSimpleCriterion(DataSetSearchFieldKind.GROUP, "koko");
        remoteConsole.prepare(fillCriteriaCmd);

        final CheckTableCommand checkResultTableCmd = createCheckSearchGridCmd();
        checkResultTableCmd.expectedSize(0);
        remoteConsole.prepare(checkResultTableCmd);

        launchTest(20000);
    }

    public final void testSearchForFileType()
    {
        loginAndGotoTab();
        FillSearchCriteria fillCriteriaCmd = new FillSearchCriteria();
        fillCriteriaCmd.addSimpleCriterion(DataSetSearchFieldKind.GROUP, "cisd");
        fillCriteriaCmd.addSimpleCriterion(DataSetSearchFieldKind.FILE_TYPE, "tiff");

        remoteConsole.prepare(fillCriteriaCmd);

        final CheckTableCommand checkResultTableCmd = createCheckSearchGridCmd();
        checkResultTableCmd.expectedSize(2);
        Row row1 =
                createTiffRow().withCell(DataSetSearchHitColDefKind.LOCATION.id(), "xxx/yyy/zzz");
        checkResultTableCmd.expectedRow(row1);
        Row row2 = createTiffRow().withCell(DataSetSearchHitColDefKind.LOCATION.id(), "a/1");
        checkResultTableCmd.expectedRow(row2);

        remoteConsole.prepare(checkResultTableCmd);

        launchTest(20000);
    }

    private Row createTiffRow()
    {
        return new Row().withCell(DataSetSearchHitColDefKind.FILE_TYPE.id(), "TIFF");
    }

    private static CheckTableCommand createCheckSearchGridCmd()
    {
        String gridID = DataSetSearchHitGrid.BROWSER_ID + AbstractExternalDataGrid.GRID_POSTFIX;
        return new CheckTableCommand(gridID, DataSetSearchHitGrid.ListEntitiesCallback.class);
    }

    private void loginAndGotoTab()
    {
        loginAndGotoTab(ActionMenuKind.DATA_SET_MENU_SEARCH);
    }
}
