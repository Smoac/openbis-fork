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

package ch.systemsx.cisd.openbis.uitest;

import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.page.dialog.AddSampleTypeDialog;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

/**
 * @author anttil
 */
@Test(groups =
    { "login-admin" })
public class SampleTypeTest extends SeleniumTest
{

    @Test
    public void cannotSaveSampleTypeWithInvalidCode()
    {
        create(aSampleType().withCode("invalid code"));

        assertThat(browser(), isShowing(AddSampleTypeDialog.class));

        get(AddSampleTypeDialog.class).cancel();
    }

    @Test
    public void newSampleTypeIsListedInSampleTypeBrowser()
    {
        SampleType sampleType = create(aSampleType());

        assertThat(sampleTypeBrowser(), lists(sampleType));
    }

    @Test
    public void nonListableSampleTypeIsNotVisibleInSampleBrowserDropDownMenu()
    {
        SampleType sampleType = create(aSampleType().thatIsNotListable());

        assertThat(sampleBrowser(), doesNotShowInToolBar(sampleType));
    }

    @Test
    public void changingSampleTypeToBeListableMakesItVisibleInSampleBrowserDropDownMenu()
    {
        SampleType sampleType = create(aSampleType().thatIsNotListable());

        perform(anUpdateOf(sampleType).settingItListable());

        assertThat(sampleBrowser(), showsInToolBar(sampleType));
    }

}
