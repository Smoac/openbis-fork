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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AuthorizationGroupGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.RoleAssignmentGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetUploadForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentRegistrationPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBatchRegistrationPanel;
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

    private MainTabPanel mainTabPanelOrNull;

    public ComponentProvider(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    private String getMessage(String key)
    {
        return viewContext.getMessage(key);
    }

    private ITabItem createTab(String dictionaryMsgKey, IDisposableComponent component)
    {
        String title = getMessage(dictionaryMsgKey);
        return DefaultTabItem.create(title, component, viewContext);
    }

    // creates a tab which requires confirmation before it can be closed
    private ITabItem createRegistrationTab(final String titleMessageKey,
            DatabaseModificationAwareComponent component)
    {
        return DefaultTabItem.create(getMessage(titleMessageKey), component, viewContext, true);
    }

    /**
     * Creates a tab with the specified component. The tab is unaware of database modifications and
     * will not be automatically refreshed if changes occur.
     */
    private ITabItem createSimpleTab(String dictionaryMsgKey, Component component,
            boolean isCloseConfirmationNeeded)
    {
        return DefaultTabItem.createUnaware(getMessage(dictionaryMsgKey), component,
                isCloseConfirmationNeeded);
    }

    public AbstractTabItemFactory getSampleBrowser(final String initialGroupOrNull,
            final String initialSampleTypeOrNull)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    System.err.println(initialGroupOrNull + " " + initialSampleTypeOrNull);

                    IDisposableComponent browser =
                            SampleBrowserGrid.create(viewContext, initialGroupOrNull,
                                    initialSampleTypeOrNull);
                    return createTab(Dict.SAMPLE_BROWSER, browser);
                }

                @Override
                public String getId()
                {
                    return SampleBrowserGrid.BROWSER_ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.BROWSE);
                }
            };
    }

    public final AbstractTabItemFactory getSampleBrowser()
    {
        return getSampleBrowser(null, null);
    }

    public final AbstractTabItemFactory getMaterialBrowser()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    IDisposableComponent browser =
                            MaterialBrowserGrid.createWithTypeChooser(viewContext);
                    return createTab(Dict.MATERIAL_BROWSER, browser);
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
                    return createTab(Dict.GROUP_BROWSER, component);
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
                    return createTab(Dict.AUTHORIZATION_GROUP_BROWSER, component);
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
                    return createTab(Dict.ROLE_ASSIGNMENT_BROWSER, component);
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
                    return createTab(Dict.PERSON_BROWSER, component);
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
            };
    }

    public final AbstractTabItemFactory getSampleRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            SampleRegistrationPanel.create(viewContext);
                    return createRegistrationTab(Dict.SAMPLE_REGISTRATION, component);
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
            };
    }

    public final AbstractTabItemFactory getExperimentRegistration()
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            ExperimentRegistrationPanel.create(viewContext);
                    return createRegistrationTab(Dict.EXPERIMENT_REGISTRATION, component);
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
            };
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
                    return createRegistrationTab(Dict.SAMPLE_BATCH_REGISTRATION, component);
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
                    return createRegistrationTab(Dict.SAMPLE_BATCH_UPDATE, component);
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
                            MaterialBatchRegistrationPanel.create(viewContext);
                    return createRegistrationTab(Dict.MATERIAL_IMPORT, component);
                }

                @Override
                public String getId()
                {
                    return MaterialBatchRegistrationPanel.ID;
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.MATERIAL, HelpPageAction.IMPORT);
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
                    return createSimpleTab(Dict.VOCABULARY_REGISTRATION, component, true);
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
                    return createRegistrationTab(Dict.PROJECT_REGISTRATION, component);
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
                    return createTab(Dict.VOCABULARY_BROWSER, component);
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
                    return createTab(Dict.PROJECT_BROWSER, component);
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
                    return createTab(Dict.EXPERIMENT_BROWSER, browser);
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
                    return createTab(Dict.PROPERTY_TYPES, component);
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
                    return createRegistrationTab(Dict.PROPERTY_TYPE_REGISTRATION, component);
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
                    return createTab(Dict.PROPERTY_TYPE_ASSIGNMENTS, component);
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
            final String messageKey)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    DatabaseModificationAwareComponent component =
                            PropertyTypeAssignmentForm.create(viewContext, entityKind);
                    return createRegistrationTab(messageKey, component);
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
                    return createTab(Dict.DATA_SET_SEARCH, browser);
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
                    return createTab(Dict.SAMPLE_SEARCH, browser);
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
                    return createTab(Dict.SAMPLE_TYPES, component);
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
                    return createTab(Dict.MATERIAL_TYPES, component);
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
                    return createTab(Dict.EXPERIMENT_TYPES, component);
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
                    return createTab(Dict.DATA_SET_TYPES, component);
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
                    return createRegistrationTab(Dict.DATA_SET_UPLOAD, component);
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
                    return createTab(Dict.FILE_FORMAT_TYPES, component);
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
            };
    }

    public MainTabPanel tryGetMainTabPanel()
    {
        return mainTabPanelOrNull;
    }

    public void setMainTabPanel(MainTabPanel mainTabPanel)
    {
        this.mainTabPanelOrNull = mainTabPanel;
    }

}
