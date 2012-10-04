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

import ch.systemsx.cisd.openbis.uitest.page.layout.ScriptBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.page.tab.ScriptBrowser;

/**
 * @author anttil
 */
public class Script implements Browsable<ScriptBrowser>
{
    private final String name;

    private ScriptType type;

    private EntityKind kind;

    private String description;

    private String content;

    Script(String name, ScriptType type, EntityKind kind, String description, String content)
    {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.description = description;
        this.content = content;
    }

    public String getName()
    {
        return name;
    }

    public ScriptType getType()
    {
        return type;
    }

    public EntityKind getKind()
    {
        return kind;
    }

    public String getDescription()
    {
        return description;
    }

    public String getContent()
    {
        return content;
    }

    void setType(ScriptType type)
    {
        this.type = type;
    }

    void setKind(EntityKind kind)
    {
        this.kind = kind;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setContent(String content)
    {
        this.content = content;
    }

    @Override
    public Collection<String> getColumns()
    {
        return Arrays.asList("Name", "Description", "Entity Kind", "Script Type", "Script");
    }

    @Override
    public String getCode()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return getCode().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Space)
        {
            return ((Space) o).getCode().equalsIgnoreCase(getCode());
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "Script" + getCode();
    }

    @Override
    public ScriptBrowserLocation getBrowserLocation()
    {
        return new ScriptBrowserLocation();
    }
}
