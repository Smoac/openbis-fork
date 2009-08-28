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

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;

/**
 * The standard implementation of {@link ISampleSetListingQuery} for database engines that support
 * querying for identifier sets.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = ISampleListingFullQuery.class)
class SampleSetListingQueryStandard implements ISampleSetListingQuery
{
    // Note: we cannot use the delegate directly, because it cannot implement any interface which
    // has methods without EODSQL annotations
    private final ISampleListingFullQuery delegate;

    public SampleSetListingQueryStandard(final ISampleListingFullQuery query)
    {
        this.delegate = query;
    }

    public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(LongSet entityIDs)
    {
        return delegate.getSamplePropertyGenericValues(entityIDs);
    }

    public Iterable<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(LongSet entityIDs)
    {
        return delegate.getSamplePropertyMaterialValues(entityIDs);
    }

    public Iterable<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(LongSet entityIDs)
    {
        return delegate.getSamplePropertyVocabularyTermValues(entityIDs);
    }

    public Iterable<SampleRecord> getSamples(LongSet sampleIds)
    {
        return delegate.getSamples(sampleIds);
    }

}
