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

import java.io.File;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * A provider of {@link IHierarchicalContent} for given data set.
 * <p>
 * <b>NOTE</b>{@link IHierarchicalContent#close()} needs to be called to release resources when
 * working with the content is done. Otherwise data set may e.g. remain locked in its share until
 * timeout occurs.
 * 
 * @author Piotr Buczek
 * @author Chandrasekhar Ramakrishnan
 */
public interface IHierarchicalContentProvider
{

    /**
     * This is the only method that supports abstraction for virtual data sets. It needs to access
     * openBIS DB first to retrieve information first.
     * 
     * @return {@link IHierarchicalContent} for the specified data set
     */
    IHierarchicalContent asContent(String dataSetCode);

    /**
     * @return {@link IHierarchicalContent} for the specified data set
     * @deprecated doesn't support abstraction for virtual data sets, use {@link #asContent(String)}
     */
    @Deprecated
    IHierarchicalContent asContent(IDatasetLocation datasetLocation);

    /**
     * <b>NOTE:</b> the data set is assumed to be locked when this method is called
     * 
     * @param datasetDirectory the directory file of the data set
     * @return {@link IHierarchicalContent} for the specified data set
     * @deprecated doesn't support abstraction for virtual data sets, use {@link #asContent(String)}
     */
    @Deprecated
    IHierarchicalContent asContent(File datasetDirectory);

}
