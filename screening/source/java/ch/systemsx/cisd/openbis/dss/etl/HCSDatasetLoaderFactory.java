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

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.io.FileBasedContent;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.HCSDatasetLoader;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Tomasz Pylak
 */
public class HCSDatasetLoaderFactory
{
    private static final IImagingUploadDAO query = createQuery();

    private static IImagingUploadDAO createQuery()
    {
        DataSource dataSource =
                ServiceProvider.getDataSourceProvider().getDataSource(
                        ScreeningConstants.IMAGING_DATA_SOURCE);
        return QueryTool.getQuery(dataSource, IImagingUploadDAO.class);
    }

    public static final IHCSDatasetLoader create(File datasetRootDir, String datasetCode)
    {
        return createImageDBLoader(datasetRootDir, datasetCode);
        // return createBDSLoader(datasetRootDir);
    }

    private static HCSDatasetLoader createImageDBLoader(File datasetRootDir, String datasetCode)
    {
        return new HCSDatasetLoader(query, datasetCode, datasetRootDir);
    }

    // remove when not needed
    @SuppressWarnings("unused")
    private static IHCSDatasetLoader createBDSLoader(File datasetRootDir)
    {
        final ch.systemsx.cisd.bds.hcs.HCSDatasetLoader loader =
                new ch.systemsx.cisd.bds.hcs.HCSDatasetLoader(datasetRootDir);
        return adapt(loader);
    }

    private static IHCSDatasetLoader adapt(final ch.systemsx.cisd.bds.hcs.HCSDatasetLoader loader)
    {
        return new IHCSDatasetLoader()
            {

                public void close()
                {
                    loader.close();
                }

                public int getChannelCount()
                {
                    return loader.getChannelCount();
                }

                public Geometry getPlateGeometry()
                {
                    return loader.getPlateGeometry();
                }

                public Geometry getWellGeometry()
                {
                    return loader.getWellGeometry();
                }

                public AbsoluteImageReference tryGetImage(int chosenChannel, Location wellLocation,
                        Location tileLocation)
                {
                    String absPath =
                            loader.tryGetStandardNodeAt(chosenChannel, wellLocation, tileLocation);
                    return new AbsoluteImageReference(new FileBasedContent(new File(absPath)), null, null);
                }

            };
    }
}
