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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;

/**
 * Implements functionality of the top menu.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class TopMenu extends LayoutContainer
{
    public static final String LOGOUT_BUTTON_ID = GenericConstants.ID_PREFIX + "logout-button";

    private final ToolBar toolBar;

    private final CommonViewContext viewContext;

    public TopMenu(final CommonViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FlowLayout());
        setBorders(true);
        toolBar = new ToolBar();
        add(toolBar);
    }

    final void refresh()
    {
        toolBar.removeAll();
        toolBar.add(new AdapterToolItem(createTitleHeader()));
        toolBar.add(new AdapterToolItem(new SearchWidget(viewContext)));
        toolBar.add(new FillToolItem());
        toolBar.add(new AdapterToolItem(userInfo()));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new LogoutButton(viewContext));
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    private final Html userInfo()
    {
        final SessionContext sessionContext = viewContext.getModel().getSessionContext();
        final User user = sessionContext.getUser();
        final String userName = user.getUserName();
        final String homeGroup = user.getHomeGroupCode();
        final String fullInfo;
        if (homeGroup == null)
        {
            fullInfo = viewContext.getMessage(Dict.HEADER_USER_WITHOUT_HOMEGROUP, userName);
        } else
        {
            fullInfo = viewContext.getMessage(Dict.HEADER_USER_WITH_HOMEGROUP, userName, homeGroup);
        }
        final Html html = new Html(fullInfo);
        html.setStyleAttribute("marginRight", "7px");
        return html;
    }

    private final Html createTitleHeader()
    {
        final Element boldElement = DOM.createElement("b");
        boldElement.setInnerHTML(viewContext.getMessage(Dict.APPLICATION_NAME));
        final Html titleHeader = new Html(DOM.toString(boldElement));
        titleHeader.setStyleAttribute("margin", "0 1em 0 1em");
        return titleHeader;
    }

    //
    // Helper classes
    //

    private final static class LogoutButton extends TextToolItem
    {

        LogoutButton(final CommonViewContext viewContext)
        {
            super(viewContext.getMessage(Dict.HEADER_LOGOUT_BUTTON_LABEL));
            final SelectionListener<ComponentEvent> listener =
                    new SelectionListener<ComponentEvent>()
                        {

                            //
                            // SelectionListener
                            //

                            @Override
                            public final void componentSelected(final ComponentEvent ce)
                            {
                                viewContext.getService().logout(
                                        new AbstractAsyncCallback<Void>(viewContext)
                                            {

                                                //
                                                // AbstractAsyncCallback
                                                //

                                                @Override
                                                public final void process(final Void result)
                                                {
                                                    viewContext.getPageController().reload(true);
                                                }
                                            });
                            }
                        };
            addSelectionListener(listener);
            setId(LOGOUT_BUTTON_ID);
        }
    }
}
