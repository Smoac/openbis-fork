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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class Help
{

    public static WebElement findElementWithText(String text, By by) {
        WebElement element = null;
        for (WebElement e : SeleniumTest.driver.findElements(by)) {
            if (e.getText().equals(text)) {
                element = e;
                break;
            }
        }
        assertThat(element, is(notNullValue()));
        
        return (WebElement) ScreenShotProxy.newInstance(element);    
    }
    
    public static void wait(final By by) {

        ExpectedCondition<?> condition =  new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(WebDriver d) {
                return d.findElement(by);
            }
        };
        
        new WebDriverWait(SeleniumTest.driver, 10).until(condition);
    }
    
}
