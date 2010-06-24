/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;

/**
 * Allows to create a {@link Widget} ({@link #asWidget()}) containing channel view and allows to
 * manually update visible channel ({@link #update(String)}) or create a listener that can be added
 * to channel selector.
 * 
 * @author Izabela Adamczyk
 */
public class ChannelWidgetWithListener
{
    final LayoutContainer container = new LayoutContainer();

    final IChanneledViewerFactory viewerFactory;

    public ChannelWidgetWithListener(final IChanneledViewerFactory viewerFactory)
    {
        this.viewerFactory = viewerFactory;
    }

    public void update(String channelName)
    {
        if (channelName != null)
        {
            GuiUtils.replaceLastItem(container, viewerFactory.create(channelName));
        }
    }

    public SelectionChangedListener<SimpleComboValue<String>> asSelectionChangedListener()
    {
        return new SelectionChangedListener<SimpleComboValue<String>>()
            {

                @Override
                public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se)
                {
                    update(se.getSelectedItem().getValue());
                }

            };
    }

    public Widget asWidget()
    {
        return container;
    }
}