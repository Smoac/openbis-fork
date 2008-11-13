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

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;

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

    public DefaultTabItem(final String title, final Component component)
    {
        this(title, component, null);
    }

    public DefaultTabItem(final String title, final Component component,
            final Listener<TabPanelEvent> tabPanelEventListener)
    {
        assert title != null : "Unspecified title.";
        assert component != null : "Unspecified component.";
        // Note that if not set, is then automatically generated. So this is why we test for
        // 'ID_PREFIX'. We want the user to set an unique id.
        assert component.getId().startsWith(GenericConstants.ID_PREFIX) : "Unspecified component id.";
        this.title = title;
        this.component = component;
        this.tabPanelEventListener = tabPanelEventListener;
    }

    //
    // ITabItem
    //

    public final Component getComponent()
    {
        return component;
    }

    public final String getTitle()
    {
        return title;
    }

    public final String getId()
    {
        return getComponent().getId();
    }

    public void initialize()
    {
        // Does nothing.
    }

    public final Listener<TabPanelEvent> getTabPanelEventListener()
    {
        return tabPanelEventListener;
    }
}
