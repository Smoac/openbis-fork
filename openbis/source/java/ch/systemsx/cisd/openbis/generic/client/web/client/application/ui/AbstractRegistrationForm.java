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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ClickableFormPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.InfoBox;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;

/**
 * An <i>abstract</i> registration form.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractRegistrationForm extends ContentPanel
{
    private static final String SESSION_KEY_PREFIX = "sessionKey_";

    private static final String SESSION_KEYS_NUMBER = "sessionKeysNumber";

    public static final String SAVE_BUTTON = "save-button";

    public static final int DEFAULT_LABEL_WIDTH = 100;

    public static final int DEFAULT_FIELD_WIDTH = 500;

    protected InfoBox infoBox;

    protected FormPanel formPanel;

    protected final int labelWidth;

    protected final int fieldWidth;

    protected Button saveButton;

    private boolean sessionKeysInitiated = false;

    private Html loadingInfo;

    protected AbstractRegistrationForm(final IMessageProvider messageProvider, final String id)
    {
        this(messageProvider, id, DEFAULT_LABEL_WIDTH, DEFAULT_FIELD_WIDTH);
    }

    protected AbstractRegistrationForm(final IMessageProvider messageProvider, final String id,
            final int labelWidth, final int fieldWidth)
    {
        this.labelWidth = labelWidth;
        this.fieldWidth = fieldWidth;
        setHeaderVisible(false);
        setLayout(new FlowLayout(5));
        setBodyBorder(false);
        setBorders(false);
        setScrollMode(Scroll.AUTO);
        setId(id);
        add(infoBox = createInfoBox());
        add(loadingInfo = createLoadingInfo());
        add(formPanel = createFormPanel(messageProvider));
    }

    private Html createLoadingInfo()
    {
        Html result = new Html("Loading...");
        result.setVisible(false);
        return result;
    }

    private final static InfoBox createInfoBox()
    {
        final InfoBox infoBox = new InfoBox();
        return infoBox;
    }

    protected void setLoading(boolean loading)
    {
        formPanel.setVisible(loading == false);
        loadingInfo.setVisible(loading);
    }

    protected void resetFieldsAfterSave()
    {
        resetPanel();
    }

    protected void resetPanel()
    {
        formPanel.reset();
    }

    protected ClickableFormPanel createFormPanel(final IMessageProvider messageProvider)
    {
        final ClickableFormPanel panel = new ClickableFormPanel();
        panel.addClickListener(new InfoBoxResetListener(infoBox));
        panel.setHeaderVisible(false);
        panel.setBodyBorder(false);
        panel.setWidth(labelWidth + fieldWidth + 40);
        panel.setLabelWidth(labelWidth);
        panel.setFieldWidth(fieldWidth);
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
                    resetPanel();
                }
            });
        panel.addButton(resetButton);
        panel.addButton(saveButton);
        return panel;
    }

    protected void setUploadEnabled(boolean enabled)
    {
        saveButton.setEnabled(enabled);
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

    protected abstract class AbstractRegistrationCallback<T> extends AbstractAsyncCallback<T>
    {
        protected AbstractRegistrationCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<T>(infoBox));
            saveButton.disable();
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected void process(final T result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo(result));
            try
            {
                resetFieldsAfterSave();
            } catch (JavaScriptException e)
            {
                // ignored because it might be thrown for file upload field in system tests on CI
                // server
            }
            setUploadEnabled(true);
            saveButton.enable();
        }

        protected abstract String createSuccessfullRegistrationInfo(T result);

        @Override
        public final void finishOnFailure(final Throwable caught)
        {
            setUploadEnabled(true);
            saveButton.enable();
        }

    }

    public final static class InfoBoxResetListener implements Listener<FieldEvent>, ClickHandler
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

        public final void onClick(ClickEvent sender)
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
    public final static HiddenField<String> createHiddenSessionField(String value, int counter)
    {
        String name = SESSION_KEY_PREFIX + counter;
        return createHiddenField(name, value);
    }

    private static HiddenField<String> createHiddenField(String name, String value)
    {
        final HiddenField<String> hiddenField = new HiddenField<String>();
        hiddenField.setName(name);
        hiddenField.setValue(value);
        return hiddenField;
    }

    protected final void addUploadFeatures(String sessionKey)
    {
        ArrayList<String> sessionKeys = new ArrayList<String>();
        sessionKeys.add(sessionKey);
        addUploadFeatures(sessionKeys);
    }

    protected final void addUploadFeatures(List<String> sessionKeys)
    {
        assert sessionKeysInitiated == false : "This method should be called only once.";
        formPanel.setWidth(AbstractRegistrationForm.DEFAULT_LABEL_WIDTH
                + AbstractRegistrationForm.DEFAULT_FIELD_WIDTH + 50);
        formPanel.setAction(GenericConstants.createServicePath("upload"));
        formPanel.setEncoding(Encoding.MULTIPART);
        formPanel.setMethod(Method.POST);
        formPanel.add(createHiddenField(SESSION_KEYS_NUMBER, sessionKeys.size() + ""));
        for (int i = 0; i < sessionKeys.size(); i++)
        {
            formPanel.add(AbstractRegistrationForm.createHiddenSessionField(sessionKeys.get(i), i));
        }
        sessionKeysInitiated = true;
    }

    public static String getEditTitle(final IMessageProvider messageProvider,
            final String entityKindDictKey, final IIdentifiable identifiable)
    {
        return messageProvider.getMessage(Dict.EDIT_TITLE, messageProvider
                .getMessage(entityKindDictKey), identifiable.getCode());
    }

}
