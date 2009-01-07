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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The {@link LayoutContainer} extension for registering a sample.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleRegistrationPanel extends LayoutContainer
{
    private final SampleTypeSelectionWidget sampleTypeSelection;

    public static final String ID = "sample-registration";

    public SampleRegistrationPanel(final CommonViewContext viewContext)
    {
        setId(GenericConstants.ID_PREFIX + ID);
        setScrollMode(Scroll.AUTO);
        sampleTypeSelection = new SampleTypeSelectionWidget(viewContext, ID, false);
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.SAMPLE_TYPE)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(new AdapterToolItem(sampleTypeSelection));
        add(toolBar);
        sampleTypeSelection
                .addSelectionChangedListener(new SelectionChangedListener<SampleTypeModel>()
                    {

                        //
                        // SelectionChangedListener
                        //

                        @Override
                        public void selectionChanged(final SelectionChangedEvent<SampleTypeModel> se)
                        {
                            final SampleTypeModel sampleTypeModel = se.getSelectedItem();
                            if (sampleTypeModel != null)
                            {
                                final SampleType sampleType =
                                        sampleTypeModel.get(ModelDataPropertyNames.OBJECT);
                                removeAll();
                                final EntityKind entityKind = EntityKind.SAMPLE;
                                add(toolBar);
                                add(viewContext.getClientPluginFactoryProvider()
                                        .getClientPluginFactory(entityKind, sampleType)
                                        .createClientPlugin(entityKind)
                                        .createRegistrationForEntityType(sampleType));
                                layout();
                            }
                        }
                    });
    }
}
