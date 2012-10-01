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

package ch.systemsx.cisd.openbis.uitest.suite;

import static org.hamcrest.CoreMatchers.not;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.uitest.infra.application.ApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.infra.application.GuiApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.infra.application.PublicApiApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.infra.dsl.DslSampleBrowser;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.CellContentMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.CollectionContainsMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.PageMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.RegisterSampleFormContainsInputsForPropertiesMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.RowExistsMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.SampleHasDataSetsMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.screenshot.FileScreenShotter;
import ch.systemsx.cisd.openbis.uitest.infra.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.infra.uid.DictionaryUidGenerator;
import ch.systemsx.cisd.openbis.uitest.infra.uid.UidGenerator;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.PageProxy;
import ch.systemsx.cisd.openbis.uitest.page.tab.BrowserRow;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.type.Browsable;
import ch.systemsx.cisd.openbis.uitest.type.Builder;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetBuilder;
import ch.systemsx.cisd.openbis.uitest.type.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentBuilder;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.ProjectBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignmentBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleBuilder;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.SampleTypeUpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.SpaceBuilder;
import ch.systemsx.cisd.openbis.uitest.type.UpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.type.VocabularyBuilder;

public abstract class SeleniumTest
{
    public static int IMPLICIT_WAIT = 30;

    public static String ADMIN_USER = "selenium";

    public static String ADMIN_PASSWORD = "selenium4CISD";

    public static WebDriver driver;

    private static UidGenerator uid;

    private PageProxy pageProxy;

    private ScreenShotter shotter;

    protected GuiApplicationRunner openbis;

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
    }

    public static void setImplicitWait(long amount, TimeUnit unit)
    {
        driver.manage().timeouts().implicitlyWait(amount, unit);
    }

    public static void setImplicitWaitToDefault()
    {
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
    }

    @AfterSuite
    public void closeBrowser()
    {
        driver.quit();
    }

    @BeforeGroups(groups = "login-admin")
    public void loginAsAdmin()
    {
        this.openbis = new GuiApplicationRunner(new PageProxy(new ScreenShotter()
            {
                @Override
                public void screenshot()
                {
                }
            }), uid);
        openbis.login(ADMIN_USER, ADMIN_PASSWORD);
        // this is because of BIS-184
        openbis.browseToSampleBrowser();

        openbisApi.login(ADMIN_USER, ADMIN_PASSWORD);
    }

    @AfterGroups(groups = "login-admin")
    public void logout()
    {
        this.openbis = new GuiApplicationRunner(new PageProxy(new ScreenShotter()
            {
                @Override
                public void screenshot()
                {
                }
            }), uid);
        openbis.logout();
    }

    @BeforeMethod(alwaysRun = true)
    public void initPageProxy(Method method)
    {
        this.shotter =
                new FileScreenShotter((TakesScreenshot) driver, "targets/dist/"
                        + this.getClass().getSimpleName() + "/" + method.getName());
        this.pageProxy = new PageProxy(shotter);
        this.openbis = new GuiApplicationRunner(this.pageProxy, uid);
    }

    @AfterMethod(alwaysRun = true)
    public void takeScreenShot() throws IOException
    {
        shotter.screenshot();
    }

    public <T> T get(Class<T> clazz)
    {
        return this.pageProxy.get(clazz);
    }

    protected WebDriver browser()
    {
        return driver;
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

    protected DslSampleBrowser sampleBrowser()
    {
        return new DslSampleBrowser(openbis.browseToSampleBrowser());
    }

    protected <T extends Browsable> BrowserRow browserEntryOf(T browsable)
    {
        return browsable.getBrowserContent(openbis);
    }

    protected void emptyTrash()
    {
        openbis.browseToTrash().empty();
    }

    protected RegisterSample sampleRegistrationPageFor(SampleType type)
    {
        openbis.browseToRegisterSample().selectSampleType(type);
        return pageProxy.get(RegisterSample.class);
    }

    protected Matcher<Sample> hasDataSets(DataSet... datasets)
    {
        return new SampleHasDataSetsMatcher(openbis, datasets);
    }

    protected Matcher<WebDriver> isShowing(Class<?> pageClass)
    {
        return new PageMatcher(pageClass, pageProxy);
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

    protected <T> Matcher<Collection<T>> contains(T t)
    {
        return new CollectionContainsMatcher<T>(t);
    }

    protected <T> Matcher<Collection<T>> doesNotContain(T t)
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

    protected SampleTypeUpdateBuilder anUpdateOf(SampleType type)
    {
        return new SampleTypeUpdateBuilder(openbis, type);
    }

    protected void perform(UpdateBuilder builder)
    {
        builder.update();
    }
}
