/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder.CATEGORIES;

/**
 * Category in the menu.
 * 
 * @author Izabela Adamczyk
 */
public class MenuCategory
{

    private final String name;

    private final List<MenuElement> elements;

    private final String partOfId;

    public MenuCategory(final CATEGORIES partOfId, final String name,
            final List<MenuElement> elements)
    {
        this.partOfId = partOfId.name();
        this.name = name;
        this.elements = elements;
    }

    public String getName()
    {
        return name;
    }

    public List<MenuElement> getElements()
    {
        return elements;
    }

    public String getPartOfId()
    {
        return partOfId;
    }
}
