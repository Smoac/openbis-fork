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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample batch registration panel.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleBatchRegistrationForm extends LayoutContainer
{
    private static final String FIELD_LABEL_TEMPLATE = "File {0}";

    private static final String FIELD_NAME_TEMPLATE = "sample_batch_registration_{0}";

    private static final int NUMBER_OF_FIELDS = 1;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final SampleType sampleType;

    private FormPanel formPanel;

    private Button submitButton;

    public GenericSampleBatchRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        super(createLayout());
        this.viewContext = viewContext;
        this.sampleType = sampleType;
        add(createUI());
    }

    private final static TableLayout createLayout()
    {
        final TableLayout tableLayout = new TableLayout();
        tableLayout.setCellSpacing(10);
        return tableLayout;
    }

    private final Component createUI()
    {
        submitButton = createButton();
        formPanel = createFormPanel(submitButton);
        final FieldSet fieldSet = createFieldSet();
        for (int i = 0; i < NUMBER_OF_FIELDS; i++)
        {
            fieldSet.add(createFileUploadField(i));
        }
        formPanel.add(fieldSet);
        return formPanel;
    }

    private final static FieldSet createFieldSet()
    {
        final FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading("Upload files");
        fieldSet.setLayout(createFormLayout());
        return fieldSet;
    }

    private final static FormLayout createFormLayout()
    {
        final FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(GenericSampleRegistrationForm.LABEL_WIDTH);
        formLayout.setDefaultWidth(GenericSampleRegistrationForm.FIELD_WIDTH);
        return formLayout;
    }

    private final static FormPanel createFormPanel(final Button button)
    {
        final FormPanel panel = new FormPanel();
        panel.setLayout(new FlowLayout());
        panel.setWidth(GenericSampleRegistrationForm.LABEL_WIDTH
                + GenericSampleRegistrationForm.FIELD_WIDTH + 50);
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setAction(GenericConstants.createServicePath("upload"));
        panel.setEncoding(Encoding.MULTIPART);
        panel.setMethod(Method.POST);
        panel.setButtonAlign(HorizontalAlignment.RIGHT);
        panel.addButton(button);
        return panel;
    }

    private final Button createButton()
    {
        final Button button =
                new Button(viewContext.getMessageProvider().getMessage("button_submit"));
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        submitButton.setEnabled(false);
                        formPanel.submit();
                    }
                }
            });
        return button;
    }

    private final static FileUploadField createFileUploadField(final int counter)
    {
        final FileUploadField file = new FileUploadField();
        file.setAllowBlank(counter > 0);
        final int number = counter + 1;
        file.setFieldLabel(Format.substitute(FIELD_LABEL_TEMPLATE, number));
        file.setName(Format.substitute(FIELD_NAME_TEMPLATE, number));
        return file;
    }
}
