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

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * A <code>SampleGeneration</code> encapsulates a generator <code>SampleDTO</code> and its
 * generated <code>SampleDTO</code>s.
 * 
 * @author Christian Ribeaud
 */
public final class SampleGenerationDTO implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    /**
     * The <code>Sample</code> parent.
     */
    private SamplePE generator;

    /** The <code>Sample</code> children. */
    private SamplePE[] generated;

    public SampleGenerationDTO()
    {

    }

    public SampleGenerationDTO(final SamplePE generator, final List<SamplePE> generated)
    {
        assert generator != null : "Generator can not be null.";
        assert generated != null : "Generated can not be null.";
        setGenerator(generator);
        setGenerated(generated.toArray(SamplePE.EMPTY_ARRAY));
    }

    /**
     * Never returns <code>null</code> but could return an empty list.
     */
    public final SamplePE[] getGenerated()
    {
        return generated;
    }

    public final SamplePE getGenerator()
    {
        return generator;
    }

    public void setGenerator(final SamplePE generator)
    {
        this.generator = generator;
    }

    public void setGenerated(final SamplePE[] generated)
    {
        this.generated = generated;
    }
}