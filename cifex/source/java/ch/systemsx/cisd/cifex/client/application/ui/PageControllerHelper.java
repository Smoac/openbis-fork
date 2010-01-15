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

import ch.systemsx.cisd.cifex.client.application.Model;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;

/**
 * A static class offering a helper method used to show page based on current application state.
 * 
 * @author Piotr Buczek
 */
public final class PageControllerHelper
{

    private PageControllerHelper()
    {
        // Can not be instantiated.
    }

    /**
     * Activates:
     * <ul>
     * <li>Login page if user is not logged in,
     * <li>'Inbox' tab if file download was triggered or there are files in the Inbox and user
     * didn't trigger data set upload from openBIS,
     * <li>'Share' tab otherwise.
     * </ul>
     */
    public final static void activatePageBasedOnCurrentContext(final ViewContext context,
            final CurrentUserInfoDTO currentUser)
    {
        if (currentUser != null)
        {
            final Model model = context.getModel();
            model.setUser(currentUser);
            if (FileDownloadHelper.startFileDownload(model)
                    || (currentUser.hasFilesForDownload() && false == isUploadTriggered(model)))
            {
                context.getPageController().showInboxPage();
            } else
            {
                context.getPageController().showSharePage();
            }
        } else
        {
            context.getPageController().showLoginPage();
        }
    }

    /**
     * @return <code>true</code> if a file upload information is specified in the URL,
     *         <code>false</code> otherwise
     */
    private static boolean isUploadTriggered(final Model model)
    {
        return model.getUrlParams().containsKey(Constants.RECIPIENTS_PARAMETER);
    }
}
