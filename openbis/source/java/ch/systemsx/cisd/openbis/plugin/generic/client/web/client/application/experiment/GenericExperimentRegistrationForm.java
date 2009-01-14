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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Field;

import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.shared.NewExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;

/**
 * The <i>generic</i> experiment registration form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentRegistrationForm
        extends
        AbstractGenericEntityRegistrationForm<ExperimentType, ExperimentTypePropertyType, ExperimentProperty>
{
    public static final String ID = createId(EntityKind.EXPERIMENT);

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final ExperimentType experimentType;

    private ProjectSelectionWidget projectSelectionWidget;

    public GenericExperimentRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final ExperimentType experimentType)
    {
        super(viewContext, experimentType.getExperimentTypePropertyTypes(), EntityKind.EXPERIMENT);
        this.viewContext = viewContext;
        this.experimentType = experimentType;
    }

    private final String createExpeimentIdentifier()
    {
        final Project project = projectSelectionWidget.tryGetSelectedProject();
        final String code = codeField.getValue();
        final String result = project.getIdentifier() + "/" + code;
        return result.toUpperCase();
    }

    @Override
    public final void submitValidForm()
    {
        final NewExperiment newExp =
                new NewExperiment(createExpeimentIdentifier(), experimentType.getCode());
        final List<ExperimentProperty> properties = extractProperties();
        newExp.setProperties(properties.toArray(ExperimentProperty.EMPTY_ARRAY));
        viewContext.getService().registerExperiment(newExp,
                new RegisterExperimentCallback(viewContext));
    }

    public final class RegisterExperimentCallback extends AbstractAsyncCallback<Void>
    {

        RegisterExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo()
        {
            return "Experiment <b>" + createExpeimentIdentifier() + "</b> successfully registered";
        }

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            formPanel.reset();
        }
    }

    @Override
    protected ExperimentProperty createEntityProperty()
    {
        return new ExperimentProperty();
    }

    @Override
    protected void createEntitySpecificFields()
    {
        projectSelectionWidget = new ProjectSelectionWidget(viewContext, getId());
        projectSelectionWidget.setLabelSeparator(GenericConstants.MANDATORY_LABEL_SEPARATOR);
        projectSelectionWidget.setFieldLabel(viewContext.getMessage(Dict.PROJECT));
        projectSelectionWidget.setAllowBlank(false);
    }

    @Override
    protected List<Field<?>> getEntitySpecificFields()
    {
        final ArrayList<Field<?>> fields = new ArrayList<Field<?>>();
        fields.add(projectSelectionWidget);
        return fields;
    }
}
