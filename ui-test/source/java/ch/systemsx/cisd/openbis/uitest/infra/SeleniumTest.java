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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.uitest.page.LoginPage;
import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.SpaceBrowser;

public abstract class SeleniumTest
{
    public static WebDriver driver;

    protected PageProxy pageProxy;

    protected LoginPage loginPage;

    private ScreenShotter shotter;

    @BeforeSuite
    public void initWebDriver()
    {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        delete(new File("targets/selenium"));
    }

    @AfterSuite
    public void closeBrowser()
    {
        driver.quit();
    }

    @BeforeMethod
    public void initPageProxy(Method method)
    {
        this.shotter =
                new ScreenShotter((TakesScreenshot) driver, "targets/dist/"
                        + this.getClass().getSimpleName() + "/" + method.getName());
        this.pageProxy = new PageProxy(shotter);
    }

    @BeforeMethod(dependsOnMethods = "initPageProxy")
    public void gotoLoginPage()
    {
        driver.manage().deleteAllCookies();
        driver.get("https://sprint-openbis.ethz.ch/openbis/");
        try
        {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException e)
        {
        }
        this.loginPage = get(LoginPage.class);
    }

    @AfterMethod
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

        @Override
        public boolean matchesSafely(WebDriver ignore)
        {
            Object o = pageProxy.get(pageClass);
            for (Field field : pageClass.getDeclaredFields())
            {
                if ((field.getAnnotation(FindBy.class) != null)
                        && (field.getAnnotation(NotAlwaysPresent.class) == null))
                {
                    WebElement element;
                    try
                    {
                        field.setAccessible(true);
                        element = (WebElement) field.get(o);
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
            return true;
        }

    }

    protected Matcher<SpaceBrowser> listsSpace(String spaceName)
    {
        return new SpaceMatcher(spaceName);
    }

    private static class SpaceMatcher extends TypeSafeMatcher<SpaceBrowser>
    {

        private String expected;

        public SpaceMatcher(String expected)
        {
            this.expected = expected;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Given list contains a space named " + expected);
        }

        @Override
        public boolean matchesSafely(SpaceBrowser browser)
        {
            for (String space : browser.getSpaces())
            {
                if (this.expected.equalsIgnoreCase(space))
                {
                    return true;
                }
            }
            return false;
        }

    }

    protected String PWD = "selenium4CISD";
}
