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
public abstract class Browser<T extends Browsable<? extends Browser<T>>>
{

    public abstract Grid getGrid();

    public abstract PagingToolBar getPaging();

    public abstract FilterToolBar getFilters();

    public abstract SettingsDialog getSettings();

    public final BrowserRow select(T browsable)
    {
        waitForPagingToolBar();
        return getGrid().select("Code", browsable.getCode());
    }

    public final void filterTo(T browsable)
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

    public final void showColumnsOf(T browsable)
    {
        waitForPagingToolBar();
        if (getGrid().getColumnNames().containsAll(browsable.getColumns()))
        {
            return;
        }
        getPaging().settings();
        getSettings().showColumnsOf(browsable);
    }

    public final List<BrowserRow> getData()
    {
        waitForPagingToolBar();
        return getGrid().getData();
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
