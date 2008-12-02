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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GroupsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.PersonsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.RolesView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SampleBatchRegistrationMock;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SampleRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment_browser.ExperimentBrowser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleBrowser;

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

    private final SampleBatchRegistrationMock sampleBatchRegistration;

    private final ExperimentBrowser experimentBrowser;

    ComponentProvider(final CommonViewContext viewContext)
    {
        sampleBrowser = new SampleBrowser(viewContext);
        dummyComponent = new DummyComponent();
        groupsView = new GroupsView(viewContext);
        rolesView = new RolesView(viewContext);
        personsView = new PersonsView(viewContext);
        sampleRegistration = new SampleRegistrationPanel(viewContext);
        sampleBatchRegistration = new SampleBatchRegistrationMock(viewContext);
        experimentBrowser = new ExperimentBrowser(viewContext);
    }

    public final ITabItem getSampleBrowser()
    {
        return new DefaultTabItem("Sample browser", sampleBrowser, sampleBrowser);
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
        return new ContentPanelAdapter(sampleRegistration);
    }

    public ITabItem getSampleBatchRegistration()
    {
        return new ContentPanelAdapter(sampleBatchRegistration);
    }

    public ITabItem getExperimentBrowser()
    {
        return new DefaultTabItem("Experiment browser", experimentBrowser, experimentBrowser);
    }
}