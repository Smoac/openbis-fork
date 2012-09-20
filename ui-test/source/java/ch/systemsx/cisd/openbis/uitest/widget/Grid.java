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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.Cell;
import ch.systemsx.cisd.openbis.uitest.infra.Row;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Refreshing;

/**
 * @author anttil
 */
public class Grid extends Widget implements Refreshing
{

    public Row getRow(String column, String value)
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
        return findAll(".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-header ')]//span[not(*)]");
    }

    private List<WebElement> getCells()
    {
        return findAll(".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-col ')]//*[not(*)]");
    }

    public void select(String string)
    {
        Collection<String> found = new ArrayList<String>();
        for (WebElement element : getCells())
        {
            if (string.equalsIgnoreCase(element.getText()))
            {
                element.click();
                return;
            }
            found.add(element.getText());
        }

        throw new IllegalArgumentException("Grid does not contain element with text " + string
                + ", found " + found);
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

    boolean itsOn = false;

    int last = 0;

    @Override
    public synchronized boolean hasRefreshed()
    {
        if (itsOn)
        {
            if (this.last != getCells().size())
            {
                this.itsOn = false;
                return true;
            } else
            {
                return false;
            }
        } else
        {
            itsOn = true;
            this.last = getCells().size();
            return false;
        }

    }
}
