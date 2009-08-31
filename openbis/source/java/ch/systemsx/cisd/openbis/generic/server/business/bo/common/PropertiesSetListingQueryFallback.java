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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import it.unimi.dsi.fastutil.longs.LongSet;

import ch.rinn.restrictions.Friend;

/**
 * A fallback implementation of {@link IEntitySetPropertyListingQuery} for database engines who
 * don't support querying for identifier sets.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = IEntityPropertyListingQuery.class)
public class PropertiesSetListingQueryFallback implements IEntitySetPropertyListingQuery
{
    private final IEntitySetPropertyListingQuery oneByOneDelegate;

    private final IEntitySetPropertyListingQuery fullTableScanDelegate;

    private final QueryStrategyChooser strategyChooser;

    public PropertiesSetListingQueryFallback(final IEntityPropertyListingQuery query,
            QueryStrategyChooser strategyChooser)
    {
        this.oneByOneDelegate = new PropertiesSetListingQueryOneByOne(query);
        this.fullTableScanDelegate = new PropertiesSetListingQueryFullTableScan(query);
        this.strategyChooser = strategyChooser;
    }

    public Iterable<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            final LongSet entityIDs)
    {
        if (strategyChooser.useFullTableScan(entityIDs))
        {
            return fullTableScanDelegate.getEntityPropertyGenericValues(entityIDs);
        } else
        {
            return oneByOneDelegate.getEntityPropertyGenericValues(entityIDs);
        }
    }

    public Iterable<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            final LongSet entityIDs)
    {
        if (strategyChooser.useFullTableScan(entityIDs))
        {
            return fullTableScanDelegate.getEntityPropertyMaterialValues(entityIDs);
        } else
        {
            return oneByOneDelegate.getEntityPropertyMaterialValues(entityIDs);
        }
    }

    public Iterable<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            final LongSet entityIDs)
    {
        if (strategyChooser.useFullTableScan(entityIDs))
        {
            return fullTableScanDelegate.getEntityPropertyVocabularyTermValues(entityIDs);
        } else
        {
            return oneByOneDelegate.getEntityPropertyVocabularyTermValues(entityIDs);
        }
    }
}
