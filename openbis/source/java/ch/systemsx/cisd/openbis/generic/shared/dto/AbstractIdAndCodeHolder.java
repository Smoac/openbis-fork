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
import java.util.Comparator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.util.EqualsHashUtils;

/**
 * An <i>abstract</i> implementation of {@link IIdAndCodeHolder}
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractIdAndCodeHolder<T extends IIdAndCodeHolder> implements
        IIdAndCodeHolder, Comparable<T>, Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public final static String CODE_PATTERN = "^[A-Z0-9_\\-]+$";

    /**
     * A static method for comparing two {@link IIdAndCodeHolder}s.
     */
    public final static int compare(final IIdAndCodeHolder o1, final IIdAndCodeHolder o2)
    {
        return IdAndCodeHolderComparator.INSTANCE.compare(o1, o2);
    }

    ToStringBuilder createStringBuilder()
    {
        final ToStringBuilder builder =
                new ToStringBuilder(this,
                        ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
        builder.append("code", getCode());
        return builder;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        EqualsHashUtils.assertDefined(getCode(), "code");
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof IIdAndCodeHolder == false)
        {
            return false;
        }
        final IIdAndCodeHolder that = (IIdAndCodeHolder) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getCode(), that.getCode());
        return builder.isEquals();
    }

    @Override
    public final int hashCode()
    {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(getCode());
        return builder.toHashCode();
    }

    @Override
    public final String toString()
    {
        return createStringBuilder().toString();
    }

    //
    // Compare
    //

    /**
     * If <code>null</code> values are present for <code>code</code>, then they come first.
     */
    public final int compareTo(final T o)
    {
        return compare(this, o);
    }

    //
    // Helper classes
    //

    private final static class IdAndCodeHolderComparator implements Comparator<IIdAndCodeHolder>
    {

        static final Comparator<IIdAndCodeHolder> INSTANCE = new IdAndCodeHolderComparator();

        //
        // Comparator
        //

        public final int compare(final IIdAndCodeHolder o1, final IIdAndCodeHolder o2)
        {
            final String thatCode = o2.getCode();
            if (o1.getCode() == null)
            {
                return thatCode == null ? 0 : -1;
            }
            if (thatCode == null)
            {
                return 1;
            }
            return o1.getCode().compareTo(thatCode);

        }
    }
}
