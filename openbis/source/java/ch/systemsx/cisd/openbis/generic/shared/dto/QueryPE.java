/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Persistent entity of a SQL query for a custom database.
 * 
 * @author Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.QUERIES_TABLE)
public class QueryPE extends AbstractExpressionPE<QueryPE>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String queryDatabaseKey = "1"; // FIXME

    private String name;

    @Column(name = ColumnNames.NAME_COLUMN)
    @NotNull(message = ValidationMessages.NAME_NOT_NULL_MESSAGE)
    @Length(min = 1, max = 200, message = ValidationMessages.NAME_LENGTH_MESSAGE)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Column(name = ColumnNames.QUERY_DATABASE_KEY_COLUMN)
    @NotNull(message = ValidationMessages.QUERY_DATABASE_KEY_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    public String getQueryDatabaseKey()
    {
        return queryDatabaseKey;
    }

    public void setQueryDatabaseKey(String queryDatabaseKey)
    {
        this.queryDatabaseKey = queryDatabaseKey;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof QueryPE == false)
        {
            return false;
        }
        final QueryPE that = (QueryPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getName(), that.getName());
        builder.append(getDatabaseInstance(), that.getDatabaseInstance());
        builder.append(getQueryDatabaseKey(), that.getQueryDatabaseKey());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getName());
        builder.append(getDatabaseInstance());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return getName();
    }

    public int compareTo(QueryPE that)
    {
        final String thatName = that.getName();
        final String thisName = getName();
        if (thisName == null)
        {
            return thatName == null ? 0 : -1;
        }
        if (thatName == null)
        {
            return 1;
        }
        return thisName.compareTo(thatName);
    }
}
