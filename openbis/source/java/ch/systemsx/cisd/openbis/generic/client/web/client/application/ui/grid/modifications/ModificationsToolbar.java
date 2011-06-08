/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.modifications;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IGenericImageBundle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Toolbar for handling table modifications.
 * 
 * @author Piotr Buczek
 */
public class ModificationsToolbar extends ToolBar
{
    private static final IGenericImageBundle IMAGE_BUNDLE = GWT
            .<IGenericImageBundle> create(IGenericImageBundle.class);

    private final IMessageProvider messageProvider;

    @SuppressWarnings("unused")
    private final IViewContext<?> viewContext; // TODO remove if not used

    public ModificationsToolbar(final IViewContext<?> viewContext,
            final IBrowserGridActionInvoker browserActionInvoker)
    {
        this.viewContext = viewContext;
        this.messageProvider = viewContext;

        add(new Label(messageProvider.getMessage(Dict.TABLE_MODIFICATIONS)));

        final AbstractImagePrototype confirmIcon =
                AbstractImagePrototype.create(IMAGE_BUNDLE.getConfirmIcon());
        final AbstractImagePrototype cancelIcon =
                AbstractImagePrototype.create(IMAGE_BUNDLE.getCancelIcon());
        add(new Button("Save", confirmIcon, new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent be)
                {
                    browserActionInvoker.saveModifications();
                }
            }));
        add(new Button("Cancel", cancelIcon, new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent be)
                {
                    browserActionInvoker.cancelModifications();
                }
            }));
    }

}
