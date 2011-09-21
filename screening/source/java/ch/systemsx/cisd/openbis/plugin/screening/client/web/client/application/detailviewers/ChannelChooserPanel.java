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

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.MERGED_CHANNELS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageTransformationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * An UI panel for selecting image channels.
 * 
 * @author Kaloyan Enimanev
 */
public class ChannelChooserPanel extends LayoutContainer
{
    private static class ComboBoxGroup extends MultiField<Widget>
    {
        private ComboBoxGroup()
        {
            super();

            this.setSpacing(5);
        }
    }

    /**
     * Can be used from external classes wishing to be notified when the channel selection changes.
     */
    public static interface ChannelSelectionListener
    {
        void selectionChanged(List<String> channels, String imageTransformationCodeOrNull);
    }

    public static final String DEFAULT_TRANSFORMATION_CODE = "$DEFAULT$";

    private static final LabeledItem<ImageTransformationInfo> DEFAULT_TRANSFORMATION =
            convertToLabeledItem(new ImageTransformationInfo(
                    DEFAULT_TRANSFORMATION_CODE,
                    "Optimal (image)",
                    "Grayscale images with color depth higher then 8 bits are transformed in the optimal way for a single image. Otherwise no filter is applied.",
                    "", false));

    private final IMessageProvider messageProvider;

    private IDefaultChannelState defaultChannelState;

    private CheckBoxGroup channelsCheckBoxGroup;

    private SimpleModelComboBox<String> channelsComboBox;

    private SimpleModelComboBox<ImageTransformationInfo> transformationsComboBox;

    private LabelField adjustLabel = new LabelField("Filter:");

    private Map<String, Set<ImageTransformationInfo>> transformationsForChannels =
            new HashMap<String, Set<ImageTransformationInfo>>();

    private List<ChannelSelectionListener> channelSelectionListeners =
            new ArrayList<ChannelSelectionListener>();

    private final Listener<BaseEvent> selectionChangeListener = new Listener<BaseEvent>()
        {
            public void handleEvent(BaseEvent be)
            {
                selectionChanged();
            }

        };

    private final Listener<BaseEvent> transformationSelection = new Listener<BaseEvent>()
        {
            public void handleEvent(BaseEvent be)
            {
                defaultChannelState.setDefaultTransformation(getSelectedValues().get(0),
                        transformationsComboBox.getSimpleValue().getItem().getCode());
                notifySelectionListeners(getSelectedValues(), tryGetSelectedTransformationCode());
            }

        };

    public ChannelChooserPanel(IMessageProvider messageProvider,
            IDefaultChannelState defChannelState)
    {
        this(messageProvider, defChannelState, Collections.<String> emptyList(), null);
    }

    public ChannelChooserPanel(IMessageProvider messageProvider,
            IDefaultChannelState defChannelState, List<String> selectedChannelsOrNull,
            ImageDatasetParameters imageDatasetParameters)
    {
        this.messageProvider = messageProvider;
        this.defaultChannelState = defChannelState;

        setAutoHeight(true);
        setAutoWidth(true);

        channelsComboBox = createChannelsComboBox();

        transformationsComboBox =
                new SimpleModelComboBox<ImageTransformationInfo>(this.messageProvider,
                        new ArrayList<LabeledItem<ImageTransformationInfo>>(), null);
        transformationsComboBox.addListener(Events.SelectionChange, transformationSelection);

        ComboBoxGroup group = new ComboBoxGroup();
        group.add(channelsComboBox);
        group.add(adjustLabel);
        group.add(transformationsComboBox);

        add(group);

        channelsCheckBoxGroup = createCheckBoxGroup();
        add(channelsCheckBoxGroup);

        addChannels(imageDatasetParameters);
        updateChannelSelection(selectedChannelsOrNull);
    }

    private SimpleModelComboBox<String> createChannelsComboBox()
    {
        SimpleModelComboBox<String> comboBox =
                new SimpleModelComboBox<String>(this.messageProvider,
                        new ArrayList<LabeledItem<String>>(), null);
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
    public void addChannels(ImageDatasetParameters imageParameters)
    {
        addChannelsForParameters(imageParameters);
        updateChannelSelection(null);
    }

    /**
     * @return the current channel selection. If all channels are selected, returns a list
     *         containing a single element - {@link ScreeningConstants#MERGED_CHANNELS}.
     */
    public List<String> getSelectedValues()
    {
        String comboBoxValue = channelsComboBox.getChosenItem();
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
                channels.add(cb.getValueAttribute());
            } else
            {
                allSelected = false;
            }
        }

        if (allSelected)
        {
            // all channels selected
            // do not list them one-by-one. use the more general "MERGED_CHANNELS" term instead
            return Collections.singletonList(MERGED_CHANNELS);
        }
        return channels;
    }

    private void addChannelsForParameters(ImageDatasetParameters imageParameters)
    {
        addChannelToComboBox(new LabeledItem<String>(MERGED_CHANNELS, MERGED_CHANNELS));

        List<LabeledItem<String>> channels = extractLabeledChannels(imageParameters);
        if (channels == null || channels.isEmpty())
        {
            return;
        }

        List<CheckBox> newCheckBoxes = new ArrayList<CheckBox>();
        for (LabeledItem<String> channel : channels)
        {
            boolean codeAdded = addChannelToComboBox(channel);
            String code = channel.getItem();
            Set<ImageTransformationInfo> transformationsForChannel =
                    transformationsForChannels.get(code);
            if (transformationsForChannel == null)
            {
                transformationsForChannel = new LinkedHashSet<ImageTransformationInfo>();
                transformationsForChannels.put(code, transformationsForChannel);
            }
            transformationsForChannel.addAll(imageParameters
                    .getAvailableImageTransformationsFor(code));

            if (codeAdded)
            {
                // also add a checkBockbox for the channel
                CheckBox checkBox = new CheckBox();
                checkBox.setBoxLabel(channel.getLabel());
                checkBox.setValueAttribute(channel.getItem());
                checkBox.addListener(Events.Change, selectionChangeListener);
                newCheckBoxes.add(checkBox);
            }
        }

        updateCheckBoxGroup(newCheckBoxes);
    }

    private List<LabeledItem<String>> extractLabeledChannels(ImageDatasetParameters imageParameters)
    {
        if (imageParameters == null || imageParameters.getChannelsNumber() == 0)
        {
            return Collections.emptyList();
        }
        List<String> codes = imageParameters.getChannelsCodes();
        List<String> labels = imageParameters.getChannelsLabels();
        List<LabeledItem<String>> result = new ArrayList<LabeledItem<String>>();
        for (int i = 0; i < codes.size(); i++)
        {
            String code = codes.get(i);
            String label = code;
            if (i < labels.size() && false == StringUtils.isBlank(labels.get(i)))
            {
                label = labels.get(i);
            }
            result.add(new LabeledItem<String>(code, label));
        }
        return result;
    }

    private boolean addChannelToComboBox(LabeledItem<String> channel)
    {
        if (channelsComboBox.findModelForVal(channel.getItem()) == null)
        {
            channelsComboBox.add(channel);
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

        String codeToSelect = ScreeningConstants.MERGED_CHANNELS;
        if (channels != null && channels.size() == 1)
        {
            codeToSelect = channels.get(0);
        }

        LabeledItem<String> itemToSelect = channelsComboBox.findModelForVal(codeToSelect);
        channelsComboBox.setSelection(itemToSelect);
        initializeCheckBoxValues(channels);
        updateTransformationComboBox();
    }

    private void initializeCheckBoxValues(List<String> selectedChannels)
    {
        boolean selectAllChannels = (selectedChannels == null) || (selectedChannels.size() < 2);

        for (CheckBox cb : getAllCheckBoxes())
        {
            @SuppressWarnings("null")
            boolean checked =
                    selectAllChannels || selectedChannels.contains(cb.getValueAttribute());
            cb.setValue(checked);
        }

    }

    private void selectionChanged()
    {
        List<String> selection = getSelectedValues();
        defaultChannelState.setDefaultChannels(selection);

        String selectedComboValue = channelsComboBox.getChosenItem();
        boolean showCheckBoxGroup = ScreeningConstants.MERGED_CHANNELS.equals(selectedComboValue);
        channelsCheckBoxGroup.setVisible(showCheckBoxGroup);

        ensureAtLeastOneCheckboxChecked();
        updateTransformationComboBox();

        notifySelectionListeners(selection, tryGetSelectedTransformationCode());
    }

    public String tryGetSelectedTransformationCode()
    {
        if (transformationsComboBox.isVisible())
        {
            String code =
                    transformationsComboBox.getSelection().get(0).getValue().getItem().getCode();

            if (DEFAULT_TRANSFORMATION_CODE.equals(code))
            {
                return null;
            } else
            {
                return code;
            }
        }

        return null;
    }

    private void notifySelectionListeners(List<String> selection,
            String imageTransformationCodeOrNull)
    {
        for (ChannelSelectionListener listener : channelSelectionListeners)
        {
            listener.selectionChanged(selection, imageTransformationCodeOrNull);
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

    private void updateTransformationComboBox()
    {
        List<String> selectedValues = getSelectedValues();

        transformationsComboBox.removeAll();
        transformationsComboBox.clearState();

        List<LabeledItem<ImageTransformationInfo>> model =
                new ArrayList<LabeledItem<ImageTransformationInfo>>();
        Set<ImageTransformationInfo> infos = null;
        if (selectedValues.size() == 1
                && (infos = transformationsForChannels.get(selectedValues.get(0))) != null
                && infos.size() > 0)
        {
            if (isDefaultTransformationDefined(infos) == false)
            {
                model.add(DEFAULT_TRANSFORMATION);
            }
            for (ImageTransformationInfo imageTransformationInfo : infos)
            {
                model.add(convertToLabeledItem(imageTransformationInfo));
            }
            if (model.size() <= 1)
            {
                transformationsComboBox.setVisible(false);
                adjustLabel.setVisible(false);
                return;
            }
            transformationsComboBox.add(model);
            setTransformationsVisible(true);

            selectTransformation(selectedValues.get(0));
        } else
        {
            setTransformationsVisible(false);
        }
    }

    private static boolean isDefaultTransformationDefined(Set<ImageTransformationInfo> infos)
    {
        for (ImageTransformationInfo imageTransformationInfo : infos)
        {
            if (imageTransformationInfo.isDefault())
            {
                return true;
            }
        }
        return false;
    }

    private void setTransformationsVisible(boolean visible)
    {
        transformationsComboBox.setVisible(visible);
        adjustLabel.setVisible(visible);
    }

    private void selectTransformation(String channelCode)
    {
        boolean selected = false;
        String code = defaultChannelState.tryGetDefaultTransformation(channelCode);

        SimpleModelComboBox<ImageTransformationInfo> combobox = transformationsComboBox;
        selected = setSelectedValue(code, combobox);
        if (false == selected)
        {
            for (SimpleComboValue<LabeledItem<ImageTransformationInfo>> info : transformationsComboBox
                    .getStore().getModels())
            {
                if (info.getValue().getItem().isDefault())
                {
                    transformationsComboBox.setSelection(Collections.singletonList(info));
                    selected = true;
                    break;
                }
            }
        }

        if (false == selected)
        {
            transformationsComboBox.setSelection(Collections.singletonList(transformationsComboBox
                    .getStore().getModels().get(0)));
        }
    }

    private boolean setSelectedValue(String code,
            SimpleModelComboBox<ImageTransformationInfo> combobox)
    {
        if (code != null)
        {
            for (SimpleComboValue<LabeledItem<ImageTransformationInfo>> info : combobox.getStore()
                    .getModels())
            {
                if (info.getValue().getItem().getCode().equals(code))
                {
                    combobox.setSelection(Collections.singletonList(info));
                    return true;
                }
            }
        }
        return false;
    }

    private static LabeledItem<ImageTransformationInfo> convertToLabeledItem(
            ImageTransformationInfo imageTransformationInfo)
    {
        return new LabeledItem<ImageTransformationInfo>(imageTransformationInfo,
                imageTransformationInfo.getLabel(), imageTransformationInfo.getDescription());
    }
}