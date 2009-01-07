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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GroupsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.PersonsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.RolesView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBatchRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;

/**
 * Creates and provides GUI modules/components (such as sample browser).
 * <p>
 * Note that the returned object must be a {@link ITabItem} implementation.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
final class ComponentProvider
{
    private final SampleBrowser sampleBrowser;

    private final DummyComponent dummyComponent;

    private final GroupsView groupsView;

    private final RolesView rolesView;

    private final PersonsView personsView;

    private final SampleRegistrationPanel sampleRegistration;

    private final SampleBatchRegistrationPanel sampleBatchRegistration;

    private final ExperimentBrowser experimentBrowser;

    private final PropertyTypeBrowser propertyTypesBrowser;

    private final PropertyTypeRegistrationForm propertyTypeRegistration;

    private final PropertyTypeAssignmentBrowser propertyTypeAssignmentBrowser;

    private PropertyTypeAssignmentForm propertyTypeExperimentTypeAssignmentForm;

    private PropertyTypeAssignmentForm propertyTypeSampleTypeAssignmentForm;

    private VocabularyRegistrationForm vocabularyRegistrationForm;

    private final CommonViewContext viewContext;

    ComponentProvider(final CommonViewContext viewContext)
    {
        this.viewContext = viewContext;
        sampleBrowser = new SampleBrowser(viewContext);
        dummyComponent = new DummyComponent();
        groupsView = new GroupsView(viewContext);
        rolesView = new RolesView(viewContext);
        personsView = new PersonsView(viewContext);
        sampleRegistration = new SampleRegistrationPanel(viewContext);
        sampleBatchRegistration = new SampleBatchRegistrationPanel(viewContext);
        experimentBrowser = new ExperimentBrowser(viewContext);
        propertyTypesBrowser = new PropertyTypeBrowser(viewContext);
        propertyTypeRegistration = new PropertyTypeRegistrationForm(viewContext);
        propertyTypeAssignmentBrowser = new PropertyTypeAssignmentBrowser(viewContext);
        propertyTypeExperimentTypeAssignmentForm =
                new PropertyTypeAssignmentForm(viewContext, EntityKind.EXPERIMENT);
        propertyTypeSampleTypeAssignmentForm =
                new PropertyTypeAssignmentForm(viewContext, EntityKind.SAMPLE);
        vocabularyRegistrationForm = new VocabularyRegistrationForm(viewContext);
    }

    public final ITabItem getSampleBrowser()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.SAMPLE_BROWSER), sampleBrowser,
                sampleBrowser);
    }

    public final ITabItem getDummyComponent()
    {
        return new DefaultTabItem("Not implemented feature", dummyComponent);
    }

    public final ITabItem getGroupsView()
    {
        return new ContentPanelAdapter(groupsView);
    }

    public final ITabItem getRolesView()
    {
        return new ContentPanelAdapter(rolesView);
    }

    public final ITabItem getPersonsView()
    {
        return new ContentPanelAdapter(personsView);
    }

    public final ITabItem getSampleRegistration()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.SAMPLE_REGISTRATION),
                sampleRegistration);
    }

    public final ITabItem getSampleBatchRegistration()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.SAMPLE_BATCH_REGISTRATION),
                sampleBatchRegistration);
    }

    public final ITabItem getVocabularyRegistration()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.VOCABULARY_REGISTRATION),
                vocabularyRegistrationForm);
    }

    public ITabItem getExperimentBrowser()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.EXPERIMENT_BROWSER),
                experimentBrowser, experimentBrowser);
    }

    public ITabItem getPropertyTypeBrowser()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.PROPERTY_TYPES), propertyTypesBrowser);
    }

    public ITabItem getPropertyTypeRegistration()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.PROPERTY_TYPE_REGISTRATION),
                propertyTypeRegistration);
    }

    public ITabItem getPropertyTypeAssignmentBrowser()
    {
        return new ViewerTabItem(viewContext.getMessage(Dict.PROPERTY_TYPE_ASSIGNMENTS),
                propertyTypeAssignmentBrowser);
    }

    public ITabItem getPropertyTypeExperimentTypeAssignmentForm()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.ASSIGN_EXPERIMENT_PROPERTY_TYPE),
                propertyTypeExperimentTypeAssignmentForm);
    }

    public ITabItem getPropertyTypeSampleTypeAssignmentForm()
    {
        return new DefaultTabItem(viewContext.getMessage(Dict.ASSIGN_SAMPLE_PROPERTY_TYPE),
                propertyTypeSampleTypeAssignmentForm);
    }
}