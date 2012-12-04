/*
 * Copyright 2012 ETH Zuerich, CISD
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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.ui.Grid;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IntegerField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.IntensityRange;

/**
 * @author Pawel Glyzewski
 */
public class UserDefinedRescalingSettingsDialog extends Dialog
{
    private final Label labelMin;

    private final Label labelMax;

    private final IntegerField minTextField;

    private final IntegerField maxTextField;

    private final IMessageProvider messageProvider;

    private final IDefaultChannelState defaultChannelState;

    private final String channelCode;

    public UserDefinedRescalingSettingsDialog(IMessageProvider messageProvider,
            IDefaultChannelState defaultChannelState, String channelCode)
    {
        super();

        this.messageProvider = messageProvider;
        this.defaultChannelState = defaultChannelState;
        this.channelCode = channelCode;

        setHeading(this.messageProvider.getMessage(Dict.TITLE_USER_DEFINED_RESCALING_DIALOG));
        setButtons(Dialog.OKCANCEL);
        labelMin = new Label(this.messageProvider.getMessage(Dict.RESCALING_DIALOG_MIN));
        labelMax = new Label(this.messageProvider.getMessage(Dict.RESCALING_DIALOG_MAX));
        minTextField =
                new IntegerField(messageProvider.getMessage(Dict.RESCALING_DIALOG_MIN), true);
        maxTextField =
                new IntegerField(messageProvider.getMessage(Dict.RESCALING_DIALOG_MAX), true);

        setInitialValues();

        Grid grid = new Grid(2, 2);
        grid.setWidget(0, 0, labelMin);
        grid.setWidget(0, 1, minTextField);
        grid.setWidget(1, 0, labelMax);
        grid.setWidget(1, 1, maxTextField);

        add(grid);

        setAutoWidth(true);
        setAutoHeight(true);

        addListener(Events.Show, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    center();
                }
            });
    }

    private void setInitialValues()
    {
        minTextField.setValue(0);
        maxTextField.setValue(255);

        IntensityRange range = defaultChannelState.tryGetIntensityRange(channelCode);
        if (range != null)
        {
            minTextField.setValue(range.getBlackPoint());
            maxTextField.setValue(range.getWhitePoint());
        }
    }

    @Override
    protected final void onButtonPressed(final Button button)
    {
        if (button.getItemId().equals(Dialog.OK))
        {
            button.disable();
            if (minTextField.isValid() && maxTextField.isValid())
            {
                super.onButtonPressed(button);
                updateIntensityRescaling();

                hide();
            } else
            {
                button.enable();
            }
        } else
        {
            super.onButtonPressed(button);
            hide();
        }
    }

    public void updateIntensityRescaling()
    {
        IntensityRange result = null;

        try
        {
            int min = Integer.parseInt(minTextField.getValue().toString());
            int max = Integer.parseInt(maxTextField.getValue().toString());

            result = new IntensityRange(min, max);
        } catch (NumberFormatException e)
        {
        }

        if (result != null)
        {
            defaultChannelState.setIntensityRange(channelCode, result);
        }
    }
}
