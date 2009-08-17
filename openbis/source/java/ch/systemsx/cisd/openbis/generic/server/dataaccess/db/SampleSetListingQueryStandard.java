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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import it.unimi.dsi.fastutil.longs.LongSet;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.CoVoSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.GenericSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.MaterialSamplePropertyVO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ISampleListingQuery.SampleRowVO;

/**
 * The standard implementation of {@link ISampleSetListingQuery} for database engines that support
 * querying for identifier sets.
 * 
 * @author Bernd Rinn
 */
public class SampleSetListingQueryStandard implements ISampleSetListingQuery
{

    private final ISampleListingFullQuery delegate;

    public SampleSetListingQueryStandard(final ISampleListingFullQuery query)
    {
        this.delegate = query;
    }

    public Iterable<GenericSamplePropertyVO> getSamplePropertyGenericValues(LongSet sampleIds)
    {
        return delegate.getSamplePropertyGenericValues(sampleIds);
    }

    public Iterable<MaterialSamplePropertyVO> getSamplePropertyMaterialValues(LongSet sampleIds)
    {
        return delegate.getSamplePropertyMaterialValues(sampleIds);
    }

    public Iterable<CoVoSamplePropertyVO> getSamplePropertyVocabularyTermValues(LongSet sampleIds)
    {
        return delegate.getSamplePropertyVocabularyTermValues(sampleIds);
    }

    public Iterable<SampleRowVO> getSamples(LongSet sampleIds)
    {
        return delegate.getSamples(sampleIds);
    }

}
