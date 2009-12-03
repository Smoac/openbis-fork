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

package ch.systemsx.cisd.cifex.client.application.ui;

import java.util.Map;

import ch.systemsx.cisd.cifex.client.application.Model;
import ch.systemsx.cisd.cifex.client.application.ServletPathConstants;
import ch.systemsx.cisd.cifex.client.application.utils.WindowUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A static class offering some helper methods regarding file download.
 * 
 * @author Christian Ribeaud
 */
public final class FileDownloadHelper {

	private FileDownloadHelper() {
		// Can not be instantiated.
	}

	/**
	 * Starts file download in a new window.
	 * <p>
	 * URL parameters are taken from given <var>model</var>. This action is only
	 * fired if a file id AND a user code have been specified in the URL.
	 * </p>
	 * 
	 * @return Return true if the a file download information was specified
	 *         (regardless of whether or not the d/l was successfully started).
	 */
	public final static boolean startFileDownload(final Model model) {
		assert model != null : "Given model can not be null.";
		final UserInfoDTO user = model.getUser();
		assert user != null : "Undefined user.";
		final Map<String, String> urlParams = model.getUrlParams();
		final String fileId = urlParams.get(Constants.FILE_ID_PARAMETER);
		final String userCode = urlParams.get(Constants.USERCODE_PARAMETER);
		if (fileId == null || userCode == null
				|| userCode.equals(user.getUserCode()) == false) {
			return false;
		}
		try {
			final String url = createDownloadUrl(Long.parseLong(fileId));
			WindowUtils.openNewDependentWindow(url);
		} catch (final NumberFormatException ex) {
			// Nothing to do here. Just do not open the new window.
		}

		return true;
	}

	/** Creates a download link for given <var>fileId</var>. */
	public final static String createDownloadUrl(final long fileId) {
		return ServletPathConstants.FILE_DOWNLOAD_SERVLET_NAME + "?"
				+ Constants.FILE_ID_PARAMETER + "=" + fileId;
	}

}
