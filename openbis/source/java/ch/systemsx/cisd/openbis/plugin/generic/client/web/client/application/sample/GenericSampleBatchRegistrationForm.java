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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample batch registration panel.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleBatchRegistrationForm extends AbstractRegistrationForm
{
    private static final String PREFIX = "sample-batch-registration";

    public final static String ID = GenericConstants.ID_PREFIX + PREFIX;

    private static final String SESSION_KEY = PREFIX;

    private static final String FIELD_LABEL_TEMPLATE = "File";

    private static final int DEFAULT_NUMBER_OF_FILES = 1;

    private final FileFieldManager fileFieldsManager;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final SampleType sampleType;

    private CheckBoxField generateCodesCheckbox;

    private GroupSelectionWidget groupSelector;

    public GenericSampleBatchRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        super(viewContext.getCommonViewContext(), ID);
        this.viewContext = viewContext;
        this.sampleType = sampleType;
        fileFieldsManager =
                new FileFieldManager(SESSION_KEY, DEFAULT_NUMBER_OF_FILES, FIELD_LABEL_TEMPLATE);
        fileFieldsManager.setMandatory();
        setScrollMode(Scroll.AUTO);
        generateCodesCheckbox = new CheckBoxField("Generate codes automatically", false);
        groupSelector =
                createGroupField(viewContext.getCommonViewContext(), "" + getId(), true,
                        generateCodesCheckbox);
        generateCodesCheckbox.addListener(Events.Change, new Listener<FieldEvent>()
            {
                public void handleEvent(FieldEvent be)
                {
                    boolean selected = (Boolean) be.value;
                    groupSelector.setVisible(selected);
                    groupSelector.setEnabled(selected);
                    groupSelector.validate();
                }
            });
        addUploadFeatures(SESSION_KEY);
    }

    private final GroupSelectionWidget createGroupField(
            IViewContext<ICommonClientServiceAsync> context, String idSuffix, boolean addShared,
            final CheckBoxField checkbox)
    {
        GroupSelectionWidget field = new GroupSelectionWidget(context, idSuffix, addShared)
            {

                @Override
                protected boolean validateValue(String val)
                {
                    if (checkbox.getValue() && tryGetSelectedGroup() == null)
                    {
                        forceInvalid(GXT.MESSAGES.textField_blankText());
                        return false;
                    }
                    clearInvalid();
                    return true;
                }
            };
        FieldUtil.markAsMandatory(field);
        field.setFieldLabel("Default Group");
        field.setVisible(false);
        return field;
    }

    @Override
    protected void resetFieldsAfterSave()
    {
        for (FileUploadField attachmentField : fileFieldsManager.getFields())
        {
            attachmentField.reset();
        }
    }

    private final void addFormFields()
    {
        formPanel.add(generateCodesCheckbox);
        formPanel.add(groupSelector);
        for (FileUploadField attachmentField : fileFieldsManager.getFields())
        {
            formPanel.add(wrapUnaware((Field<?>) attachmentField).get());
        }
        formPanel.add(createTemplateField());
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    save();
                }

                @Override
                protected void setUploadEnabled()
                {
                    GenericSampleBatchRegistrationForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    protected void save()
    {
        String defaultGroupIdentifier = null;
        Group selectedGroup = groupSelector.tryGetSelectedGroup();
        if (generateCodesCheckbox.getValue() && selectedGroup != null)
        {
            defaultGroupIdentifier = selectedGroup.getIdentifier();
        }
        viewContext.getService().registerSamples(sampleType, SESSION_KEY, defaultGroupIdentifier,
                new RegisterSamplesCallback(viewContext));
    }

    void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (fileFieldsManager.filesDefined() > 0)
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            save();
                        }
                    }
                }
            });
    }

    private final class RegisterSamplesCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<List<BatchRegistrationResult>>
    {
        RegisterSamplesCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(
                final List<BatchRegistrationResult> result)
        {
            final StringBuilder builder = new StringBuilder();
            for (final BatchRegistrationResult batchRegistrationResult : result)
            {
                builder.append("<b>" + batchRegistrationResult.getFileName() + "</b>:");
                builder.append(batchRegistrationResult.getMessage());
                builder.append("<br />");
            }
            return builder.toString();
        }

    }

    @Override
    protected void resetPanel()
    {
        super.resetPanel();
        groupSelector.setVisible(false);
        groupSelector.setEnabled(false);
    }

    @Override
    protected final void submitValidForm()
    {
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    private LabelField createTemplateField()
    {
        LabelField result =
                new LabelField(LinkRenderer.renderAsLink(viewContext
                        .getMessage(Dict.FILE_TEMPLATE_LABEL)));
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Event.ONCLICK, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    WindowUtils.openWindow(UrlParamsHelper.createTemplateURL(EntityKind.SAMPLE,
                            sampleType, generateCodesCheckbox.getValue()));
                }
            });
        return result;
    }

}
