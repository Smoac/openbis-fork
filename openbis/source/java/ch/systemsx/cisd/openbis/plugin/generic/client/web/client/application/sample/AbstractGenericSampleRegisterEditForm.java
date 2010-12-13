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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IOnSuccessAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserButton;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserButton.SampleChooserButtonAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.AttachmentsFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * A {@link AbstractGenericEntityRegistrationForm} extension for registering and editing samples.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractGenericSampleRegisterEditForm extends
        AbstractGenericEntityRegistrationForm<SampleType, SampleTypePropertyType>
{
    public static final String ID_SUFFIX_CONTAINER = "container";

    public static final String ID_SUFFIX_PARENT = "parent";

    public static final String ID_SUFFIX_EXPERIMENT = "experiment";

    protected SampleType sampleType; // used for code prefix and container/parent field visibility

    protected AttachmentsFileFieldManager attachmentsManager;

    protected String attachmentsSessionKey;

    protected String simpleId;

    protected ExperimentChooserFieldAdaptor experimentField;

    private ExperimentIdentifier initialExperimentIdentifierOrNull;

    protected GroupSelectionWidget groupSelectionWidget;

    private String initialGroupCodeOrNull;

    protected SampleChooserFieldAdaptor container;

    protected SampleChooserButtonAdaptor parentButton;

    protected ParentSamplesArea parentsArea;

    private String sampleIdentifierForUploadOrNull = null;

    private Button saveUploadButton;

    protected AbstractGenericSampleRegisterEditForm(
            IViewContext<IGenericClientServiceAsync> viewContext, ActionContext actionContext)
    {
        this(viewContext, actionContext, null);
    }

    protected AbstractGenericSampleRegisterEditForm(
            IViewContext<IGenericClientServiceAsync> viewContext, ActionContext actionContext,
            IIdAndCodeHolder identifiable)
    {
        super(viewContext, identifiable, EntityKind.SAMPLE);
        this.simpleId = createSimpleId(identifiable, EntityKind.SAMPLE);
        this.attachmentsSessionKey = simpleId + "_attachments";
        List<String> sesionKeys = new ArrayList<String>();
        sesionKeys.add(attachmentsSessionKey);
        addUploadFeatures(sesionKeys);
        extractInitialValues(actionContext);
        saveUploadButton = createSaveAndUploadButton();
        boolean cifexConfigured =
                StringUtils
                        .isBlank(viewContext.getModel().getApplicationInfo().getCifexRecipient()) == false
                        && StringUtils.isBlank(viewContext.getModel().getApplicationInfo()
                                .getCIFEXURL()) == false;
        if (cifexConfigured)
        {
            formPanel.addButton(saveUploadButton);
        }
    }

    private void extractInitialValues(ActionContext context)
    {
        this.initialExperimentIdentifierOrNull = tryGetExperimentIdentifier(context);
        this.initialGroupCodeOrNull = tryGetSpaceCode(context);
    }

    private ExperimentIdentifier tryGetExperimentIdentifier(ActionContext context)
    {
        return (context.tryGetExperiment() == null) ? null : ExperimentIdentifier
                .createIdentifier(context.tryGetExperiment());
    }

    private String tryGetSpaceCode(ActionContext context)
    {
        return (context.tryGetSpaceCode() == null) ? null : context.tryGetSpaceCode();
    }

    private ExperimentChooserFieldAdaptor createExperimentField()
    {
        String label = viewContext.getMessage(Dict.EXPERIMENT);
        return ExperimentChooserField.create(label, false, initialExperimentIdentifierOrNull,
                viewContext.getCommonViewContext());
    }

    @Override
    protected void setUploadEnabled(boolean enabled)
    {
        super.setUploadEnabled(enabled);
        saveUploadButton.setEnabled(enabled);
    }

    private void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (attachmentsManager.filesDefined() > 0)
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

    private Button createSaveAndUploadButton()
    {
        Button button = new Button(viewContext.getMessage(Dict.BUTTON_SAVE_AND_UPLOAD));
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        sampleIdentifierForUploadOrNull = createSampleIdentifier();
                        if (attachmentsManager.filesDefined() > 0)
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
        return button;
    }

    protected <R, T extends AbstractRegistrationForm.AbstractRegistrationCallback<R>> T enrichWithPostRegistration(
            T callback)
    {
        callback.addOnFailureAction(new IDelegatedAction()
            {
                public void execute()
                {
                    sampleIdentifierForUploadOrNull = null;
                }
            });
        callback.addOnSuccessAction(new IOnSuccessAction<R>()
            {
                public void execute(R result)
                {
                    if (StringUtils.isBlank(sampleIdentifierForUploadOrNull) == false)
                    {
                        DispatcherHelper.dispatchNaviEvent(new ComponentProvider(viewContext
                                .getCommonViewContext())
                                .getDataSetUploadTab(sampleIdentifierForUploadOrNull));
                    }
                    sampleIdentifierForUploadOrNull = null;
                }
            });
        return callback;
    }

    protected abstract void save();

    @Override
    public final void submitValidForm()
    {
    }

    @Override
    protected PropertiesEditor<SampleType, SampleTypePropertyType> createPropertiesEditor(
            String id, IViewContext<ICommonClientServiceAsync> context)
    {
        SamplePropertyEditor editor = new SamplePropertyEditor(id, context);
        return editor;
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(wrapUnaware(experimentField.getField()));
        fields.add(groupSelectionWidget.asDatabaseModificationAware());
        fields.add(wrapUnaware(parentsArea));
        fields.add(wrapUnaware(parentButton.getField()));
        fields.add(wrapUnaware(container.getField()));
        return fields;
    }

    @Override
    protected void addFormFieldsToPanel(FormPanel panel)
    {
        super.addFormFieldsToPanel(panel);
        attachmentsManager.addAttachmentFieldSetsToPanel(panel);
    }

    @Override
    protected void resetPanel()
    {
        super.resetPanel();
        attachmentsManager.resetAttachmentFieldSetsInPanel(formPanel);
    }

    @Override
    protected String getGeneratedCodePrefix()
    {
        // for samples prefix is taken from sample type property
        return sampleType.getGeneratedCodePrefix();
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        groupSelectionWidget =
                new GroupSelectionWidget(viewContext, getId(), true, false, initialGroupCodeOrNull);
        FieldUtil.markAsMandatory(groupSelectionWidget);
        groupSelectionWidget.setFieldLabel(viewContext.getMessage(Dict.GROUP));
        parentButton =
                SampleChooserButton.create(null, viewContext.getMessage(Dict.ADD_PARENT), true,
                        false, false, viewContext.getCommonViewContext(), getId()
                                + ID_SUFFIX_PARENT,
                        SampleTypeDisplayID.SAMPLE_REGISTRATION_PARENT_CHOOSER
                                .withSuffix(getSampleTypeCode()));
        parentsArea = new ParentSamplesArea(viewContext, getId());
        SampleChooserButton parentChooserButton = parentButton.getChooserButton();
        parentChooserButton.addChosenEntityListener(new IChosenEntityListener<TableModelRowWithObject<Sample>>()
            {
                public void entityChosen(TableModelRowWithObject<Sample> entity)
                {
                    if (entity != null)
                    {
                        String sampleIdentifier = entity.getObjectOrNull().getIdentifier();
                        parentsArea.appendItem(sampleIdentifier);
                    }
                }
            });
        container =
                SampleChooserField.create(viewContext.getMessage(Dict.PART_OF_SAMPLE), false, null,
                        true, false, false, viewContext.getCommonViewContext(), getId()
                                + ID_SUFFIX_CONTAINER,
                        SampleTypeDisplayID.SAMPLE_REGISTRATION_CONTAINER_CHOOSER
                                .withSuffix(getSampleTypeCode()));
        experimentField = createExperimentField();
        experimentField.getChooserField().setId(getId() + ID_SUFFIX_EXPERIMENT);
        attachmentsManager = new AttachmentsFileFieldManager(attachmentsSessionKey, viewContext);
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
                    AbstractGenericSampleRegisterEditForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
        setContainerAndParentVisibility(sampleType);
    }

    protected final String createSampleIdentifier()
    {
        final Space space = groupSelectionWidget.tryGetSelectedGroup();
        final boolean shared = GroupSelectionWidget.isSharedGroup(space);
        final String code = codeField.getValue();
        final StringBuilder builder = new StringBuilder("/");
        if (shared == false)
        {
            builder.append(space.getCode() + "/");
        }
        builder.append(code);
        return builder.toString().toUpperCase();
    }

    /** sets visibility of container and parent fields dependent on sample type */
    private final void setContainerAndParentVisibility(final SampleType sampleType)
    {
        boolean showContainer = sampleType.isShowContainer();
        boolean showParents = sampleType.isShowParents();
        FieldUtil.setVisibility(showContainer, container.getField());
        FieldUtil.setVisibility(showParents, parentButton.getField(), parentsArea);
    }

    private String getSampleTypeCode()
    {
        return sampleType.getCode();
    }

    protected String[] getParents()
    {
        return parentsArea.tryGetSamples();
    }

}
