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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.google.gwt.user.client.Event;

/**
 * Additional event codes.
 * <p>
 * Make sure that values assigned to constants declared here are unique and different than events
 * declared in {@link Event} and {@link Events}.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public class AppEvents
{

    /**
     * Useful links: <a href=
     * "http://google-web-toolkit.googlecode.com/svn/javadoc/1.5/index.html?overview-summary.html"
     * >GWT event constants</a>, <a href="http://extjs.com/deploy/gxtdocs/constant-values.html">GXT
     * event constants</a>
     */
    private final static int STARTING_VALUE = 10;

    // opens the main application window
    public final static EventType INIT = new EventType(STARTING_VALUE + 20);

    // opens the starting page with login window
    public final static EventType LOGIN = new EventType(STARTING_VALUE + 30);

    // opens a new tab
    public static final EventType NAVI_EVENT = new EventType(STARTING_VALUE + 40);

    // opens a new browser window with a specified URL
    public static final EventType OPEN_URL_EVENT = new EventType(STARTING_VALUE + 50);

    public static final EventType CloseViewer = new EventType(STARTING_VALUE + 70);
}
