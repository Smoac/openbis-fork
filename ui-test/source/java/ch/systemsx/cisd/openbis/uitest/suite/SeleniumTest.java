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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.uitest.infra.ApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.infra.Browsable;
import ch.systemsx.cisd.openbis.uitest.infra.Browser;
import ch.systemsx.cisd.openbis.uitest.infra.Cell;
import ch.systemsx.cisd.openbis.uitest.infra.User;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.BrowserListsElementMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.CellDisplaysMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.CellLinksToMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.PageMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.RegisterSampleFormContainsInputsForPropertiesMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.SampleBrowserSampleTypeDropDownMenuMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.screenshot.FileScreenShotter;
import ch.systemsx.cisd.openbis.uitest.infra.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.infra.uid.DictionaryUidGenerator;
import ch.systemsx.cisd.openbis.uitest.infra.uid.UidGenerator;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.PageProxy;
import ch.systemsx.cisd.openbis.uitest.page.tab.ExperimentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.ExperimentTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.ProjectBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SpaceBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.Trash;
import ch.systemsx.cisd.openbis.uitest.page.tab.VocabularyBrowser;
import ch.systemsx.cisd.openbis.uitest.type.Builder;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentBuilder;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.ProjectBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignmentBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
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
    public static int IMPLICIT_WAIT = 20;

    public static WebDriver driver;

    private static UidGenerator uid;

    private PageProxy pageProxy;

    private ScreenShotter shotter;

    protected ApplicationRunner openbis;

    @BeforeSuite
    public void initWebDriver() throws Exception
    {
        uid = new DictionaryUidGenerator(new File("resource/corncob_lowercase.txt"));

        System.setProperty("webdriver.firefox.profile", "default");

        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
        delete(new File("targets/dist"));

        driver.manage().deleteAllCookies();

        String url = System.getProperty("ui-test.url");
        if (url == null || url.length() == 0)
        {
            url =
                    "http://127.0.0.1:8888/ch.systemsx.cisd.openbis.OpenBIS/index.html?gwt.codesvr=127.0.0.1:9997";
        }

        driver.get(url);
    }

    @AfterSuite
    public void closeBrowser()
    {
        driver.quit();
    }

    @BeforeGroups(groups = "login-admin")
    public void loginAsAdmin()
    {
        this.openbis = new ApplicationRunner(new PageProxy(new ScreenShotter()
            {
                @Override
                public void screenshot()
                {
                }
            }), uid);
        openbis.login(User.ADMIN);

        // this is because of BIS-184
        sampleBrowser();
    }

    @AfterGroups(groups = "login-admin")
    public void logout()
    {
        this.openbis = new ApplicationRunner(new PageProxy(new ScreenShotter()
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
        this.openbis = new ApplicationRunner(this.pageProxy, uid);
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

    protected SampleBrowser sampleBrowser()
    {
        return openbis.browseToSampleBrowser();
    }

    protected ExperimentBrowser experimentBrowser()
    {
        return openbis.browseToExperimentBrowser();
    }

    protected Trash trash()
    {
        return openbis.browseToTrash();
    }

    protected SampleTypeBrowser sampleTypeBrowser()
    {
        return openbis.browseToSampleTypeBrowser();
    }

    protected ExperimentTypeBrowser experimentTypeBrowser()
    {
        return openbis.browseToExperimentTypeBrowser();
    }

    protected VocabularyBrowser vocabularyBrowser()
    {
        return openbis.browseToVocabularyBrowser();
    }

    protected PropertyTypeBrowser propertyTypeBrowser()
    {
        return openbis.browseToPropertyTypeBrowser();
    }

    protected SpaceBrowser spaceBrowser()
    {
        return openbis.browseToSpaceBrowser();
    }

    protected ProjectBrowser projectBrowser()
    {
        return openbis.browseToProjectBrowser();
    }

    protected RegisterSample sampleRegistrationPageFor(SampleType type)
    {
        openbis.browseToRegisterSample().selectSampleType(type);
        return pageProxy.get(RegisterSample.class);
    }

    protected PropertyTypeAssignmentBrowser propertyTypeAssignmentBrowser()
    {
        return openbis.browseToPropertyTypeAssignmentBrowser();
    }

    protected Matcher<WebDriver> isShowing(Class<?> pageClass)
    {
        return new PageMatcher(pageClass, pageProxy);
    }

    protected Matcher<SampleBrowser> showsInToolBar(SampleType sampleType)
    {
        return new SampleBrowserSampleTypeDropDownMenuMatcher(sampleType);
    }

    protected Matcher<SampleBrowser> doesNotShowInToolBar(SampleType sampleType)
    {
        return not(new SampleBrowserSampleTypeDropDownMenuMatcher(sampleType));
    }

    protected <T extends Browsable, U extends Browser<T>> Matcher<U> lists(T browsable)
    {
        return new BrowserListsElementMatcher<T, U>(browsable);
    }

    protected <T extends Browsable, U extends Browser<T>> Matcher<U> doesNotList(T browsable)
    {
        return not(new BrowserListsElementMatcher<T, U>(browsable));
    }

    protected Matcher<RegisterSample> hasInputsForProperties(PropertyType... fields)
    {
        return new RegisterSampleFormContainsInputsForPropertiesMatcher(fields);
    }

    protected Matcher<Cell> linksTo(String url)
    {
        return new CellLinksToMatcher(url);
    }

    protected Matcher<Cell> displays(String text)
    {
        return new CellDisplaysMatcher(text);
    }

    protected <T> T create(Builder<T> builder)
    {
        return builder.create();
    }

    protected void delete(Space space)
    {
        openbis.delete(space);
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

    protected SampleTypeUpdateBuilder anUpdateOf(SampleType type)
    {
        return new SampleTypeUpdateBuilder(openbis, type);
    }

    protected void perform(UpdateBuilder builder)
    {
        builder.update();
    }
}
