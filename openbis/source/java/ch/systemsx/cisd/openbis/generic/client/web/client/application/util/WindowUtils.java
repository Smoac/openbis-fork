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

import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;

/**
 * Window utilities.
 * 
 * @author Izabela Adamczyk
 */
public class WindowUtils
{

    /**
     * Opens a new window with given parameters if pop-up blocker has not been detected and displays
     * an alert message otherwise.
     */
    static public void openWindow(String url)
    {
        // triggered only for test purposes
        DispatcherHelper.dispatchOpenUrlEvent(url);
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
