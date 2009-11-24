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

package ch.systemsx.cisd.cifex.client.application;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.cifex.client.application.ui.CreateUserWidget;
import ch.systemsx.cisd.cifex.client.application.utils.DateTimeUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Franz-Josef Elmer
 */
// TODO 2008-02-13, Christian Ribeaud: the toolbar should be share between 'AdminMainPage',
// 'EditCurrentUserPage' and
// 'MainPage'. The is no reason to re-create it everytime I switch the page.
abstract class AbstractMainPage extends Viewport
{

    protected final ViewContext context;

    protected static final void addTitlePart(LayoutContainer container, final String text)
    {
        final Html html = new Html(text);
        html.setStyleName("cifex-heading");
        container.add(html, new FlowData(new Margins(3, 0, 0, 0)));
    }

    AbstractMainPage(final ViewContext context)
    {
        this.context = context;
        setLayout(new RowLayout());
        Button externalAuthenticationButton = createExternalAuthenticationButton(context);
        SeparatorToolItem externalSeparator = new SeparatorToolItem();
        add(createToolbarPanel(context, externalSeparator, externalAuthenticationButton),
                new RowData(1, -1, new Margins(5)));
        LayoutContainer mainPanel = createMainPanel();
        mainPanel.setScrollMode(Scroll.AUTO);
        add(mainPanel, new RowData(1, 1, new Margins(5)));
        final FooterPanel footerPanel = new FooterPanel(context);
        add(footerPanel, new RowData(1, -1));
        maybeSetExternalAuthenticationVisible(externalAuthenticationButton, externalSeparator,
                context);
    }

    static private void maybeSetExternalAuthenticationVisible(
            final Button eXternalAuthenticationButton, final SeparatorToolItem externalSeparator,
            ViewContext context)
    {
        setExternalAuthenticationVisible(eXternalAuthenticationButton, externalSeparator, false);
        context.getCifexService().showSwitchToExternalOption(context.getModel().getUser(),
                new AbstractAsyncCallback<Boolean>(context)
                    {

                        public void onSuccess(final Boolean result)
                        {
                            final boolean visible = result != null && result.booleanValue();
                            setExternalAuthenticationVisible(eXternalAuthenticationButton,
                                    externalSeparator, visible);
                        }
                    });
    }

    private static void setExternalAuthenticationVisible(Button externalAuthenticationButton,
            SeparatorToolItem externalSeparator, final boolean visible)
    {
        externalAuthenticationButton.setVisible(visible);
        externalSeparator.setVisible(visible);
    }

    static private ToolBar createToolbarPanel(ViewContext context,
            SeparatorToolItem externalSeparator, Button externalAuthenticationButton)
    {
        final UserInfoDTO user = context.getModel().getUser();
        final ToolBar toolbar = new ToolBar();
        toolbar.add(createUserDescription(user));
        if (user.isPermanent() == true)
        {
            toolbar.add(new SeparatorToolItem());
            toolbar.add(createMainViewButton(context));
        }

        if (user.isAdmin() == true)
        {
            toolbar.add(new SeparatorToolItem());
            toolbar.add(createAdminViewButton(context));
        }

        if (user.isExternallyAuthenticated() == false && user.isPermanent() == true)
        {
            toolbar.add(new SeparatorToolItem());
            toolbar.add(createEditProfileButton(context));
        }

        toolbar.add(externalSeparator);
        toolbar.add(externalAuthenticationButton);
        toolbar.add(new SeparatorToolItem());
        toolbar.add(createLogoutButton(context));
        return toolbar;
    }

    private static LabelToolItem createUserDescription(final UserInfoDTO user)
    {
        final StringBuffer buffer = new StringBuffer();
        final String fullUserName = user.getUserFullName();
        if (fullUserName != null)
        {
            buffer.append(fullUserName);
        } else
        {
            buffer.append(user.getEmail());
        }
        buffer.append(" (");
        buffer.append(user.getUserCode());
        buffer.append(")   <i>&lt;Status: ");
        if (user.isAdmin())
        {
            buffer.append("administrator");
        } else if (user.isPermanent())
        {
            buffer.append("regular");
        } else
        {
            buffer.append("temporary account: expiration date: ").append(
                    DateTimeUtils.formatDate(user.getExpirationDate()));
        }
        buffer.append("&gt;</i>");
        return new LabelToolItem(buffer.toString());
    }

    static private final Button createLogoutButton(final ViewContext context)
    {
        final Button logoutButton = new Button(context.getMessageResources().getLogoutLinkLabel());
        logoutButton.setToolTip(context.getMessageResources().getLogoutLinkTooltip());
        logoutButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    context.getCifexService().logout(AsyncCallbackAdapter.EMPTY_ASYNC_CALLBACK);
                    context.getModel().getUrlParams().clear();
                    context.getPageController().createLoginPage();
                }
            });
        return logoutButton;
    }

    static private final Button createMainViewButton(final ViewContext context)
    {
        final Button editProfileButton =
                new Button(context.getMessageResources().getMainViewLinkLabel());
        editProfileButton.setToolTip(context.getMessageResources().getMainViewTooltipLabel());
        editProfileButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    context.getPageController().createMainPage();
                }
            });
        return editProfileButton;
    }

    static private final Button createEditProfileButton(final ViewContext context)
    {
        final Button editProfileButton =
                new Button(context.getMessageResources().getEditUserLinkLabel());
        editProfileButton.setToolTip(context.getMessageResources().getEditUserTooltipLabel());
        editProfileButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    context.getPageController().createEditCurrentUserPage();
                }
            });
        return editProfileButton;
    }

    static private final Button createExternalAuthenticationButton(final ViewContext context)
    {
        final String externalAuthenticationTitle =
                context.getMessageResources().getExternalAuthenticationLabel();
        final Button editProfileButton = new Button(externalAuthenticationTitle);
        editProfileButton.setToolTip(context.getMessageResources()
                .getExternalAuthenticationButtonTooltip());
        editProfileButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    context.getPageController().createExternalAuthenticationPage();
                }
            });
        return editProfileButton;
    }

    static private final Button createAdminViewButton(final ViewContext context)
    {
        final Button adminViewButton =
                new Button(context.getMessageResources().getAdminViewLinkLabel());
        adminViewButton.setToolTip(context.getMessageResources().getAdminViewTooltipLabel());
        adminViewButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    context.getPageController().createAdminPage();
                }
            });
        return adminViewButton;
    }

    static LayoutContainer createContainer()
    {
        final LayoutContainer container = new LayoutContainer();
        container.setWidth("100%");
        return container;
    }

    protected static final LayoutContainer createUserPanel(final boolean allowPermanentUsers,
            ViewContext context)
    {
        LayoutContainer createUserPanel = createContainer();
        if (allowPermanentUsers)
        {
            addTitlePart(createUserPanel, context.getMessageResources().getAdminCreateUserLabel());
        } else
        {
            addTitlePart(createUserPanel, context.getMessageResources().getCreateUserLabel());
        }
        final CreateUserWidget createUserWidget =
                new CreateUserWidget(context, allowPermanentUsers);
        createUserPanel.add(createUserWidget);
        return createUserPanel;
    }

    protected abstract LayoutContainer createMainPanel();

}
