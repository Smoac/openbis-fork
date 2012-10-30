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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * @author Pawel Glyzewski
 */
public class CustomImportForm extends AbstractRegistrationForm
{
    private static final String FIELD_LABEL_TEMPLATE = "File";

    private static final int NUMBER_OF_FIELDS = 1;

    private final String sessionKey;

    private final CustomImport customImport;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final BasicFileFieldManager fileFieldsManager;

    /**
     * @param viewContext
     * @param id
     */
    public CustomImportForm(IViewContext<ICommonClientServiceAsync> viewContext, String id,
            CustomImport customImport)
    {
        super(viewContext, id);

        saveButton.setText(messageProvider.getMessage(Dict.BUTTON_UPLOAD));
        setResetButtonVisible(true);
        setDirtyCheckEnabled(false);

        this.sessionKey = id + "-" + customImport.getCode();
        this.customImport = customImport;
        this.viewContext = viewContext;

        if (isTemplateAvailable())
        {
            MultiField<Object> multifield =
                    new MultiField<Object>("", createTemplateField(),
                            createEntityWithTemplateField());
            multifield.setLabelSeparator("");
            formPanel.add(multifield);
        }

        fileFieldsManager =
                new BasicFileFieldManager(sessionKey, NUMBER_OF_FIELDS, FIELD_LABEL_TEMPLATE);
        fileFieldsManager.setMandatory();
        for (FileUploadField field : fileFieldsManager.getFields())
        {
            formPanel.add(wrapUnaware((Field<?>) field).get());
        }
        addUploadFeatures(sessionKey);

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
                protected void setUploadEnabled()
                {
                    CustomImportForm.this.setUploadEnabled(true);
                }

                @Override
                protected void onSuccessfullUpload()
                {
                    infoBox.displayProgress(messageProvider.getMessage(Dict.PROGRESS_PROCESSING));

                    CustomImportForm.this.viewContext.getCommonService().performCustomImport(
                            sessionKey,
                            CustomImportForm.this.customImport.getCode(),
                            new AbstractRegistrationCallback<String>(
                                    CustomImportForm.this.viewContext)
                                {
                                    @Override
                                    protected void process(String result)
                                    {
                                        super.process(result);
                                        resetPanel();
                                    }

                                    @Override
                                    protected String createSuccessfullRegistrationInfo(String result)
                                    {
                                        return result
                                                + " succesfully uploaded to the datastore server.";
                                    }

                                    @Override
                                    public void finishOnFailure(Throwable caught)
                                    {
                                        super.finishOnFailure(caught);
                                        resetPanel();
                                    }

                                });

                }
            });
    }

    @Override
    protected void submitValidForm()
    {
        CustomImportForm.this.setUploadEnabled(false);
        formPanel.submit();
    }

    private boolean isTemplateAvailable()
    {
        return getTemplateEntityKind() != null && getTemplateEntityPermId() != null
                && getTemplateAttachmentName() != null;
    }

    private Field<?> createTemplateField()
    {

        LabelField linkToTemplate =
                new LabelField(LinkRenderer.renderAsLink(viewContext
                        .getMessage(Dict.FILE_TEMPLATE_LABEL)));
        linkToTemplate.sinkEvents(Event.ONCLICK);
        linkToTemplate.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    onLinkToTemplateClick();
                }
            });
        return linkToTemplate;
    }

    private Field<?> createEntityWithTemplateField()
    {
        LabelField linkToEntityWithTemplate =
                new LabelField(LinkRenderer.renderAsLink(viewContext
                        .getMessage(Dict.ENTITY_WITH_FILE_TEMPLATE_LABEL)));
        linkToEntityWithTemplate.sinkEvents(Event.ONCLICK);
        linkToEntityWithTemplate.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    onLinkToEntityWithTemplateClick();
                }
            });

        linkToEntityWithTemplate.addStyleName("entityWithTemplateField");
        return linkToEntityWithTemplate;
    }

    private void onLinkToTemplateClick()
    {

        if (AttachmentHolderKind.PROJECT.equals(getTemplateEntityKind()))
        {
            viewContext.getService().getProjectInfoByPermId(getTemplateEntityPermId(),
                    new LinkToProjectTemplateCallback(viewContext));
        } else
        {
            openUrl(PermlinkUtilities.createAttachmentPermlinkURL(GWTUtils.getBaseIndexURL(),
                    getTemplateAttachmentName(), null, getTemplateEntityKind(),
                    getTemplateEntityPermId()));
        }
    }

    private void onLinkToEntityWithTemplateClick()
    {
        if (AttachmentHolderKind.PROJECT.equals(getTemplateEntityKind()))
        {
            viewContext.getService().getProjectInfoByPermId(getTemplateEntityPermId(),
                    new LinkToEntityWithProjectTemplateCallback(viewContext));
        } else
        {
            openUrl(PermlinkUtilities.createPermlinkURL(GWTUtils.getBaseIndexURL(),
                    EntityKind.valueOf(getTemplateEntityKind().name()), getTemplateEntityPermId()));
        }
    }

    private class LinkToProjectTemplateCallback extends AbstractAsyncCallback<Project>
    {

        public LinkToProjectTemplateCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Project project)
        {
            openUrl(PermlinkUtilities.createProjectAttachmentPermlinkURL(
                    GWTUtils.getBaseIndexURL(), getTemplateAttachmentName(), null,
                    project.getCode(), project.getSpace().getCode()));
        }
    }

    private class LinkToEntityWithProjectTemplateCallback extends AbstractAsyncCallback<Project>
    {

        public LinkToEntityWithProjectTemplateCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(Project project)
        {
            openUrl(PermlinkUtilities.createProjectPermlinkURL(GWTUtils.getBaseIndexURL(),
                    project.getCode(), project.getSpace().getCode()));
        }
    }

    private AttachmentHolderKind getTemplateEntityKind()
    {
        String str =
                customImport.getProperty(CustomImport.PropertyNames.TEMPLATE_ENTITY_KIND.getName());
        if (str == null)
        {
            return null;
        } else
        {
            try
            {
                return AttachmentHolderKind.valueOf(str);
            } catch (IllegalArgumentException e)
            {
                return null;
            }
        }
    }

    private String getTemplateEntityPermId()
    {
        return customImport
                .getProperty(CustomImport.PropertyNames.TEMPLATE_ENTITY_PERMID.getName());
    }

    private String getTemplateAttachmentName()
    {
        return customImport.getProperty(CustomImport.PropertyNames.TEMPLATE_ATTACHMENT_NAME
                .getName());
    }

    @Override
    protected void setUploadEnabled(boolean enabled)
    {
        super.setUploadEnabled(enabled);
        infoBoxResetListener.setEnabled(enabled);
    }

    private void openUrl(String url)
    {
        Window.open(url, "_blank", "");
    }

}
