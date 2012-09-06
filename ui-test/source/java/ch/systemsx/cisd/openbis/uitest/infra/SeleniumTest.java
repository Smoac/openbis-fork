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

import ch.systemsx.cisd.openbis.uitest.infra.matcher.BrowserListsElementMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.PageMatcher;
import ch.systemsx.cisd.openbis.uitest.infra.matcher.SampleBrowserSampleTypeDropDownMenuMatcher;
import ch.systemsx.cisd.openbis.uitest.page.BrowserPage;
import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SpaceBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.VocabularyBrowser;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

public abstract class SeleniumTest
{
    public static WebDriver driver;

    private PageProxy pageProxy;

    private ScreenShotter shotter;

    protected ApplicationRunner openbis;

    @BeforeSuite
    public void initWebDriver()
    {
        // System.setProperty("webdriver.firefox.bin",
        // "/Users/anttil/Desktop/Firefox 10.app/Contents/MacOS/firefox");

        // System.setProperty("webdriver.firefox.profile", "default");

        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
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
            }));
        openbis.login(User.ADMIN);
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
            }));
        openbis.logout();
    }

    @BeforeMethod(alwaysRun = true)
    public void initPageProxy(Method method)
    {
        this.shotter =
                new FileScreenShotter((TakesScreenshot) driver, "targets/dist/"
                        + this.getClass().getSimpleName() + "/" + method.getName());
        this.pageProxy = new PageProxy(shotter);
        this.openbis = new ApplicationRunner(this.pageProxy);
    }

    @AfterMethod(alwaysRun = true)
    public void takeScreenShot() throws IOException
    {
        shotter.screenshot();
    }

    public <T extends Page> T get(Class<T> clazz)
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

    protected SampleTypeBrowser sampleTypeBrowser()
    {
        return openbis.browseToSampleTypeBrowser();
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

    protected Matcher<WebDriver> isShowing(Class<? extends Page> pageClass)
    {
        return new PageMatcher(pageClass, pageProxy);
    }

    protected Matcher<SampleBrowser> showsInToolBar(SampleType sampleType)
    {
        return not(new SampleBrowserSampleTypeDropDownMenuMatcher(sampleType));
    }

    protected Matcher<SampleBrowser> doesNotShowInToolBar(SampleType sampleType)
    {
        return not(new SampleBrowserSampleTypeDropDownMenuMatcher(sampleType));
    }

    protected Matcher<BrowserPage> lists(Browsable browsable)
    {
        return new BrowserListsElementMatcher(browsable);
    }
}
