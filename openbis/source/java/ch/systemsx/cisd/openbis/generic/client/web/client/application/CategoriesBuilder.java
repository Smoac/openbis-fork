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

import java.util.ArrayList;
import java.util.List;

class CategoriesBuilder
{

    private final ComponentProvider provider;

    private final List<MenuCategory> categories;

    public CategoriesBuilder(ComponentProvider provider)
    {
        this.provider = provider;
        categories = new ArrayList<MenuCategory>();
        defineCategories();
    }

    private void defineCategories()
    {
        categories.add(createSampleCategory());
        categories.add(createRolesCategory());
        categories.add(createGroupsCategory());
        categories.add(createProjectsTypesCategory());
        categories.add(createPersonsCategory());
        categories.add(createPropertyTypesCategory());
        categories.add(createVocabulariesCategory());
        categories.add(createMaterialCategory());
        categories.add(createExperimentCategory());
    }

    private MenuCategory createSampleCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getSampleBrowser()));
        elements.add(new MenuElement("List types", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        elements.add(new MenuElement("Invalidate", provider.getDummyComponent()));
        elements.add(new MenuElement("Assign properties", provider.getDummyComponent()));
        elements.add(new MenuElement("Search", provider.getDummyComponent()));
        return new MenuCategory("Samples", elements);
    }

    private MenuCategory createRolesCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        elements.add(new MenuElement("Assign", provider.getDummyComponent()));
        return new MenuCategory("Roles", elements);
    }

    private MenuCategory createGroupsCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        return new MenuCategory("Groups", elements);
    }

    private MenuCategory createProjectsTypesCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        return new MenuCategory("Projects", elements);
    }

    private MenuCategory createPersonsCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        elements.add(new MenuElement("Add role", provider.getDummyComponent()));
        return new MenuCategory("Persons", elements);
    }

    private MenuCategory createPropertyTypesCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        return new MenuCategory("Property types", elements);
    }

    private MenuCategory createVocabulariesCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        return new MenuCategory("Vocabularies", elements);
    }

    private MenuCategory createMaterialCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("List types", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        elements.add(new MenuElement("Search", provider.getDummyComponent()));
        return new MenuCategory("Materials", elements);
    }

    private MenuCategory createExperimentCategory()
    {
        List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("List types", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        elements.add(new MenuElement("Invalidate", provider.getDummyComponent()));
        elements.add(new MenuElement("Assign properties", provider.getDummyComponent()));
        elements.add(new MenuElement("Search", provider.getDummyComponent()));
        return new MenuCategory("Experiments", elements);
    }

    public List<MenuCategory> getCategories()
    {
        return categories;
    }
}