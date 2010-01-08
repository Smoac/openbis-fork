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

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;

/**
 * @author Tomasz Pylak
 */
class ChannelChooser
{
    public static interface IChanneledViewerFactory
    {
        Widget create(int channel);
    }

    public static LayoutContainer createViewerWithChannelChooser(
            final IChanneledViewerFactory viewerFactory, final DefaultChannelState channelState,
            int channelsNum)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());

        int initialChannel = channelState.getDefaultChannel();
        if (channelsNum > 1)
        {
            final List<String> channelNames = createChannelsDescriptions(channelsNum);
            ComboBox<SimpleComboValue<String>> channelChooser =
                    createChannelChooser(channelNames, initialChannel);
            channelChooser
                    .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
                        {
                            @Override
                            public void selectionChanged(
                                    SelectionChangedEvent<SimpleComboValue<String>> se)
                            {
                                String value = se.getSelectedItem().getValue();
                                int channel = channelNames.indexOf(value) + 1;
                                Widget viewerWidget = viewerFactory.create(channel);
                                channelState.setDefaultChannel(channel);
                                GuiUtils.replaceLastItem(container, viewerWidget);
                            }
                        });
            RowData channelLayoutData = new RowData();
            channelLayoutData.setMargins(new Margins(10, 0, 0, 0));
            container.add(GuiUtils.withLabel(channelChooser, "Channel:"), channelLayoutData);
        }
        container.add(viewerFactory.create(initialChannel));
        return container;
    }

    private static ComboBox<SimpleComboValue<String>> createChannelChooser(
            List<String> channelNames, int initialChannel)
    {
        SimpleComboBox<String> combo = new SimpleComboBox<String>();
        combo.setTriggerAction(TriggerAction.ALL);
        combo.add(channelNames);
        combo.setAllowBlank(false);
        combo.setEditable(false);
        combo.setSimpleValue(channelNames.get(initialChannel - 1));
        return combo;
    }

    private static List<String> createChannelsDescriptions(int channelsNum)
    {
        assert channelsNum > 0 : "there has to be at least one channel";

        final List<String> channelNames = new ArrayList<String>();
        for (int i = 1; i <= channelsNum; i++)
        {
            channelNames.add(createChannelName(i));
        }
        return channelNames;
    }

    private static String createChannelName(int channel)
    {
        return "Channel " + channel;
    }

    /** Allows to get and set the channel which is chosen by default when well images are shown. */
    public static class DefaultChannelState
    {
        private int defaultChannel = 1;

        public DefaultChannelState()
        {

        }

        public int getDefaultChannel()
        {
            return defaultChannel;
        }

        public void setDefaultChannel(int defaultChannel)
        {
            this.defaultChannel = defaultChannel;
        }
    }
}
