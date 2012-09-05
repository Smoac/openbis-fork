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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.uitest.page.BrowserPage;
import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.SampleBrowser;

public abstract class SeleniumTest
{
    public static WebDriver driver;

    private PageProxy pageProxy;

    private ScreenShotter shotter;

    protected ApplicationRunner openbis;

    @BeforeSuite
    public void initWebDriver()
    {
        System.setProperty("webdriver.firefox.bin",
                "/Users/anttil/Desktop/Firefox 10.app/Contents/MacOS/firefox");

        System.setProperty("webdriver.firefox.profile", "default");

        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        delete(new File("targets/dist"));

        driver.manage().deleteAllCookies();
        driver.get("https://sprint-openbis.ethz.ch/openbis/");
        // driver.get("http://127.0.0.1:8888/ch.systemsx.cisd.openbis.OpenBIS/index.html?gwt.codesvr=127.0.0.1:9997");

        try
        {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException e)
        {
        }
        this.loginAsAdmin();
        this.logout();
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

    protected Matcher<WebDriver> isShowing(Class<? extends Page> pageClass)
    {
        return new PageMatcher(pageClass, pageProxy);
    }

    private static class PageMatcher extends TypeSafeMatcher<WebDriver>
    {

        private Class<? extends Page> pageClass;

        private PageProxy pageProxy;

        public PageMatcher(Class<? extends Page> pageClass, PageProxy pageProxy)
        {
            this.pageClass = pageClass;
            this.pageProxy = pageProxy;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Browser on page described by " + this.pageClass.getName());
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean matchesSafely(WebDriver ignore)
        {
            Object o = pageProxy.get(pageClass);
            while (pageClass != null)
            {
                for (Field field : pageClass.getDeclaredFields())
                {
                    if ((field.getAnnotation(FindBy.class) != null)
                            && (field.getAnnotation(NotAlwaysPresent.class) == null))
                    {
                        WebElement element;
                        try
                        {
                            field.setAccessible(true);
                            Object potentialWebElement = field.get(o);
                            if (potentialWebElement instanceof Collection)
                            {
                                continue;
                            }
                            element = (WebElement) potentialWebElement;
                        } catch (IllegalArgumentException ex)
                        {
                            ex.printStackTrace();
                            return false;
                        } catch (IllegalAccessException ex)
                        {
                            ex.printStackTrace();
                            return false;
                        }
                        if (!element.isDisplayed())
                        {
                            return false;
                        }
                    }
                }

                pageClass = (Class<? extends Page>) pageClass.getSuperclass();
            }
            return true;
        }

    }

    protected Matcher<Class<SampleBrowser>> showsInToolBar(SampleType sampleType)
    {
        return not(new SampleBrowserSampleTypeListMatcher(sampleType, openbis));
    }

    protected Matcher<Class<SampleBrowser>> doesNotShowInToolBar(SampleType sampleType)
    {
        return not(new SampleBrowserSampleTypeListMatcher(sampleType, openbis));
    }

    private static class SampleBrowserSampleTypeListMatcher extends
            TypeSafeMatcher<Class<SampleBrowser>>
    {

        private SampleType sampleType;

        private ApplicationRunner openbis;

        public SampleBrowserSampleTypeListMatcher(SampleType sampleType, ApplicationRunner openbis)
        {
            this.sampleType = sampleType;
            this.openbis = openbis;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Sample browser listing sample type " + sampleType.getCode()
                    + " in sample type combo box");
        }

        @Override
        public boolean matchesSafely(Class<SampleBrowser> item)
        {
            for (String sampleTypeCode : openbis.browseToSampleBrowser().getSampleTypes())
            {
                if (sampleTypeCode.equalsIgnoreCase(sampleType.getCode()))
                {
                    return true;
                }
            }

            return false;
        }

    }

    protected Matcher<Class<? extends BrowserPage>> listsSampleType(Browsable browsable)
    {
        return new ListsElementMatcher(browsable, new Opener(this.openbis)
            {
                @Override
                public BrowserPage open(Class<? extends BrowserPage> pageClass)
                {
                    return this.openbis.browseToSampleTypeBrowser();
                }

            });
    }

    protected Matcher<Class<? extends BrowserPage>> listsSpace(Browsable browsable)
    {
        return new ListsElementMatcher(browsable, new Opener(this.openbis)
            {
                @Override
                public BrowserPage open(Class<? extends BrowserPage> pageClass)
                {
                    return this.openbis.browseToSpaceBrowser();
                }

            });
    }

    private abstract class Opener
    {
        protected ApplicationRunner openbis;

        public Opener(ApplicationRunner openbis)
        {
            this.openbis = openbis;
        }

        public abstract BrowserPage open(Class<? extends BrowserPage> pageClass);

    }

    private static class ListsElementMatcher extends
            TypeSafeMatcher<Class<? extends BrowserPage>>
    {
        private Browsable expected;

        private Opener opener;

        public ListsElementMatcher(Browsable expected, Opener opener)
        {
            this.expected = expected;
            this.opener = opener;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("SampleTypeBrowser that contains element " + this.expected);
        }

        @Override
        public boolean matchesSafely(Class<? extends BrowserPage> actual)
        {
            BrowserPage browser = opener.open(actual);
            for (Map<String, String> row : browser.getTableContent())
            {
                if (this.expected.isRepresentedBy(row))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
