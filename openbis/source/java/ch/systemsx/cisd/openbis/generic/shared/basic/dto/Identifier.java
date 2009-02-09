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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Comparator;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;

/**
 * An identifier.
 * 
 * @author Christian Ribeaud
 */
public class Identifier<T extends Identifier<T>> implements IsSerializable, Comparable<T>,
        IIdentifierHolder
{
    public final static Comparator<IIdentifierHolder> IDENTIFIER_HOLDER_COMPARATOR =
            new IdentifierHolderComparator();

    private String identifier;

    @BeanProperty(label = "identifier")
    public final void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }

    //
    // IIdentifierHolder
    //

    public final String getIdentifier()
    {
        return identifier;
    }

    //
    // Comparable
    //

    public final int compareTo(final T o)
    {
        return IDENTIFIER_HOLDER_COMPARATOR.compare(this, o);
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof Identifier == false)
        {
            return false;
        }
        final Identifier<?> that = (Identifier<?>) obj;
        return getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public final int hashCode()
    {
        return getIdentifier().hashCode();
    }

    //
    // Helper classes
    //

    public final static class IdentifierHolderComparator implements Comparator<IIdentifierHolder>,
            Serializable
    {
        private static final long serialVersionUID = 1L;

        //
        // Comparable
        //

        public int compare(final IIdentifierHolder o1, final IIdentifierHolder o2)
        {
            assert o1 != null : "Unspecified code provider.";
            assert o2 != null : "Unspecified code provider.";
            final String thisIdentifier = o1.getIdentifier();
            final String thatIdentifier = o2.getIdentifier();
            if (thisIdentifier == null)
            {
                return thatIdentifier == null ? 0 : -1;
            }
            if (thatIdentifier == null)
            {
                return 1;
            }
            return thisIdentifier.compareTo(thatIdentifier);
        }
    }
}
