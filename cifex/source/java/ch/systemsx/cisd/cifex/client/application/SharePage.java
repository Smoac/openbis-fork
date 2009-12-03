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

package ch.systemsx.cisd.cifex.client.application;

import ch.systemsx.cisd.cifex.client.application.ui.FileUploadWidget;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * The page where files may be uploaded, and uploaded files can be maintained.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
final class SharePage extends AbstractFileListPage {
	SharePage(final ViewContext context) {
		super(context);
	}

	private static final Widget createExplanationPanel(ViewContext context) {
		IMessageResources messageResources = context.getMessageResources();
		Model model = context.getModel();
		final boolean isPermanent = model.getUser().isPermanent();
		final UserInfoDTO user = model.getUser();
		StringBuffer notesText = new StringBuffer();
		notesText.append(messageResources.getUploadFilesHelpUpload(
				getMaxFileSize(user.getMaxFileSizePerQuotaGroupInMB()),
				getCurrentFileSizeInMB(user.getCurrentFileSize()),
				getMaxFileCount(user.getMaxFileCountPerQuotaGroup()), user
						.getCurrentFileCount()));
		if (isPermanent) {
			notesText
					.append(messageResources.getUploadFilesHelpPermanentUser());
		} else {
			notesText
					.append(messageResources.getUploadFilesHelpTemporaryUser());
		}
		notesText.append(messageResources.getUploadFilesHelpSecurity());
		return new HTML(notesText.toString());
	}

	@Override
	protected final LayoutContainer createMainPanel() {
		final LayoutContainer contentPanel = new LayoutContainer();
		contentPanel.add(createUploadPart(context));
		createListFilesGrid(contentPanel, UPLOAD, context);
		return contentPanel;
	}

	static private LayoutContainer createUploadPart(ViewContext context) {
		IMessageResources messageResources = context.getMessageResources();
		final LayoutContainer verticalPanel = createContainer();
		addTitlePart(verticalPanel, context.getMessageResources()
				.getUploadFilesPartTitle());
		verticalPanel.add(createExplanationPanel(context));
		addTitlePart(verticalPanel, messageResources
				.getUploadFilesPartTitleLess2GB());
		verticalPanel.add(new FileUploadWidget(context));
		addTitlePart(verticalPanel, messageResources
				.getUploadFilesPartTitleGreater2GB());
		String webStartLink = messageResources
				.getUploadFilesHelpJavaUploaderLink();
		String webStartTitle = messageResources
				.getUploadFilesHelpJavaUploaderTitle();
		String anchorWebstart = DOMUtils.createAnchor(webStartTitle,
				webStartLink, ServletPathConstants.FILE2GB_UPLOAD_SERVLET_NAME,
				null, null, false);
		String cliLink = messageResources.getUploadFilesHelpCLILink();
		String cliTitle = messageResources.getUploadFilesHelpCLITitle();
		String anchorCLI = DOMUtils.createAnchor(cliTitle, cliLink,
				ServletPathConstants.COMMAND_LINE_CLIENT_DISTRIBUTION, null,
				null, false);
		verticalPanel.add(new HTML(messageResources
				.getUploadFilesHelpJavaUpload(anchorWebstart, anchorCLI)));
		return verticalPanel;
	}
}
