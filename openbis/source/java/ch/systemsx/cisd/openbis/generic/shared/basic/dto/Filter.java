/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores information describing the filter.
 * 
 * @author Izabela Adamczyk
 */
public class Filter extends AbstractRegistrationHolder implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String COLUMN_FILTER = "Column Filter";

    private String name;

    private String expression;

    private boolean isPublic;

    private Date modificationDate;

    private String description;

    private long id;

    private DatabaseInstance databaseInstance;

    private Set<String> parameters;

    private Set<String> columns;

    public Filter()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public boolean isPublic()
    {
        return isPublic;
    }

    public void setPublic(boolean isPublic)
    {
        this.isPublic = isPublic;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public DatabaseInstance getDatabaseInstance()
    {
        return databaseInstance;
    }

    public void setDatabaseInstance(DatabaseInstance databaseInstance)
    {
        this.databaseInstance = databaseInstance;
    }

    public Set<String> getParameters()
    {
        return parameters;
    }

    public void setParameters(Set<String> parameters)
    {
        this.parameters = parameters;
    }

    public Set<String> getColumns()
    {
        return columns;
    }

    public void setColumns(Set<String> columns)
    {
        this.columns = columns;
    }

}
