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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> sample registration form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleRegistrationForm extends AbstractGenericSampleRegisterEditForm
{
    private static final IIdentifiable REGISTRATION_IDENTIFIER = null;

    // For tests only
    public static final String ID = createId(REGISTRATION_IDENTIFIER, EntityKind.SAMPLE);

    private final SampleType sampleType;

    public GenericSampleRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext, final SampleType sampleType)
    {
        super(viewContext);
        this.sampleType = sampleType;
    }

    @Override
    protected void resetFieldsAfterSave()
    {
        codeField.reset();
        attachmentsManager.resetAttachmentFieldSetsInPanel(formPanel);
    }

    public final class RegisterSampleCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        public RegisterSampleCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Void result)
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
    protected void initializeFormFields()
    {
        propertiesEditor.initWithoutProperties(sampleType.getAssignedPropertyTypes());
        experimentField.getField().setVisible(false);
    }

    @Override
    protected void loadForm()
    {
        initGUI();
    }

    @Override
    protected void save()
    {
        final NewSample newSample =
                new NewSample(createSampleIdentifier(), sampleType, StringUtils.trimToNull(parent
                        .getValue()), StringUtils.trimToNull(container.getValue()));
        final List<SampleProperty> properties = extractProperties();
        newSample.setProperties(properties.toArray(SampleProperty.EMPTY_ARRAY));
        newSample.setAttachments(attachmentsManager.extractAttachments());
        viewContext.getService().registerSample(attachmentsSessionKey, newSample,
                new RegisterSampleCallback(viewContext));
    }

}
