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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * A role for fast sample listing.
 *
 * @author Bernd Rinn
 */
public interface ISampleLister
{
    
    /**
     * A method for fast sample listing.
     * 
     * Returns a sorted list of {@link Sample}.
     */
    public List<Sample> list(ListSampleCriteria criteria);
    
    /**
     * Returns <code>true</code>, if this sample lister can handle the given <var>criteria</var>.
     */
    public boolean canHandle(ListSampleCriteria criteria);

}
