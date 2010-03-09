/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

/**
 * Utility methods for widgets.
 * 
 * @author Izabela Adamczyk
 */
public class WidgetUtils
{
    public static RadioGroup createAllOrSelectedRadioGroup(Radio selected, Radio all, String label,
            long selectedSize, final IDelegatedAction onChangeActionOrNull)
    {
        final RadioGroup result = new RadioGroup();
        result.setFieldLabel(label);
        result.setSelectionRequired(true);
        result.setOrientation(Orientation.HORIZONTAL);
        if (selectedSize > 0)
        {
            result.add(selected);
        }
        result.add(all);
        result.setValue(selectedSize > 0 ? selected : all);
        result.setAutoHeight(true);
        if (onChangeActionOrNull != null)
        {
            result.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        onChangeActionOrNull.execute();
                    }
                });
        }
        return result;
    }

    public static RadioGroup createAllOrSelectedRadioGroup(Radio selected, Radio all, String label,
            long selectedSize)
    {
        return createAllOrSelectedRadioGroup(selected, all, label, selectedSize, null);
    }

    public static final Radio createRadio(final String label)
    {
        Radio result = new Radio();
        result.setBoxLabel(label);
        return result;
    }

    public static boolean isSelected(Radio radioOrNull)
    {
        if (radioOrNull == null)
        {
            return false;
        } else
        {
            return radioOrNull.getValue();
        }
    }

}
