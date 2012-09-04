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

import ch.systemsx.cisd.openbis.uitest.page.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.page.AddSpaceDialog;
import ch.systemsx.cisd.openbis.uitest.page.LoginPage;
import ch.systemsx.cisd.openbis.uitest.page.PrivatePage;
import ch.systemsx.cisd.openbis.uitest.page.SampleTypeBrowser;
import ch.systemsx.cisd.openbis.uitest.page.SpaceBrowser;

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

    public void createSpace(Space space)
    {
        AddSpaceDialog dialog = browseToAddSpaceDialog();
        dialog.fillWith(space);
        dialog.save();
    }

    public void create(SampleType sampleType)
    {
        AddSampleTypeDialog dialog = browseToAddSampleTypeDialog();
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

    public AddSampleTypeDialog browseToAddSampleTypeDialog()
    {
        PrivatePage p = proxy.get(PrivatePage.class);
        return p.adminMenu().types().sampleTypes().add();
    }

    public SampleTypeBrowser browseToSampleTypeBrowser()
    {
        PrivatePage p = proxy.get(PrivatePage.class);
        return p.adminMenu().types().sampleTypes();
    }

    public SpaceBrowser browseToSpaceBrowser()
    {
        PrivatePage p = proxy.get(PrivatePage.class);
        return p.adminMenu().spaces();
    }

    public AddSpaceDialog browseToAddSpaceDialog()
    {
        PrivatePage p = proxy.get(PrivatePage.class);
        return p.adminMenu().spaces().addSpace();
    }
}
