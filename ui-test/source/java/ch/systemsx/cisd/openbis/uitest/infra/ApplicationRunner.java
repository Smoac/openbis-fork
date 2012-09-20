/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.infra;

import ch.systemsx.cisd.openbis.uitest.infra.uid.UidGenerator;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.PageProxy;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddExperimentTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddVocabularyDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.EditSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.menu.AdminMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.AuthorizationMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.BrowseMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.MetadataMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.NewMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.menu.TypesMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.UserMenu;
import ch.systemsx.cisd.openbis.uitest.page.tab.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.AssignSamplePropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.ExperimentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.ExperimentTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.LoginPage;
import ch.systemsx.cisd.openbis.uitest.page.tab.ProjectBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterExperiment;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterProject;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.page.tab.RoleAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SpaceBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.Trash;
import ch.systemsx.cisd.openbis.uitest.page.tab.VocabularyBrowser;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
public class ApplicationRunner
{

    private PageProxy proxy;

    private UidGenerator uid;

    public ApplicationRunner(PageProxy proxy, UidGenerator uid)
    {
        this.proxy = proxy;
        this.uid = uid;
    }

    public String uid()
    {
        return uid.uid();
    }

    public Space create(Space space)
    {
        AddSpaceDialog dialog = browseToAddSpaceDialog();
        dialog.fillWith(space);
        dialog.save();
        return space;
    }

    public void delete(Space space)
    {
        SpaceBrowser browser = browseToSpaceBrowser();
        browser.filter(space.getCode());
        browser.select(space);
        browser.delete();
    }

    public Project create(Project project)
    {
        RegisterProject register = browseToRegisterProject();
        register.fillWith(project);
        register.save();
        return project;
    }

    public SampleType create(SampleType sampleType)
    {
        AddSampleTypeDialog dialog = browseToAddSampleTypeDialog();
        dialog.fillWith(sampleType);
        dialog.save();
        return sampleType;
    }

    public ExperimentType create(ExperimentType experimentType)
    {
        AddExperimentTypeDialog dialog = browseToAddExperimentTypeDialog();
        dialog.fillWith(experimentType);
        dialog.save();
        return experimentType;
    }

    public Vocabulary create(Vocabulary vocabulary)
    {
        AddVocabularyDialog dialog = browseToAddVocabularyDialog();
        dialog.fillWith(vocabulary);
        dialog.save();
        return vocabulary;
    }

    public PropertyType create(PropertyType propertyType)
    {
        AddPropertyType dialog = browseToAddPropertyType();
        dialog.fillWith(propertyType);
        dialog.save();
        return propertyType;
    }

    public Sample create(Sample sample)
    {
        RegisterSample register = browseToRegisterSample();
        register.selectSampleType(sample.getType());
        register.fillWith(sample);
        register.save();
        return sample;
    }

    public Experiment create(Experiment experiment)
    {
        RegisterExperiment register = browseToRegisterExperiment();
        register.selectExperimentType(experiment.getType());
        register.fillWith(experiment);
        register.save();
        return experiment;
    }

    public PropertyTypeAssignment create(PropertyTypeAssignment assignment)
    {
        AssignSamplePropertyType assign = browseToAssignSamplePropertyType();
        assign.fillWith(assignment);
        assign.save();
        return assignment;
    }

    public void update(SampleType sampleType)
    {
        browseToSampleTypeBrowser().editSampleType(sampleType);
        EditSampleTypeDialog dialog = proxy.get(EditSampleTypeDialog.class);
        dialog.fillWith(sampleType);
        dialog.save();
    }

    public void login(String userName, String password)
    {
        LoginPage loginPage = proxy.get(LoginPage.class);
        loginPage.loginAs(userName, password);
    }

    public void login(User user)
    {
        LoginPage loginPage = proxy.get(LoginPage.class);
        loginPage.loginAs(user.getName(), user.getPassword());
    }

    public void logout()
    {
        getMenus().user();
        load(UserMenu.class).logout();
    }

    public SampleTypeBrowser browseToSampleTypeBrowser()
    {
        getMenus().admin();
        load(AdminMenu.class).types();
        load(TypesMenu.class).sampleTypes();
        return load(SampleTypeBrowser.class);
    }

    public ExperimentTypeBrowser browseToExperimentTypeBrowser()
    {
        getMenus().admin();
        load(AdminMenu.class).types();
        load(TypesMenu.class).experimentTypes();
        return load(ExperimentTypeBrowser.class);
    }

    public Trash browseToTrash()
    {
        getMenus().trash();
        return load(Trash.class);
    }

    public AddSampleTypeDialog browseToAddSampleTypeDialog()
    {
        browseToSampleTypeBrowser().add();
        return load(AddSampleTypeDialog.class);
    }

    public AddExperimentTypeDialog browseToAddExperimentTypeDialog()
    {
        browseToExperimentTypeBrowser().add();
        return load(AddExperimentTypeDialog.class);
    }

    public SpaceBrowser browseToSpaceBrowser()
    {
        getMenus().admin();
        load(AdminMenu.class).spaces();
        SpaceBrowser browser = load(SpaceBrowser.class);
        browser.resetFilters();
        return browser;
    }

    public ProjectBrowser browseToProjectBrowser()
    {
        getMenus().browse();
        load(BrowseMenu.class).projects();
        return load(ProjectBrowser.class);
    }

    public AddSpaceDialog browseToAddSpaceDialog()
    {
        browseToSpaceBrowser().addSpace();
        return load(AddSpaceDialog.class);
    }

    public SampleBrowser browseToSampleBrowser()
    {
        getMenus().browse();
        load(BrowseMenu.class).samples();
        return load(SampleBrowser.class);
    }

    public ExperimentBrowser browseToExperimentBrowser()
    {
        getMenus().browse();
        load(BrowseMenu.class).experiments();
        return load(ExperimentBrowser.class);
    }

    public RoleAssignmentBrowser browseToRoleAssignmentBrowser()
    {
        getMenus().admin();
        load(AdminMenu.class).authorization();
        load(AuthorizationMenu.class).roles();
        return load(RoleAssignmentBrowser.class);
    }

    public VocabularyBrowser browseToVocabularyBrowser()
    {
        getMenus().admin();
        load(AdminMenu.class).vocabularies();
        return load(VocabularyBrowser.class);
    }

    public AddVocabularyDialog browseToAddVocabularyDialog()
    {
        browseToVocabularyBrowser().add();
        return load(AddVocabularyDialog.class);
    }

    public PropertyTypeBrowser browseToPropertyTypeBrowser()
    {
        getMenus().admin();
        load(AdminMenu.class).metadata();
        load(MetadataMenu.class).propertyTypes();
        return load(PropertyTypeBrowser.class);
    }

    public AddPropertyType browseToAddPropertyType()
    {
        getMenus().admin();
        load(AdminMenu.class).metadata();
        load(MetadataMenu.class).newPropertyType();
        return load(AddPropertyType.class);
    }

    public AssignSamplePropertyType browseToAssignSamplePropertyType()
    {
        getMenus().admin();
        load(AdminMenu.class).metadata();
        load(MetadataMenu.class).assignToSampleType();
        return load(AssignSamplePropertyType.class);
    }

    public PropertyTypeAssignmentBrowser browseToPropertyTypeAssignmentBrowser()
    {
        getMenus().admin();
        load(AdminMenu.class).metadata();
        load(MetadataMenu.class).propertyTypeAssignments();
        return load(PropertyTypeAssignmentBrowser.class);
    }

    public RegisterSample browseToRegisterSample()
    {
        getMenus().newMenu();
        load(NewMenu.class).sample();
        return load(RegisterSample.class);
    }

    public RegisterExperiment browseToRegisterExperiment()
    {
        getMenus().newMenu();
        load(NewMenu.class).experiment();
        return load(RegisterExperiment.class);
    }

    public RegisterProject browseToRegisterProject()
    {
        getMenus().newMenu();
        load(NewMenu.class).project();
        return load(RegisterProject.class);
    }

    private TopBar getMenus()
    {
        return proxy.get(TopBar.class);
    }

    private <T> T load(Class<T> clazz)
    {
        return proxy.get(clazz);
    }
}
