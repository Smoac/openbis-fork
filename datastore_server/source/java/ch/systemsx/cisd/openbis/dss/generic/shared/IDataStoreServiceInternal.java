/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import ch.systemsx.cisd.common.resource.IInitializable;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;

/**
 * {@link IDataStoreService} for internal(invisible for openBIS) usage.
 * 
 * @author Kaloyan Enimanev
 */
public interface IDataStoreServiceInternal extends IInitializable, IDataStoreService
{
    /**
     * Return an {@link IDataSetDeleter} instance.
     */
    IDataSetDeleter getDataSetDeleter();

    /**
     * Return an {@link IArchiverPlugin} or null if none is configured.
     */
    IArchiverPlugin getArchiverPlugin();

    /**
     * Returns the data set directory provider.
     */
    IDataSetDirectoryProvider getDataSetDirectoryProvider();
}
