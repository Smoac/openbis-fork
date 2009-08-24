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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import it.unimi.dsi.fastutil.longs.LongSet;

import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.GenericEntityPropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.MaterialSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery.SampleRowVO;

/**
 * A DAO query interface for obtaining sets of samples or sample-related entities based on a set of
 * sample ids.
 * <p>
 * May need different implementations for different database engines.
 * 
 * @author Bernd Rinn
 */
interface ISampleSetListingQuery
{
    //
    // Samples by id
    //

    /**
     * Returns the samples for the given <var>sampleIds</var>.
     */
    public Iterable<SampleRowVO> getSamples(LongSet sampleIds);

    //
    // Sample Properties
    //

    /**
     * Returns all generic property values of all samples specified by <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    public Iterable<GenericEntityPropertyVO> getSamplePropertyGenericValues(LongSet sampleIds);

    /**
     * Returns all controlled vocabulary property values of all samples specified by
     * <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    public Iterable<CoVoSamplePropertyVO> getSamplePropertyVocabularyTermValues(LongSet sampleIds);

    /**
     * Returns all material-type property values of all samples specified by <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    public Iterable<MaterialSamplePropertyVO> getSamplePropertyMaterialValues(LongSet sampleIds);

}
