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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractViewer<T extends IClientServiceAsync> extends ContentPanel
{
    private final ToolBar toolBar;
    protected final IViewContext<T> viewContext;

    public AbstractViewer(final IViewContext<T> viewContext, String title)
    {
        this.viewContext = viewContext;
        setHeaderVisible(false);
        toolBar = new ToolBar();
        setTopComponent(toolBar);
        toolBar.add(new LabelToolItem(title));
        toolBar.add(new FillToolItem());
        Button button = new Button(viewContext.getMessage(Dict.BUTTON_EDIT));
        button.addListener(Events.Select, new Listener<BaseEvent>()
            {
        
                public void handleEvent(BaseEvent be)
                {
                    MessageBox.alert("Info", "Not yet implemented", null);
                }
        
            });
        toolBar.add(new AdapterToolItem(button));
    }
}
