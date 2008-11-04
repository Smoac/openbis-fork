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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An entity that matches the <i>Hibernate Search</i> query and which has been returned by the
 * server.
 * 
 * @author Christian Ribeaud
 */
public final class MatchingEntity implements IsSerializable
{
    private String identifier;

    public final String getIdentifier()
    {
        return identifier;
    }

    public final void setIdentifier(final String identifier)
    {
        this.identifier = identifier;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getIdentifier();
    }
}
