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

/**
 * Allows to define categories which will be displayed in {@link LeftMenu}.
 * 
 * @author Izabela Adamczyk
 */
class CategoriesBuilder
{

    private final ComponentProvider provider;

    private final List<MenuCategory> categories;

    public CategoriesBuilder(final ComponentProvider provider)
    {
        this.provider = provider;
        categories = new ArrayList<MenuCategory>();
        defineCategories();
    }

    private void defineCategories()
    {
        categories.add(createSampleCategory());
        categories.add(createExperimentCategory());
        categories.add(createMaterialCategory());
        categories.add(createPropertyTypesCategory());
        categories.add(createVocabulariesCategory());
        categories.add(createProjectsTypesCategory());
        categories.add(createGroupsCategory());
        categories.add(createPersonsCategory());
        categories.add(createRolesCategory());
    }

    private MenuCategory createSampleCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
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
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getRolesView()));
        elements.add(new MenuElement("Assign", provider.getDummyComponent()));
        return new MenuCategory("Roles", elements);
    }

    private MenuCategory createGroupsCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getGroupsView()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        return new MenuCategory("Groups", elements);
    }

    private MenuCategory createProjectsTypesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        return new MenuCategory("Projects", elements);
    }

    private MenuCategory createPersonsCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getPersonsView()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        elements.add(new MenuElement("Add role", provider.getDummyComponent()));
        return new MenuCategory("Persons", elements);
    }

    private MenuCategory createPropertyTypesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        return new MenuCategory("Property types", elements);
    }

    private MenuCategory createVocabulariesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        return new MenuCategory("Vocabularies", elements);
    }

    private MenuCategory createMaterialCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement("List", provider.getDummyComponent()));
        elements.add(new MenuElement("List types", provider.getDummyComponent()));
        elements.add(new MenuElement("Register", provider.getDummyComponent()));
        elements.add(new MenuElement("Search", provider.getDummyComponent()));
        return new MenuCategory("Materials", elements);
    }

    private MenuCategory createExperimentCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
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