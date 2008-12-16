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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.MenuCategory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.MenuElement;

/**
 * Allows to define categories which will be displayed in {@link LeftMenu}.
 * 
 * @author Izabela Adamczyk
 */
public class CategoriesBuilder
{
    public static class CATEGORIES
    {
        public static final String VOCABULARIES = "VOCABULARIES";

        public static final String MATERIALS = "MATERIALS";

        public static final String EXPERIMENTS = "EXPERIMENTS";

        public static final String PERSONS = "PERSONS";

        public static final String PROJECTS = "PROJECTS";

        public static final String GROUPS = "GROUPS";

        public static final String ROLES = "ROLES";

        public static final String SAMPLES = "SAMPLES";

        public static final String PROPERTY_TYPES = "PROPERTY_TYPES";
    }

    public static class MENU_ELEMENTS
    {

        public static final String ADD_ROLE = "ADD_ROLE";

        public static final String SEARCH = "SEARCH";

        public static final String LIST_TYPES = "LIST_TYPES";

        public static final String INVALIDATE = "INVALIDATE";

        public static final String ASSIGN_PROPERTIES = "ASSIGN_PROPERTIES";

        public static final String ASSIGN = "ASSIGN";

        public static final String ASSIGN_STPT = "ASSIGN_STPT";

        public static final String ASSIGN_ETPT = "ASSIGN_ETPT";

        public static final String REGISTER = "REGISTER";

        public static final String REGISTER_FROM_FILE = "REGISTER_FROM_FILE";

        public static final String LIST = "LIST";

        public static final String MANAGE = "MANAGE";

        public static final String LIST_ASSIGNMENTS = "LIST_ASSIGNMENTS";
    }

    public final ComponentProvider provider;

    public final List<MenuCategory> categories;

    CategoriesBuilder(final ComponentProvider provider)
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
        elements.add(new MenuElement(MENU_ELEMENTS.LIST, "List", provider.getSampleBrowser()));
        elements.add(new MenuElement(MENU_ELEMENTS.LIST_TYPES, "List types", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.REGISTER, "Register", provider
                .getSampleRegistration()));
        elements.add(new MenuElement(MENU_ELEMENTS.REGISTER_FROM_FILE, "Register from file",
                provider.getSampleBatchRegistration()));
        elements.add(new MenuElement(MENU_ELEMENTS.INVALIDATE, "Invalidate", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.ASSIGN_PROPERTIES, "Assign properties", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.SEARCH, "Search", provider.getDummyComponent()));
        return new MenuCategory(CATEGORIES.SAMPLES, "Samples", elements);
    }

    private MenuCategory createRolesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements
                .add(new MenuElement(MENU_ELEMENTS.LIST, "List and Assign", provider.getRolesView()));
        return new MenuCategory(CATEGORIES.ROLES, "Roles", elements);
    }

    private MenuCategory createGroupsCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MENU_ELEMENTS.LIST, "List and Add", provider.getGroupsView()));
        return new MenuCategory(CATEGORIES.GROUPS, "Groups", elements);
    }

    private MenuCategory createProjectsTypesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MENU_ELEMENTS.LIST, "List", provider.getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.REGISTER, "Register", provider
                .getDummyComponent()));
        return new MenuCategory(CATEGORIES.PROJECTS, "Projects", elements);
    }

    private MenuCategory createPersonsCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements
                .add(new MenuElement(MENU_ELEMENTS.LIST, "List and Add", provider.getPersonsView()));
        return new MenuCategory(CATEGORIES.PERSONS, "Persons", elements);
    }

    private MenuCategory createPropertyTypesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements
                .add(new MenuElement(MENU_ELEMENTS.LIST, "List", provider.getPropertyTypeBrowser()));
        elements.add(new MenuElement(MENU_ELEMENTS.LIST_ASSIGNMENTS, "List assignments", provider
                .getPropertyTypeAssignmentBrowser()));
        elements.add(new MenuElement(MENU_ELEMENTS.REGISTER, "Register", provider
                .getPropertyTypeRegistration()));
        elements.add(new MenuElement(MENU_ELEMENTS.ASSIGN_ETPT, "Assign to expriment type",
                provider.getPropertyTypeExperimentTypeAssignmentForm()));
        elements.add(new MenuElement(MENU_ELEMENTS.ASSIGN_STPT, "Assign to sample type", provider
                .getPropertyTypeSampleTypeAssignmentForm()));
        return new MenuCategory(CATEGORIES.PROPERTY_TYPES, "Property types", elements);
    }

    private MenuCategory createVocabulariesCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MENU_ELEMENTS.LIST, "List", provider.getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.REGISTER, "Register", provider
                .getDummyComponent()));
        return new MenuCategory(CATEGORIES.VOCABULARIES, "Vocabularies", elements);
    }

    private MenuCategory createMaterialCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MENU_ELEMENTS.LIST, "List", provider.getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.LIST_TYPES, "List types", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.REGISTER, "Register", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.SEARCH, "Search", provider.getDummyComponent()));
        return new MenuCategory(CATEGORIES.MATERIALS, "Materials", elements);
    }

    private MenuCategory createExperimentCategory()
    {
        final List<MenuElement> elements = new ArrayList<MenuElement>();
        elements.add(new MenuElement(MENU_ELEMENTS.LIST, "List", provider.getExperimentBrowser()));
        elements.add(new MenuElement(MENU_ELEMENTS.LIST_TYPES, "List types", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.REGISTER, "Register", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.INVALIDATE, "Invalidate", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.ASSIGN_PROPERTIES, "Assign properties", provider
                .getDummyComponent()));
        elements.add(new MenuElement(MENU_ELEMENTS.SEARCH, "Search", provider.getDummyComponent()));
        return new MenuCategory(CATEGORIES.EXPERIMENTS, "Experiments", elements);
    }

    public List<MenuCategory> getCategories()
    {
        return categories;
    }
}