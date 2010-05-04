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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Technical identifier of an entity.
 * 
 * @author Piotr Buczek
 */
public class TechId implements IIdHolder, IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private Long id;

    protected TechId()
    {
        // for serialization
    }

    public TechId(Long id)
    {
        assert id != null : "id cannot be null";
        this.id = id;
    }

    public TechId(Number id)
    {
        assert id != null : "id cannot be null";
        this.id = id.longValue();
    }

    public TechId(IIdAndCodeHolder identifiable)
    {
        this(identifiable.getId());
    }

    /**
     * @return Technical id with id from given <var>idHolder</var> or null if the holder does not
     *         provide any id.
     */
    public static TechId create(IIdHolder idHolder)
    {
        if (idHolder == null || idHolder.getId() == null)
        {
            return null;
        } else
        {
            return new TechId(idHolder.getId());
        }
    }

    /**
     * Convenience method for getting a list of technical ids from given list of objects with
     * identifiers.
     * 
     * @see #create(IIdHolder)
     */
    public static List<TechId> createList(List<? extends IIdHolder> idHolders)
    {
        List<TechId> results = new ArrayList<TechId>();
        for (IIdHolder idHolder : idHolders)
        {
            results.add(create(idHolder));
        }
        return results;
    }

    public Long getId()
    {
        return id;
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
        if (obj instanceof TechId == false)
        {
            return false;
        }
        return this.toString().equals(obj.toString());
    }

    @Override
    public final int hashCode()
    {
        return this.toString().hashCode();
    }

    @Override
    public String toString()
    {
        return String.valueOf(id);
    }

    //
    // Helper for tests framework
    //

    /** Creates a {@link TechId} which string representation matches all technical identifiers. */
    public static TechId createWildcardTechId()
    {
        return new WildcardTechId();
    }

    /** {@link TechId} which string representation matches all technical identifiers. */
    private static class WildcardTechId extends TechId
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        public WildcardTechId()
        {
            super();
        }

        @Override
        public String toString()
        {
            return ".*";
        }
    }

}
