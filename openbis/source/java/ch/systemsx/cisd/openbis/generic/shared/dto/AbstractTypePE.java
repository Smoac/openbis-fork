/*
 * Copyright 2007 ETH Zuerich, CISD
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
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * Abstract Persistence Entity representing a type.
 * 
 * @author Izabela Adamczyk
 */
@MappedSuperclass
public abstract class AbstractTypePE extends AbstractIdAndCodeHolder<AbstractTypePE>
{
    private static final long serialVersionUID = IServer.VERSION;

    transient protected Long id;

    private String code;

    private String description;

    public void setId(final Long id)
    {
        this.id = id;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    @Column(name = ColumnNames.DESCRIPTION_COLUMN)
    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    //
    // AbstractIdAndCodeHolder
    //

    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regex = AbstractIdAndCodeHolder.CODE_PATTERN, flags = java.util.regex.Pattern.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Field(index = Index.TOKENIZED, store = Store.YES, name = SearchFieldConstants.CODE)
    public String getCode()
    {
        return code;
    }

    @Override
    ToStringBuilder createStringBuilder()
    {
        final ToStringBuilder builder = super.createStringBuilder();
        builder.append("description", getDescription());
        return builder;
    }
}
