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

package ch.systemsx.cisd.openbis.uitest.page.common;

import org.openqa.selenium.WebElement;

/**
 * @author anttil
 */
public class Cell
{
    private final String text;

    private final String url;

    private final WebElement element;

    public Cell(String text, String url, WebElement element)
    {
        this.text = text;
        this.url = url;
        this.element = element;
    }

    public String getText()
    {
        return text;
    }

    public String getUrl()
    {
        return url;
    }

    public WebElement getElement()
    {
        return this.element;
    }

    public boolean hasLink()
    {
        return this.url != null;
    }

    @Override
    public String toString()
    {
        return "Cell[text=" + text + ", url=" + url + "]";
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Cell))
        {
            return false;
        }

        Cell cell = (Cell) o;
        return toString().equals(cell.toString());
    }
}
