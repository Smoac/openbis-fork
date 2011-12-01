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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents.OpenUrlEvent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;

/**
 * Window utilities.
 * 
 * @author Izabela Adamczyk
 */
public class WindowUtils
{

    /**
     * Requests to open a new window with given URL.
     */
    static public void openWindow(String url)
    {
        DispatcherHelper.dispatchOpenUrlEvent(url);
    }

    /**
     * Requests to redirect the browser to a given URL.
     */
    static public void redirect(String url, String target)
    {
        DispatcherHelper.dispatchRedirectUrlEvent(url, target);
    }

    /** Creates a controller which handles requests to open the URL in a new browser window. */
    public static Controller createOpenUrlController()
    {
        return new BrowserUrlController();
    }

    private static class BrowserUrlController extends Controller
    {
        public BrowserUrlController()
        {
            registerEventTypes(OpenUrlEvent.OPEN_URL_EVENT);
        }

        @Override
        public void handleEvent(AppEvent event)
        {
            if (event.getType() == OpenUrlEvent.OPEN_URL_EVENT)
            {
                OpenUrlEvent openUrlEvent = (OpenUrlEvent) event;
                doOpenWindow(openUrlEvent.getURL(), openUrlEvent.getTargetWindow());
            }
        }
    }

    /**
     * Opens a new window with given parameters if pop-up blocker has not been detected and displays
     * an alert message otherwise.
     */
    private static void doOpenWindow(String url, String target)
    {
        boolean opened = openWindow(url, target, "scrollbars=yes,resizable=yes");
        if (opened == false)
        {
            MessageBox.alert("", GenericConstants.POPUP_BLOCKER_DETECTED, null);
            return;
        }
    }

    /**
     * @return true if the window has been opened, false otherwise (it can be a case e.g. when the
     *         pop-up detector is switched on)
     */
    private static native boolean openWindow(String url, String target, String features)
    /*-{      
       var pop = $wnd.open(url, target, features);
       return (pop != null);
    }-*/;
}
