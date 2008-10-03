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

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Footer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AMC;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleBrowser;

/**
 * Panel of the application.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class Application extends Viewport
{
    private static final int MARGIN_SIZE = 3;

    Application(GenericViewContext viewContext)
    {
        createGUI(viewContext);
    }

    private void createGUI(final GenericViewContext viewContext)
    {

        setLayout(new FitLayout());
        LayoutContainer panels = new LayoutContainer();
        panels.setLayout(new RowLayout());

        TopMenu north = new TopMenu(viewContext);
        Widget center = chosenMainPanel(viewContext);
        Footer south = new Footer(viewContext);

        RowData northData = new RowData(1, -1, new Margins(MARGIN_SIZE));
        RowData centerData = new RowData(1, 1, new Margins(MARGIN_SIZE));
        RowData southData = new RowData(1, -1, new Margins(MARGIN_SIZE));

        panels.add(north, northData);
        panels.add(center, centerData);
        panels.add(south, southData);

        add(panels);
    }

    private Widget chosenMainPanel(final GenericViewContext viewContext)
    {
        if ("amc".equals(Window.Location.getParameter("view")))
        {
            return new AMC(viewContext);
        } else
        {
            return new SampleBrowser(viewContext);
        }
    }

}
