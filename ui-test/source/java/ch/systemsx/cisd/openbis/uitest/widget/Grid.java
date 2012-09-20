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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.page.common.Cell;
import ch.systemsx.cisd.openbis.uitest.page.common.Row;

/**
 * @author anttil
 */
public class Grid extends Widget
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
            throw new IllegalArgumentException("Row with value " + value + " in column " + column
                    + " not found");
        }

        index = index - (index % numColumns);

        Map<String, Cell> m = new HashMap<String, Cell>();
        for (int i = 0; i < numColumns; i++)
        {
            WebElement element = cells.get(i + index);
            m.put(columns.get(i).getText(),
                    new Cell(element.getText(), element.getAttribute("href"), element));
        }
        return new Row(m);
    }

    public List<WebElement> getColumns()
    {
        return context
                .findElements(By
                        .xpath(".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-header ')]//span[not(*)]"));
    }

    public List<WebElement> getCells()
    {
        return context
                .findElements(By
                        .xpath(".//td[not(ancestor::div[contains(@style,'display:none')]) and contains(@class, 'x-grid') and contains(@class, '-col ')]//*[not(*)]"));
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
}
