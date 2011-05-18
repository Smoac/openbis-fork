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
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * An UI panel for selecting image channels.
 * 
 * @author Kaloyan Enimanev
 */
public class ChannelChooserPanel extends LayoutContainer
{

    /**
     * Can be used from external classes wishing to be notified when the channel selection changes.
     */
    public static interface ChannelSelectionListener
    {
        void selectionChanged(List<String> channels);
    }

    private IDefaultChannelState defaultChannelState;

    private CheckBoxGroup channelsCheckBoxGroup;

    private SimpleComboBox<String> channelsComboBox;

    private List<ChannelSelectionListener> channelSelectionListeners =
            new ArrayList<ChannelSelectionListener>();

    private final Listener<BaseEvent> selectionChangeListener = new Listener<BaseEvent>()
        {

            public void handleEvent(BaseEvent be)
            {
                selectionChanged();
            }

        };

    public ChannelChooserPanel(IDefaultChannelState defChannelState)
    {
        this(defChannelState, Collections.<String> emptyList(), Collections.<String> emptyList());
    }

    public ChannelChooserPanel(IDefaultChannelState defChannelState, List<String> names,
            List<String> selectedChannelsOrNull)
    {
        this.defaultChannelState = defChannelState;

        setAutoHeight(true);
        setAutoWidth(true);

        channelsComboBox = createChannelsComboBox();
        add(channelsComboBox);

        channelsCheckBoxGroup = createCheckBoxGroup();
        add(channelsCheckBoxGroup);

        addChannels(names);
        updateChannelSelection(selectedChannelsOrNull);
    }

    private SimpleComboBox<String> createChannelsComboBox()
    {
        SimpleComboBox<String> comboBox = new SimpleComboBox<String>();

        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setAllowBlank(false);
        comboBox.setEditable(false);
        comboBox.setEmptyText("Choose...");
        comboBox.addListener(Events.SelectionChange, selectionChangeListener);

        return comboBox;
    }
    

    private CheckBoxGroup createCheckBoxGroup()
    {
        CheckBoxGroup group = new CheckBoxGroup();
        return group;
    }    

    /**
     * adds a {@link ChannelSelectionListener} that will be receiving notifications when the
     * selection changes.
     */
    public void addSelectionChangedListener(ChannelSelectionListener selectionListener)
    {
        channelSelectionListeners.add(selectionListener);
    }

    /**
     * a quite specific method, currently only needed by the well-search grid.
     */
    public void addCodes(List<String> codes)
    {
        addChannels(codes);
        updateChannelSelection(null);
    }

    /**
     * @return the current channel selection. If all channels are selected, returns a list
     *         containing a single element - {@link ScreeningConstants#MERGED_CHANNELS}.
     */
    public List<String> getSelectedValues()
    {
        String comboBoxValue = channelsComboBox.getSimpleValue();
        if (comboBoxValue == null)
        {
            return Collections.<String> emptyList();
        }
        if (ScreeningConstants.MERGED_CHANNELS.equals(comboBoxValue))
        {
            // multiple channel selection
            return getMergedChannelSelection();
        } else
        {
            // single channel selection
            return Collections.singletonList(comboBoxValue);
        }
    }

    private List<String> getMergedChannelSelection()
    {
        boolean allSelected = true;
        List<String> channels = new ArrayList<String>();
        for (CheckBox cb : getAllCheckBoxes())
        {
            if (cb.getValue() == true)
            {
                channels.add(cb.getBoxLabel());
            } else
            {
                allSelected = false;
            }
        }

        if (allSelected)
        {
            // all channels selected
            // do not list them one-by-one. use the more general "MERGED_CHANNELS" term instead
            return Collections.singletonList(ScreeningConstants.MERGED_CHANNELS);
        }
        return channels;
    }

    private void addChannels(List<String> codes)
    {
        addCodeToComboBox(ScreeningConstants.MERGED_CHANNELS);
        if (codes == null || codes.isEmpty())
        {
            return;
        }

        List<CheckBox> newCheckBoxes = new ArrayList<CheckBox>();
        for (String code : codes)
        {
            boolean codeAdded = addCodeToComboBox(code);
            if (codeAdded)
            {
                // also add a checkBockbox for the channel
                CheckBox checkBox = new CheckBox();
                checkBox.setBoxLabel(code);
                checkBox.addListener(Events.Change, selectionChangeListener);
                newCheckBoxes.add(checkBox);
            }
        }

        updateCheckBoxGroup(newCheckBoxes);
    }

    private boolean addCodeToComboBox(String code)
    {
        if (channelsComboBox.findModel(code) == null)
        {
            channelsComboBox.add(code);
            return true;
        }
        return false;
    }

    private void updateChannelSelection(List<String> selectedChannels)
    {
        List<String> channels = selectedChannels;
        if (channels == null || channels.size() == 0)
        {
            if (defaultChannelState != null)
            {
                channels = defaultChannelState.tryGetDefaultChannels();
            }
        }

        String comboBoxValue = ScreeningConstants.MERGED_CHANNELS;
        if (channels != null && channels.size() == 1)
        {
            comboBoxValue = channels.get(0);
        }

        channelsComboBox.setSimpleValue(comboBoxValue);

        initializeCheckBoxValues(channels);
    }

    private void initializeCheckBoxValues(List<String> selectedChannels)
    {
        boolean selectAllChannels = (selectedChannels == null) || (selectedChannels.size() < 2);

        for (CheckBox cb : getAllCheckBoxes())
        {
            @SuppressWarnings("null")
            boolean checked = selectAllChannels || selectedChannels.contains(cb.getBoxLabel());
            cb.setValue(checked);
        }

    }

    private void selectionChanged()
    {
        List<String> selection = getSelectedValues();
        defaultChannelState.setDefaultChannels(selection);

        String selectedComboValue = channelsComboBox.getSimpleValue();
        boolean showCheckBoxGroup = ScreeningConstants.MERGED_CHANNELS.equals(selectedComboValue);
        channelsCheckBoxGroup.setVisible(showCheckBoxGroup);

        ensureAtLeastOneCheckboxChecked();
        notifySelectionListeners(selection);
    }

    private void notifySelectionListeners(List<String> selection)
    {
        for (ChannelSelectionListener listener : channelSelectionListeners)
        {
            listener.selectionChanged(selection);
        }
    }

    private void updateCheckBoxGroup(List<CheckBox> newCheckBoxes)
    {
        if (newCheckBoxes.isEmpty())
        {
            return;
        }

        boolean recreateCheckBoxGroup = channelsCheckBoxGroup.isRendered();

        if (recreateCheckBoxGroup)
        {
            // we must create a new CheckBoxGroup because the old one
            // wouldn't accept new checkboxes after rendering
            newCheckBoxes.addAll(0, getAllCheckBoxes());
            remove(channelsCheckBoxGroup);
            channelsCheckBoxGroup = createCheckBoxGroup();
        }

        for (CheckBox cb : newCheckBoxes)
        {
            channelsCheckBoxGroup.add(cb);
        }

        if (recreateCheckBoxGroup)
        {
            add(channelsCheckBoxGroup);
            layout(true);
        }

    }

    private void ensureAtLeastOneCheckboxChecked()
    {
        List<CheckBox> checkboxes = getAllCheckBoxes();
        int selected = 0;

        for (CheckBox cb : checkboxes)
        {
            if (cb.getValue())
            {
                selected++;
            }
        }

        if (selected > 1)
        {
            for (CheckBox cb : checkboxes)
            {
                cb.setEnabled(true);
            }
        } else
        {
            for (CheckBox cb : checkboxes)
            {
                if (cb.getValue())
                {
                    cb.setEnabled(false);
                }
            }
        }

    }

    private List<CheckBox> getAllCheckBoxes()
    {
        List<CheckBox> result = new ArrayList<CheckBox>();
        for (Field<?> field : channelsCheckBoxGroup.getAll())
        {
            if (field instanceof CheckBox)
            {
                result.add((CheckBox) field);
            }
        }
        return result;
    }
}