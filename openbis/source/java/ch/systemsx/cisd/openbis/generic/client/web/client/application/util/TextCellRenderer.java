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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.table.CellRenderer;

/**
 * A cell renderer which return a substitute for <code>null</code> or <code>toString()</code>
 * of the cell object.
 *
 * @author Franz-Josef Elmer
 */
public class TextCellRenderer implements CellRenderer<Component>
{
    private final String nullSubstitute;

    /**
     * Creates an instance with the specified substitute for <code>null</code>.
     */
    public TextCellRenderer(String nullSubstitute)
    {
        this.nullSubstitute = nullSubstitute;
    }
    
    public String render(Component item, String property, Object value)
    {
        return value == null ? nullSubstitute : value.toString();
    }

}
