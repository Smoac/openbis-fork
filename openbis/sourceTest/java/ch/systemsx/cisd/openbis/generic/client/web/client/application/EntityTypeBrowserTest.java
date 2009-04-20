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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.EntityTypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * Tests for sample type, material type and experiment type browsers.
 * 
 * @author Tomasz Pylak
 */
public class EntityTypeBrowserTest extends AbstractGWTTestCase
{

    public final void testListMaterialTypes()
    {
        loginAndGotoTab(ActionMenuKind.MATERIAL_MENU_TYPES);
        CheckTableCommand table =
                new CheckTableCommand(MaterialTypeGrid.GRID_ID,
                        MaterialTypeGrid.ListEntitiesCallback.class);
        checkGridRows(table, "BACTERIUM", 7);
    }

    public final void testListSampleTypes()
    {
        loginAndGotoTab(ActionMenuKind.SAMPLE_MENU_TYPES);
        CheckTableCommand table =
                new CheckTableCommand(SampleTypeGrid.GRID_ID,
                        SampleTypeGrid.ListEntitiesCallback.class);
        checkGridRows(table, "WELL", 6);
    }

    public final void testListExperimentTypes()
    {
        loginAndGotoTab(ActionMenuKind.EXPERIMENT_MENU_TYPES);
        CheckTableCommand table =
                new CheckTableCommand(ExperimentTypeGrid.GRID_ID,
                        ExperimentTypeGrid.ListEntitiesCallback.class);
        checkGridRows(table, "SIRNA_HCS", 2);
    }

    public final void testListDataSetTypes()
    {
        loginAndGotoTab(ActionMenuKind.DATA_SET_MENU_TYPES);
        CheckTableCommand table =
                new CheckTableCommand(DataSetTypeGrid.GRID_ID,
                        DataSetTypeGrid.ListEntitiesCallback.class);
        checkGridRows(table, "HCS_IMAGE_ANALYSIS_DATA", 3);
    }

    private void checkGridRows(CheckTableCommand table, String expectedCode, int expectedRowsNum)
    {
        table.expectedRow(new Row().withCell(EntityTypeColDefKind.CODE.id(), expectedCode));
        remoteConsole.prepare(table.expectedSize(expectedRowsNum));

        launchTest(20000);
    }
}
