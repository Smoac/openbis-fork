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
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ClickableFormPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * An <i>abstract</i> registration form.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractRegistrationForm extends LayoutContainer
{
    public static final String SAVE_BUTTON = "save-button";

    public static final int DEFAULT_LABEL_WIDTH = 100;

    public static final int DEFAULT_FIELD_WIDTH = 400;

    protected InfoBox infoBox;

    protected FormPanel formPanel;

    protected final int labelWidth;

    protected final int fieldWitdh;

    protected Button saveButton;

    protected AbstractRegistrationForm(final IMessageProvider messageProvider, final String id)
    {
        this(messageProvider, id, DEFAULT_LABEL_WIDTH, DEFAULT_FIELD_WIDTH);
    }

    protected AbstractRegistrationForm(final IMessageProvider messageProvider, final String id,
            final int labelWidth, final int fieldWidth)
    {
        this.labelWidth = labelWidth;
        this.fieldWitdh = fieldWidth;
        setLayout(new FlowLayout(5));
        setScrollMode(Scroll.AUTO);
        setId(id);
        add(infoBox = createInfoBox());
        add(formPanel = createFormPanel(messageProvider));
    }

    private final static InfoBox createInfoBox()
    {
        final InfoBox infoBox = new InfoBox();
        return infoBox;
    }

    protected ClickableFormPanel createFormPanel(final IMessageProvider messageProvider)
    {
        final ClickableFormPanel panel = new ClickableFormPanel();
        panel.addClickListener(new InfoBoxResetListener(infoBox));
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setWidth(labelWidth + fieldWitdh + 40);
        panel.setLabelWidth(labelWidth);
        panel.setFieldWidth(fieldWitdh);
        panel.setButtonAlign(HorizontalAlignment.RIGHT);
        saveButton = new Button(messageProvider.getMessage(Dict.BUTTON_SAVE));
        saveButton.setStyleAttribute("marginRight", "20px");
        saveButton.setId(getId() + SAVE_BUTTON);
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (panel.isValid())
                    {
                        submitValidForm();
                    }
                }
            });
        final Button resetButton = new Button(messageProvider.getMessage(Dict.BUTTON_RESET));
        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    panel.reset();
                }
            });
        panel.addButton(resetButton);
        panel.addButton(saveButton);
        return panel;
    }

    /**
     * Submits a valid form.
     * <p>
     * This method only gets executed when the form has been successfully validated.
     * </p>
     */
    protected abstract void submitValidForm();

    //
    // Helper classes
    //

    public final static class InfoBoxResetListener implements Listener<FieldEvent>, ClickListener
    {
        private final InfoBox infoBox;

        public InfoBoxResetListener(final InfoBox infoBox)
        {
            assert infoBox != null : "Unspecified info box.";
            this.infoBox = infoBox;
        }

        private void resetInfoBox()
        {
            infoBox.reset();
        }

        //
        // Listener
        //

        public final void handleEvent(final FieldEvent be)
        {
            resetInfoBox();
        }

        //
        // ClickListener
        //

        public final void onClick(Widget sender)
        {
            resetInfoBox();
        }
    }

    /**
     * Field specifying the session key needed by the file uploader on the server side.
     * <p>
     * This key is the session attribute key where you can access the uploaded files.
     * </p>
     */
    public final static HiddenField<String> createHiddenSessionField(String value)
    {
        final HiddenField<String> hiddenField = new HiddenField<String>();
        hiddenField.setName("sessionKey");
        hiddenField.setValue(value);
        return hiddenField;
    }

    static protected final void addUploadFeatures(FormPanel panel, String sessionKey)
    {
        panel.setWidth(AbstractRegistrationForm.DEFAULT_LABEL_WIDTH
                + AbstractRegistrationForm.DEFAULT_FIELD_WIDTH + 50);
        panel.setAction(GenericConstants.createServicePath("upload"));
        panel.setEncoding(Encoding.MULTIPART);
        panel.setMethod(Method.POST);
        final HiddenField<String> sessionKeyField =
                AbstractRegistrationForm.createHiddenSessionField(sessionKey);
        panel.add(sessionKeyField);
    }

}
