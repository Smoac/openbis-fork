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

package ch.systemsx.cisd.openbis.uitest.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.Browsable;
import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;

/**
 * @author anttil
 */
public abstract class BrowserPage extends NavigationPage
{

    protected abstract List<WebElement> getColumns();

    protected abstract List<WebElement> getData();

    public Collection<Map<String, Cell>> getTableContent()
    {
        List<Map<String, Cell>> content = new ArrayList<Map<String, Cell>>();

        List<WebElement> columnNames = getColumns();
        List<WebElement> gridValues = getData();

        List<String> columns = new ArrayList<String>();
        for (WebElement columnName : columnNames)
        {
            columns.add(columnName.getText());
        }

        int index = 0;
        Map<String, Cell> map = new HashMap<String, Cell>();

        for (WebElement element : gridValues)
        {
            String columnName = columns.get(index % columns.size());
            map.put(columnName, new Cell(element.getText(), element.getAttribute("href"), element));

            index++;

            if (index % columns.size() == 0)
            {
                content.add(map);
                map = new HashMap<String, Cell>();
            }
        }
        return content;
    }

    public Cell cell(Browsable browsable, String columnName)
    {
        for (Map<String, Cell> row : getTableContent())
        {
            if (browsable.isRepresentedBy(row))
            {
                return row.get(columnName);
            }
        }
        throw new IllegalStateException("Could not find " + browsable + " from " + toString());
    }

    protected abstract WebElement getDeleteButton();

    public boolean deleteIfExists(String column, String value)
    {
        for (Map<String, Cell> row : getTableContent())
        {

            Cell cell = row.get(column);
            if (cell != null && cell.getText().equalsIgnoreCase(value))
            {
                cell.getElement().findElement(By.xpath("./..")).click();
                waitForClickability(getDeleteButton());
                getDeleteButton().click();

                for (WebElement ellu : this.findElements(cell.getElement(),
                        "//*[@id='deletion-confirmation-dialog']//textarea"))
                {
                    ellu.sendKeys("reason for deletion");
                }

                this.findElement(cell.getElement(),
                        "//*[@id='deletion-confirmation-dialog']//button[text()='OK' or text()='Yes']")
                        .click();

                return true;
            }
        }
        return false;
    }

    @Override
    public String toString()
    {
        String value = getClass().getSimpleName() + "\n";
        int numColumns = 0;
        for (WebElement column : getColumns())
        {
            value += column.getText() + "\t";
            numColumns++;
        }

        int counter = 0;
        for (WebElement cell : getData())
        {

            if (counter % numColumns == 0)
            {
                value += "\n";
            }
            value += "-" + cell.getText() + "-\t";
            counter++;
        }

        return value;

    }

    protected String getXPath(WebElement webElement)
    {
        String jscript = "function getPathTo(node) {" +
                "  var stack = [];" +
                "  while(node.parentNode !== null) {" +
                "    stack.unshift(node.tagName);" +
                "    node = node.parentNode;" +
                "  }" +
                "  return stack.join('/');" +
                "}" +
                "return getPathTo(arguments[0]);";
        return (String) ((JavascriptExecutor) SeleniumTest.driver).executeScript(jscript,
                webElement);
    }
}
