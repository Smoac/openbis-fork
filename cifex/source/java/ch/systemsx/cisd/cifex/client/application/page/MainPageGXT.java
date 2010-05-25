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

package ch.systemsx.cisd.cifex.client.application.page;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.AsyncCallbackAdapter;
import ch.systemsx.cisd.cifex.client.application.HTMLRequestCallback;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.WidgetFactory;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.DateTimeUtils;
import ch.systemsx.cisd.cifex.client.application.utils.ImageUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public final class MainPageGXT extends Viewport
{

    private final ViewContext context;

    private final MainPageTabPanel tabPanel;

    public MainPageGXT(final ViewContext context, MainPageTabPanel tabPanel)
    {
        super();
        setLayout(new BorderLayout());
        this.context = context;
        this.tabPanel = tabPanel;

        BorderLayoutData headerLayoutData = new BorderLayoutData(LayoutRegion.NORTH, 50);
        headerLayoutData.setMargins(new Margins(10));
        add(createHeaderWidget(), headerLayoutData);

        BorderLayoutData centerLayoutData = new BorderLayoutData(LayoutRegion.CENTER);
        centerLayoutData.setMargins(new Margins(0, 10, 10, 10));
        add(this.tabPanel, centerLayoutData);

    }

    private Widget createHeaderWidget()
    {
        LayoutContainer container = new LayoutContainer();
        TableRowLayout layout = new TableRowLayout();
        layout.setWidth("100%");
        container.setLayout(layout);

        final Image cifexLogo = ImageUtils.getCIFEXLogoImageSmall();
        cifexLogo.setTitle(context.getMessageResources().getCISDLogoTitle());
        cifexLogo.setPixelSize(81, 50);
        Anchor cifexLogoLinked =
                new Anchor(cifexLogo.getElement().getString(), true, context.getMessageResources()
                        .getWebpageLink(), "_blank");
        container.add(cifexLogoLinked, new TableData(HorizontalAlignment.LEFT,
                VerticalAlignment.BOTTOM));
        container.add(createUserInfoWidget(), new TableData(HorizontalAlignment.RIGHT,
                VerticalAlignment.MIDDLE));

        return container;
    }

    private Widget createUserInfoWidget()
    {
        final UserInfoDTO user = context.getModel().getUser();

        LayoutContainer container = new LayoutContainer();

        RowLayout layout = new RowLayout();

        container.setLayout(layout);
        container.add(userDescriptionInlineHTML(user));
        boolean hasModifyableSettings =
                user.isExternallyAuthenticated() == false && user.isPermanent() == true;
        if (hasModifyableSettings)
        {
            container.add(new InlineHTML(" | "));
            container.add(createEditSettingsWidget());
        }

        container.add(new InlineHTML(" | "));
        container.add(createFAQWidget());

        container.add(new InlineHTML(" | "));
        container.add(createDisclamerWidget());

        container.add(new InlineHTML(" | "));
        container.add(createHelpWidget());

        container.add(new InlineHTML(" | "));
        container.add(createLogoutWidget());
        return container;
    }

    private InlineHTML userDescriptionInlineHTML(final UserInfoDTO user)
    {

        final StringBuffer buffer = new StringBuffer();
        buffer.append(user.getUserCode());
        buffer.append(" (");
        final String fullUserName = user.getUserFullName();
        if (fullUserName != null)
        {
            buffer.append(fullUserName);
        } else
        {
            buffer.append(user.getEmail());
        }
        buffer.append(")");
        boolean isTemporary = !(user.isAdmin() || user.isPermanent());
        if (isTemporary)
        {
            buffer.append("<i>&lt;valid until ");
            buffer.append(DateTimeUtils.formatDate(user.getExpirationDate()));
            buffer.append("&gt;</i>");
        }
        InlineHTML html = new InlineHTML(buffer.toString());
        if (user.isAdmin())
        {
            html.setTitle("administrator");
        } else if (user.isPermanent())
        {
            html.setTitle("regular user");
        } else
        {
            html.setTitle("temporary user");
        }

        return html;
    }

    private Widget createEditSettingsWidget()
    {
        Anchor html =
                clickableHTMLWidget(context.getMessageResources().getEditUserLinkLabel(), context
                        .getMessageResources().getEditUserTooltipLabel());
        html.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    context.getPageController().showEditCurrentUserPage();
                }
            });
        return html;
    }

    private Widget createLogoutWidget()
    {
        Anchor html =
                clickableHTMLWidget(context.getMessageResources().getLogoutLinkLabel(), context
                        .getMessageResources().getLogoutLinkTooltip());

        html.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    context.getCifexService().logout(AsyncCallbackAdapter.EMPTY_ASYNC_CALLBACK);
                    context.getModel().getUrlParams().clear();
                    context.getPageController().showLoginPage();
                }
            });
        return html;
    }

    private Widget createHelpWidget()
    {
        Anchor html =
                clickableHTMLWidget(context.getMessageResources().getHelpPageLinkLabel(), context
                        .getMessageResources().getHelpPageTooltipLabel());
        html.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    context.getPageController().showHelpPage();
                }
            });
        return html;

    }

    private Widget createFAQWidget()
    {
        final IMessageResources messageResources = context.getMessageResources();
        Anchor html =
                clickableHTMLWidget(messageResources.getFooterDocumentationLinkLabel(),
                        messageResources.getFooterDocumentationDialogTitle());
        html.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    try
                    {
                        new RequestBuilder(RequestBuilder.GET, "documentation.html").sendRequest(
                                null, new HTMLRequestCallback(context, messageResources
                                        .getFooterDocumentationDialogTitle(),
                                        DefaultLayoutDialog.DEFAULT_WIDTH * 2,
                                        DefaultLayoutDialog.DEFAULT_HEIGHT * 2));
                    } catch (final RequestException ex)
                    {
                        showErrorMessage(ex);
                    }
                }
            });
        return html;
    }

    private Widget createDisclamerWidget()
    {
        final IMessageResources messageResources = context.getMessageResources();
        Anchor html =
                clickableHTMLWidget(messageResources.getFooterDisclaimerLinkLabel(),
                        messageResources.getFooterDisclaimerDialogTitle());
        html.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    try
                    {
                        new RequestBuilder(RequestBuilder.GET, "disclaimer.html").sendRequest(null,
                                new HTMLRequestCallback(context, messageResources
                                        .getFooterDisclaimerDialogTitle()));
                    } catch (final RequestException ex)
                    {
                        showErrorMessage(ex);
                    }
                }
            });
        return html;
    }

    private Anchor clickableHTMLWidget(String title, String tooltip)
    {
        return WidgetFactory.createClickableHTMLWidget(title, tooltip);
    }

    private final void showErrorMessage(final Throwable ex)
    {
        final String msg;
        final String message = ex.getMessage();
        final IMessageResources messageResources = context.getMessageResources();
        if (StringUtils.isBlank(message))
        {
            msg = messageResources.getExceptionWithoutMessage(ex.getClass().getName());
        } else
        {
            msg = message;
        }
        MessageBox.alert(messageResources.getMessageBoxErrorTitle(), msg, null);
    }
}
