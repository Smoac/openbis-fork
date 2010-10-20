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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AuthorizationGroupGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.RoleAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetBatchUpdatePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetUploadForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBatchRegistrationUpdatePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project.ProjectRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type.PropertyTypeRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBatchRegisterUpdatePanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log.LoggingConsole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Creates and provides GUI modules/components (such as sample browser).
 * <p>
 * Note that the returned object must be a {@link ITabItem} implementation.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
public final class ComponentProvider
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private IMainPanel mainTabPanelOrNull;

    public ComponentProvider(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    private String getMessage(String key)
    {
        return viewContext.getMessage(key);
    }

    private ITabItem createTab(String title, IDisposableComponent component)
    {
        return DefaultTabItem.create(title, component, viewContext);
    }

    // creates a tab which requires confirmation before it can be closed
    private ITabItem createRegistrationTab(final String title,
            DatabaseModificationAwareComponent component)
    {
        return DefaultTabItem.create(title, component, viewContext, true);
    }

    /**
     * Creates a tab with the specified component. The tab is unaware of database modifications and
     * will not be automatically refreshed if changes occur.
     */
    private ITabItem createSimpleTab(String title, Component component,
            boolean isCloseConfirmationNeeded)
    {
        return DefaultTabItem.createUnaware(title, component, isCloseConfirmationNeeded);
    }

    public AbstractTabItemFactory getSampleBrowser(final String initialGroupOrNull,
            final String initialSampleTypeOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser =
                            SampleBrowserGrid.create(viewContext, initialGroupOrNull,
                                    initialSampleTypeOrNull);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return SampleBrowserGrid.MAIN_BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {

                    return getMessage(Dict.SAMPLE_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createSampleBrowserLink(initialGroupOrNull,
                            initialSampleTypeOrNull);
                }
            };
    }

    public final AbstractTabItemFactory getSampleBrowser()
    {
        return getSampleBrowser(null, null);
    }

    public final AbstractTabItemFactory getMaterialBrowser()
    {
        return getMaterialBrowser(null);
    }

    public final AbstractTabItemFactory getMaterialBrowser(final String initialMaterialTypeOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser =
                            MaterialBrowserGrid.createWithTypeChooser(viewContext,
                                    initialMaterialTypeOrNull);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return MaterialBrowserGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.MATERIAL, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.MATERIAL_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createMaterialBrowserLink(initialMaterialTypeOrNull);
                }

            };
    }

    public final AbstractTabItemFactory getGroupBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = GroupGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return GroupGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.GROUP, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.GROUP_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getAuthorizationGroupBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = AuthorizationGroupGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return AuthorizationGroupGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.AUTHORIZATION_GROUPS,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.AUTHORIZATION_GROUP_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getRoleAssignmentBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = RoleAssignmentGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return RoleAssignmentGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.ROLES, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.ROLE_ASSIGNMENT_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getPersonBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = PersonGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PersonGrid.createBrowserId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.USERS, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PERSON_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getSampleRegistration(final ActionContext context)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            SampleRegistrationPanel.create(viewContext, context);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SampleRegistrationPanel.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getSampleRegistration()
    {
        return getSampleRegistration(new ActionContext());
    }

    public final AbstractTabItemFactory getExperimentRegistration(final ActionContext context)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            ExperimentRegistrationPanel.create(viewContext, context);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ExperimentRegistrationPanel.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                            HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.EXPERIMENT_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getExperimentRegistration()
    {
        return getExperimentRegistration(new ActionContext());
    }

    public final AbstractTabItemFactory getSampleBatchRegistration()
    {
        final boolean update = false;
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            SampleBatchRegisterUpdatePanel.create(viewContext, update);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SampleBatchRegisterUpdatePanel.getId(update);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.IMPORT);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_BATCH_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getSampleBatchUpdate()
    {
        final boolean update = true;

        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            SampleBatchRegisterUpdatePanel.create(viewContext, true);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SampleBatchRegisterUpdatePanel.getId(update);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE,
                            HelpPageAction.BATCH_UPDATE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_BATCH_UPDATE);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getDataSetBatchUpdate()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            DataSetBatchUpdatePanel.create(viewContext);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return DataSetBatchUpdatePanel.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.DATA_SET,
                            HelpPageAction.BATCH_UPDATE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DATA_SET_BATCH_UPDATE);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getMaterialBatchRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            MaterialBatchRegistrationUpdatePanel.create(viewContext, false);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return MaterialBatchRegistrationUpdatePanel.getId(false);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.MATERIAL, HelpPageAction.IMPORT);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.MATERIAL_IMPORT);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getMaterialBatchUpdate()
    {
        return new AbstractTabItemFactory()
        {
            @Override
            public ITabItem create()
            {
                DatabaseModificationAwareComponent component =
                        MaterialBatchRegistrationUpdatePanel.create(viewContext, true);
                return createRegistrationTab(getTabTitle(), component);
            }

            @Override
            public String getId()
            {
                return MaterialBatchRegistrationUpdatePanel.getId(true);
            }

            @Override
            public HelpPageIdentifier getHelpPageIdentifier()
            {
                return new HelpPageIdentifier(HelpPageDomain.MATERIAL, HelpPageAction.BATCH_UPDATE);
            }

            @Override
            public String getTabTitle()
            {
                return getMessage(Dict.MATERIAL_BATCH_UPDATE);
            }

            @Override
            public String tryGetLink()
            {
                return null;
            }

        };
    }

    public final AbstractTabItemFactory getVocabularyRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    Component component = new VocabularyRegistrationForm(viewContext);
                    return createSimpleTab(getTabTitle(), component, true);
                }

                @Override
                public String getId()
                {
                    return VocabularyRegistrationForm.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.VOCABULARY,
                            HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.VOCABULARY_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getProjectRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            ProjectRegistrationForm.create(viewContext);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ProjectRegistrationForm.createId();
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROJECT, HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROJECT_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getVocabularyBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = VocabularyGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return VocabularyGrid.GRID_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.VOCABULARY, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.VOCABULARY_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public final AbstractTabItemFactory getProjectBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = ProjectGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ProjectGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROJECT, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROJECT_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public AbstractTabItemFactory getExperimentBrowser(final String initialProjectOrNull,
            final String initialExperimentTypeOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser =
                            ExperimentBrowserGrid.create(viewContext, initialProjectOrNull,
                                    initialExperimentTypeOrNull);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return ExperimentBrowserGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.EXPERIMENT_BROWSER);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createExperimentBrowserLink(initialProjectOrNull,
                            initialExperimentTypeOrNull);
                }

            };
    }

    public AbstractTabItemFactory getExperimentBrowser()
    {
        return getExperimentBrowser(null, null);
    }

    public AbstractTabItemFactory getPropertyTypeBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = PropertyTypeGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PropertyTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROPERTY_TYPE,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROPERTY_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getPropertyTypeRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            PropertyTypeRegistrationForm.create(viewContext);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PropertyTypeRegistrationForm.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROPERTY_TYPE,
                            HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROPERTY_TYPE_REGISTRATION);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getPropertyTypeAssignmentBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = PropertyTypeAssignmentGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PropertyTypeAssignmentGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.ASSIGNMENT, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.PROPERTY_TYPE_ASSIGNMENTS);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getPropertyTypeExperimentTypeAssignmentForm()
    {
        return getPropertyTypeAssignmentForm(EntityKind.EXPERIMENT,
                Dict.ASSIGN_EXPERIMENT_PROPERTY_TYPE);
    }

    public AbstractTabItemFactory getPropertyTypeMaterialTypeAssignmentForm()
    {
        return getPropertyTypeAssignmentForm(EntityKind.MATERIAL,
                Dict.ASSIGN_MATERIAL_PROPERTY_TYPE);
    }

    public AbstractTabItemFactory getPropertyTypeDataSetTypeAssignmentForm()
    {
        return getPropertyTypeAssignmentForm(EntityKind.DATA_SET,
                Dict.ASSIGN_DATA_SET_PROPERTY_TYPE);
    }

    public AbstractTabItemFactory getPropertyTypeSampleTypeAssignmentForm()
    {
        return getPropertyTypeAssignmentForm(EntityKind.SAMPLE, Dict.ASSIGN_SAMPLE_PROPERTY_TYPE);
    }

    private AbstractTabItemFactory getPropertyTypeAssignmentForm(final EntityKind entityKind,
            final String tabTitleMessageKey)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            PropertyTypeAssignmentForm.create(viewContext, entityKind);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return PropertyTypeAssignmentForm.createId(entityKind);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.ASSIGNMENT,
                            HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(tabTitleMessageKey);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getDataSetSearch()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser = DataSetSearchHitGrid.create(viewContext);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return DataSetSearchHitGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.DATA_SET, HelpPageAction.SEARCH);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DATA_SET_SEARCH);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createSearchLink(EntityKind.DATA_SET);
                }
            };
    }

    public AbstractTabItemFactory getSampleSearch()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser = SampleSearchHitGrid.create(viewContext);
                    return createTab(getTabTitle(), browser);
                }

                @Override
                public String getId()
                {
                    return SampleSearchHitGrid.SEARCH_BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.SEARCH);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_SEARCH);
                }

                @Override
                public String tryGetLink()
                {
                    return LinkExtractor.createSearchLink(EntityKind.SAMPLE);
                }
            };
    }

    public AbstractTabItemFactory getSampleTypeBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = SampleTypeGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return SampleTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE_TYPE, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.SAMPLE_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

    public AbstractTabItemFactory getMaterialTypeBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = MaterialTypeGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return MaterialTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.MATERIAL_TYPE,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.MATERIAL_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getExperimentTypeBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = ExperimentTypeGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return ExperimentTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT_TYPE,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.EXPERIMENT_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getDataSetTypeBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = DataSetTypeGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return DataSetTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.DATA_SET_TYPE,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DATA_SET_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getDataSetUploadTab()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            DataSetUploadForm.create(viewContext);
                    return createRegistrationTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return DataSetUploadForm.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.DATA_SET, HelpPageAction.REGISTER);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.DATA_SET_UPLOAD);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public AbstractTabItemFactory getFileFormatTypeBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent component = FileFormatTypeGrid.create(viewContext);
                    return createTab(getTabTitle(), component);
                }

                @Override
                public String getId()
                {
                    return FileFormatTypeGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.FILE_TYPE, HelpPageAction.BROWSE);
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.FILE_FORMAT_TYPES);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    public IMainPanel tryGetMainTabPanel()
    {
        return mainTabPanelOrNull;
    }

    public void setMainPanel(IMainPanel mainTabPanel)
    {
        this.mainTabPanelOrNull = mainTabPanel;
    }

    public AbstractTabItemFactory getLoggingConsole()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    return createSimpleTab(getTabTitle(), LoggingConsole.create(viewContext), false);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    // null would be better
                    return new HelpPageIdentifier(HelpPageDomain.ADMINISTRATION,
                            HelpPageAction.BROWSE);
                }

                @Override
                public String getId()
                {
                    return LoggingConsole.ID;
                }

                @Override
                public String getTabTitle()
                {
                    return getMessage(Dict.LOGGING_CONSOLE);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }

            };
    }

}
