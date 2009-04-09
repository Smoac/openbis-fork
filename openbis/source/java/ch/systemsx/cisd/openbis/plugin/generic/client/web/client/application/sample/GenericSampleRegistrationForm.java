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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.AttachmentManager;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> sample registration form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleRegistrationForm extends
        AbstractGenericEntityRegistrationForm<SampleType, SampleTypePropertyType, SampleProperty>
{
    public static final String ID = createId(EntityKind.SAMPLE);

    public static final String ID_SUFFIX_CONTAINER = "container";

    public static final String ID_SUFFIX_PARENT = "parent";

    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    public static final String SESSION_KEY = createSimpleId(EntityKind.SAMPLE);

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final SampleType sampleType;

    private GroupSelectionWidget groupSelectionWidget;

    private TextField<String> container;

    private TextField<String> parent;

    private AttachmentManager attachmentManager =
            new AttachmentManager(SESSION_KEY, DEFAULT_NUMBER_OF_ATTACHMENTS, "Attachment");

    public GenericSampleRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        super(viewContext, sampleType.getSampleTypePropertyTypes(), EntityKind.SAMPLE);
        this.viewContext = viewContext;
        this.sampleType = sampleType;
        addUploadFeatures(formPanel, SESSION_KEY);
    }

    private final String createSampleIdentifier()
    {
        final Group group = groupSelectionWidget.tryGetSelectedGroup();
        final boolean shared = GroupSelectionWidget.isSharedGroup(group);
        final String code = codeField.getValue();
        final StringBuilder builder = new StringBuilder("/");
        if (shared == false)
        {
            builder.append(group.getCode() + "/");
        }
        builder.append(code);
        return builder.toString().toUpperCase();
    }

    @Override
    public final void submitValidForm()
    {
    }

    public final void registerSample()
    {
        final NewSample newSample =
                new NewSample(createSampleIdentifier(), sampleType, StringUtils.trimToNull(parent
                        .getValue()), StringUtils.trimToNull(container.getValue()));
        final List<SampleProperty> properties = extractProperties();
        newSample.setProperties(properties.toArray(SampleProperty.EMPTY_ARRAY));
        viewContext.getService().registerSample(SESSION_KEY, newSample,
                new RegisterSampleCallback(viewContext));
    }

    public final class RegisterSampleCallback extends AbstractAsyncCallback<Void>

    {
        public RegisterSampleCallback(IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        @Override
        protected void process(Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            resetPanel();
            setUploadEnabled(true);
        }

        @Override
        protected final void finishOnFailure(final Throwable caught)
        {
            setUploadEnabled(true);
        }

        private void resetPanel()
        {
            formPanel.reset();
        }

        private String createSuccessfullRegistrationInfo()
        {
            String code = codeField.getValue();
            final Group selectedGroup = groupSelectionWidget.tryGetSelectedGroup();
            boolean shared = GroupSelectionWidget.isSharedGroup(selectedGroup);
            if (shared)
            {
                return "Shared sample <b>" + code + "</b> successfully registered";

            } else
            {
                return "Sample <b>" + code + "</b> successfully registered in group <b>"
                        + selectedGroup.getCode() + "</b>";
            }
        }
    }

    @Override
    protected void createEntitySpecificFields()
    {
        groupSelectionWidget = new GroupSelectionWidget(viewContext, getId(), true);
        FieldUtil.markAsMandatory(groupSelectionWidget);
        groupSelectionWidget.setFieldLabel(viewContext.getMessage(Dict.GROUP));

        parent = new VarcharField(viewContext.getMessage(Dict.GENERATED_FROM_SAMPLE), false);
        parent.setId(getId() + ID_SUFFIX_PARENT);

        container = new VarcharField(viewContext.getMessage(Dict.PART_OF_SAMPLE), false);
        container.setId(getId() + ID_SUFFIX_CONTAINER);

        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    registerSample();
                }

                @Override
                protected void setUploadEnabled()
                {
                    GenericSampleRegistrationForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    protected void setUploadEnabled(boolean enabled)
    {
        saveButton.setEnabled(enabled);
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
                        if (attachmentManager.attachmentsDefined() > 0)
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            registerSample();
                        }
                    }
                }
            });
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(groupSelectionWidget.asDatabaseModificationAware());
        fields.add(wrapUnaware(parent));
        fields.add(wrapUnaware(container));
        for (FileUploadField attachmentField : attachmentManager.getFields())
        {
            fields.add(wrapUnaware((Field<?>) attachmentField));
        }
        return fields;
    }

    @Override
    protected PropertiesEditor<SampleType, SampleTypePropertyType, SampleProperty> createPropertiesEditor(
            List<SampleTypePropertyType> entityTypesPropertyTypes, String string,
            IViewContext<ICommonClientServiceAsync> context)
    {
        return new SamplePropertyEditor(entityTypesPropertyTypes, string, context);
    }
}
