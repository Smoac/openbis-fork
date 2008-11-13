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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;

/**
 * A {@link ITabItem} implementation to adapt a {@link ContentPanel}.
 * 
 * @author Christian Ribeaud
 */
// TODO 2008-11-12, Christian Ribeaud: Remove this when each component is a ITabItem.
public final class ContentPanelAdapter implements ITabItem
{
    private final ContentPanel contentPanel;

    private final Listener<TabPanelEvent> tabPanelEventListener;

    public ContentPanelAdapter(final ContentPanel contentPanel)
    {
        this(contentPanel, null);
    }

    public ContentPanelAdapter(final ContentPanel contentPanel,
            final Listener<TabPanelEvent> tabPanelEventListener)
    {
        this.contentPanel = contentPanel;
        this.tabPanelEventListener = tabPanelEventListener;
    }

    //
    // ITabItem
    //

    public final Component getComponent()
    {
        return contentPanel;
    }

    public final String getTitle()
    {
        final Header header = contentPanel.getHeader();
        return contentPanel.getHeader() != null ? header.getText() : contentPanel.getId();
    }

    public final String getId()
    {
        return contentPanel.getId();
    }

    public final void initialize()
    {
        // Does nothing.
    }

    public final Listener<TabPanelEvent> getTabPanelEventListener()
    {
        return tabPanelEventListener;
    }
}
