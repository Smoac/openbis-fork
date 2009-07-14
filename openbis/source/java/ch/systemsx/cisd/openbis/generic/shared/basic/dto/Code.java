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
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeProvider;

/**
 * A code.
 * 
 * @author Christian Ribeaud
 */
public class Code<T extends Code<T>> implements IsSerializable, ICodeProvider, Comparable<T>,
        Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String CODE = "code";

    public final static Comparator<ICodeProvider> CODE_PROVIDER_COMPARATOR =
            new CodeProviderComparator();

    private String code;

    @BeanProperty(label = CODE)
    public final void setCode(final String code)
    {
        this.code = code;
    }

    //
    // ICodeProvider
    //

    public final String getCode()
    {
        return code;
    }

    //
    // Comparable
    //

    public final int compareTo(final T o)
    {
        return CODE_PROVIDER_COMPARATOR.compare(this, o);
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
        if (obj instanceof Code<?> == false)
        {
            return false;
        }
        final Code<?> that = (Code<?>) obj;
        return getCode().equals(that.getCode());
    }

    @Override
    public final int hashCode()
    {
        return getCode().hashCode();
    }

    //
    // Helper classes
    //

    public final static class CodeProviderComparator implements Comparator<ICodeProvider>,
            Serializable
    {
        private static final long serialVersionUID = 1L;

        //
        // Comparable
        //

        public int compare(final ICodeProvider o1, final ICodeProvider o2)
        {
            assert o1 != null : "Unspecified code provider.";
            assert o2 != null : "Unspecified code provider.";
            final String thisCode = o1.getCode();
            final String thatCode = o2.getCode();
            if (thisCode == null)
            {
                return thatCode == null ? 0 : -1;
            }
            if (thatCode == null)
            {
                return 1;
            }
            return thisCode.compareTo(thatCode);
        }
    }
}
