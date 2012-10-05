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

package ch.systemsx.cisd.openbis.uitest.page;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Predicate;

import ch.systemsx.cisd.openbis.uitest.type.Browsable;
import ch.systemsx.cisd.openbis.uitest.widget.FilterToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.Grid;
import ch.systemsx.cisd.openbis.uitest.widget.PagingToolBar;
import ch.systemsx.cisd.openbis.uitest.widget.SettingsDialog;

/**
 * @author anttil
 */
public abstract class Browser
{

    protected abstract Grid getGrid();

    protected abstract PagingToolBar getPaging();

    protected abstract FilterToolBar getFilters();

    protected abstract SettingsDialog getSettings();

    protected abstract void delete();

    public final BrowserRow select(Browsable browsable)
    {
        waitForPagingToolBar();
        return getGrid().select("Code", browsable.getCode());
    }

    public final BrowserRow getRow(Browsable browsable)
    {
        showColumnsOf(browsable);
        filterTo(browsable);
        List<BrowserRow> rows = getData();
        try
        {
            if (rows.size() == 0)
            {
                return new BrowserRow();
            } else if (rows.size() == 1)
            {
                return rows.get(0);
            } else
            {
                throw new IllegalStateException("multiple rows found:\n" + rows);
            }
        } finally
        {
            resetFilters();
        }
    }

    public final void filterTo(Browsable browsable)
    {
        waitForPagingToolBar();
        getPaging().filters();
        getFilters().setCode(browsable.getCode(), getPaging());
    }

    public final void resetFilters()
    {
        waitForPagingToolBar();
        getPaging().filters();
        getFilters().reset();
    }

    public final void showColumnsOf(Browsable browsable)
    {
        waitForPagingToolBar();
        if (getGrid().getColumnNames().containsAll(browsable.getColumns()))
        {
            return;
        }
        getPaging().settings();
        getSettings().showColumnsOf(browsable);
    }

    private final List<BrowserRow> getData()
    {
        waitForPagingToolBar();
        return getGrid().getData();
    }

    public final void delete(Browsable browsable)
    {
        filterTo(browsable);
        BrowserRow row = select(browsable);
        if (row.exists())
        {
            delete();
        }
        resetFilters();

    }

    @Override
    public String toString()
    {
        String s = getClass().getSimpleName() + "\n==========\n";
        return s + getGrid().toString();
    }

    private void waitForPagingToolBar()
    {
        new FluentWait<PagingToolBar>(getPaging())
            {

            }.withTimeout(30, TimeUnit.SECONDS).pollingEvery(100, TimeUnit.MILLISECONDS).until(
                new Predicate<PagingToolBar>()
                    {

                        @Override
                        public boolean apply(PagingToolBar paging)
                        {
                            System.out.println("waiting for paging toolbar to get enabled");
                            return paging.isEnabled();
                        }
                    });
    }

}
