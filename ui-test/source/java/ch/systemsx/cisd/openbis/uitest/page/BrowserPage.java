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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;

/**
 * @author anttil
 */
public abstract class BrowserPage extends PrivatePage
{

    protected abstract List<WebElement> getColumns();

    protected abstract List<WebElement> getData();

    public Collection<Map<String, String>> getTableContent()
    {
        List<Map<String, String>> content = new ArrayList<Map<String, String>>();

        List<WebElement> columnNames = arrange(getColumns());
        List<WebElement> gridValues = arrange(getData());
        int index = 0;
        Map<String, String> map = new HashMap<String, String>();
        for (WebElement element : gridValues)
        {
            String columnName = columnNames.get(index % columnNames.size()).getText();
            map.put(columnName, element.getText());

            index++;

            if (index % columnNames.size() == 0)
            {
                content.add(map);
                map = new HashMap<String, String>();
            }
        }
        return content;
    }

    private List<WebElement> arrange(Collection<WebElement> elements)
    {
        List<WebElement> sorted = new ArrayList<WebElement>(elements);
        Collections.sort(sorted, new Comparator<WebElement>()
            {

                @Override
                public int compare(WebElement o1, WebElement o2)
                {
                    int yDiff = o1.getLocation().getY() - o2.getLocation().getY();
                    if (yDiff == 0)
                    {
                        return o1.getLocation().getX() - o2.getLocation().getX();
                    } else
                    {
                        return yDiff;
                    }
                }
            });
        return sorted;
    }
}
