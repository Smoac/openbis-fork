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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.ButtonConfig;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.ToolbarSeparator;
import com.gwtext.client.widgets.ToolbarTextItem;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.BorderLayout;
import com.gwtext.client.widgets.layout.ContentPanel;
import com.gwtext.client.widgets.layout.LayoutRegionConfig;

import ch.systemsx.cisd.cifex.client.application.ui.CreateUserWidget;
import ch.systemsx.cisd.cifex.client.application.utils.DateTimeUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Franz-Josef Elmer
 */
// TODO 2008-02-13, Christian Ribeaud: the toolbar should be share between 'AdminMainPage',
// 'EditCurrentUserPage' and
// 'MainPage'. The is no reason to re-create it everytime I switch the page.
abstract class AbstractMainPage extends BorderLayout
{
    private static final String TOGGLE_GROUP = "toggleGroup";

    protected IMessageResources messageResources;

    protected VerticalPanel createUserPanel;

    private final ToolbarSeparator externalSeparator;

    private final ToolbarButton eXternalAuthenticationButton;

    private final static LayoutRegionConfig createCenterRegion()
    {
        final LayoutRegionConfig center = new LayoutRegionConfig();
        center.setTitlebar(false);
        center.setAutoScroll(true);
        return center;
    }

    private final static LayoutRegionConfig createNorthRegion()
    {
        final LayoutRegionConfig north = new LayoutRegionConfig();
        north.setSplit(false);
        north.setInitialSize(30);
        north.setTitlebar(false);
        north.setAutoScroll(false);
        return north;
    }

    private final static LayoutRegionConfig createSouthRegion()
    {
        final LayoutRegionConfig south = new LayoutRegionConfig();
        south.setTitlebar(false);
        south.setAutoScroll(false);
        south.setInitialSize(20);
        return south;
    }

    protected static final Widget createPartTitle(final String text)
    {
        final HTML html = new HTML(text);
        html.setStyleName("cifex-heading");
        return html;
    }

    protected final ViewContext context;

    AbstractMainPage(final ViewContext context)
    {
        super("100%", "100%", createNorthRegion(), createSouthRegion(), null, null,
                createCenterRegion());
        this.context = context;
        this.messageResources = context.getMessageResources();
        eXternalAuthenticationButton = createExternalAuthenticationButton();
        externalSeparator = new ToolbarSeparator();
        add(LayoutRegionConfig.NORTH, createToolbarPanel());
        add(LayoutRegionConfig.CENTER, createMainPanel());
        final FooterPanel footerPanel = new FooterPanel(context);
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.add(footerPanel);
        add(LayoutRegionConfig.SOUTH, contentPanel);
        maybeSetExternalAuthenticationVisible();
    }

    private void maybeSetExternalAuthenticationVisible()
    {
        setExternalAuthenticationVisible(false);
        context.getCifexService().showSwitchToExternalOption(context.getModel().getUser(),
                new AbstractAsyncCallback(context)
                    {

                        public void onSuccess(final Object result)
                        {
                            final Boolean resultAsBoolean = (Boolean) result;
                            final boolean visible =
                                    resultAsBoolean != null && resultAsBoolean.booleanValue();
                            setExternalAuthenticationVisible(visible);
                        }
                    });
    }

    private void setExternalAuthenticationVisible(final boolean visible)
    {
        eXternalAuthenticationButton.setVisible(visible);
        externalSeparator.setVisible(visible);
    }

    private ContentPanel createToolbarPanel()
    {
        final UserInfoDTO user = context.getModel().getUser();
        final ContentPanel contentPanel = new ContentPanel("cifex-toolbar-panel");
        final Toolbar toolbar = new Toolbar(Ext.generateId());
        toolbar.addItem(createUserDescription(user));
        if (user.isPermanent() == true)
        {
            toolbar.addSeparator();
            toolbar.addButton(createMainViewButton());
        }

        if (user.isAdmin() == true)
        {
            toolbar.addSeparator();
            toolbar.addButton(createAdminViewButton());
        }

        if (user.isExternallyAuthenticated() == false && user.isPermanent() == true)
        {
            toolbar.addSeparator();
            toolbar.addButton(createEditProfileButton());
        }

        toolbar.addItem(externalSeparator);
        toolbar.addButton(eXternalAuthenticationButton);
        toolbar.addSeparator();
        toolbar.addButton(createLogoutButton());
        contentPanel.add(toolbar);
        return contentPanel;
    }

    private ToolbarTextItem createUserDescription(final UserInfoDTO user)
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
        return new ToolbarTextItem(buffer.toString());
    }

    private final ToolbarButton createLogoutButton()
    {
        final ToolbarButton logoutButton =
                new ToolbarButton(messageResources.getLogoutLinkLabel(), new ButtonConfig()
                    {
                        {
                            setTooltip(messageResources.getLogoutLinkTooltip());
                        }
                    });
        logoutButton.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button button, final EventObject e)
                {
                    context.getCifexService().logout(AsyncCallbackAdapter.EMPTY_ASYNC_CALLBACK);
                    context.getModel().getUrlParams().clear();
                    context.getPageController().createLoginPage();
                }
            });
        return logoutButton;
    }

    private final ToolbarButton createMainViewButton()
    {
        final ToolbarButton editProfileButton =
                new ToolbarButton(messageResources.getMainViewLinkLabel(), new ButtonConfig()
                    {
                        {
                            setTooltip(messageResources.getMainViewTooltipLabel());
                            setToggleGroup(TOGGLE_GROUP);
                        }
                    });
        editProfileButton.addButtonListener(new ButtonListenerAdapter()
            {
                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button button, final EventObject e)
                {
                    context.getPageController().createMainPage();
                }
            });
        return editProfileButton;
    }

    private final ToolbarButton createEditProfileButton()
    {
        final ToolbarButton editProfileButton =
                new ToolbarButton(messageResources.getEditUserLinkLabel(), new ButtonConfig()
                    {
                        {
                            setTooltip(messageResources.getEditUserTooltipLabel());
                            setToggleGroup(TOGGLE_GROUP);
                        }
                    });
        editProfileButton.addButtonListener(new ButtonListenerAdapter()
            {
                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button button, final EventObject e)
                {
                    context.getPageController().createEditCurrentUserPage();
                }
            });
        return editProfileButton;
    }

    private final ToolbarButton createExternalAuthenticationButton()
    {
        final String externalAuthenticationTitle =
                messageResources.getExternalAuthenticationLabel();

        final ToolbarButton editProfileButton =
                new ToolbarButton(externalAuthenticationTitle, new ButtonConfig()
                    {
                        {
                            setTooltip(messageResources.getExternalAuthenticationButtonTooltip());
                            setToggleGroup(TOGGLE_GROUP);
                        }
                    });
        editProfileButton.addButtonListener(new ButtonListenerAdapter()
            {
                public final void onClick(final Button button, final EventObject e)
                {

                    context.getPageController().createExternalAuthenticationPage();
                }
            });
        return editProfileButton;
    }

    private final ToolbarButton createAdminViewButton()
    {
        final ToolbarButton adminViewButton =
                new ToolbarButton(messageResources.getAdminViewLinkLabel(), new ButtonConfig()
                    {
                        {
                            setTooltip(messageResources.getAdminViewTooltipLabel());
                            setToggleGroup(TOGGLE_GROUP);
                        }
                    });
        adminViewButton.addButtonListener(new ButtonListenerAdapter()
            {
                public final void onClick(final Button button, final EventObject e)
                {
                    context.getPageController().createAdminPage();
                }
            });
        return adminViewButton;
    }

    private final CreateUserWidget createCreateUserWidget(final boolean allowPermanentUsers)
    {

        final CreateUserWidget createUserWidget =
                new CreateUserWidget(context, allowPermanentUsers);
        return createUserWidget;

    }

    static VerticalPanel createVerticalPanelPart()
    {
        final VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setWidth("100%");
        verticalPanel.setSpacing(5);
        return verticalPanel;
    }

    protected final void createUserPanel(final boolean allowPermanentUsers)
    {
        createUserPanel = createVerticalPanelPart();
        if (allowPermanentUsers)
        {
            createUserPanel.add(createPartTitle(messageResources.getAdminCreateUserLabel()));
        } else
        {
            createUserPanel.add(createPartTitle(messageResources.getCreateUserLabel()));
        }
        final CreateUserWidget createUserWidget = createCreateUserWidget(allowPermanentUsers);
        createUserPanel.add(createUserWidget);
    }

    protected abstract ContentPanel createMainPanel();

}
