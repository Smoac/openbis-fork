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

import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;

/**
 * Window utilities.
 * 
 * @author Izabela Adamczyk
 */
public class WindowUtils
{

    // TODO 2009-01-05, IA: perhaps this method should be called when the application starts
    /**
     * Shows alert message to the user if pop-up blocker has been detected.
     */
    public static void detectPopUps()
    {
        checkPopUps(GenericConstants.POPUP_BLOCKER_DETECTED);
    }

    /**
     * Opens a new window with given parameters if pop-up blocker has not been detected and displays
     * an alert message otherwise.
     */
    static public void openWindow(String parameters)
    {
        detectPopUps();
        Window.open(parameters, "", null);
    }

    private static native void checkPopUps(String message)
    /*-{      
       var pop = $wnd.open(" "," ", "width=10,height=10");
       if (pop) 
           pop.close(); 
       else 
           alert(message);
    }-*/;

}
