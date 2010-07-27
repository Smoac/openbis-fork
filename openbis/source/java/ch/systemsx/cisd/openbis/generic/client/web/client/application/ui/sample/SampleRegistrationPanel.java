/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.EntityRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * The {@link EntityRegistrationPanel} extension for registering a sample.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleRegistrationPanel extends
        EntityRegistrationPanel<SampleTypeModel, SampleTypeSelectionWidget>
{

    public static final String ID = EntityRegistrationPanel.createId(EntityKind.SAMPLE);

    public static final DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext, final ActionContext context)
    {
        SampleRegistrationPanel panel = new SampleRegistrationPanel(viewContext, context);
        return new DatabaseModificationAwareComponent(panel, panel);
    }

    private SampleRegistrationPanel(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ActionContext context)
    {
        super(viewContext, EntityKind.SAMPLE, new SampleTypeSelectionWidget(viewContext,
                EntityRegistrationPanel.createId(EntityKind.SAMPLE), false,
                SampleTypeDisplayID.SAMPLE_REGISTRATION, context.tryGetSampleTypeCode()), context);
    }

}
