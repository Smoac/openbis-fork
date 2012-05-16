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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;

/**
 * @author Pawel Glyzewski
 */
public class CustomImportForm extends AbstractRegistrationForm
{
    private static final String FIELD_LABEL_TEMPLATE = "File";

    private static final int NUMBER_OF_FIELDS = 1;

    private final String sessionKey;

    private final String customImportCode;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final BasicFileFieldManager fileFieldsManager;

    /**
     * @param viewContext
     * @param id
     */
    public CustomImportForm(IViewContext<ICommonClientServiceAsync> viewContext, String id,
            String customImportCode)
    {
        super(viewContext, id);

        this.sessionKey = id + "-" + customImportCode;
        this.customImportCode = customImportCode;
        this.viewContext = viewContext;

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
                            CustomImportForm.this.customImportCode,
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

    @Override
    protected void submitValidForm()
    {
        CustomImportForm.this.setUploadEnabled(false);
        formPanel.submit();
    }
}
