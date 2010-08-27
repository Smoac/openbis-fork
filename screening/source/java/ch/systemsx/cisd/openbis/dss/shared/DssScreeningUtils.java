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

package ch.systemsx.cisd.openbis.dss.shared;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * Utility methods for DSS.
 * 
 * @author Franz-Josef Elmer
 */
public class DssScreeningUtils
{
    private static final IImagingReadonlyQueryDAO query = createQuery();

    public static IImagingReadonlyQueryDAO getQuery()
    {
        return query;
    }

    /**
     * Creates a DAO based on imaging database specified in DSS service.properties by data source
     * {@link ScreeningConstants#IMAGING_DATA_SOURCE}.
     * <p>
     * Returned query is reused and should not be closed.
     * </p>
     */
    private static IImagingReadonlyQueryDAO createQuery()
    {
        DataSource dataSource =
                ServiceProvider.getDataSourceProvider().getDataSource(
                        ScreeningConstants.IMAGING_DATA_SOURCE);
        return QueryTool.getQuery(dataSource, IImagingReadonlyQueryDAO.class);
    }
}
