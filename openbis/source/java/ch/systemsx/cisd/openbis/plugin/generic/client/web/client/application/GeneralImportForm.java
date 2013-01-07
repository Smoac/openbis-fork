package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

public class GeneralImportForm extends AbstractRegistrationForm
{
    protected final class RegisterOrUpdateSamplesAndMaterialsCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<List<BatchRegistrationResult>>
    {
        RegisterOrUpdateSamplesAndMaterialsCallback(
                final IViewContext<IGenericClientServiceAsync> viewContext)
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
                builder.append("<b>" + batchRegistrationResult.getFileName() + "</b>: ");
                builder.append(batchRegistrationResult.getMessage());
                builder.append("<br />");
            }
            return builder.toString();
        }
    }

    private static final String FIELD_LABEL_TEMPLATE = "File";

    private static final int NUMBER_OF_FIELDS = 1;

    private final String sessionKey;

    private final BasicFileFieldManager fileFieldsManager;

    private final TextField<String> emailField;

    private final CheckBox asynchronous;

    private final IViewContext<IGenericClientServiceAsync> genericViewContext;

    /**
     * @param genericViewContext
     * @param id
     */
    public GeneralImportForm(IViewContext<IGenericClientServiceAsync> genericViewContext,
            String id, String sessionKey)
    {
        super(genericViewContext, id);
        setResetButtonVisible(true);
        this.sessionKey = sessionKey;
        this.genericViewContext = genericViewContext;
        setScrollMode(Scroll.AUTO);
        asynchronous = createCheckBox();
        emailField =
                createEmailField(genericViewContext.getModel().getSessionContext().getUser()
                        .getUserEmail());
        fileFieldsManager =
                new BasicFileFieldManager(sessionKey, NUMBER_OF_FIELDS, FIELD_LABEL_TEMPLATE);
        fileFieldsManager.setMandatory();
        addUploadFeatures(sessionKey);
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    private CheckBox createCheckBox()
    {
        final CheckBox checkBox = new CheckBox();
        checkBox.setFieldLabel("Send confirmation?");
        checkBox.setBoxLabel("");
        checkBox.setValue(true);
        checkBox.addListener(Events.Change, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    if (checkBox.getValue())
                    {
                        formPanel.remove(asynchronous);
                        for (FileUploadField attachmentField : fileFieldsManager.getFields())
                        {
                            formPanel.remove(wrapUnaware((Field<?>) attachmentField).get());
                        }
                        addOnlyFormFields(true);
                    } else
                    {
                        formPanel.remove(emailField);
                    }
                    formPanel.layout();
                }
            });
        return checkBox;
    }

    private TextField<String> createEmailField(String userEmail)
    {
        TextField<String> field = new TextField<String>();
        field.setAllowBlank(false);
        field.setFieldLabel("Email");
        FieldUtil.markAsMandatory(field);
        field.setValue(userEmail);
        field.setValidateOnBlur(true);
        field.setRegex(GenericConstants.EMAIL_REGEX);
        field.getMessages().setRegexText("Expected email address format: user@domain.com");
        AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(genericViewContext.getImageBundle().getInfoIcon());
        FieldUtil.addInfoIcon(field,
                "All relevant notifications will be send to this email address",
                infoIcon.createImage());
        return field;
    }

    @Override
    protected void submitValidForm()
    {
    }

    @Override
    protected void resetFieldsAfterSave()
    {
        for (FileUploadField attachmentField : fileFieldsManager.getFields())
        {
            attachmentField.reset();
        }
        updateDirtyCheckAfterSave();
    }

    private final void addOnlyFormFields(boolean forceAddEmailField)
    {
        formPanel.add(asynchronous);
        if (forceAddEmailField || asynchronous.getValue())
        {
            formPanel.add(emailField);
        }
        for (FileUploadField attachmentField : fileFieldsManager.getFields())
        {
            formPanel.add(wrapUnaware((Field<?>) attachmentField).get());
        }
    }

    private final void addFormFields()
    {
        addOnlyFormFields(false);

        formPanel.addListener(Events.BeforeSubmit, new Listener<FormEvent>()
            {
                @Override
                public void handleEvent(FormEvent be)
                {
                    infoBox.displayProgress(messageProvider.getMessage(Dict.PROGRESS_UPLOADING));
                }
            });

        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    infoBox.displayProgress(messageProvider.getMessage(Dict.PROGRESS_PROCESSING));
                    save();
                }

                @Override
                protected void setUploadEnabled()
                {
                    GeneralImportForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        addSaveButtonConfirmationListener();
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

    protected void save()
    {
        genericViewContext.getService().registerOrUpdateSamplesAndMaterials(sessionKey, null, true,
                asynchronous.getValue(), emailField.getValue(),
                new RegisterOrUpdateSamplesAndMaterialsCallback(genericViewContext));
    }

    @Override
    protected void setUploadEnabled(boolean enabled)
    {
        super.setUploadEnabled(enabled);
        infoBoxResetListener.setEnabled(enabled);
    }
}
