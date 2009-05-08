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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SearchWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.administration.AdministrationMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.dataset.DataSetMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.experiment.ExperimentMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.material.MaterialMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.sample.SampleMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.user.ChangeUserHomeGroupDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * Implements functionality of the top menu.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class TopMenu extends LayoutContainer
{
    public final static class LogoutCallback extends AbstractAsyncCallback<Void>
    {
        LogoutCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final Void result)
        {
            viewContext.getPageController().reload(true);
        }
    }

    public static final String ID = GenericConstants.ID_PREFIX + "top-menu";

    public static final String LOGOUT_BUTTON_ID = GenericConstants.ID_PREFIX + "logout-button";

    public static final String CHANGE_USER_HOME_GROUP_BUTTON_ID =
            GenericConstants.ID_PREFIX + "change-user-home-group-button";

    public static final String ICON_STYLE = "icon-menu-show";

    /** {@link ActionMenu} kind enum with names matching dictionary keys */
    public static enum ActionMenuKind
    {
        ADMINISTRATION_MENU_MANAGE_GROUPS,

        AUTHORIZATION_MENU_USERS, AUTHORIZATION_MENU_ROLES,

        DATA_SET_MENU_SEARCH, DATA_SET_MENU_TYPES,

        EXPERIMENT_MENU_BROWSE, EXPERIMENT_MENU_NEW, EXPERIMENT_MENU_TYPES,

        MATERIAL_MENU_BROWSE, MATERIAL_MENU_IMPORT, MATERIAL_MENU_TYPES,

        SAMPLE_MENU_BROWSE, SAMPLE_MENU_NEW, SAMPLE_MENU_IMPORT, SAMPLE_MENU_TYPES,

        PROJECT_MENU_BROWSE, PROJECT_MENU_NEW,

        DATA_SET_MENU_FILE_FORMATS,

        PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES, PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS,
        PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES, PROPERTY_TYPES_MENU_ASSIGN_TO_EXPERIMENT_TYPE,
        PROPERTY_TYPES_MENU_ASSIGN_TO_MATERIAL_TYPE, PROPERTY_TYPES_MENU_ASSIGN_TO_DATA_SET_TYPE,
        PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE,

        VOCABULARY_MENU_BROWSE, VOCABULARY_MENU_NEW;

        public String getMenuId()
        {
            return ID + "_" + this.name();
        }

        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name());
        }
    }

    private final ToolBar toolBar;

    private final CommonViewContext viewContext;

    private final ComponentProvider componentProvider;

    public TopMenu(final CommonViewContext viewContext, ComponentProvider componentProvider)
    {
        this.viewContext = viewContext;
        this.componentProvider = componentProvider;
        setId(ID);
        setLayout(new FlowLayout());
        setBorders(true);
        toolBar = new ToolBar();
        add(toolBar);
    }

    final void refresh()
    {
        toolBar.removeAll();

        toolBar.add(new DataSetMenu(viewContext, componentProvider));
        toolBar.add(new ExperimentMenu(viewContext, componentProvider));
        toolBar.add(new SampleMenu(viewContext, componentProvider));
        toolBar.add(new MaterialMenu(viewContext, componentProvider));
        toolBar.add(new AdministrationMenu(viewContext, componentProvider));

        toolBar.add(new FillToolItem());
        toolBar.add(new AdapterToolItem(new SearchWidget(viewContext)));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new ChangeUserHomeGroupButton(viewContext));
        toolBar.add(new LogoutButton(viewContext));
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    //
    // Helper classes
    //

    private static final class ChangeUserHomeGroupButton extends TextToolItem
    {
        private final CommonViewContext viewContext;

        ChangeUserHomeGroupButton(final CommonViewContext viewContext)
        {
            super();
            this.viewContext = viewContext;
            addSelectionListener();
            setId(CHANGE_USER_HOME_GROUP_BUTTON_ID);
            refreshButtonText();
        }

        private void addSelectionListener()
        {
            final ChangeUserHomeGroupButton changeUserHomeGroupButton = this;
            final SelectionListener<ComponentEvent> listener =
                    new SelectionListener<ComponentEvent>()
                        {

                            //
                            // SelectionListener
                            //

                            @Override
                            public final void componentSelected(final ComponentEvent ce)
                            {
                                ChangeUserHomeGroupDialog dialog =
                                        new ChangeUserHomeGroupDialog(viewContext,
                                                new IDelegatedAction()
                                                    {
                                                        public void execute()
                                                        {
                                                            changeUserHomeGroupButton
                                                                    .refreshButtonText();
                                                        }
                                                    });
                                dialog.show();
                            }
                        };
            addSelectionListener(listener);
        }

        public void refreshButtonText()
        {
            setText(getUserInfo());
        }

        private final String getUserInfo()
        {
            final SessionContext sessionContext = viewContext.getModel().getSessionContext();
            final User user = sessionContext.getUser();
            final String userName = user.getUserName();
            final String homeGroup = user.getHomeGroupCode();
            final String info;
            if (homeGroup == null)
            {
                info = viewContext.getMessage(Dict.HEADER_USER_WITHOUT_HOMEGROUP, userName);
            } else
            {
                info = viewContext.getMessage(Dict.HEADER_USER_WITH_HOMEGROUP, userName, homeGroup);
            }
            return info;
        }
    }

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
                                DisplaySettings displaySettings =
                                        viewContext.getModel().getSessionContext()
                                                .getDisplaySettings();
                                viewContext.getService().logout(displaySettings,
                                        new LogoutCallback(viewContext));
                            }
                        };
            addSelectionListener(listener);
            setId(LOGOUT_BUTTON_ID);
        }
    }
}
