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

import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.LabeledItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SimpleModelComboBox;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageResolution;

/**
 * @author pkupczyk
 */
public class ResolutionChooser extends LayoutContainer
{

    private SimpleModelComboBox<ImageResolution> comboBox;

    public ResolutionChooser(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final List<ImageResolution> resolutions, final ImageResolution defaultResolution)
    {

        List<LabeledItem<ImageResolution>> defaultItems =
                Collections.singletonList(new LabeledItem<ImageResolution>(null, viewContext
                        .getMessage(Dict.RESOLUTION_CHOOSER_DEFAULT)));

        this.comboBox = new SimpleModelComboBox<ImageResolution>(viewContext, defaultItems, null);

        Collections.sort(resolutions);

        if (resolutions != null)
        {
            for (ImageResolution resolution : resolutions)
            {
                String resolutionText =
                        viewContext.getMessage(Dict.RESOLUTION_CHOOSER_RESOLUTION,
                                resolution.getWidth(), resolution.getHeight());
                comboBox.add(new LabeledItem<ImageResolution>(resolution, resolutionText));
            }
        }

        LabeledItem<ImageResolution> defaultResolutionItem =
                comboBox.findModelForVal(defaultResolution);
        if (defaultResolutionItem != null)
        {
            comboBox.setSelection(defaultResolutionItem);
        }

        add(comboBox);
    }

    public ImageResolution getResolution()
    {
        return comboBox.tryGetChosenItem();
    }

    public void addResolutionChangedListener(Listener<BaseEvent> listener)
    {
        comboBox.addListener(Events.SelectionChange, listener);
    }

}
