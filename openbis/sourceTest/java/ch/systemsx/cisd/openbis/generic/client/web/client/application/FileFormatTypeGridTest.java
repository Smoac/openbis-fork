/*
 * Copyright 2009 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.TypeColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.entity_type.AddTypeDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * @author Franz-Josef Elmer
 */
public class FileFormatTypeGridTest extends AbstractGWTTestCase
{
    public void testShowGrid()
    {
        loginAndInvokeAction(ActionMenuKind.DATA_SET_MENU_FILE_FORMATS);
        CheckTableCommand table = new CheckTableCommand(FileFormatTypeGrid.GRID_ID);
        table.expectedRow(new Row().withCell(TypeColDefKind.CODE.id(), "XML"));
        remoteConsole.prepare(table.expectedSize(8));

        launchTest(20000);
    }

    public void testCreateNewType()
    {
        loginAndInvokeAction(ActionMenuKind.DATA_SET_MENU_FILE_FORMATS);
        CheckTableCommand table = new CheckTableCommand(FileFormatTypeGrid.GRID_ID);
        table.expectedSize(8);
        remoteConsole.prepare(table);
        remoteConsole.prepare(addNewTypeCommand());
        table = new CheckTableCommand(FileFormatTypeGrid.GRID_ID);
        table.expectedRow(new Row().withCell(TypeColDefKind.CODE.id(), "MY-TYPE").withCell(
                TypeColDefKind.DESCRIPTION.id(), "hello"));
        table.expectedSize(9);
        remoteConsole.prepare(table);

        launchTest(20000);
    }

    private AbstractDefaultTestCommand addNewTypeCommand()
    {
        return new AbstractDefaultTestCommand()
            {
                public void execute()
                {
                    GWTTestUtil.selectTabItemWithId(MainTabPanel.ID, FileFormatTypeGrid.BROWSER_ID
                            + MainTabPanel.TAB_SUFFIX);
                    ToolItem toolItem =
                            (ToolItem) GWTTestUtil
                                    .getWidgetWithID(FileFormatTypeGrid.ADD_NEW_TYPE_BUTTON_ID);
                    toolItem.fireEvent(Events.Select, new ToolBarEvent(toolItem.getToolBar()));
                    GWTTestUtil.getTextFieldWithID(AddTypeDialog.CODE_FIELD_ID).setValue("my-type");
                    GWTTestUtil.getTextFieldWithID(AddTypeDialog.DESCRIPTION_FIELD_ID).setValue(
                            "hello");
                    GWTTestUtil.clickButtonWithID(AddTypeDialog.SAVE_BUTTON_ID);
                }
            };
    }
}
