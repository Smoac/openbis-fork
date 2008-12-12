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

import java.util.Iterator;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;

/**
 * A helper for using {@link Dispatcher}.
 * 
 * @author Christian Ribeaud
 */
public final class DispatcherHelper
{
    private DispatcherHelper()
    {
        // Can not be instantiated.
    }

    /**
     * Create an event of type {@link AppEvents#NAVI_EVENT}.
     */
    public final static AppEvent<ITabItem> createNaviEvent(final ITabItem tabItem)
    {
        final AppEvent<ITabItem> event = new AppEvent<ITabItem>(AppEvents.NAVI_EVENT);
        event.data = tabItem;
        return event;
    }

    /**
     * Removes all the {@link Controller}s that have been added to the {@link Dispatcher}.
     */
    public final static void clearControllers()
    {
        final Dispatcher dispatcher = Dispatcher.get();
        final Iterator<Controller> iterator = dispatcher.getControllers().iterator();
        while (iterator.hasNext())
        {
            iterator.next();
            iterator.remove();
        }
    }
}
