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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
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

    /** Creates a controller which handles requests to open the URL in a new browser window. */
    public static Controller createOpenUrlController()
    {
        return new OpenUrlController();
    }

    private static class OpenUrlController extends Controller
    {
        public OpenUrlController()
        {
            registerEventTypes(AppEvents.OPEN_URL_EVENT);
        }

        @Override
        public void handleEvent(AppEvent event)
        {
            if (event.getType() == AppEvents.OPEN_URL_EVENT)
            {
                String openedUrl = (String) event.getData();
                doOpenWindow(openedUrl);
            }
        }
    }

    /**
     * Opens a new window with given parameters if pop-up blocker has not been detected and displays
     * an alert message otherwise.
     */
    private static void doOpenWindow(String url)
    {
        boolean opened = openWindow(url, "", null);
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
    private static native boolean openWindow(String url, String name, String features)
    /*-{      
       var pop = $wnd.open(url, name, features);
       return (pop != null);
    }-*/;
}
