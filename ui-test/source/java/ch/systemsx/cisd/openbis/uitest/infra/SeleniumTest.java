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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.openbis.uitest.page.LoginPage;
import ch.systemsx.cisd.openbis.uitest.page.Page;
import ch.systemsx.cisd.openbis.uitest.page.SpaceBrowser;

public abstract class SeleniumTest
{
    public static final WebDriver driver = new FirefoxDriver();
    {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    protected LoginPage loginPage;

    @BeforeMethod
    public void gotoLoginPage()
    {
        driver.manage().deleteAllCookies();
        driver.get("https://sprint-openbis.ethz.ch/openbis/");
        this.loginPage = get(LoginPage.class);
    }

    @AfterMethod
    public void takeScreenShot() throws IOException
    {
        ScreenShotProxy.screenshot();
    }

    @AfterSuite
    public void closeBrowser()
    {
        driver.quit();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Page> T get(Class<T> clazz)
    {

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);

        MethodHandler handler = new MethodHandler()
            {
                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args)
                        throws Throwable
                {
                    try
                    {
                        return proceed.invoke(self, args);
                    } catch (InvocationTargetException e)
                    {
                        if (e.getTargetException() instanceof StaleElementReferenceException)
                        {
                            PageFactory.initElements(new ScreenShotDecorator(), self);
                            return proceed.invoke(self, args);
                        } else
                        {
                            throw e.getTargetException();
                        }
                    }
                }
            };

        T t;
        try
        {
            t = (T) factory.create(new Class<?>[]
                { WebDriver.class }, new Object[]
                { driver }, handler);
        } catch (IllegalArgumentException ex)
        {
            throw new RuntimeException(ex);
        } catch (SecurityException ex)
        {
            throw new RuntimeException(ex);
        } catch (InstantiationException ex)
        {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex)
        {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex)
        {
            try
            {
                t = (T) factory.create(new Class<?>[0], new Object[0], handler);
            } catch (IllegalArgumentException ex1)
            {
                throw new RuntimeException(ex1);
            } catch (NoSuchMethodException ex1)
            {
                throw new RuntimeException(ex1);
            } catch (InstantiationException ex1)
            {
                throw new RuntimeException(ex1);
            } catch (IllegalAccessException ex1)
            {
                throw new RuntimeException(ex1);
            } catch (InvocationTargetException ex1)
            {
                throw new RuntimeException(ex1);
            }

        }
        PageFactory.initElements(new ScreenShotDecorator(), t);
        return t;
    }

    protected static WebDriver browser()
    {
        return driver;
    }

    protected static Matcher<WebDriver> isShowing(Class<? extends Page> pageClass)
    {
        return new PageMatcher(pageClass);
    }

    private static class PageMatcher extends TypeSafeMatcher<WebDriver>
    {

        private Class<? extends Page> pageClass;

        public PageMatcher(Class<? extends Page> pageClass)
        {
            this.pageClass = pageClass;
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Browser on page described by " + this.pageClass.getName());
        }

        @Override
        public boolean matchesSafely(WebDriver ignore)
        {
            Object o = get(pageClass);
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

    protected static Matcher<SpaceBrowser> listsSpace(String spaceName)
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
