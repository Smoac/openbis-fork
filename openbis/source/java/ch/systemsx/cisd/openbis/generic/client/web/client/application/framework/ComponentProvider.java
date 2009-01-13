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

import com.extjs.gxt.ui.client.widget.Component;

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
    private final CommonViewContext viewContext;

    ComponentProvider(final CommonViewContext viewContext)
    {
        this.viewContext = viewContext;
    }

    private String getMessage(String key)
    {
        return viewContext.getMessage(key);
    }

    public final ITabItemFactory getSampleBrowser()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    SampleBrowser sampleBrowser = new SampleBrowser(viewContext);
                    return new DefaultTabItem(getMessage(Dict.SAMPLE_BROWSER), sampleBrowser,
                            sampleBrowser);
                }

                public String getId()
                {
                    return SampleBrowser.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return false;
                }
            };
    }

    public final ITabItemFactory getDummyComponent()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    return new DefaultTabItem("Not implemented feature", new DummyComponent());
                }

                public String getId()
                {
                    return DummyComponent.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return false;
                }
            };
    }

    public final ITabItemFactory getGroupsView()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    return new ContentPanelAdapter(new GroupsView(viewContext));
                }

                public String getId()
                {
                    return GroupsView.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return false;
                }
            };
    }

    public final ITabItemFactory getRolesView()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    return new ContentPanelAdapter(new RolesView(viewContext));
                }

                public String getId()
                {
                    return RolesView.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return false;
                }
            };
    }

    public final ITabItemFactory getPersonsView()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    return new ContentPanelAdapter(new PersonsView(viewContext));
                }

                public String getId()
                {
                    return PersonsView.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return false;
                }
            };
    }

    public final ITabItemFactory getSampleRegistration()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    Component component = new SampleRegistrationPanel(viewContext);
                    return new DefaultTabItem(getMessage(Dict.SAMPLE_REGISTRATION), component);
                }

                public String getId()
                {
                    return SampleRegistrationPanel.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return true;
                }
            };
    }

    public final ITabItemFactory getSampleBatchRegistration()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    Component component = new SampleBatchRegistrationPanel(viewContext);
                    return new DefaultTabItem(getMessage(Dict.SAMPLE_BATCH_REGISTRATION), component);
                }

                public String getId()
                {
                    return SampleBatchRegistrationPanel.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return true;
                }
            };
    }

    public final ITabItemFactory getVocabularyRegistration()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    Component component = new VocabularyRegistrationForm(viewContext);
                    return new DefaultTabItem(getMessage(Dict.VOCABULARY_REGISTRATION), component);
                }

                public String getId()
                {
                    return VocabularyRegistrationForm.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return true;
                }
            };
    }

    public ITabItemFactory getExperimentBrowser()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    ExperimentBrowser experimentBrowser = new ExperimentBrowser(viewContext);
                    return new DefaultTabItem(getMessage(Dict.EXPERIMENT_BROWSER),
                            experimentBrowser, experimentBrowser);
                }

                public String getId()
                {
                    return ExperimentBrowser.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return false;
                }
            };
    }

    public ITabItemFactory getPropertyTypeBrowser()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    Component component = new PropertyTypeBrowser(viewContext);
                    return new DefaultTabItem(getMessage(Dict.PROPERTY_TYPES), component);
                }

                public String getId()
                {
                    return PropertyTypeBrowser.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return false;
                }
            };
    }

    public ITabItemFactory getPropertyTypeRegistration()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    Component component = new PropertyTypeRegistrationForm(viewContext);
                    return new DefaultTabItem(getMessage(Dict.PROPERTY_TYPE_REGISTRATION),
                            component);
                }

                public String getId()
                {
                    return PropertyTypeRegistrationForm.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return true;
                }
            };
    }

    public ITabItemFactory getPropertyTypeAssignmentBrowser()
    {
        return new ITabItemFactory()
            {
                public ITabItem create()
                {
                    Component component = new PropertyTypeAssignmentBrowser(viewContext);
                    return new DefaultTabItem(getMessage(Dict.PROPERTY_TYPE_ASSIGNMENTS), component);
                }

                public String getId()
                {
                    return PropertyTypeAssignmentBrowser.ID;
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return false;
                }
            };
    }

    public ITabItemFactory getPropertyTypeExperimentTypeAssignmentForm()
    {
        return new ITabItemFactory()
            {
                EntityKind entityKind = EntityKind.EXPERIMENT;

                public ITabItem create()
                {
                    Component component = new PropertyTypeAssignmentForm(viewContext, entityKind);
                    return new DefaultTabItem(getMessage(Dict.ASSIGN_EXPERIMENT_PROPERTY_TYPE),
                            component);
                }

                public String getId()
                {
                    return PropertyTypeAssignmentForm.createId(entityKind);
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return true;
                }
            };
    }

    public ITabItemFactory getPropertyTypeSampleTypeAssignmentForm()
    {
        return new ITabItemFactory()
            {
                EntityKind entityKind = EntityKind.SAMPLE;

                public ITabItem create()
                {
                    Component component = new PropertyTypeAssignmentForm(viewContext, entityKind);
                    return new DefaultTabItem(getMessage(Dict.ASSIGN_SAMPLE_PROPERTY_TYPE),
                            component);
                }

                public String getId()
                {
                    return PropertyTypeAssignmentForm.createId(entityKind);
                }

                public boolean isCloseConfirmationNeeded()
                {
                    return true;
                }
            };
    }
}