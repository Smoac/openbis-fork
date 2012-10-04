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
import ch.systemsx.cisd.openbis.uitest.infra.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.infra.uid.UidGenerator;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.PageProxy;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddExperimentTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddVocabularyDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.EditSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.layout.AddExperimentTypeLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.AddPropertyTypeLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.AddSampleTypeLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.AddSpaceDialogLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.AddVocabularyLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.AssignSamplePropertyLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.ExperimentBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.ExperimentTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.Location;
import ch.systemsx.cisd.openbis.uitest.page.layout.ProjectBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.PropertyTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.RegisterExperimentLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.RegisterProjectLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.RegisterSampleLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.RegisterScriptLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.SampleTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.SpaceBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.layout.VocabularyBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.menu.TabBar;
import ch.systemsx.cisd.openbis.uitest.page.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.menu.UserMenu;
import ch.systemsx.cisd.openbis.uitest.page.tab.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.AssignSamplePropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.Browser;
import ch.systemsx.cisd.openbis.uitest.page.tab.BrowserRow;
import ch.systemsx.cisd.openbis.uitest.page.tab.ExperimentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.ExperimentTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.LoginPage;
import ch.systemsx.cisd.openbis.uitest.page.tab.ProjectBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterExperiment;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterProject;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterScript;
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
import ch.systemsx.cisd.openbis.uitest.type.Script;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
public class GuiApplicationRunner implements ApplicationRunner
{

    private PageProxy proxy;

    private UidGenerator uid;

    public GuiApplicationRunner(UidGenerator uid)
    {
        this.proxy = new PageProxy(new ScreenShotter()
            {
                @Override
                public void screenshot()
                {
                }
            });
        this.uid = uid;
    }

    public void setScreenShotter(ScreenShotter shotter)
    {
        proxy.setScreenShotter(shotter);
    }

    public void screenshot()
    {
        proxy.screenshot();
    }

    @Override
    public String uid()
    {
        return uid.uid();
    }

    @Override
    public Space create(Space space)
    {
        AddSpaceDialog dialog = goTo(new AddSpaceDialogLocation());
        dialog.fillWith(space);
        dialog.save();
        return space;
    }

    @Override
    public void delete(Space space)
    {
        SpaceBrowser browser = goTo(new SpaceBrowserLocation());
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
        ExperimentBrowser browser = goTo(new ExperimentBrowserLocation());
        if (browser.selectProject(project))
        {
            browser.deleteAll();
        }
    }

    @Override
    public void delete(Project project)
    {
        ProjectBrowser browser = goTo(new ProjectBrowserLocation());
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
        SampleTypeBrowser browser = goTo(new SampleTypeBrowserLocation());
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
        ExperimentTypeBrowser browser = goTo(new ExperimentTypeBrowserLocation());
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
        PropertyTypeBrowser browser = goTo(new PropertyTypeBrowserLocation());
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
        VocabularyBrowser browser = goTo(new VocabularyBrowserLocation());
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
        RegisterProject register = goTo(new RegisterProjectLocation());
        register.fillWith(project);
        register.save();
        return project;
    }

    @Override
    public SampleType create(SampleType sampleType)
    {
        AddSampleTypeDialog dialog = goTo(new AddSampleTypeLocation());
        dialog.fillWith(sampleType);
        dialog.save();
        return sampleType;
    }

    @Override
    public ExperimentType create(ExperimentType experimentType)
    {
        AddExperimentTypeDialog dialog = goTo(new AddExperimentTypeLocation());
        dialog.fillWith(experimentType);
        dialog.save();
        return experimentType;
    }

    @Override
    public Vocabulary create(Vocabulary vocabulary)
    {
        AddVocabularyDialog dialog = goTo(new AddVocabularyLocation());
        dialog.fillWith(vocabulary);
        dialog.save();
        return vocabulary;
    }

    @Override
    public PropertyType create(PropertyType propertyType)
    {
        AddPropertyType dialog = goTo(new AddPropertyTypeLocation());
        dialog.fillWith(propertyType);
        dialog.save();
        return propertyType;
    }

    @Override
    public Sample create(Sample sample)
    {
        RegisterSample register = goTo(new RegisterSampleLocation());
        register.selectSampleType(sample.getType());
        register.fillWith(sample);
        register.save();
        return sample;
    }

    @Override
    public Experiment create(Experiment experiment)
    {
        RegisterExperiment register = goTo(new RegisterExperimentLocation());
        register.selectExperimentType(experiment.getType());
        register.fillWith(experiment);
        register.save();
        return experiment;
    }

    @Override
    public PropertyTypeAssignment create(PropertyTypeAssignment assignment)
    {
        AssignSamplePropertyType assign = goTo(new AssignSamplePropertyLocation());
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
    public Script create(Script script)
    {
        RegisterScript register = goTo(new RegisterScriptLocation());
        register.fillWith(script);
        register.save();
        return script;
    }

    @Override
    public void update(SampleType sampleType)
    {
        SampleTypeBrowser browser = goTo(new SampleTypeBrowserLocation());
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

    public <T> T goTo(Location<T> location)
    {
        if (load(TabBar.class).selectTab(location.getTabName()) == false)
        {
            location.moveTo(this);
        }
        return load(location.getPage());
    }

    public <T extends Browsable<U>, U extends Browser<T>> BrowserRow getBrowserContentOf(T browsable)
    {
        U browser = goTo(browsable.getBrowserLocation());
        browser.showColumnsOf(browsable);
        browser.filterTo(browsable);
        List<BrowserRow> rows = browser.getData();
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
            browser.resetFilters();
        }
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

    private TopBar getMenus()
    {
        return proxy.get(TopBar.class);
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
