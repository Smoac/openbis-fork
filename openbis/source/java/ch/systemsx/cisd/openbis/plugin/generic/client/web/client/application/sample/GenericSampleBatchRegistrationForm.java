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

import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.UrlParamsHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample batch registration panel.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleBatchRegistrationForm extends AbstractSampleBatchRegistrationForm
{

    private static final String SESSION_KEY = "sample-batch-registration";

    private final SampleType sampleType;

    private final CheckBoxField generateCodesCheckbox;

    private final GroupSelectionWidget groupSelector;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    public GenericSampleBatchRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        super(viewContext.getCommonViewContext(), SESSION_KEY);
        this.viewContext = viewContext;
        this.sampleType = sampleType;
        generateCodesCheckbox = new CheckBoxField("Generate codes automatically", false);
        groupSelector =
                createGroupField(viewContext.getCommonViewContext(), "" + getId(), true,
                        generateCodesCheckbox);
        generateCodesCheckbox.addListener(Events.Change, new Listener<FieldEvent>()
            {
                public void handleEvent(FieldEvent be)
                {
                    boolean selected = (Boolean) be.getValue();
                    FieldUtil.setVisibility(selected, groupSelector);
                }
            });
    }

    @Override
    protected void save()
    {
        String defaultGroupIdentifier = null;
        Space selectedGroup = groupSelector.tryGetSelectedGroup();
        if (generateCodesCheckbox.getValue() && selectedGroup != null)
        {
            defaultGroupIdentifier = selectedGroup.getIdentifier();
        }
        viewContext.getService().registerSamples(sampleType, getSessionKey(),
                defaultGroupIdentifier, new RegisterSamplesCallback(viewContext));
    }

    @Override
    protected void addSpecificFormFields(FormPanel form)
    {
        form.add(generateCodesCheckbox);
        form.add(groupSelector);
        form.add(createTemplateField());
    }

    @Override
    protected void resetPanel()
    {
        super.resetPanel();
        groupSelector.setVisible(false);
        groupSelector.setEnabled(false);
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
                    WindowUtils.openWindow(UrlParamsHelper.createTemplateURL(EntityKind.SAMPLE,
                            sampleType, generateCodesCheckbox.getValue(), true,
                            BatchOperationKind.REGISTRATION));
                }
            });
        return result;
    }

    private final GroupSelectionWidget createGroupField(
            IViewContext<ICommonClientServiceAsync> context, String idSuffix, boolean addShared,
            final CheckBoxField checkbox)
    {
        GroupSelectionWidget field = new GroupSelectionWidget(context, idSuffix, addShared, false)
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
        field.setFieldLabel(viewContext.getMessage(Dict.DEFAULT_GROUP));
        field.setVisible(false);
        return field;
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
                builder.append("<b>" + batchRegistrationResult.getFileName() + "</b>: ");
                builder.append(batchRegistrationResult.getMessage());
                builder.append("<br />");
            }
            return builder.toString();
        }

    }

}
