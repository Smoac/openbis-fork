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

package ch.systemsx.cisd.openbis.uitest.page.tab;

import java.util.List;

import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.page.common.BrowserPage;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;

public class SampleBrowser extends BrowserPage
{

    @Locate("openbis_sample-browser_main-grid")
    private Grid grid;

    @Locate("openbis_sample-browser_main_add-button")
    private Button addSample;

    @Locate("openbis_select_sample-typesample-browser-toolbar")
    private DropDown sampleTypeList;

    @Locate("openbis_select_group-selectsample-browser-toolbar")
    private DropDown spaceList;

    public RegisterSample addSample()
    {
        addSample.click();
        return get(RegisterSample.class);
    }

    @Override
    protected List<WebElement> getColumns()
    {
        return grid.getColumns();
    }

    @Override
    protected List<WebElement> getData()
    {
        return grid.getCells();
    }

    public SampleBrowser selectSampleType(SampleType sampleType)
    {
        sampleTypeList.select(sampleType.getCode());
        return get(SampleBrowser.class);
    }

    public SampleBrowser allSpaces()
    {
        spaceList.select("(all)");
        return get(SampleBrowser.class);
    }

    public List<String> getSampleTypes()
    {
        return sampleTypeList.getChoices();
    }

    @Override
    protected WebElement getDeleteButton()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
