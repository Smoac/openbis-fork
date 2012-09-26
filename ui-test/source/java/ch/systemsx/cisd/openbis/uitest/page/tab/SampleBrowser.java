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

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;

public class SampleBrowser implements Browser<Sample>
{

    @Locate("openbis_sample-browser_main-grid")
    private Grid grid;

    @Locate("openbis_sample-browser_main_add-button")
    private Button addSample;

    @Locate("openbis_select_sample-typesample-browser-toolbar")
    private DropDown sampleTypeList;

    @Locate("openbis_select_group-selectsample-browser-toolbar")
    private DropDown spaceList;

    @Locate("openbis_sample-browser_main-grid-paging-toolbar")
    private PagingToolBar paging;

    @Lazy
    @Locate("openbis_sample-browser_main-grid-filter-toolbar")
    private FilterToolBar filters;

    public void addSample()
    {
        addSample.click();
    }

    public void selectSampleType(SampleType sampleType)
    {
        sampleTypeList.select(sampleType.getCode());
    }

    public void allSpaces()
    {
        spaceList.select("(all)");
    }

    public List<String> getSampleTypes()
    {
        return sampleTypeList.getChoices();
    }

    @Override
    public BrowserRow select(Sample sample)
    {
        return grid.select("Code", sample.getCode());
    }

    @Override
    public BrowserCell cell(Sample sample, String column)
    {
        return select(sample).get(column);
    }

    @Override
    public void filter(Sample sample)
    {
        paging.filters();
        filters.setCode(sample.getCode(), paging);
    }

    @Override
    public void resetFilters()
    {
        paging.filters();
        filters.reset();
    }

    @Override
    public String toString()
    {
        String s = "SampleBrowser\n==========\n";
        s += "Sample Type Choices: " + sampleTypeList.getChoices() + "\n";
        return s + grid.toString();
    }
}
