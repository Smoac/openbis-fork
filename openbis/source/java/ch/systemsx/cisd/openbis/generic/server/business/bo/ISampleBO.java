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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleToRegisterDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A generic sample <i>Business Object</i>.
 * 
 * @author Christian Ribeaud
 */
public interface ISampleBO
{

    /** Loads a sample given by its identifier. */
    void loadBySampleIdentifier(final SampleIdentifier identifier);

    /** Returns the sample which has been loaded. */
    SamplePE getSample();

    /**
     * Defines a new sample. After invocation of this method {@link IBusinessObject#save()} should
     * be invoked to store the new sample in the Data Access Layer.
     * 
     * @throws UserFailureException if specified sample type code is not a valid one.
     */
    void define(SampleToRegisterDTO newSample);

    /**
     * Writes changed are added data to the Data Access Layers.
     */
    public void save() throws UserFailureException;
}
