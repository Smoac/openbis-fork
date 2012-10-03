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

package ch.systemsx.cisd.openbis.uitest.infra.application;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.NoSuchElementException;

import ch.systemsx.cisd.openbis.uitest.infra.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.infra.uid.UidGenerator;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.PageProxy;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddExperimentTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddVocabularyDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.EditSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.menu.AdminMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.BrowseMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.NewMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.TabBar;
import ch.systemsx.cisd.openbis.uitest.page.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.menu.UserMenu;
import ch.systemsx.cisd.openbis.uitest.page.tab.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.AssignSamplePropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.Browser;
import ch.systemsx.cisd.openbis.uitest.page.tab.BrowserRow;
import ch.systemsx.cisd.openbis.uitest.page.tab.DataSetTypeBrowser;
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
import ch.systemsx.cisd.openbis.uitest.type.Browsable;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
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
public class GuiApplicationRunner implements ApplicationRunner
{

    private PageProxy proxy;

    private UidGenerator uid;

    public GuiApplicationRunner(PageProxy proxy, UidGenerator uid)
    {
        this.proxy = proxy;
        this.uid = uid;
    }

    @Override
    public String uid()
    {
        return uid.uid();
    }

    @Override
    public Space create(Space space)
    {
        AddSpaceDialog dialog = browseToAddSpaceDialog();
        dialog.fillWith(space);
        dialog.save();
        return space;
    }

    @Override
    public void delete(Space space)
    {
        SpaceBrowser browser = browseToSpaceBrowser();
        browser.filterTo(space);
        BrowserRow row = browser.select(space);
        if (row.exists())
        {
            browser.delete();
        }
        browser.resetFilters();
    }

    public void deleteExperimentsFrom(Project project)
    {
        ExperimentBrowser browser = browseToExperimentBrowser();
        if (browser.selectProject(project))
        {
            browser.deleteAll();
        }
    }

    @Override
    public void delete(Project project)
    {
        ProjectBrowser browser = browseToProjectBrowser();
        browser.filterTo(project);
        BrowserRow row = browser.select(project);
        if (row.exists())
        {
            browser.delete();
        }
        browser.resetFilters();
    }

    @Override
    public void delete(SampleType sampleType)
    {
        SampleTypeBrowser browser = browseToSampleTypeBrowser();
        browser.filterTo(sampleType);
        BrowserRow row = browser.select(sampleType);
        if (row.exists())
        {
            browser.delete();
        }
        browser.resetFilters();
    }

    @Override
    public void delete(ExperimentType experimentType)
    {
        ExperimentTypeBrowser browser = browseToExperimentTypeBrowser();
        browser.filterTo(experimentType);
        BrowserRow row = browser.select(experimentType);
        if (row.exists())
        {
            browser.delete();
        }
        browser.resetFilters();
    }

    @Override
    public void delete(PropertyType propertyType)
    {
        PropertyTypeBrowser browser = browseToPropertyTypeBrowser();
        browser.filterTo(propertyType);
        BrowserRow row = browser.select(propertyType);
        if (row.exists())
        {
            browser.delete();
        }
        browser.resetFilters();
    }

    @Override
    public void delete(Vocabulary vocabulary)
    {
        VocabularyBrowser browser = browseToVocabularyBrowser();
        browser.filterTo(vocabulary);
        BrowserRow row = browser.select(vocabulary);
        if (row.exists())
        {
            browser.delete();
        }
        browser.resetFilters();
    }

    @Override
    public Project create(Project project)
    {
        RegisterProject register = browseToRegisterProject();
        register.fillWith(project);
        register.save();
        return project;
    }

    @Override
    public SampleType create(SampleType sampleType)
    {
        AddSampleTypeDialog dialog = browseToAddSampleTypeDialog();
        dialog.fillWith(sampleType);
        dialog.save();
        return sampleType;
    }

    @Override
    public ExperimentType create(ExperimentType experimentType)
    {
        AddExperimentTypeDialog dialog = browseToAddExperimentTypeDialog();
        dialog.fillWith(experimentType);
        dialog.save();
        return experimentType;
    }

    @Override
    public Vocabulary create(Vocabulary vocabulary)
    {
        AddVocabularyDialog dialog = browseToAddVocabularyDialog();
        dialog.fillWith(vocabulary);
        dialog.save();
        return vocabulary;
    }

    @Override
    public PropertyType create(PropertyType propertyType)
    {
        AddPropertyType dialog = browseToAddPropertyType();
        dialog.fillWith(propertyType);
        dialog.save();
        return propertyType;
    }

    @Override
    public Sample create(Sample sample)
    {
        RegisterSample register = browseToRegisterSample();
        register.selectSampleType(sample.getType());
        register.fillWith(sample);
        register.save();
        return sample;
    }

    @Override
    public Experiment create(Experiment experiment)
    {
        RegisterExperiment register = browseToRegisterExperiment();
        register.selectExperimentType(experiment.getType());
        register.fillWith(experiment);
        register.save();
        return experiment;
    }

    @Override
    public PropertyTypeAssignment create(PropertyTypeAssignment assignment)
    {
        AssignSamplePropertyType assign = browseToAssignSamplePropertyType();
        assign.fillWith(assignment);
        assign.save();
        return assignment;
    }

    @Override
    public DataSetType create(DataSetType type)
    {
        throw new UnsupportedOperationException(
                "Data set type creation through GUI not implemented yet");
    }

    @Override
    public DataSet create(DataSet dataSet)
    {
        throw new UnsupportedOperationException(
                "Data set creation through GUI not implemented yet");
    }

    @Override
    public void update(SampleType sampleType)
    {
        SampleTypeBrowser browser = browseToSampleTypeBrowser();
        browser.filterTo(sampleType);
        browser.select(sampleType);
        browser.edit();
        EditSampleTypeDialog dialog = proxy.get(EditSampleTypeDialog.class);
        dialog.fillWith(sampleType);
        dialog.save();
        browser.resetFilters();
    }

    @Override
    public void login(String userName, String password)
    {
        LoginPage loginPage = proxy.get(LoginPage.class);
        loginPage.loginAs(userName, password);
    }

    @Override
    public void logout()
    {
        getMenus().user();
        load(UserMenu.class).logout();
    }

    public BrowserRow browseTo(Sample sample)
    {
        browseToSampleBrowser();
        return getRow(SampleBrowser.class, sample);
    }

    public BrowserRow browseTo(SampleType type)
    {
        browseToSampleTypeBrowser();
        return getRow(SampleTypeBrowser.class, type);
    }

    public BrowserRow browseTo(Vocabulary vocabulary)
    {
        browseToVocabularyBrowser();
        return getRow(VocabularyBrowser.class, vocabulary);
    }

    public BrowserRow browseTo(Experiment experiment)
    {
        browseToExperimentBrowser();
        return getRow(ExperimentBrowser.class, experiment);
    }

    public BrowserRow browseTo(ExperimentType type)
    {
        browseToExperimentTypeBrowser();
        return getRow(ExperimentTypeBrowser.class, type);
    }

    public BrowserRow browseTo(Project project)
    {
        browseToProjectBrowser();
        return getRow(ProjectBrowser.class, project);
    }

    public BrowserRow browseTo(PropertyTypeAssignment assignment)
    {
        browseToPropertyTypeAssignmentBrowser();
        return getRow(PropertyTypeAssignmentBrowser.class, assignment);
    }

    public BrowserRow browseTo(PropertyType type)
    {
        browseToPropertyTypeBrowser();
        return getRow(PropertyTypeBrowser.class, type);
    }

    public BrowserRow browseTo(Space space)
    {
        browseToSpaceBrowser();
        return getRow(SpaceBrowser.class, space);
    }

    public BrowserRow browseTo(DataSetType type)
    {
        browseToDataSetTypeBrowser();
        return getRow(DataSetTypeBrowser.class, type);
    }

    private <T extends Browsable> BrowserRow getRow(Class<? extends Browser<T>> browserClass,
            T browsable)
    {
        load(browserClass).showColumnsOf(browsable);
        load(browserClass).filterTo(browsable);
        List<BrowserRow> rows = load(browserClass).getData();
        try
        {
            if (rows.size() == 0)
            {
                return new BrowserRow();
            } else if (rows.size() == 1)
            {
                return rows.get(0);
            } else
            {
                throw new IllegalStateException("multiple rows found:\n" + rows);
            }
        } finally
        {
            load(browserClass).resetFilters();
        }
    }

    public SampleTypeBrowser browseToSampleTypeBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Sample Types");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).sampleTypes();
        }
        return getBrowser(SampleTypeBrowser.class);
    }

    public ExperimentTypeBrowser browseToExperimentTypeBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Experiment Types");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).experimentTypes();
        }
        return getBrowser(ExperimentTypeBrowser.class);
    }

    public DataSetTypeBrowser browseToDataSetTypeBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Data Set Types");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).dataSetTypes();
        }
        return getBrowser(DataSetTypeBrowser.class);
    }

    public void emptyTrash()
    {
        boolean success = load(TabBar.class).selectTab("Trash");
        if (!success)
        {
            getMenus().trash();
        }
        load(Trash.class).empty();
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
        boolean success = load(TabBar.class).selectTab("Space Browser");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).spaces();
        }
        return getBrowser(SpaceBrowser.class);
    }

    public ProjectBrowser browseToProjectBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Project Browser");
        if (!success)
        {
            getMenus().browse();
            load(BrowseMenu.class).projects();
        }
        return getBrowser(ProjectBrowser.class);
    }

    public AddSpaceDialog browseToAddSpaceDialog()
    {
        browseToSpaceBrowser().addSpace();
        return load(AddSpaceDialog.class);
    }

    public SampleBrowser browseToSampleBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Sample Browser");
        if (!success)
        {
            getMenus().browse();
            load(BrowseMenu.class).samples();
        }
        return getBrowser(SampleBrowser.class);
    }

    public ExperimentBrowser browseToExperimentBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Experiment Browser");
        if (!success)
        {
            getMenus().browse();
            load(BrowseMenu.class).experiments();
        }
        return load(ExperimentBrowser.class);
    }

    public RoleAssignmentBrowser browseToRoleAssignmentBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Role Assignment Browser");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).roles();
        }
        return getBrowser(RoleAssignmentBrowser.class);
    }

    public VocabularyBrowser browseToVocabularyBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Vocabulary Browser");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).vocabularies();
        }
        return getBrowser(VocabularyBrowser.class);
    }

    public AddVocabularyDialog browseToAddVocabularyDialog()
    {
        browseToVocabularyBrowser().add();
        return load(AddVocabularyDialog.class);
    }

    public PropertyTypeBrowser browseToPropertyTypeBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Property Types");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).browsePropertyTypes();
        }
        return getBrowser(PropertyTypeBrowser.class);
    }

    public AddPropertyType browseToAddPropertyType()
    {
        boolean success = load(TabBar.class).selectTab("Property Type Registration");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).newPropertyType();
        }
        return load(AddPropertyType.class);
    }

    public AssignSamplePropertyType browseToAssignSamplePropertyType()
    {
        boolean success = load(TabBar.class).selectTab("Assign Sample Property Type");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).assignPropertyTypeToSampleType();
        }
        return load(AssignSamplePropertyType.class);
    }

    public PropertyTypeAssignmentBrowser browseToPropertyTypeAssignmentBrowser()
    {
        boolean success = load(TabBar.class).selectTab("Property Type Assignments");
        if (!success)
        {
            getMenus().admin();
            load(AdminMenu.class).browsePropertyTypeAssignments();
        }
        return getBrowser(PropertyTypeAssignmentBrowser.class);
    }

    public RegisterSample browseToRegisterSample()
    {
        boolean success = load(TabBar.class).selectTab("Sample Registration");
        if (!success)
        {
            getMenus().newMenu();
            load(NewMenu.class).sample();
        }
        return load(RegisterSample.class);
    }

    public RegisterExperiment browseToRegisterExperiment()
    {
        boolean success = load(TabBar.class).selectTab("Experiment Registration");
        if (!success)
        {
            getMenus().newMenu();
            load(NewMenu.class).experiment();
        }
        return load(RegisterExperiment.class);
    }

    public RegisterProject browseToRegisterProject()
    {
        boolean success = load(TabBar.class).selectTab("Project Registration");
        if (!success)
        {
            getMenus().newMenu();
            load(NewMenu.class).project();
        }
        return load(RegisterProject.class);
    }

    private TopBar getMenus()
    {
        return proxy.get(TopBar.class);
    }

    private <T extends Browser<?>> T getBrowser(Class<T> clazz)
    {
        T browser = load(clazz);
        return browser;
    }

    public <T> T load(Class<T> clazz)
    {
        return proxy.get(clazz);
    }

    public <T> T tryLoad(Class<T> clazz)
    {
        return tryLoad(clazz, SeleniumTest.IMPLICIT_WAIT, TimeUnit.SECONDS);
    }

    public <T> T tryLoad(Class<T> clazz, long timeout, TimeUnit unit)
    {
        SeleniumTest.setImplicitWait(timeout, unit);
        try
        {
            return load(clazz);
        } catch (NoSuchElementException e)
        {
            e.printStackTrace();
            return null;
        } finally
        {
            SeleniumTest.setImplicitWaitToDefault();
        }

    }

    @Override
    public String loggedInAs()
    {
        TopBar t = tryLoad(TopBar.class);
        if (t != null)
        {
            return t.getUserName();
        } else
        {
            return null;
        }
    }
}
