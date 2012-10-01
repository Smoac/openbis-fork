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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.uitest.infra.application.GuiApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.page.tab.BrowserRow;

/**
 * @author anttil
 */
public class Project implements Browsable
{
    private final String code;

    private Space space;

    private String description;

    Project(String code, String description, Space space)
    {
        this.code = code;
        this.space = space;
        this.description = description;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    public Space getSpace()
    {
        return space;
    }

    public String getDescription()
    {
        return description;
    }

    void setSpace(Space space)
    {
        this.space = space;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public BrowserRow getBrowserContent(GuiApplicationRunner openbis)
    {
        return openbis.browseTo(this);
    }

    @Override
    public Collection<String> getColumns()
    {
        return Arrays.asList("Code", "Description", "Space");
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Project)
        {
            return ((Project) o).getCode().equals(code);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

    @Override
    public String toString()
    {
        return "Project " + code;
    }

}
