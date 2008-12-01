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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericSampleRegistrationForm}.
 * 
 * @author Izabela Adamczyk
 */
public class GenericSampleRegistrationTest extends AbstractGWTTestCase
{

    private static final String SHARED_CL = "SHARED_CL";

    private static final String GROUP_CL = "GROUP_CL";

    private static final String CONTROL_LAYOUT = "CONTROL_LAYOUT";

    public final void testRegisterSharedSample()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.REGISTER));
        remoteConsole.prepare(new ChooseTypeOfNewSample(CONTROL_LAYOUT));
        remoteConsole.prepare(new FillSampleRegistrationForm(true, null, SHARED_CL));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST,
                GenericSampleRegistrationForm.RegisterSampleCallback.class));
        remoteConsole.prepare(new ListSamples(true, true, "CISD", CONTROL_LAYOUT));
        remoteConsole.prepare(new CheckSampleTable().expectedRow(new SampleRow(SHARED_CL)
                .identifier("CISD")));
        remoteConsole.finish(20000);
        client.onModuleLoad();
    }

    public final void testRegisterGroupSample()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.REGISTER));
        remoteConsole.prepare(new ChooseTypeOfNewSample(CONTROL_LAYOUT));
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
}
