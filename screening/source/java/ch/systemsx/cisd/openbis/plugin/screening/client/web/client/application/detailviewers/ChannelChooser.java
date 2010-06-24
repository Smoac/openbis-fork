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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Handles displaying images in different channels.
 * 
 * @author Tomasz Pylak
 */
class ChannelChooser
{
    public static interface IChanneledViewerFactory
    {
        Widget create(String value);
    }

    public static LayoutContainer createViewerWithChannelChooser(
            final IChanneledViewerFactory viewerFactory, final DefaultChannelState channelState,
            List<String> channelsNames)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        String initialChannel = channelState.getDefaultChannel(channelsNames);
        if (channelsNames.size() > 1)
        {
            ComboBox<SimpleComboValue<String>> channelChooser =
                    new ChannelComboBox(channelsNames, initialChannel);
            channelChooser
                    .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
                        {
                            @Override
                            public void selectionChanged(
                                    SelectionChangedEvent<SimpleComboValue<String>> se)
                            {
                                String value = se.getSelectedItem().getValue();
                                Widget viewerWidget = viewerFactory.create(value);
                                channelState.setDefaultChannel(value);
                                GuiUtils.replaceLastItem(container, viewerWidget);
                            }
                        });
            RowData channelLayoutData = new RowData();
            channelLayoutData.setMargins(new Margins(10, 0, 0, 0));
            container.add(GuiUtils.withLabel(channelChooser, "Channel:"), channelLayoutData);
        }
        container.add(viewerFactory.create(initialChannel));
        container.setScrollMode(Scroll.AUTO);
        return container;
    }

    public static List<String> createChannelsDescriptions(List<String> realChannelsNames)
    {
        assert realChannelsNames.size() > 0 : "there has to be at least one channel";

        final List<String> allChannelsNames = new ArrayList<String>();
        allChannelsNames.add(ScreeningConstants.MERGED_CHANNELS);
        allChannelsNames.addAll(realChannelsNames);
        return allChannelsNames;
    }

    /**
     * Allows to get and set the channel which is chosen by default when well images are shown.
     * Channel 0 consists of all other channels merged.
     */
    public static class DefaultChannelState
    {
        private String defaultChannel = null;

        public DefaultChannelState()
        {

        }

        public String getDefaultChannel(List<String> channelsNames)
        {
            if (defaultChannel == null)
            {
                defaultChannel = channelsNames.get(0);
            }
            return defaultChannel;
        }

        public void setDefaultChannel(String value)
        {
            this.defaultChannel = value;
        }
    }
}
