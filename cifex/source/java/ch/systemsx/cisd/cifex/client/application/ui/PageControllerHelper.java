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

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
    /** Number of milli-seconds in a minute.. */
    private static final int MILLIS_PER_MIN = 60 * 1000;

    /** Keep-alive period in milli-seconds. */
    private static final int KEEPALIVE_TIMER_PERIOD_MILLIS = 1 * MILLIS_PER_MIN;

    /** The minimal number of keep-alive pings per timeout period. */
    private static final int MIN_KEEPALIVE_PINGS_PER_TIMEOUT_PERIOD = 4;

    /**
     * private static final int MIN_KEEPALIVE_PINGS_PER_TIMEOUT_PERIOD = 4; private
     * PageControllerHelper() { // Can not be instantiated. } /** Activates:
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
            keepSessionAlive(context);
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

    /**
     * Tries to keep session alive until user logs out or closes browser
     */
    private static void keepSessionAlive(final ViewContext context)
    {
        final Timer t = new Timer()
            {
                @Override
                public void run()
                {
                    // Callback will cancel keeping session alive if something went wrong
                    // or user logged out.
                    final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>()
                        {

                            public void onSuccess(Boolean result)
                            {
                                if (result == false)
                                {
                                    cancel();
                                }
                            }

                            public void onFailure(Throwable caught)
                            {
                                cancel();
                            }
                        };
                    context.getCifexService().keepSessionAlive(callback);
                }
            };
        context.setKeepAliveTimerOrNull(t);
        final int sessionTimeoutMillis =
                context.getModel().getConfiguration().getSessionTimeoutMin() * MILLIS_PER_MIN;
        final int keepAliveTimerPeriod =
                Math.min(KEEPALIVE_TIMER_PERIOD_MILLIS, sessionTimeoutMillis
                        / MIN_KEEPALIVE_PINGS_PER_TIMEOUT_PERIOD);
        t.scheduleRepeating(keepAliveTimerPeriod);
    }
}
