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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IInfoHandler;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A {@link ICallbackListener} which outputs the failure message to the
 * specified {@link IInfoHandler}.
 * 
 * @author Christian Ribeaud
 */
public class InfoBoxCallbackListener<T> extends CallbackListenerAdapter<T> {
	private final IInfoHandler infoHandler;

	public InfoBoxCallbackListener(final IInfoHandler infoBox) {
		this.infoHandler = infoBox;
	}

	//
	// ICallbackListener
	//

	@Override
	public final void onFailureOf(final IMessageProvider messageProvider,
			final AbstractAsyncCallback<T> callback,
			final String failureMessage, final Throwable throwable) {
		if (failureMessage.contains("502")
				&& failureMessage.contains("Proxy Error")) {
			infoHandler
					.displayProgress("Client lost connectivity with the server:<br>"
							+ "If you are uploading data it will be probably available in a few minutes.Check it before uploading again. ");
		} else {
			infoHandler.displayError(failureMessage.replace("\n", "<br>"));
		}
	}
}
