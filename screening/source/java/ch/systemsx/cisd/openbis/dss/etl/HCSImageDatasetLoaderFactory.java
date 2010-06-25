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

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.openbis.dss.etl.dataaccess.HCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;

/**
 * @author Tomasz Pylak
 */
public class HCSImageDatasetLoaderFactory
{
    private static final Map<String, IContentRepositoryFactory> repositoryFactories =
            createFactories();

    private static final IImagingQueryDAO query = createQuery();

    private static IImagingQueryDAO createQuery()
    {
        DataSource dataSource =
                ServiceProvider.getDataSourceProvider().getDataSource(
                        ScreeningConstants.IMAGING_DATA_SOURCE);
        return QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
    }

    private static Map<String, IContentRepositoryFactory> createFactories()
    {
        Map<String, IContentRepositoryFactory> factories =
                new HashMap<String, IContentRepositoryFactory>();
        factories.put("h5", new Hdf5BasedContentRepositoryFactory());
        return factories;
    }

    public static final IHCSImageDatasetLoader create(File datasetRootDir, String datasetCode)
    {
        return createImageDBLoader(datasetRootDir, datasetCode);
    }

    private static HCSImageDatasetLoader createImageDBLoader(File datasetRootDir, String datasetCode)
    {
        IContentRepository repository = new ContentRepository(datasetRootDir, repositoryFactories);
        return new HCSImageDatasetLoader(query, datasetCode, repository);
    }

}
