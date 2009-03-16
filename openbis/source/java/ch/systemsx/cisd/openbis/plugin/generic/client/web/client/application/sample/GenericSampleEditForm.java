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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> sample edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleEditForm extends
        AbstractGenericEntityEditForm<SampleType, SampleTypePropertyType, SampleProperty>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public GenericSampleEditForm(IViewContext<ICommonClientServiceAsync> viewContext,
            IEditableEntity<SampleType, SampleTypePropertyType, SampleProperty> entity,
            boolean editMode)
    {
        super(viewContext, entity, editMode);
        this.viewContext = viewContext;
    }

    public static final String ID_PREFIX = createId(EntityKind.SAMPLE, "");

    @Override
    public final void submitValidForm()
    {
        final List<SampleProperty> properties = extractProperties();
        viewContext.getService().updateSample(entity.getIdentifier(), properties,
                new RegisterSampleCallback(viewContext));
    }

    public final class RegisterSampleCallback extends AbstractAsyncCallback<Void>
    {

        RegisterSampleCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo()
        {
            return "Sample successfully updated";
        }

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            showPropertyGrid();
        }
    }

    @Override
    protected PropertiesEditor<SampleType, SampleTypePropertyType, SampleProperty> createPropertiesEditor(
            List<SampleTypePropertyType> entityTypesPropertyTypes, List<SampleProperty> properties,
            String id)
    {
        return new SamplePropertyEditor<SampleType, SampleTypePropertyType, SampleProperty>(
                entityTypesPropertyTypes, properties, id);
    }

}
