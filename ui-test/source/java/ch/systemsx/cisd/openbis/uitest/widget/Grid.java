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

package ch.systemsx.cisd.openbis.uitest.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.Cell;
import ch.systemsx.cisd.openbis.uitest.infra.Contextual;
import ch.systemsx.cisd.openbis.uitest.infra.Refreshing;
import ch.systemsx.cisd.openbis.uitest.infra.Row;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WidgetWebElement;
import ch.systemsx.cisd.openbis.uitest.suite.SeleniumTest;

/**
 * @author anttil
 */
public class Grid implements Contextual, Refreshing
{

    private WidgetWebElement context;

    public Row select(String column, String value)
    {

        List<WebElement> columns = this.getColumns();

        int index = 0;
        boolean found = false;
        for (WebElement element : columns)
        {
            if (element.getText().equalsIgnoreCase(column))
            {
                found = true;
                break;
            }
            index++;
        }

        if (!found)
        {
            throw new IllegalArgumentException("Column " + column + " does not exist");
        }

        int numColumns = columns.size();

        List<WebElement> cells = this.getCells();
        found = false;
        for (; index < cells.size(); index += numColumns)
        {
            if (cells.get(index).getText().equalsIgnoreCase(value))
            {
                cells.get(index).findElement(By.xpath("./..")).click();
                found = true;
                break;
            }
        }

        if (!found)
        {
            return new Row();
        }

        index = index - (index % numColumns);

        Map<String, Cell> m = new HashMap<String, Cell>();
        for (int i = 0; i < numColumns; i++)
        {
            WebElement element = cells.get(i + index);
            m.put(columns.get(i).getText(),
                    new Cell(element.getText(), element.getAttribute("href")));
        }
        return new Row(m);
    }

    private List<WebElement> getColumns()
    {
        return context
                .findAll(".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-header ')]//span[not(*)]");
    }

    private List<WebElement> getCells()
    {
        SeleniumTest.setImplicitWait(500, TimeUnit.MILLISECONDS);
        try
        {
            return context
                    .findAll(".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-col ')]//*[not(*)]");
        } finally
        {
            SeleniumTest.setImplicitWaitToDefault();
        }
    }

    @Override
    public String toString()
    {
        List<WebElement> columns = getColumns();
        String s = "";
        for (WebElement column : columns)
        {
            s += column.getText() + "\t";
        }
        s += "\n";

        int counter = 0;
        for (WebElement cell : getCells())
        {
            s += cell.getText() + "\t";
            counter++;
            if (counter % columns.size() == 0)
            {
                s += "\n";
            }
        }
        return s;
    }

    @Override
    public synchronized String getState()
    {
        return "" + this.getCells().size();
    }

    @Override
    public void setContext(WidgetWebElement context)
    {
        this.context = context;
    }
}
