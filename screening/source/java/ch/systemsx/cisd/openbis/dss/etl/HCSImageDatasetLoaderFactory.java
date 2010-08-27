/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.etl.dataaccess.HCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;

/**
 * @author Tomasz Pylak
 */
public class HCSImageDatasetLoaderFactory
{
    private static final Map<String, IContentRepositoryFactory> repositoryFactories =
            createFactories();

    private static Map<String, IContentRepositoryFactory> createFactories()
    {
        Map<String, IContentRepositoryFactory> factories =
                new HashMap<String, IContentRepositoryFactory>();
        factories.put("h5", new Hdf5BasedContentRepositoryFactory());
        return factories;
    }

    /** the loader has to be closed when it is not used any more to free database resources! */
    public static final IHCSImageDatasetLoader create(File datasetRootDir, String datasetCode)
    {
        return createImageDBLoader(datasetRootDir, datasetCode);
    }

    private static HCSImageDatasetLoader createImageDBLoader(File datasetRootDir, String datasetCode)
    {
        IContentRepository repository = new ContentRepository(datasetRootDir, repositoryFactories);
        return new HCSImageDatasetLoader(DssScreeningUtils.getQuery(), datasetCode, repository);
    }

}
