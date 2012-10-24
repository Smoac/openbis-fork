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

package ch.systemsx.cisd.openbis.uitest.dsl;

import static org.hamcrest.CoreMatchers.not;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.hamcrest.Matcher;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.uitest.dsl.matcher.CellContentMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.CollectionContainsExactlyMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.CollectionContainsMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.CurrentPageMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.RegisterSampleFormContainsInputsForPropertiesMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.RowExistsMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.type.Builder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.DataSetBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.ExperimentBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.MetaProjectBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.ProjectBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.PropertyTypeAssignmentBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.PropertyTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SampleBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SampleTypeUpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.ScriptBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SpaceBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.UpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.UserBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.VocabularyBuilder;
import ch.systemsx.cisd.openbis.uitest.gui.CreateExperimentGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateExperimentTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateProjectGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreatePropertyTypeAssignmentGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreatePropertyTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateSampleGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateSampleTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateScriptGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateSpaceGui;
import ch.systemsx.cisd.openbis.uitest.gui.CreateVocabularyGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteExperimentTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteExperimentsOfProjectGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteProjectGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeletePropertyTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteSampleTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteSpaceGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteVocabularyGui;
import ch.systemsx.cisd.openbis.uitest.gui.EmptyTrashGui;
import ch.systemsx.cisd.openbis.uitest.gui.LoginGui;
import ch.systemsx.cisd.openbis.uitest.gui.LogoutGui;
import ch.systemsx.cisd.openbis.uitest.gui.UpdateSampleTypeGui;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterSampleLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SampleBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.menu.TabBar;
import ch.systemsx.cisd.openbis.uitest.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.Browsable;
import ch.systemsx.cisd.openbis.uitest.page.BrowserRow;
import ch.systemsx.cisd.openbis.uitest.page.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.page.SampleDetails;
import ch.systemsx.cisd.openbis.uitest.request.AddEntitiesToMetaProject;
import ch.systemsx.cisd.openbis.uitest.request.CreateDataSet;
import ch.systemsx.cisd.openbis.uitest.request.CreateDataSetType;
import ch.systemsx.cisd.openbis.uitest.request.CreateExperiment;
import ch.systemsx.cisd.openbis.uitest.request.CreateExperimentType;
import ch.systemsx.cisd.openbis.uitest.request.CreateMetaProject;
import ch.systemsx.cisd.openbis.uitest.request.CreateProject;
import ch.systemsx.cisd.openbis.uitest.request.CreatePropertyType;
import ch.systemsx.cisd.openbis.uitest.request.CreatePropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.request.CreateSample;
import ch.systemsx.cisd.openbis.uitest.request.CreateSampleType;
import ch.systemsx.cisd.openbis.uitest.request.CreateScript;
import ch.systemsx.cisd.openbis.uitest.request.CreateSpace;
import ch.systemsx.cisd.openbis.uitest.request.CreateUser;
import ch.systemsx.cisd.openbis.uitest.request.CreateVocabulary;
import ch.systemsx.cisd.openbis.uitest.request.DeleteExperimentType;
import ch.systemsx.cisd.openbis.uitest.request.DeleteExperimentsOfProject;
import ch.systemsx.cisd.openbis.uitest.request.DeleteProject;
import ch.systemsx.cisd.openbis.uitest.request.DeletePropertyType;
import ch.systemsx.cisd.openbis.uitest.request.DeleteSampleType;
import ch.systemsx.cisd.openbis.uitest.request.DeleteSpace;
import ch.systemsx.cisd.openbis.uitest.request.DeleteVocabulary;
import ch.systemsx.cisd.openbis.uitest.request.EmptyTrash;
import ch.systemsx.cisd.openbis.uitest.request.ListDataSetsOfSample;
import ch.systemsx.cisd.openbis.uitest.request.ListExperiments;
import ch.systemsx.cisd.openbis.uitest.request.ListMetaProjects;
import ch.systemsx.cisd.openbis.uitest.request.ListSamplesOfExperiment;
import ch.systemsx.cisd.openbis.uitest.request.Login;
import ch.systemsx.cisd.openbis.uitest.request.Logout;
import ch.systemsx.cisd.openbis.uitest.request.SearchForDataSets;
import ch.systemsx.cisd.openbis.uitest.request.SearchForSamples;
import ch.systemsx.cisd.openbis.uitest.request.UpdateSampleType;
import ch.systemsx.cisd.openbis.uitest.rmi.AddEntitiesToMetaProjectRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateDataSetRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateDataSetTypeRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateExperimentRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateExperimentTypeRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateMetaProjectRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateProjectRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateSampleRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateSampleTypeRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateSpaceRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.CreateUserRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListDataSetsOfSampleRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListExperimentsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListMetaProjectsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListSamplesOfExperimentRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.SearchForDataSetsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.SearchForSamplesRmi;
import ch.systemsx.cisd.openbis.uitest.screenshot.FileScreenShotter;
import ch.systemsx.cisd.openbis.uitest.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.type.BrowsableWrapper;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Entity;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Script;
import ch.systemsx.cisd.openbis.uitest.type.ScriptType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.User;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.uid.DictionaryUidGenerator;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

public abstract class SeleniumTest
{
    public static int IMPLICIT_WAIT = 30;

    public static String ADMIN_USER = "selenium";

    public static String ADMIN_PASSWORD = "selenium4CISD";

    public static WebDriver driver;

    private static UidGenerator uid;

    private static Application openbis;

    private static Application defaultApplication;

    private static Pages pages;

    private static String asUrl;

    private static String dssUrl;

    private static String startPage;

    @BeforeSuite
    public void initialization() throws Exception
    {
        deleteOldScreenShots();
        initializeLogging();
        uid = new DictionaryUidGenerator(new File("resource/corncob_lowercase.txt"));

        asUrl = System.getProperty("ui-test.as-url");
        dssUrl = System.getProperty("ui-test.dss-url");
        startPage = System.getProperty("ui-test.start-page");

        /* Run against sprint server */
        /*
        asUrl = "https://sprint-openbis.ethz.ch/openbis";
        dssUrl = "https://sprint-openbis.ethz.ch";
        startPage = asUrl;        
        */

        /* Run against local DSS and local AS in development mode */
        /* Firefox profile should be one with GWT dev mode plugin available */
        /*
        asUrl = "http://127.0.0.1:8888";
        dssUrl = "http://127.0.0.1:8889";
        startPage =
                asUrl + "/ch.systemsx.cisd.openbis.OpenBIS/index.html?gwt.codesvr=127.0.0.1:9997";
        System.setProperty("webdriver.firefox.profile", "default");
        */

        if (asUrl == null || asUrl.length() == 0)
        {
            asUrl = StartApplicationServer.go();
            startPage = asUrl;
        }

        if (dssUrl == null || dssUrl.length() == 0)
        {
            dssUrl = StartDataStoreServer.go();
        }

        System.out.println("asUrl: " + asUrl);
        System.out.println("dssUrl: " + dssUrl);
        System.out.println("startPage: " + startPage);

        pages = new Pages();
    }

    private void startWebDriver()
    {
        if (driver == null)
        {
            driver = new FirefoxDriver();
            setImplicitWaitToDefault();
            driver.manage().deleteAllCookies();

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenResolution =
                    new Dimension((int) toolkit.getScreenSize().getWidth(), (int) toolkit
                            .getScreenSize().getHeight());
            driver.manage().window().setSize(screenResolution);
        }
        driver.get(startPage);
    }

    private void deleteOldScreenShots()
    {
        delete(new File("targets/dist"));

    }

    private void initializeLogging()
    {
        Logger rootLogger = Logger.getRootLogger();
        if (rootLogger.getAllAppenders().hasMoreElements())
        {
            throw new IllegalStateException("log4j has appenders!");
        }
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(new ConsoleAppender(
                new PatternLayout("%-5p [%t]: %m%n")));
    }

    @AfterSuite
    public void closeBrowser() throws Exception
    {
        if (driver != null)
        {
            driver.quit();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void initPageProxy(Method method) throws Exception
    {
        System.out.println("--- " + method.getDeclaringClass().getSimpleName() + "."
                + method.getName() + "() STARTS ---");

        ScreenShotter shotter;
        if (driver != null)
        {
            shotter =
                    new FileScreenShotter((TakesScreenshot) driver, "targets/dist/"
                            + this.getClass().getSimpleName() + "/" + method.getName());
        } else
        {
            shotter = new ScreenShotter()
                {
                    @Override
                    public void screenshot()
                    {
                    }
                };
        }

        pages.setScreenShotter(shotter);
    }

    @AfterMethod(alwaysRun = true)
    public void takeScreenShot(Method method) throws IOException
    {
        pages.screenshot();
        System.out.println("--- " + method.getDeclaringClass().getSimpleName() + "."
                + method.getName() + "() ENDS ---");
    }

    @AfterTest
    public void takeScreenShot()
    {
        pages.screenshot();
    }

    private void delete(File f)
    {
        if (f.isDirectory())
        {
            for (File c : f.listFiles())
                delete(c);
        }
        f.delete();
    }

    public <T> T using(Application application, T t)
    {
        openbis = defaultApplication;
        return t;
    }

    public <T> T as(User user, T t)
    {
        openbis.changeLogin(assume(aUser().withName(SeleniumTest.ADMIN_USER)));
        return t;
    }

    public User user(User user)
    {
        openbis.changeLogin(user);
        return user;
    }

    public static void setImplicitWait(long amount, TimeUnit unit)
    {
        driver.manage().timeouts().implicitlyWait(amount, unit);
    }

    public static void setImplicitWaitToDefault()
    {
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
    }

    public void logout()
    {
        openbis.execute(new Logout());
    }

    protected void login(String user, String password)
    {
        openbis.execute(new Login(user, password));
    }

    public String loggedInAs()
    {
        TopBar t = pages.tryLoad(TopBar.class);
        if (t != null)
        {
            return t.getUserName();
        } else
        {
            return null;
        }
    }

    public boolean tabsContain(Location<?> location)
    {
        TabBar bar = assumePage(TabBar.class);
        return bar.getTabs().contains(location.getTabName());
    }

    public <T> T switchTabTo(Location<T> location)
    {
        TabBar bar = assumePage(TabBar.class);
        bar.selectTab(location.getTabName());
        return assumePage(location.getPage());
    }

    protected SampleBrowserLocation sampleBrowser()
    {
        return new SampleBrowserLocation();
    }

    protected Collection<SampleType> sampleTypesInSampleBrowser()
    {
        Set<SampleType> types = new HashSet<SampleType>();

        for (String code : pages.goTo(sampleBrowser()).getSampleTypes())
        {
            types.add(assume(aSampleType().withCode(code)));
        }
        return types;
    }

    protected List<MetaProject> listOfAllMetaProjects()
    {
        return openbis.execute(new ListMetaProjects());
    }

    protected Pages browser()
    {
        return pages;
    }

    protected BrowserRow browserEntryOf(DataSetType type)
    {
        return getRow(new BrowsableWrapper(type));
    }

    protected BrowserRow browserEntryOf(ExperimentType type)
    {
        return getRow(new BrowsableWrapper(type));
    }

    protected BrowserRow browserEntryOf(Project project)
    {
        return getRow(new BrowsableWrapper(project));
    }

    protected BrowserRow browserEntryOf(PropertyType type)
    {
        return getRow(new BrowsableWrapper(type));
    }

    protected BrowserRow browserEntryOf(PropertyTypeAssignment assignment)
    {
        return getRow(new BrowsableWrapper(assignment));
    }

    protected BrowserRow browserEntryOf(SampleType type)
    {
        return getRow(new BrowsableWrapper(type));
    }

    protected BrowserRow browserEntryOf(Sample sample)
    {
        return getRow(new BrowsableWrapper(sample));
    }

    protected BrowserRow browserEntryOf(Script script)
    {
        return getRow(new BrowsableWrapper(script));
    }

    protected BrowserRow browserEntryOf(Space space)
    {
        return getRow(new BrowsableWrapper(space));
    }

    protected BrowserRow browserEntryOf(Vocabulary vocabulary)
    {
        return getRow(new BrowsableWrapper(vocabulary));
    }

    private BrowserRow getRow(Browsable browsable)
    {
        return pages.goTo(browsable.getBrowserLocation()).getRow(browsable);
    }

    protected SampleDetails detailsOf(Sample sample)
    {
        // return openbis.browseToDetailsOf(sample);
        return null;
    }

    protected void emptyTrash()
    {
        openbis.execute(new EmptyTrash());
    }

    public RegisterSample sampleRegistrationPageFor(SampleType type)
    {
        pages.goTo(new RegisterSampleLocation()).selectSampleType(type);
        return assumePage(RegisterSample.class);
    }

    public <T> T assumePage(Class<T> pageClass)
    {
        return pages.load(pageClass);
    }

    public Matcher<Pages> displays(Class<?> pageClass)
    {
        return new CurrentPageMatcher(pageClass);
    }

    protected Matcher<RegisterSample> hasInputsForProperties(PropertyType... fields)
    {
        return new RegisterSampleFormContainsInputsForPropertiesMatcher(fields);
    }

    protected Matcher<BrowserRow> containsValue(String column, String value)
    {
        return new CellContentMatcher(column, value, false);
    }

    protected Matcher<BrowserRow> exists()
    {
        return new RowExistsMatcher();
    }

    protected Matcher<BrowserRow> doesNotExist()
    {
        return not(new RowExistsMatcher());
    }

    protected Matcher<BrowserRow> containsLink(String column, String value)
    {
        return new CellContentMatcher(column, value, true);
    }

    protected <T> Matcher<Collection<T>> contain(T t)
    {
        return new CollectionContainsMatcher<T>(t);
    }

    protected <T> Matcher<Collection<T>> contains(T t)
    {
        return new CollectionContainsMatcher<T>(t);
    }

    protected <T> Matcher<Collection<T>> doNotContain(T t)
    {
        return not(new CollectionContainsMatcher<T>(t));
    }

    protected <T> Matcher<Collection<T>> doesNotContain(T t)
    {
        return not(new CollectionContainsMatcher<T>(t));
    }

    protected <T> Matcher<Collection<T>> containsExactly(T... t)
    {
        return new CollectionContainsExactlyMatcher<T>(t);
    }

    protected <T> Matcher<Collection<T>> containExactly(T... t)
    {
        return new CollectionContainsExactlyMatcher<T>(t);
    }

    protected <T> T create(Builder<T> builder)
    {
        return builder.build(openbis);
    }

    protected <T> T assume(Builder<T> builder)
    {
        return using(dummyApplication(), builder.build(openbis));
    }

    protected Void delete(Space space)
    {
        openbis.execute(new DeleteSpace(space));
        return null;
    }

    protected void deleteExperimentsFrom(Project project)
    {
        openbis.execute(new DeleteExperimentsOfProject(project));
    }

    protected void delete(Project project)
    {
        openbis.execute(new DeleteProject(project));
    }

    protected void delete(SampleType sampleType)
    {
        openbis.execute(new DeleteSampleType(sampleType));
    }

    protected void delete(ExperimentType experimentType)
    {
        openbis.execute(new DeleteExperimentType(experimentType));
    }

    protected void delete(PropertyType propertyType)
    {
        openbis.execute(new DeletePropertyType(propertyType));
    }

    protected void delete(Vocabulary vocabulary)
    {
        openbis.execute(new DeleteVocabulary(vocabulary));
    }

    protected UserBuilder aUser()
    {
        return new UserBuilder(uid);
    }

    protected SpaceBuilder aSpace()
    {
        return new SpaceBuilder(uid);
    }

    protected ProjectBuilder aProject()
    {
        return new ProjectBuilder(uid);
    }

    protected MetaProjectBuilder aMetaProject()
    {
        return new MetaProjectBuilder(uid);
    }

    protected SampleTypeBuilder aSampleType()
    {
        return new SampleTypeBuilder(uid);
    }

    protected ExperimentTypeBuilder anExperimentType()
    {
        return new ExperimentTypeBuilder(uid);
    }

    protected SampleBuilder aSample()
    {
        return new SampleBuilder(uid);
    }

    protected ExperimentBuilder anExperiment()
    {
        return new ExperimentBuilder(uid);
    }

    protected VocabularyBuilder aVocabulary()
    {
        return new VocabularyBuilder(uid);
    }

    protected PropertyTypeBuilder aBooleanPropertyType()
    {
        return new PropertyTypeBuilder(uid, PropertyTypeDataType.BOOLEAN);
    }

    protected PropertyTypeBuilder anIntegerPropertyType()
    {
        return new PropertyTypeBuilder(uid, PropertyTypeDataType.INTEGER);
    }

    protected PropertyTypeBuilder aRealPropertyType()
    {
        return new PropertyTypeBuilder(uid, PropertyTypeDataType.REAL);
    }

    protected PropertyTypeBuilder aVarcharPropertyType()
    {
        return new PropertyTypeBuilder(uid, PropertyTypeDataType.VARCHAR);
    }

    protected PropertyTypeBuilder aVocabularyPropertyType(Vocabulary vocabulary)
    {
        return new PropertyTypeBuilder(uid, vocabulary);
    }

    protected PropertyTypeAssignmentBuilder aSamplePropertyTypeAssignment()
    {
        return new PropertyTypeAssignmentBuilder(uid);
    }

    protected DataSetTypeBuilder aDataSetType()
    {
        return new DataSetTypeBuilder(uid);
    }

    protected DataSetBuilder aDataSet()
    {
        return new DataSetBuilder(uid);
    }

    protected ScriptBuilder anEntityValidationScript()
    {
        return new ScriptBuilder(uid, ScriptType.ENTITY_VALIDATOR);
    }

    protected ScriptBuilder aDynamicPropertyEvaluatorScript()
    {
        return new ScriptBuilder(uid, ScriptType.DYNAMIC_PROPERTY_EVALUATOR);
    }

    protected ScriptBuilder aManagedPropertyHandlerScript()
    {
        return new ScriptBuilder(uid, ScriptType.MANAGED_PROPERTY_HANDLER);
    }

    protected SampleTypeUpdateBuilder anUpdateOf(SampleType type)
    {
        return new SampleTypeUpdateBuilder(type);
    }

    protected void perform(UpdateBuilder builder)
    {
        builder.update(openbis);
    }

    protected Void addTo(MetaProject metaProject, Entity... entities)
    {
        openbis.execute(new AddEntitiesToMetaProject(metaProject, Arrays.asList(entities)));
        return null;
    }

    public Application publicApi()
    {
        Application application = new Application(asUrl, dssUrl, pages);
        application.setExecutor(CreateSpace.class, new CreateSpaceRmi());
        application.setExecutor(CreateSampleType.class, new CreateSampleTypeRmi());
        application.setExecutor(CreateSample.class, new CreateSampleRmi());
        application.setExecutor(CreateDataSet.class, new CreateDataSetRmi());
        application.setExecutor(CreateDataSetType.class, new CreateDataSetTypeRmi());
        application.setExecutor(CreateMetaProject.class, new CreateMetaProjectRmi());
        application.setExecutor(CreateExperiment.class, new CreateExperimentRmi());
        application.setExecutor(CreateExperimentType.class, new CreateExperimentTypeRmi());
        application.setExecutor(CreateProject.class, new CreateProjectRmi());
        application.setExecutor(ListMetaProjects.class, new ListMetaProjectsRmi());
        application.setExecutor(AddEntitiesToMetaProject.class, new AddEntitiesToMetaProjectRmi());
        application.setExecutor(SearchForSamples.class, new SearchForSamplesRmi());
        application.setExecutor(ListSamplesOfExperiment.class, new ListSamplesOfExperimentRmi());
        application.setExecutor(ListExperiments.class, new ListExperimentsRmi());
        application.setExecutor(ListDataSetsOfSample.class, new ListDataSetsOfSampleRmi());
        application.setExecutor(SearchForDataSets.class, new SearchForDataSetsRmi());
        application.setExecutor(CreateUser.class, new CreateUserRmi());
        openbis = application;
        return application;
    }

    public Application dummyApplication()
    {
        Application application = new Application();
        application.setExecutor(CreateExperiment.class,
                new Executor<CreateExperiment, Experiment>()
                    {
                        @Override
                        public Experiment run(CreateExperiment request)
                        {
                            return request.getExperiment();
                        }
                    });
        application.setExecutor(CreateExperimentType.class,
                new Executor<CreateExperimentType, ExperimentType>()
                    {
                        @Override
                        public ExperimentType run(CreateExperimentType request)
                        {
                            return request.getType();
                        }
                    });
        application.setExecutor(CreateProject.class,
                new Executor<CreateProject, Project>()
                    {
                        @Override
                        public Project run(CreateProject request)
                        {
                            return request.getProject();
                        }
                    });
        application.setExecutor(CreatePropertyTypeAssignment.class,
                new Executor<CreatePropertyTypeAssignment, PropertyTypeAssignment>()
                    {
                        @Override
                        public PropertyTypeAssignment run(CreatePropertyTypeAssignment request)
                        {
                            return request.getAssignment();
                        }
                    });
        application.setExecutor(CreatePropertyType.class,
                new Executor<CreatePropertyType, PropertyType>()
                    {
                        @Override
                        public PropertyType run(CreatePropertyType request)
                        {
                            return request.getType();
                        }
                    });
        application.setExecutor(CreateSample.class,
                new Executor<CreateSample, Sample>()
                    {
                        @Override
                        public Sample run(CreateSample request)
                        {
                            return request.getSample();
                        }
                    });
        application.setExecutor(CreateSampleType.class,
                new Executor<CreateSampleType, SampleType>()
                    {
                        @Override
                        public SampleType run(CreateSampleType request)
                        {
                            System.out.println("RETURNING SAMPLE TYPE " + request.getType());
                            return request.getType();
                        }
                    });
        application.setExecutor(CreateScript.class,
                new Executor<CreateScript, Script>()
                    {
                        @Override
                        public Script run(CreateScript request)
                        {
                            return request.getScript();
                        }
                    });
        application.setExecutor(CreateSpace.class,
                new Executor<CreateSpace, Space>()
                    {
                        @Override
                        public Space run(CreateSpace request)
                        {
                            return request.getSpace();
                        }
                    });
        application.setExecutor(CreateVocabulary.class,
                new Executor<CreateVocabulary, Vocabulary>()
                    {
                        @Override
                        public Vocabulary run(CreateVocabulary request)
                        {
                            return request.getVocabulary();
                        }
                    });
        application.setExecutor(CreateMetaProject.class,
                new Executor<CreateMetaProject, MetaProject>()
                    {
                        @Override
                        public MetaProject run(CreateMetaProject request)
                        {
                            return request.getMetaProject();
                        }
                    });
        application.setExecutor(CreateUser.class, new Executor<CreateUser, User>()
            {
                @Override
                public User run(CreateUser request)
                {
                    return request.getUser();
                }
            });
        openbis = application;
        return application;
    }

    protected void useGui()
    {
        startWebDriver();
        openbis = gui();
        defaultApplication = openbis;
    }

    protected void usePublicApi()
    {
        openbis = publicApi();
        defaultApplication = openbis;
    }

    public Application gui()
    {
        Application application = new Application(asUrl, dssUrl, pages);
        application.setExecutor(CreateExperiment.class, new CreateExperimentGui());
        application.setExecutor(CreateExperimentType.class, new CreateExperimentTypeGui());
        application.setExecutor(CreateProject.class, new CreateProjectGui());
        application.setExecutor(CreatePropertyTypeAssignment.class,
                new CreatePropertyTypeAssignmentGui());
        application.setExecutor(CreatePropertyType.class, new CreatePropertyTypeGui());
        application.setExecutor(CreateSample.class, new CreateSampleGui());
        application.setExecutor(CreateSampleType.class, new CreateSampleTypeGui());
        application.setExecutor(CreateScript.class, new CreateScriptGui());
        application.setExecutor(CreateSpace.class, new CreateSpaceGui());
        application.setExecutor(CreateVocabulary.class, new CreateVocabularyGui());
        application.setExecutor(DeleteExperimentsOfProject.class,
                new DeleteExperimentsOfProjectGui());
        application.setExecutor(DeleteExperimentType.class, new DeleteExperimentTypeGui());
        application.setExecutor(DeleteProject.class, new DeleteProjectGui());
        application.setExecutor(DeletePropertyType.class, new DeletePropertyTypeGui());
        application.setExecutor(DeleteSampleType.class, new DeleteSampleTypeGui());
        application.setExecutor(DeleteSpace.class, new DeleteSpaceGui());
        application.setExecutor(DeleteVocabulary.class, new DeleteVocabularyGui());
        application.setExecutor(EmptyTrash.class, new EmptyTrashGui());
        application.setExecutor(Login.class, new LoginGui());
        application.setExecutor(Logout.class, new LogoutGui());
        application.setExecutor(UpdateSampleType.class, new UpdateSampleTypeGui());
        openbis = application;
        return application;
    }

    public static void mouseOver(WebElement element)
    {
        pages.screenshot();
        Actions builder = new Actions(SeleniumTest.driver);
        builder.moveToElement(element).build().perform();
    }

    public static <U extends Widget> U initializeWidget(Class<U> widgetClass,
            WebElement context)
    {
        return pages.initializeWidget(widgetClass, context, false);
    }

    public Sample searchSample(Sample sample)
    {
        List<Sample> samples = openbis.execute(new SearchForSamples(sample.getCode()));
        if (samples.size() != 1)
        {
            throw new IllegalStateException("Got wrong amount of samples: " + samples);
        }
        if (samples.get(0).equals(sample) == false)
        {
            throw new IllegalStateException("Got wrong sample: " + samples.get(0)
                    + ", should have been " + sample);
        }
        return samples.get(0);
    }

    public Sample listSample(Sample sample)
    {
        List<Sample> samples = openbis.execute(new ListSamplesOfExperiment(sample.getExperiment()));
        if (samples.size() != 1)
        {
            throw new IllegalStateException("Got wrong amount of samples: " + samples);
        }
        if (samples.get(0).equals(sample) == false)
        {
            throw new IllegalStateException("Got wrong sample: " + samples.get(0)
                    + ", should have been " + sample);
        }
        return samples.get(0);
    }

    public Experiment listExperiment(Experiment experiment)
    {
        List<Experiment> experiments = openbis.execute(new ListExperiments(experiment));
        if (experiments.size() != 1)
        {
            throw new IllegalStateException("Got wrong amount of experiments: " + experiments);
        }
        if (experiments.get(0).equals(experiment) == false)
        {
            throw new IllegalStateException("Got wrong experiment: " + experiments.get(0)
                    + ", should have been " + experiment);
        }
        return experiments.get(0);
    }

    public DataSet listDataSet(DataSet dataSet)
    {
        List<DataSet> dataSets = openbis.execute(new ListDataSetsOfSample(dataSet.getSample()));
        if (dataSets.size() != 1)
        {
            throw new IllegalStateException("Got wrong amount of datasets: " + dataSets);
        }
        if (dataSets.get(0).equals(dataSet) == false)
        {
            throw new IllegalStateException("Got wrong dataset: " + dataSets.get(0)
                    + ", should have been " + dataSet);
        }
        return dataSets.get(0);
    }

    public DataSet searchDataSet(DataSet dataSet)
    {
        List<DataSet> dataSets = openbis.execute(new SearchForDataSets(dataSet.getCode()));
        if (dataSets.size() != 1)
        {
            throw new IllegalStateException("Got wrong amount of datasets: " + dataSets);
        }
        if (dataSets.get(0).equals(dataSet) == false)
        {
            throw new IllegalStateException("Got wrong dataset: " + dataSets.get(0)
                    + ", should have been " + dataSet);
        }
        return dataSets.get(0);
    }

    public Collection<MetaProject> metaProjectsOf(Entity entity)
    {
        return entity.getMetaProjects();
    }
}
