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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;

/**
 * A {@link ITabItem} implementation to adapt a {@link ContentPanel}.
 * 
 * @author Christian Ribeaud
 */
public final class ContentPanelAdapter implements ITabItem
{
    private final ContentPanel contentPanel;

    private final boolean isCloseConfirmationNeeded;

    public ContentPanelAdapter(final ContentPanel contentPanel, boolean isCloseConfirmationNeeded)
    {
        // Note that if not set, is then automatically generated. So this is why we test for
        // 'ID_PREFIX'. We want the user to set an unique id.
        assert contentPanel.getId().startsWith(GenericConstants.ID_PREFIX) : "Unspecified component id.";
        this.contentPanel = contentPanel;
        this.isCloseConfirmationNeeded = isCloseConfirmationNeeded;
    }

    //
    // ITabItem
    //

    public final Component getComponent()
    {
        return contentPanel;
    }

    public final String getTabTitle()
    {
        final Header header = contentPanel.getHeader();
        return header != null ? header.getText() : contentPanel.getId();
    }

    public final String getId()
    {
        return contentPanel.getId();
    }

    public final void initialize()
    {
        // Does nothing.
    }

    public boolean isCloseConfirmationNeeded()
    {
        return isCloseConfirmationNeeded;
    }

    public void onActivate()
    {
        // TODO 2009-03-26, Tomasz Pylak: add refresh on db modifications support
    }

    public void onClose()
    {
        // do nothing
    }
}
