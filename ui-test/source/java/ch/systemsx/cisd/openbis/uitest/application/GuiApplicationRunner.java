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

package ch.systemsx.cisd.openbis.uitest.application;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.NoSuchElementException;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.layout.AddExperimentTypeLocation;
import ch.systemsx.cisd.openbis.uitest.layout.AddPropertyTypeLocation;
import ch.systemsx.cisd.openbis.uitest.layout.AddSampleTypeLocation;
import ch.systemsx.cisd.openbis.uitest.layout.AddSpaceDialogLocation;
import ch.systemsx.cisd.openbis.uitest.layout.AddVocabularyLocation;
import ch.systemsx.cisd.openbis.uitest.layout.AssignSamplePropertyLocation;
import ch.systemsx.cisd.openbis.uitest.layout.ExperimentBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterExperimentLocation;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterProjectLocation;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterSampleLocation;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterScriptLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SampleTypeBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.menu.TabBar;
import ch.systemsx.cisd.openbis.uitest.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.menu.UserMenu;
import ch.systemsx.cisd.openbis.uitest.page.AddExperimentTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.page.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.page.AddVocabularyDialog;
import ch.systemsx.cisd.openbis.uitest.page.AssignSamplePropertyType;
import ch.systemsx.cisd.openbis.uitest.page.Browser;
import ch.systemsx.cisd.openbis.uitest.page.EditSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.ExperimentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.LoginPage;
import ch.systemsx.cisd.openbis.uitest.page.RegisterExperiment;
import ch.systemsx.cisd.openbis.uitest.page.RegisterProject;
import ch.systemsx.cisd.openbis.uitest.page.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.page.RegisterScript;
import ch.systemsx.cisd.openbis.uitest.page.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.Trash;
import ch.systemsx.cisd.openbis.uitest.screenshot.ScreenShotter;
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
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;
import ch.systemsx.cisd.openbis.uitest.webdriver.PageProxy;

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
    public void delete(Space space)
    {
        delete(new Browsable(space));
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
        delete(new Browsable(project));
    }

    @Override
    public void delete(SampleType sampleType)
    {
        delete(new Browsable(sampleType));
    }

    @Override
    public void delete(ExperimentType experimentType)
    {
        delete(new Browsable(experimentType));
    }

    @Override
    public void delete(PropertyType propertyType)
    {
        delete(new Browsable(propertyType));
    }

    @Override
    public void delete(Vocabulary vocabulary)
    {
        delete(new Browsable(vocabulary));
    }

    private void delete(Browsable browsable)
    {
        Browser browser = goTo(browsable.getBrowserLocation());
        browser.delete(browsable);
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
        Browsable b = new Browsable(sampleType);

        SampleTypeBrowser browser = goTo(new SampleTypeBrowserLocation());
        browser.filterTo(b);
        browser.select(b);
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
