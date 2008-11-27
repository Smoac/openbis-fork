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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FileUpload;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * @author Izabela Adamczyk
 */
public class SampleBatchRegistrationMock extends ContentPanel
{

    private final SampleTypeSelectionWidget sampleTypeSelection;

    public SampleBatchRegistrationMock(final CommonViewContext viewContext)
    {
        setHeading("Sample batch registration mock");
        setBodyBorder(false);
        sampleTypeSelection = new SampleTypeSelectionWidget(viewContext, true);
        sampleTypeSelection.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(final SelectionChangedEvent<ModelData> se)
                {
                    final SampleType selectedType = sampleTypeSelection.tryGetSelected();
                    if (selectedType != null)
                    {
                        removeAll();
                        add(new SampleBatchRegistrationForm(viewContext, selectedType));
                        layout();
                    }
                }
            });
        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Sample type:"));
        toolBar.add(new AdapterToolItem(sampleTypeSelection));
        setTopComponent(toolBar);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        createUI();
    }

    void createUI()
    {

    }

    static class SampleBatchRegistrationForm extends ContentPanel
    {

        SampleBatchRegistrationForm(final CommonViewContext viewContext,
                final SampleType sampleType)
        {
            setStyleAttribute("margin", "30");
            setHeaderVisible(false);
            setBodyBorder(false);
            setButtonAlign(HorizontalAlignment.LEFT);
            final ButtonBar bb = new ButtonBar();
            bb.setCellSpacing(20);

            bb.add(new Button("View & Register", new SelectionListener<ComponentEvent>()
                {

                    @Override
                    public void componentSelected(final ComponentEvent ce)
                    {
                        MessageBox
                                .alert(
                                        "Samples to be registered",
                                        "List of samples which will be registered and REGISTER button (or parsing error message) will be shown.",
                                        null);
                    }
                }));

            bb.add(new Button("Register", new SelectionListener<ComponentEvent>()
                {

                    @Override
                    public void componentSelected(final ComponentEvent ce)
                    {
                        MessageBox.alert("Registration finished",
                                "List of registered samples is displayed or error message", null);
                    }
                }));
            setButtonBar(bb);

        }

        @Override
        protected void onRender(final Element target, final int index)
        {
            super.onRender(target, index);
            refresh();
        }

        public void refresh()
        {

            final FileUpload upload = new FileUpload();
            add(upload);

        }
    }
}
