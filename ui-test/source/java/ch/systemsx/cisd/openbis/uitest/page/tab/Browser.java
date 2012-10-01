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

import ch.systemsx.cisd.openbis.uitest.type.Browsable;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.SettingsDialog;

/**
 * @author anttil
 */
public abstract class Browser<T extends Browsable>
{

    public abstract Grid getGrid();

    public abstract PagingToolBar getPaging();

    public abstract FilterToolBar getFilters();

    public abstract SettingsDialog getSettings();

    public final BrowserRow select(T browsable)
    {
        return getGrid().select("Code", browsable.getCode());
    }

    public final void filterTo(T browsable)
    {
        getPaging().filters();
        getFilters().setCode(browsable.getCode(), getPaging());
    }

    public final void resetFilters()
    {
        getPaging().filters();
        getFilters().reset();
    }

    public final void showColumnsOf(T browsable)
    {
        getPaging().settings();
        getSettings().showColumnsOf(browsable);
    }

    public final List<BrowserRow> getData()
    {
        return getGrid().getData();
    }

    @Override
    public String toString()
    {
        String s = getClass().getSimpleName() + "\n==========\n";
        return s + getGrid().toString();
    }

}
