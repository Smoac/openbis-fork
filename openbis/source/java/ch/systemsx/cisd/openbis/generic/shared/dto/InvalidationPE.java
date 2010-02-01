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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.Length;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Class representing invalidation.
 * 
 * @author     Franz-Josef Elmer
 */
@Entity
@Table(name = TableNames.INVALIDATION_TABLE)
public class InvalidationPE extends HibernateAbstractRegistrationHolder implements IIdHolder,
        Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private String reason;

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @Id
    @SequenceGenerator(name = SequenceNames.INVALIDATION_SEQUENCE, sequenceName = SequenceNames.INVALIDATION_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.INVALIDATION_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    @Length(max = GenericConstants.DESCRIPTION_2000, message = ValidationMessages.DESCRIPTION_LENGTH_MESSAGE)
    public String getReason()
    {
        return reason;
    }

    public void setReason(final String description)
    {
        this.reason = description;
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof InvalidationPE == false)
        {
            return false;
        }
        final InvalidationPE that = (InvalidationPE) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(id, that.id);
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(id);
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}
