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

import ch.systemsx.cisd.openbis.uitest.infra.Browser;
import ch.systemsx.cisd.openbis.uitest.infra.Cell;
import ch.systemsx.cisd.openbis.uitest.infra.Row;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;

public class VocabularyBrowser implements Browser<Vocabulary>
{
    @Locate("openbis_vocabulary-browser-grid")
    private Grid grid;

    @Locate("openbis_vocabulary-browser_add-button")
    private Button add;

    @SuppressWarnings("unused")
    @Locate("openbis_vocabulary-browser_delete-button")
    private Button delete;

    @Locate("openbis_vocabulary-browser-grid-paging-toolbar")
    private PagingToolBar paging;

    @Lazy
    @Locate("openbis_vocabulary-browser-grid-filter-toolbar")
    private FilterToolBar filters;

    public void add()
    {
        add.click();
    }

    @Override
    public Row select(Vocabulary vocabulary)
    {
        return grid.select("Code", vocabulary.getCode());
    }

    @Override
    public Cell cell(Vocabulary vocabulary, String column)
    {
        return select(vocabulary).get(column);
    }

    @Override
    public void filter(Vocabulary vocabulary)
    {
        paging.filters();
        filters.setCode(vocabulary.getCode(), grid);
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
        String s = "VocabularyBrowser\n==========\n";
        return s + grid.toString();
    }
}
