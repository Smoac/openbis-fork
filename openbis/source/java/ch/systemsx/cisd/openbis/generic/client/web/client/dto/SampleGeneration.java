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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A <code>SampleGeneration</code> encapsulates a generator <code>Sample</code> and its
 * generated <code>Sample</code>s.
 * 
 * @author Christian Ribeaud
 */
public final class SampleGeneration implements IsSerializable
{
    /**
     * The parent <code>Sample</code>.
     */
    private Sample generator;

    /** The children <code>Sample</code>. */
    private Sample[] generated = Sample.EMPTY_ARRAY;

    /**
     * Never returns <code>null</code> but could return an empty array.
     */
    public final Sample[] getGenerated()
    {
        if (generated == null)
        {
            return Sample.EMPTY_ARRAY;
        }
        return generated;
    }

    public final void setGenerated(final Sample[] generated)
    {
        this.generated = generated;
    }

    public final Sample getGenerator()
    {
        return generator;
    }

    public final void setGenerator(final Sample generator)
    {
        this.generator = generator;
    }
}
