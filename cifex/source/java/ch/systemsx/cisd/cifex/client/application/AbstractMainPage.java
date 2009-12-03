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

import ch.systemsx.cisd.cifex.client.application.ui.CreateUserWidget;
import ch.systemsx.cisd.cifex.client.application.utils.DateTimeUtils;
import ch.systemsx.cisd.cifex.client.application.utils.ImageUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
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
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Franz-Josef Elmer
 */
// TODO 2008-02-13, Christian Ribeaud: the toolbar should be share between
// 'AdminMainPage',
// 'EditCurrentUserPage' and
// 'MainPage'. The is no reason to re-create it everytime I switch the page.
abstract class AbstractMainPage extends Viewport {

	protected final ViewContext context;

	protected static final void addTitlePart(LayoutContainer container,
			final String text) {
		final Html html = new Html(text);
		html.setStyleName("cifex-heading");
		container.add(html, new FlowData(new Margins(3, 0, 0, 0)));
	}

	AbstractMainPage(final ViewContext context) {
		this.context = context;
		setLayout(new RowLayout());
		Button externalAuthenticationButton = createExternalAuthenticationButton(context);
		SeparatorToolItem externalSeparator = new SeparatorToolItem();

		add(createHeaderWidget(), new RowData(1, Style.DEFAULT, new Margins(5)));
		add(createToolbarPanel(context, externalSeparator,
				externalAuthenticationButton), new RowData(1, Style.DEFAULT,
				new Margins(5)));
		LayoutContainer mainPanel = createMainPanel();
		mainPanel.setScrollMode(Scroll.AUTO);
		add(mainPanel, new RowData(1, 1, new Margins(5)));
		final FooterPanel footerPanel = new FooterPanel(context);
		add(footerPanel, new RowData(1, Style.DEFAULT));
		maybeSetExternalAuthenticationVisible(externalAuthenticationButton,
				externalSeparator, context);
	}

	private Widget createHeaderWidget() {
		LayoutContainer container = new LayoutContainer();
		TableRowLayout layout = new TableRowLayout();
		layout.setWidth("100%");
		container.setLayout(layout);

		final Image cifexLogo = ImageUtils.getCIFEXLogoImageSmall();
		cifexLogo.setTitle(context.getMessageResources().getCISDLogoTitle());
		cifexLogo.setPixelSize(81, 50);
		Anchor cifexLogoLinked = new Anchor(cifexLogo.getElement().getString(),
				true, "http://www.cisd.ethz.ch/software/ghost_of_CIFEX",
				"_blank");
		container.add(cifexLogoLinked, new TableData(HorizontalAlignment.LEFT,
				VerticalAlignment.BOTTOM));
		container.add(createUserInfoWidget(), new TableData(
				HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE));
		// layout.setCellHorizontalAlign(HorizontalAlignment.RIGHT);

		return container;
	}

	private Widget createUserInfoWidget() {
		final UserInfoDTO user = context.getModel().getUser();

		LayoutContainer container = new LayoutContainer();

		RowLayout layout = new RowLayout();

		container.setLayout(layout);
		container.add(userDescriptionInlineHTML(user));
		boolean hasModifyableSettings = user.isExternallyAuthenticated() == false
				&& user.isPermanent() == true;
		if (hasModifyableSettings) {
			container.add(new InlineHTML(" | "));
			container.add(createEditSettingsWidget());
		}

		// TODO Not yet implemented
		// container.add(new InlineHTML("|"));
		// container.add(createHelpWidget());

		container.add(new InlineHTML(" | "));
		container.add(createLogoutWidget());
		return container;
	}

	private InlineHTML userDescriptionInlineHTML(final UserInfoDTO user) {

		final StringBuffer buffer = new StringBuffer();
		buffer.append(user.getUserCode());
		buffer.append(" (");
		final String fullUserName = user.getUserFullName();
		if (fullUserName != null) {
			buffer.append(fullUserName);
		} else {
			buffer.append(user.getEmail());
		}
		buffer.append(")");
		boolean isTemporary = !(user.isAdmin() || user.isPermanent());
		if (isTemporary) {
			buffer.append("<i>&lt;valid until ");
			buffer.append(DateTimeUtils.formatDate(user.getExpirationDate()));
			buffer.append("&gt;</i>");
		}
		InlineHTML html = new InlineHTML(buffer.toString());
		if (user.isAdmin()) {
			html.setTitle("administrator");
		} else if (user.isPermanent()) {
			html.setTitle("regular user");
		} else {
			html.setTitle("temporary user");
		}

		return html;

		// Original implementation of the user description, but provides
		// unneeded information
		// final StringBuffer buffer = new StringBuffer();
		// final String fullUserName = user.getUserFullName();
		// if (fullUserName != null) {
		// buffer.append(fullUserName);
		// } else {
		// buffer.append(user.getEmail());
		// }
		// buffer.append(" (");
		// buffer.append(user.getUserCode());
		// buffer.append(")   <i>&lt;Status: ");
		// if (user.isAdmin()) {
		// buffer.append("administrator");
		// } else if (user.isPermanent()) {
		// buffer.append("regular");
		// } else {
		// buffer.append("expires on: ").append(
		// DateTimeUtils.formatDate(user.getExpirationDate()));
		// }
		// buffer.append("&gt;</i>");
		// return new InlineHTML(buffer.toString());

	}

	private Widget createEditSettingsWidget() {
		Anchor html = clickableHTMLWidget(context.getMessageResources()
				.getEditUserLinkLabel(), context.getMessageResources()
				.getEditUserTooltipLabel());
		html.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				context.getPageController().createEditCurrentUserPage();
			}
		});
		return html;
	}

	private Widget createLogoutWidget() {
		Anchor html = clickableHTMLWidget(context.getMessageResources()
				.getLogoutLinkLabel(), context.getMessageResources()
				.getLogoutLinkTooltip());

		html.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				context.getCifexService().logout(
						AsyncCallbackAdapter.EMPTY_ASYNC_CALLBACK);
				context.getModel().getUrlParams().clear();
				context.getPageController().createLoginPage();
			}
		});
		return html;
	}

	private Anchor clickableHTMLWidget(String title, String tooltip) {
		Anchor html = new Anchor(title);
		html.setTitle(tooltip);
		return html;
	}

	static private void maybeSetExternalAuthenticationVisible(
			final Button eXternalAuthenticationButton,
			final SeparatorToolItem externalSeparator, ViewContext context) {
		setExternalAuthenticationVisible(eXternalAuthenticationButton,
				externalSeparator, false);
		context.getCifexService().showSwitchToExternalOption(
				context.getModel().getUser(),
				new AbstractAsyncCallback<Boolean>(context) {

					public void onSuccess(final Boolean result) {
						final boolean visible = result != null
								&& result.booleanValue();
						setExternalAuthenticationVisible(
								eXternalAuthenticationButton,
								externalSeparator, visible);
					}
				});
	}

	private static void setExternalAuthenticationVisible(
			Button externalAuthenticationButton,
			SeparatorToolItem externalSeparator, final boolean visible) {
		externalAuthenticationButton.setVisible(visible);
		externalSeparator.setVisible(visible);
	}

	static private ToolBar createToolbarPanel(ViewContext context,
			SeparatorToolItem externalSeparator,
			Button externalAuthenticationButton) {
		final UserInfoDTO user = context.getModel().getUser();
		final ToolBar toolbar = new ToolBar();
		if (user.isPermanent() == true) {
			toolbar.add(createInboxPageButton(context));
			toolbar.add(createSharePageButton(context));
			toolbar.add(createInvitePageButton(context));
		}

		if (user.isAdmin() == true) {
			toolbar.add(new SeparatorToolItem());
			toolbar.add(createAdminViewButton(context));
		}

		toolbar.add(externalSeparator);
		toolbar.add(externalAuthenticationButton);
		return toolbar;
	}

	static private final Button createInboxPageButton(final ViewContext context) {
		final Button editProfileButton = new Button(context
				.getMessageResources().getInboxViewLinkLabel());
		editProfileButton.setToolTip(context.getMessageResources()
				.getInboxViewTooltipLabel());
		editProfileButton
				.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						context.getPageController().createInboxPage();
					}
				});
		return editProfileButton;
	}

	static private final Button createSharePageButton(final ViewContext context) {
		final Button shareButton = new Button(context
				.getMessageResources().getShareViewLinkLabel());
		shareButton.setToolTip(context.getMessageResources()
				.getShareViewTooltipLabel());
		shareButton
				.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						context.getPageController().createSharePage();
					}
				});
		return shareButton;
	}

	static private final Button createInvitePageButton(final ViewContext context) {
		final Button inviteButton = new Button(context
				.getMessageResources().getInviteViewLinkLabel());
		inviteButton.setToolTip(context.getMessageResources()
				.getInviteViewTooltipLabel());
		inviteButton
				.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						context.getPageController().createInvitePage();
					}
				});
		return inviteButton;
	}

	static private final Button createExternalAuthenticationButton(
			final ViewContext context) {
		final String externalAuthenticationTitle = context
				.getMessageResources().getExternalAuthenticationLabel();
		final Button editProfileButton = new Button(externalAuthenticationTitle);
		editProfileButton.setToolTip(context.getMessageResources()
				.getExternalAuthenticationButtonTooltip());
		editProfileButton
				.addSelectionListener(new SelectionListener<ButtonEvent>() {

					@Override
					public void componentSelected(ButtonEvent ce) {
						context.getPageController()
								.createExternalAuthenticationPage();
					}
				});
		return editProfileButton;
	}

	static private final Button createAdminViewButton(final ViewContext context) {
		final Button adminViewButton = new Button(context.getMessageResources()
				.getAdminViewLinkLabel());
		adminViewButton.setToolTip(context.getMessageResources()
				.getAdminViewTooltipLabel());
		adminViewButton
				.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						context.getPageController().createAdminPage();
					}
				});
		return adminViewButton;
	}

	static LayoutContainer createContainer() {
		final LayoutContainer container = new LayoutContainer();
		container.setWidth("100%");
		return container;
	}

	protected static final LayoutContainer createUserPanel(
			final boolean allowPermanentUsers, ViewContext context) {
		LayoutContainer createUserPanel = createContainer();
		if (allowPermanentUsers) {
			addTitlePart(createUserPanel, context.getMessageResources()
					.getAdminCreateUserLabel());
		} else {
			addTitlePart(createUserPanel, context.getMessageResources()
					.getCreateUserLabel());
		}
		final CreateUserWidget createUserWidget = new CreateUserWidget(context,
				allowPermanentUsers);
		createUserPanel.add(createUserWidget);
		return createUserPanel;
	}

	protected abstract LayoutContainer createMainPanel();

}
