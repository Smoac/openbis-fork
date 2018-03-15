/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.service.execute.TableColumn")
public class TableColumn implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String title;

    public TableColumn(String title)
    {
        if (StringUtils.isEmpty(title))
        {
            throw new IllegalArgumentException("Unspecified column title.");
        }
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }
    
    @Override
    public int hashCode()
    {
        return title == null ? 0 : title.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (super.equals(obj) == false || getClass() != obj.getClass())
        {
            return false;
        }
        TableColumn that = (TableColumn) obj;
        return title == null ? title == that.title : title.equals(that.title);
    }

    @Override
    public String toString()
    {
        return String.valueOf(title);
    }
    
    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private TableColumn()
    {
    }

    @SuppressWarnings("unused")
    private void setTitle(String title)
    {
        this.title = title;
    }

}
