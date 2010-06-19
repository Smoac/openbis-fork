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

import com.google.gwt.user.client.Timer;

import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;

/**
 * Context with everything needed by view classes.
 * 
 * @author Franz-Josef Elmer
 */
public class ViewContext
{
    private final IPageController pageController;

    private final IHistoryController historyController;

    private final ICIFEXServiceAsync cifexService;

    private final Model model;
    
    private Timer keepAliveTimerOrNull;

    ViewContext(final IPageController pageController, final IHistoryController historyController,
            final ICIFEXServiceAsync cifexService, final Model model)
    {
        this.pageController = pageController;
        this.historyController = historyController;
        this.cifexService = cifexService;
        this.model = model;
    }

    public final ICIFEXServiceAsync getCifexService()
    {
        return cifexService;
    }

    public final Model getModel()
    {
        return model;
    }

    public final IPageController getPageController()
    {
        return pageController;
    }

    public final IHistoryController getHistoryController()
    {
        return historyController;
    }

    public void setKeepAliveTimerOrNull(Timer keepAliveTimerOrNull)
    {
        this.keepAliveTimerOrNull = keepAliveTimerOrNull;
    }
    
    public void logoutAndShowLoginPage()
    {
        getCifexService().logout(AsyncCallbackAdapter.EMPTY_ASYNC_CALLBACK);
        getModel().getUrlParams().clear();
        cancelKeepAliveTimer();
        getPageController().showLoginPage();
    }

    private void cancelKeepAliveTimer()
    {
        if (keepAliveTimerOrNull != null)
        {
            keepAliveTimerOrNull.cancel();
        }
    }

}
