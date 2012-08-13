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
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;

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
        setResetButtonVisible(true);
        this.sessionKey = id + "-" + customImport.getCode();
        this.customImport = customImport;
        this.viewContext = viewContext;

        Field<?> templateField = createTemplateField();
        if (templateField != null)
        {
            formPanel.add(templateField);
        }

        fileFieldsManager =
                new BasicFileFieldManager(sessionKey, NUMBER_OF_FIELDS, FIELD_LABEL_TEMPLATE);
        fileFieldsManager.setMandatory();
        for (FileUploadField field : fileFieldsManager.getFields())
        {
            formPanel.add(wrapUnaware((Field<?>) field).get());
        }
        addUploadFeatures(sessionKey);

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
                    CustomImportForm.this.viewContext.getCommonService().performCustomImport(
                            sessionKey,
                            CustomImportForm.this.customImport.getCode(),
                            new AbstractRegistrationCallback<String>(
                                    CustomImportForm.this.viewContext)
                                {
                                    @Override
                                    protected String createSuccessfullRegistrationInfo(String result)
                                    {
                                        return result
                                                + " succesfully uploaded to the datastore server.";
                                    }
                                });
                    for (FileUploadField field : fileFieldsManager.getFields())
                    {
                        field.clear();
                    }
                    setUploadEnabled();
                }
            });
    }

    private LabelField createTemplateField()
    {
        final String templateEntityKind =
                customImport.getProperty(CustomImport.PropertyNames.TEMPLATE_ENTITY_KIND.getName());
        final String templateEntityPermId =
                customImport.getProperty(CustomImport.PropertyNames.TEMPLATE_ENTITY_PERMID
                        .getName());
        final String templateAttachmentName =
                customImport.getProperty(CustomImport.PropertyNames.TEMPLATE_ATTACHMENT_NAME
                        .getName());

        if (templateEntityKind == null || AttachmentHolderKind.valueOf(templateEntityKind) == null
                || templateEntityPermId == null || templateAttachmentName == null)
        {
            return null;
        }

        LabelField result =
                new LabelField(LinkRenderer.renderAsLink(viewContext
                        .getMessage(Dict.FILE_TEMPLATE_LABEL)));
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    AttachmentHolderKind attachmentHolderKind =
                            AttachmentHolderKind.valueOf(templateEntityKind);

                    if (AttachmentHolderKind.PROJECT.equals(attachmentHolderKind))
                    {
                        Window.alert("TODO: implement me !!!");
                    } else
                    {
                        WindowUtils.openWindow(PermlinkUtilities.createAttachmentPermlinkURL(
                                GWTUtils.getBaseIndexURL(), templateAttachmentName, null,
                                attachmentHolderKind, templateEntityPermId));
                    }
                }
            });
        return result;
    }

    @Override
    protected void submitValidForm()
    {
        CustomImportForm.this.setUploadEnabled(false);
        formPanel.submit();
    }

}
