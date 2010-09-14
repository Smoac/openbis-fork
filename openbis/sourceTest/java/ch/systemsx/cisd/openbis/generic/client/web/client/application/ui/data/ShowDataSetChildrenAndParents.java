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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer;

/**
 * A {@link AbstractDefaultTestCommand} extension for showing detail view of a data set with given
 * code. View is displayed because of simulation of a click on show details button in a toolbar of a
 * data set search results browser.
 * 
 * @author Piotr Buczek
 */
public class ShowDataSetChildrenAndParents extends AbstractDefaultTestCommand
{
    private final String code;

    public ShowDataSetChildrenAndParents(final String code)
    {
        this.code = code;
    }

    public void execute()
    {
        clickButton(DisplayTypeIDGenerator.DATA_SET_CHILDREN_SECTION);
        clickButton(DisplayTypeIDGenerator.DATA_SET_PARENTS_SECTION);
    }

    private void clickButton(DisplayTypeIDGenerator idGenerator)
    {
        Widget widget = GWTTestUtil.getWidgetWithID(GenericConstants.ID_PREFIX
                + idGenerator.createID(GenericDataSetViewer.PREFIX + code)
                + SectionsPanel.POSTFIX_SECTION_TAB_ID);
        if (widget instanceof ToggleButton)
        {
            ToggleButton button = (ToggleButton) widget;
            button.toggle(true);
            button.fireEvent(Events.Select);
        }
    }
}
