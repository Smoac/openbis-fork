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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ChooseTypeOfNewSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.CheckSampleTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
import ch.systemsx.cisd.openbis.generic.shared.IPluginCommonServer;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericSampleRegistrationForm}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericSampleRegistrationTest extends AbstractGWTTestCase
{

    private static final String DILUTION_PLATE = "DILUTION_PLATE";

    private static final String SHARED_CL = "SHARED_CL";

    private static final String GROUP_CL = "GROUP_CL";

    private static final String CONTROL_LAYOUT = "CONTROL_LAYOUT";

    private final void loginAndPreprareRegistration(final String sampleType)
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.REGISTER));
        remoteConsole.prepare(new ChooseTypeOfNewSample(sampleType));
    }

    public final void testRegisterSharedSample()
    {
        loginAndPreprareRegistration(CONTROL_LAYOUT);
        remoteConsole.prepare(new FillSampleRegistrationForm(true, null, SHARED_CL));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST,
                GenericSampleRegistrationForm.RegisterSampleCallback.class));
        remoteConsole.prepare(new ListSamples(true, true, "CISD", CONTROL_LAYOUT));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(SHARED_CL)
                .identifier("CISD")));
        remoteConsole.finish(30000);
        client.onModuleLoad();
    }

    public final void testRegisterGroupSample()
    {
        loginAndPreprareRegistration(CONTROL_LAYOUT);
        remoteConsole.prepare(new FillSampleRegistrationForm(false, "CISD", GROUP_CL));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST,
                GenericSampleRegistrationForm.RegisterSampleCallback.class));
        remoteConsole.prepare(new ListSamples(true, true, "CISD", CONTROL_LAYOUT));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(GROUP_CL)
                .identifier("CISD", "CISD")));
        remoteConsole.finish(20000);
        client.onModuleLoad();
    }
    
    /**
     * Tests that authorization annotations of
     * {@link IPluginCommonServer#registerSample(String, ch.systemsx.cisd.openbis.generic.client.web.client.dto.NewSample)}
     * are obeyed.
     */
    public final void testRegisterSampleByAnUnauthorizedUser()
    {
        remoteConsole.prepare(new Login("observer", "observer"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.REGISTER));
        remoteConsole.prepare(new ChooseTypeOfNewSample(CONTROL_LAYOUT));
        remoteConsole.prepare(new FillSampleRegistrationForm(true, null, SHARED_CL + "1"));
        FailureExpectation failureExpectation =
                new FailureExpectation(GenericSampleRegistrationForm.RegisterSampleCallback.class)
                        .with("Authorization failure: None of method roles "
                                + "'[GROUP.USER, GROUP.ADMIN, INSTANCE.ADMIN]' "
                                + "could be found in roles of user 'observer'.");
        remoteConsole.prepare(failureExpectation);
        remoteConsole.finish(20000);
        client.onModuleLoad();
    }

    public final void testRegisterGroupSampleWithParent()
    {
        loginAndPreprareRegistration(DILUTION_PLATE);
        final String sampleCode = "dp4";
        remoteConsole.prepare(new FillSampleRegistrationForm(false, "CISD", sampleCode)
                .parent("MP1-MIXED"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST,
                GenericSampleRegistrationForm.RegisterSampleCallback.class));
        remoteConsole.prepare(new ListSamples(true, true, "CISD", DILUTION_PLATE));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(sampleCode
                .toUpperCase()).identifier("CISD", "CISD")));
        remoteConsole.finish(20000);
        client.onModuleLoad();
    }

    public final void testRegisterSampleWithContainer()
    {
        // loginAndPreprareRegistration("WELL");
        // remoteConsole.prepare(new FillSampleRegistrationForm(false, "CISD", GROUP_CL));
    }

}
