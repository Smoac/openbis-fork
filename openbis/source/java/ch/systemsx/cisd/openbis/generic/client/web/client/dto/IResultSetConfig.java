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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridFilterInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;

/**
 * Configures the returned result set.
 * <p>
 * This object typically comes from the client side and determines the returned {@link IResultSet}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IResultSetConfig<K, T> extends IResultSetKeyHolder<K>
{
    public static final int NO_LIMIT = -1;

    /**
     * The offset for the first record to retrieve.
     */
    public int getOffset();

    /**
     * The number of records being requested.
     * 
     * @return {@link #NO_LIMIT} if no specified (meaning all).
     */
    public int getLimit();

    /**
     * Returns the sort info.
     */
    public SortInfo<T> getSortInfo();

    /** The filters which should be applied for the result */
    public List<GridFilterInfo<T>> getFilterInfos();

    /**
     * Returns custom filter info.
     */
    public CustomFilterInfo<T> tryGetCustomFilterInfo();

}
