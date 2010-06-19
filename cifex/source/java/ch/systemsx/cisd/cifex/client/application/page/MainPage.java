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

import static ch.systemsx.cisd.cifex.client.application.utils.InfoDictionary.*;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

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

import ch.systemsx.cisd.cifex.client.application.HTMLRequestCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.WidgetFactory;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.DateTimeUtils;
import ch.systemsx.cisd.cifex.client.application.utils.ImageUtils;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public final class MainPage extends Viewport
{

    public static final String CISD_LOGO_TITLE =
            "CISD - Center for Information Sciences and Databases";

    private final ViewContext context;

    private final MainPageTabPanel tabPanel;

    public MainPage(final ViewContext context, final MainPageTabPanel tabPanel)
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
        cifexLogo.setTitle(CISD_LOGO_TITLE);
        cifexLogo.setPixelSize(81, 50);
        Anchor cifexLogoLinked =
                new Anchor(cifexLogo.getElement().getString(), true, info(HEADER_WEBPAGE_LINK),
                        "_blank");
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
        Anchor html = clickableHTMLWidget(msg(PROFILE_LINK_LABEL), msg(PROFILE_LINK_TOOPTIP));
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
        Anchor html = clickableHTMLWidget(msg(LOGOUT_LINK_LABEL), msg(LOGOUT_LINK_TOOLTIP));

        html.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    context.logoutAndShowLoginPage();
                }
            });
        return html;
    }

    private Widget createHelpWidget()
    {
        Anchor html = clickableHTMLWidget(msg(HELP_LINK_LABEL), msg(HELP_LINK_TOOLTIP));
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
        Anchor html = clickableHTMLWidget(msg(HELP_FAQ_LABEL), msg(HELP_FAQ_TITLE));
        html.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    try
                    {
                        new RequestBuilder(RequestBuilder.GET, HelpDialogController.FAQ_HTML)
                                .sendRequest(null, new HTMLRequestCallback(msg(HELP_FAQ_TITLE),
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
        Anchor html = clickableHTMLWidget(msg(HELP_DISCLAIMER_LABEL), msg(HELP_DISCLAIMER_TITLE));
        html.addClickHandler(new ClickHandler()
            {
                public void onClick(ClickEvent event)
                {
                    try
                    {
                        new RequestBuilder(RequestBuilder.GET, HelpDialogController.DISCLAIMER_HTML)
                                .sendRequest(null, new HTMLRequestCallback(
                                        msg(HELP_DISCLAIMER_TITLE)));
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
        if (StringUtils.isBlank(message))
        {
            msg = msg(UNKNOWN_FAILURE_MSG, ex.getClass().getName());
        } else
        {
            msg = message;
        }
        MessageBox.alert(msg(MESSAGE_BOX_ERROR_TITLE), msg, null);
    }
}
