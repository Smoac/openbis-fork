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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * A class that overrides {@link #equals(Object)}, {@link #hashCode()}, {@link #toString()} for a
 * given <code>code</code>.
 * <p>
 * This class also implements {@link Comparable} interface.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class Code<T extends Code<T>> implements Serializable, Comparable<T>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private String code;

    public final String getCode()
    {
        return code;
    }

    @BeanProperty(label = "code")
    public final void setCode(final String code)
    {
        this.code = code;
    }

    //
    // Object
    //

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Code == false)
        {
            return false;
        }
        final Code<?> that = (Code<?>) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(that.code, code);
        return builder.isEquals();
    }

    @Override
    public int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(code);
        return builder.toHashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }

    //
    // Comparable
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    public int compareTo(final T o)
    {
        final String thatCode = o.code;
        if (code == null)
        {
            return thatCode == null ? 0 : -1;
        }
        if (thatCode == null)
        {
            return 1;
        }
        return code.compareTo(thatCode);
    }

}