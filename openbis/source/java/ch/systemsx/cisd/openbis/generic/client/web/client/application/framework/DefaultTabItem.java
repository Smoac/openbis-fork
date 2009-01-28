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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableComponent;

/**
 * A default {@link ITabItem} implementation.
 * 
 * @author Christian Ribeaud
 */
public class DefaultTabItem implements ITabItem
{
    private final String title;

    private final Component component;

    private final Listener<TabPanelEvent> tabPanelEventListener;

    private final boolean isCloseConfirmationNeeded;

    public DefaultTabItem(final String title, final Component component,
            boolean isCloseConfirmationNeeded)
    {
        this(title, component, isCloseConfirmationNeeded, null);
    }

    public static DefaultTabItem create(final String title, final DisposableComponent component,
            boolean isCloseConfirmationNeeded)
    {
        Listener<TabPanelEvent> eventListener = createCloseEventListener(component);
        return new DefaultTabItem(title, component.getComponent(), isCloseConfirmationNeeded,
                eventListener);
    }

    private static Listener<TabPanelEvent> createCloseEventListener(
            final DisposableComponent component)
    {
        return new Listener<TabPanelEvent>()
            {
                public final void handleEvent(final TabPanelEvent be)
                {
                    if (be.type == Events.Close)
                    {
                        component.dispose();
                    }
                }
            };
    }

    private DefaultTabItem(final String title, final Component component,
            boolean isCloseConfirmationNeeded, final Listener<TabPanelEvent> tabPanelEventListener)
    {
        assert title != null : "Unspecified title.";
        assert component != null : "Unspecified component.";
        this.title = title;
        this.component = component;
        this.isCloseConfirmationNeeded = isCloseConfirmationNeeded;
        this.tabPanelEventListener = tabPanelEventListener;
    }

    //
    // ITabItem
    //

    public final Component getComponent()
    {
        return component;
    }

    public final String getTabTitle()
    {
        return title;
    }

    public final Listener<TabPanelEvent> tryGetEventListener()
    {
        return tabPanelEventListener;
    }

    public boolean isCloseConfirmationNeeded()
    {
        return isCloseConfirmationNeeded;
    }
}
