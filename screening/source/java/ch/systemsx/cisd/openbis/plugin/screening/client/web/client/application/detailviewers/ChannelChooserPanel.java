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

    // TODO KE: 2011-02-16 refactor this to use CheckBoxGroupWithModel
    private CheckBoxGroup channelsCheckBoxGroup;

    private SimpleComboBox<String> channelsComboBox;

    private List<ChannelSelectionListener> channelSelectionListeners =
            new ArrayList<ChannelSelectionListener>();

    /** when set to to true will generate a checkbox per channel */
    private boolean createCheckBoxes;

    private final Listener<BaseEvent> selectionChangeListener = new Listener<BaseEvent>()
        {

        public void handleEvent(BaseEvent be)
            {
                selectionChanged();
            }

        };

    public ChannelChooserPanel(IDefaultChannelState defChannelState)
    {
        this(defChannelState, Collections.<String> emptyList(), Collections.<String> emptyList(),
                true);
    }

    public ChannelChooserPanel(IDefaultChannelState defChannelState, List<String> names,
            List<String> selectedChannelsOrNull, boolean createCheckBoxes)
    {
        this.defaultChannelState = defChannelState;
        this.createCheckBoxes = createCheckBoxes;

        setAutoHeight(true);
        setAutoWidth(true);
        // TODO KE:2011-02-16 set layout as Tomek wants it
        // setLayout(new HBoxLayout());

        channelsComboBox = createChannelsComboBox();
        add(channelsComboBox);

        channelsCheckBoxGroup = new CheckBoxGroup();
        add(channelsCheckBoxGroup);

        initializeAvailableChannels(names);
        initializeChannelSelection(selectedChannelsOrNull);
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
    public void initializeCodesForWellSearchGrid(List<String> codes)
    {
        initializeAvailableChannels(codes);
        initializeChannelSelection(null);
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
    
    private void initializeAvailableChannels(List<String> codes)
    {
        addCodeToComboBox(ScreeningConstants.MERGED_CHANNELS);
        if (codes == null || codes.isEmpty())
        {
            return;
        }

        for (String code : codes)
        {
            boolean codeAdded = addCodeToComboBox(code);
            if (codeAdded && createCheckBoxes)
            {
                // also add a checkBockbox for the channel
                CheckBox checkBox = new CheckBox();
                checkBox.setBoxLabel(code);
                checkBox.addListener(Events.Change, selectionChangeListener);
                channelsCheckBoxGroup.add(checkBox);
            }
        }
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
    
    private void initializeChannelSelection(List<String> selectedChannels)
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

        ensureAtLeastOneCheckboxChecked();
        channelsCheckBoxGroup.setVisible(showCheckBoxGroup);

        notifySelectionListeners(selection);
    }

    private void notifySelectionListeners(List<String> selection)
    {
        for (ChannelSelectionListener listener : channelSelectionListeners)
        {
            listener.selectionChanged(selection);
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