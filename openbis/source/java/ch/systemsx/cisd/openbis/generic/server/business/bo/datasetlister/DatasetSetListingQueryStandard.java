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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.LongSet;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;

/**
 * The standard implementation of {@link IDatasetSetListingQuery} for database engines that support
 * querying for identifier sets.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = IDatasetListingFullQuery.class)
class DatasetSetListingQueryStandard implements IDatasetSetListingQuery
{
    // Note: we cannot use the delegate directly, because it cannot implement any interface which
    // has methods without EODSQL annotations
    private final IDatasetListingFullQuery delegate;

    public DatasetSetListingQueryStandard(final IDatasetListingFullQuery query)
    {
        this.delegate = query;
    }

    public Iterable<DatasetRecord> getDatasets(LongSet datasetIds)
    {
        return delegate.getDatasets(datasetIds);
    }

    public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(LongSet entityIDs)
    {
        return delegate.getEntityPropertyGenericValues(entityIDs);
    }

    public Iterable<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(LongSet entityIDs)
    {
        return delegate.getEntityPropertyMaterialValues(entityIDs);
    }

    public Iterable<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(LongSet entityIDs)
    {
        return delegate.getEntityPropertyVocabularyTermValues(entityIDs);
    }

}
