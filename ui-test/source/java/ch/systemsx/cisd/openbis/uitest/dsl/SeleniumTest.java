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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.uitest.application.ApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.application.GuiApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.application.PublicApiApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterSampleLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SampleBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.BrowserRow;
import ch.systemsx.cisd.openbis.uitest.page.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.page.SampleDetails;
import ch.systemsx.cisd.openbis.uitest.screenshot.FileScreenShotter;
import ch.systemsx.cisd.openbis.uitest.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.type.Browsable;
import ch.systemsx.cisd.openbis.uitest.type.Builder;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetBuilder;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentBuilder;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.ProjectBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignmentBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleBuilder;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.SampleTypeUpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.type.Script;
import ch.systemsx.cisd.openbis.uitest.type.ScriptBuilder;
import ch.systemsx.cisd.openbis.uitest.type.ScriptType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.SpaceBuilder;
import ch.systemsx.cisd.openbis.uitest.type.UpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.type.VocabularyBuilder;
import ch.systemsx.cisd.openbis.uitest.uid.DictionaryUidGenerator;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

public abstract class SeleniumTest
{
    public static int IMPLICIT_WAIT = 30;

    public static String ADMIN_USER = "selenium";

    public static String ADMIN_PASSWORD = "selenium4CISD";

    public static WebDriver driver;

    private static UidGenerator uid;

    private static GuiApplicationRunner openbis;

    private static ApplicationRunner openbisApi;

    @BeforeSuite
    public void initWebDriver() throws Exception
    {
        String asUrl = System.getProperty("ui-test.as-url");
        String dssUrl = System.getProperty("ui-test.dss-url");
        String startPage;

        if (asUrl == null || asUrl.length() == 0)
        {
            asUrl = "http://localhost:8888";
            startPage =
                    "http://127.0.0.1:8888/ch.systemsx.cisd.openbis.OpenBIS/index.html?gwt.codesvr=127.0.0.1:9997";
            System.setProperty("webdriver.firefox.profile", "default");
        } else
        {
            startPage = asUrl;
        }

        if (dssUrl == null || dssUrl.length() == 0)
        {
            dssUrl = "http://localhost:8889";
        }

        /*
        asUrl = "https://sprint-openbis.ethz.ch/openbis";
        dssUrl = "https://sprint-openbis.ethz.ch";
        startPage = asUrl;
        */

        System.out.println("asUrl: " + asUrl);
        System.out.println("dssUrl: " + dssUrl);
        System.out.println("startPage: " + startPage);

        driver = new FirefoxDriver();
        setImplicitWaitToDefault();
        delete(new File("targets/dist"));
        driver.manage().deleteAllCookies();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenResolution =
                new Dimension((int) toolkit.getScreenSize().getWidth(), (int) toolkit
                        .getScreenSize().getHeight());
        driver.manage().window().setSize(screenResolution);

        driver.get(startPage);

        uid = new DictionaryUidGenerator(new File("resource/corncob_lowercase.txt"));
        openbisApi = new PublicApiApplicationRunner(asUrl, dssUrl, uid);
        openbis = new GuiApplicationRunner(uid);

    }

    @AfterSuite
    public void closeBrowser()
    {
        driver.quit();
    }

    @BeforeMethod(alwaysRun = true)
    public void initPageProxy(Method method)
    {
        System.out.println("--- " + method.getDeclaringClass().getSimpleName() + "."
                + method.getName() + "() STARTS ---");
        ScreenShotter shotter =
                new FileScreenShotter((TakesScreenshot) driver, "targets/dist/"
                        + this.getClass().getSimpleName() + "/" + method.getName());
        openbis.setScreenShotter(shotter);
    }

    @AfterMethod(alwaysRun = true)
    public void takeScreenShot(Method method) throws IOException
    {
        openbis.screenshot();
        System.out.println("--- " + method.getDeclaringClass().getSimpleName() + "."
                + method.getName() + "() ENDS ---");
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
        openbis.logout();
    }

    protected void login(String user, String password)
    {
        openbis.login(user, password);
    }

    public String loggedInAs()
    {
        return openbis.loggedInAs();
    }

    protected Collection<SampleType> sampleTypesInSampleBrowser()
    {
        Set<SampleType> types = new HashSet<SampleType>();

        for (String code : openbis.goTo(new SampleBrowserLocation()).getSampleTypes())
        {
            types.add(new SampleTypeBuilder(openbis).withCode(code).build());
        }
        return types;
    }

    protected BrowserRow browserEntryOf(DataSetType type)
    {
        return getRow(new Browsable(type));
    }

    protected BrowserRow browserEntryOf(ExperimentType type)
    {
        return getRow(new Browsable(type));
    }

    protected BrowserRow browserEntryOf(Project project)
    {
        return getRow(new Browsable(project));
    }

    protected BrowserRow browserEntryOf(PropertyType type)
    {
        return getRow(new Browsable(type));
    }

    protected BrowserRow browserEntryOf(PropertyTypeAssignment assignment)
    {
        return getRow(new Browsable(assignment));
    }

    protected BrowserRow browserEntryOf(SampleType type)
    {
        return getRow(new Browsable(type));
    }

    protected BrowserRow browserEntryOf(Sample sample)
    {
        return getRow(new Browsable(sample));
    }

    protected BrowserRow browserEntryOf(Script script)
    {
        return getRow(new Browsable(script));
    }

    protected BrowserRow browserEntryOf(Space space)
    {
        return getRow(new Browsable(space));
    }

    protected BrowserRow browserEntryOf(Vocabulary vocabulary)
    {
        return getRow(new Browsable(vocabulary));
    }

    private BrowserRow getRow(Browsable browsable)
    {
        return openbis.goTo(browsable.getBrowserLocation()).getRow(browsable);
    }

    protected SampleDetails detailsOf(Sample sample)
    {
        // return openbis.browseToDetailsOf(sample);
        return null;
    }

    protected void emptyTrash()
    {
        openbis.emptyTrash();
    }

    public RegisterSample sampleRegistrationPageFor(SampleType type)
    {
        openbis.goTo(new RegisterSampleLocation()).selectSampleType(type);
        return assumePage(RegisterSample.class);
    }

    public <T> T assumePage(Class<T> pageClass)
    {
        return openbis.load(pageClass);
    }

    public GuiApplicationRunner browser()
    {
        return openbis;
    }

    public Matcher<GuiApplicationRunner> displays(Class<?> pageClass)
    {
        return new CurrentPageMatcher(pageClass);
    }

    protected Matcher<Sample> hasDataSets(DataSet... datasets)
    {
        return new SampleHasDataSetsMatcher(openbis, datasets);
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

    protected <T> Matcher<Collection<T>> doNotContain(T t)
    {
        return not(new CollectionContainsMatcher<T>(t));
    }

    protected <T> T create(Builder<T> builder)
    {
        return builder.create();
    }

    protected <T> T assume(Builder<T> builder)
    {
        return builder.build();
    }

    protected void delete(Space space)
    {
        openbis.delete(space);
    }

    protected void deleteExperimentsFrom(Project project)
    {
        openbis.deleteExperimentsFrom(project);
    }

    protected void delete(Project project)
    {
        openbis.delete(project);
    }

    protected void delete(SampleType sampleType)
    {
        openbis.delete(sampleType);
    }

    protected void delete(ExperimentType experimentType)
    {
        openbis.delete(experimentType);
    }

    protected void delete(PropertyType propertyType)
    {
        openbis.delete(propertyType);
    }

    protected void delete(Vocabulary vocabulary)
    {
        openbis.delete(vocabulary);
    }

    protected SpaceBuilder aSpace()
    {
        return new SpaceBuilder(openbis);
    }

    protected ProjectBuilder aProject()
    {
        return new ProjectBuilder(openbis);
    }

    protected SampleTypeBuilder aSampleType()
    {
        return new SampleTypeBuilder(openbis);
    }

    protected ExperimentTypeBuilder anExperimentType()
    {
        return new ExperimentTypeBuilder(openbis);
    }

    protected SampleBuilder aSample()
    {
        return new SampleBuilder(openbis);
    }

    protected ExperimentBuilder anExperiment()
    {
        return new ExperimentBuilder(openbis);
    }

    protected VocabularyBuilder aVocabulary()
    {
        return new VocabularyBuilder(openbis);
    }

    protected PropertyTypeBuilder aBooleanPropertyType()
    {
        return new PropertyTypeBuilder(openbis, PropertyTypeDataType.BOOLEAN);
    }

    protected PropertyTypeBuilder anIntegerPropertyType()
    {
        return new PropertyTypeBuilder(openbis, PropertyTypeDataType.INTEGER);
    }

    protected PropertyTypeBuilder aRealPropertyType()
    {
        return new PropertyTypeBuilder(openbis, PropertyTypeDataType.REAL);
    }

    protected PropertyTypeBuilder aVarcharPropertyType()
    {
        return new PropertyTypeBuilder(openbis, PropertyTypeDataType.VARCHAR);
    }

    protected PropertyTypeBuilder aVocabularyPropertyType(Vocabulary vocabulary)
    {
        return new PropertyTypeBuilder(openbis, vocabulary);
    }

    protected PropertyTypeAssignmentBuilder aSamplePropertyTypeAssignment()
    {
        return new PropertyTypeAssignmentBuilder(openbis);
    }

    protected DataSetTypeBuilder aDataSetType()
    {
        return new DataSetTypeBuilder(openbisApi);
    }

    protected DataSetBuilder aDataSet()
    {
        return new DataSetBuilder(openbisApi);
    }

    protected ScriptBuilder anEntityValidationScript()
    {
        return new ScriptBuilder(openbis, ScriptType.ENTITY_VALIDATOR);
    }

    protected SampleTypeUpdateBuilder anUpdateOf(SampleType type)
    {
        return new SampleTypeUpdateBuilder(openbis, type);
    }

    protected void perform(UpdateBuilder builder)
    {
        builder.update();
    }
}
