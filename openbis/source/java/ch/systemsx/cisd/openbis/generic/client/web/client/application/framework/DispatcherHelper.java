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

import com.extjs.gxt.ui.client.mvc.AppEvent;
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
     * Creates and dispatches an event of type {@link AppEvents#OPEN_URL_EVENT}.
     */
    public final static void dispatchOpenUrlEvent(String url)
    {
        AppEvent<String> event = createEvent(AppEvents.OPEN_URL_EVENT, url);
        Dispatcher.get().dispatch(event);
    }

    /**
     * Creates and dispatches an event of type {@link AppEvents#NAVI_EVENT}.
     */
    public final static void dispatchNaviEvent(final ITabItemFactory tabItemFactory)
    {
        AppEvent<ITabItemFactory> event = createEvent(AppEvents.NAVI_EVENT, tabItemFactory);
        Dispatcher.get().dispatch(event);
    }

    private final static <T> AppEvent<T> createEvent(int eventType, T data)
    {
        final AppEvent<T> event = new AppEvent<T>(eventType);
        event.data = data;
        return event;
    }
}
