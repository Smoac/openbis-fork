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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GroupsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.PersonsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.RolesView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SampleBatchRegistrationMock;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SampleRegistrationMock;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleBrowser;

/**
 * Creates and provides GUI modules/components (such as sample browser).
 * 
 * @author Izabela Adamczyk
 */
// TODO 2008-11-12, Christian Ribeaud: Each component should be a ITabItem.
final class ComponentProvider
{
    private final SampleBrowser sampleBrowser;

    private final DummyComponent dummyComponent;

    private final GroupsView groupsView;

    private final RolesView rolesView;

    private final PersonsView personsView;

    private final SampleRegistrationMock sampleRegistration;

    private final SampleBatchRegistrationMock sampleBatchRegistration;

    ComponentProvider(final GenericViewContext viewContext)
    {
        sampleBrowser = new SampleBrowser(viewContext);
        dummyComponent = new DummyComponent();
        groupsView = new GroupsView(viewContext);
        rolesView = new RolesView(viewContext);
        personsView = new PersonsView(viewContext);
        sampleRegistration = new SampleRegistrationMock(viewContext);
        sampleBatchRegistration = new SampleBatchRegistrationMock(viewContext);
    }

    public SampleBrowser getSampleBrowser()
    {
        return sampleBrowser;
    }

    public DummyComponent getDummyComponent()
    {
        return dummyComponent;
    }

    public GroupsView getGroupsView()
    {
        return groupsView;
    }

    public RolesView getRolesView()
    {
        return rolesView;
    }

    public PersonsView getPersonsView()
    {
        return personsView;
    }

    public SampleRegistrationMock getSampleRegistration()
    {
        return sampleRegistration;
    }

    public SampleBatchRegistrationMock getSampleBatchRegistration()
    {
        return sampleBatchRegistration;
    }
}