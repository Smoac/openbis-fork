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

package ch.systemsx.cisd.openbis.uitest.infra;

import ch.systemsx.cisd.openbis.uitest.page.LoginPage;
import ch.systemsx.cisd.openbis.uitest.page.NavigationPage;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddVocabularyDialog;
import ch.systemsx.cisd.openbis.uitest.page.dialog.EditSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.tab.AddPropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.AssignSamplePropertyType;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.page.tab.RoleAssignmentBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.SpaceBrowser;
import ch.systemsx.cisd.openbis.uitest.page.tab.VocabularyBrowser;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;

/**
 * @author anttil
 */
public class ApplicationRunner
{

    private PageProxy proxy;

    public ApplicationRunner(PageProxy proxy)
    {
        this.proxy = proxy;
    }

    public Space create(Space space)
    {
        AddSpaceDialog dialog = browseToAddSpaceDialog();
        dialog.fillWith(space);
        dialog.save();
        return space;
    }

    public SampleType create(SampleType sampleType)
    {
        AddSampleTypeDialog dialog = browseToAddSampleTypeDialog();
        dialog.fillWith(sampleType);
        dialog.save();
        return sampleType;
    }

    public Vocabulary create(Vocabulary vocabulary)
    {
        AddVocabularyDialog dialog = browseToAddVocabularyDialog();
        dialog.fillWith(vocabulary);
        dialog.save();
        return vocabulary;
    }

    public PropertyType create(PropertyType propertyType)
    {
        AddPropertyType dialog = browseToAddPropertyType();
        dialog.fillWith(propertyType);
        dialog.save();
        return propertyType;
    }

    public Sample create(Sample sample)
    {
        RegisterSample register = browseToRegisterSample();
        register.selectSampleType(sample.getType());
        register.fillWith(sample);
        register.save();
        return sample;
    }

    public PropertyTypeAssignment create(PropertyTypeAssignment assignment)
    {
        AssignSamplePropertyType assign = browseToAssignSamplePropertyType();
        assign.fillWith(assignment);
        assign.save();
        return assignment;
    }

    public void update(SampleType sampleType)
    {
        SampleTypeBrowser sampleTypeBrowser = browseToSampleTypeBrowser();
        EditSampleTypeDialog dialog = sampleTypeBrowser.editSampleType(sampleType);
        dialog.fillWith(sampleType);
        dialog.save();
    }

    public void login(String userName, String password)
    {
        LoginPage loginPage = proxy.get(LoginPage.class);
        loginPage.loginAs(userName, password);
    }

    public void login(User user)
    {
        LoginPage loginPage = proxy.get(LoginPage.class);
        loginPage.loginAs(user.getName(), user.getPassword());
    }

    public void logout()
    {
        getMenus().user().logout();
    }

    public SampleTypeBrowser browseToSampleTypeBrowser()
    {
        return getMenus().admin().types().sampleTypes();
    }

    public AddSampleTypeDialog browseToAddSampleTypeDialog()
    {
        return browseToSampleTypeBrowser().add();
    }

    public SpaceBrowser browseToSpaceBrowser()
    {
        return getMenus().admin().spaces();
    }

    public AddSpaceDialog browseToAddSpaceDialog()
    {
        return browseToSpaceBrowser().addSpace();
    }

    public SampleBrowser browseToSampleBrowser()
    {
        return getMenus().browse().samples();
    }

    public RoleAssignmentBrowser browseToRoleAssignmentBrowser()
    {
        return getMenus().admin().authorization().roles();
    }

    public VocabularyBrowser browseToVocabularyBrowser()
    {
        return getMenus().admin().vocabularies();
    }

    public AddVocabularyDialog browseToAddVocabularyDialog()
    {
        return browseToVocabularyBrowser().add();
    }

    public PropertyTypeBrowser browseToPropertyTypeBrowser()
    {
        return getMenus().admin().metadata().propertyTypes();
    }

    public AddPropertyType browseToAddPropertyType()
    {
        return getMenus().admin().metadata().newPropertyType();
    }

    public AssignSamplePropertyType browseToAssignSamplePropertyType()
    {
        return getMenus().admin().metadata().assignToSampleType();
    }

    public PropertyTypeAssignmentBrowser browseToPropertyTypeAssignmentBrowser()
    {
        return getMenus().admin().metadata().propertyTypeAssignments();
    }

    public RegisterSample browseToRegisterSample()
    {
        return getMenus().newMenu().sample();
    }

    public void closeAllTabs()
    {
        getMenus().closeTabs();
    }

    private NavigationPage getMenus()
    {
        return proxy.get(NavigationPage.class);
    }
}
