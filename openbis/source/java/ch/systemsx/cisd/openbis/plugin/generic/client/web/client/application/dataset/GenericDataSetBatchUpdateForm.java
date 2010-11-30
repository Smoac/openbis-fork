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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> data set batch update panel.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericDataSetBatchUpdateForm extends AbstractRegistrationForm
{
    private static final String PREFIX = "data-set-batch-update";

    public final static String ID = GenericConstants.ID_PREFIX + PREFIX;

    private static final String SESSION_KEY = PREFIX;

    private static final String FIELD_LABEL_TEMPLATE = "File";

    private static final int DEFAULT_NUMBER_OF_FILES = 1;

    private final BasicFileFieldManager fileFieldsManager;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final DataSetType dataSetType;

    public GenericDataSetBatchUpdateForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final DataSetType dataSetType)
    {
        super(viewContext.getCommonViewContext(), ID);
        this.viewContext = viewContext;
        this.dataSetType = dataSetType;
        fileFieldsManager =
                new BasicFileFieldManager(SESSION_KEY, DEFAULT_NUMBER_OF_FILES,
                        FIELD_LABEL_TEMPLATE);
        fileFieldsManager.setMandatory();
        setScrollMode(Scroll.AUTO);
        addUploadFeatures(SESSION_KEY);
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
                    GenericDataSetBatchUpdateForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    protected void save()
    {
        viewContext.getService().updateDataSets(dataSetType, SESSION_KEY,
                new UpdateDataSetsCallback(viewContext));
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

    private final class UpdateDataSetsCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<List<BatchRegistrationResult>>
    {
        UpdateDataSetsCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
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
        result.addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    WindowUtils.openWindow(UrlParamsHelper.createTemplateURL(EntityKind.DATA_SET,
                            dataSetType, false, true, BatchOperationKind.UPDATE));
                }
            });
        return result;
    }

}
