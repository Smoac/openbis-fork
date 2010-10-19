/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractMaterialBatchRegistrationForm extends AbstractRegistrationForm
{
    private static final String FIELD_LABEL_TEMPLATE = "File";

    private static final int NUMBER_OF_FIELDS = 1;

    private static String createId(String sessionKey)
    {
        return GenericConstants.ID_PREFIX + sessionKey;
    }
    
    protected final MaterialType materialType;
    
    protected final IViewContext<IGenericClientServiceAsync> viewContext;
    
    private final BasicFileFieldManager fileFieldsManager;

    private final BatchOperationKind batchOperationKind;

    AbstractMaterialBatchRegistrationForm(IViewContext<IGenericClientServiceAsync> viewContext,
            String sessionKey, BatchOperationKind batchOperationKind, MaterialType materialType)
    {
        super(viewContext.getCommonViewContext(), createId(sessionKey));
        this.viewContext = viewContext;
        this.batchOperationKind = batchOperationKind;
        this.materialType = materialType;
        setScrollMode(Scroll.AUTO);
        fileFieldsManager =
                new BasicFileFieldManager(sessionKey, NUMBER_OF_FIELDS, FIELD_LABEL_TEMPLATE);
        fileFieldsManager.setMandatory();
        addUploadFeatures(sessionKey);
    }
    
    /**
     * Adds additional fields to the form panel. File upload field will be added automatically after
     * specific fields.
     */
    protected void addSpecificFormFields(FormPanel form)
    {
    }
    
    /**
     * Perform registration on the service
     */
    protected abstract void save();


    @Override
    protected void submitValidForm()
    {
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }
    
    private final void addFormFields()
    {
        addSpecificFormFields(formPanel);
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
                    AbstractMaterialBatchRegistrationForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    private LabelField createTemplateField()
    {
        LabelField result =
                new LabelField(LinkRenderer.renderAsLink(viewContext
                        .getMessage(Dict.FILE_TEMPLATE_LABEL)));
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    WindowUtils.openWindow(UrlParamsHelper.createTemplateURL(EntityKind.MATERIAL,
                            materialType, false, false, batchOperationKind));
                }
            });
        return result;
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

    protected final class RegisterMaterialsCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<List<BatchRegistrationResult>>
    {
        RegisterMaterialsCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
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

}
