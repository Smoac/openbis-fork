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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;

/**
 * A {@link IOriginalDataProvider} implementation for listing materials.
 * 
 * @author Christian Ribeaud
 */
final class ListMaterialOriginalDataProvider extends AbstractOriginalDataProvider<Material>
{
    private final ListMaterialDisplayCriteria displayCriteria;

    ListMaterialOriginalDataProvider(final ICommonServer commonServer, final String sessionToken,
            final ListMaterialDisplayCriteria listCriteria)
    {
        super(commonServer, sessionToken);
        this.displayCriteria = listCriteria;
    }

    //
    // AbstractOriginalDataProvider
    //

    public final List<Material> getOriginalData()
    {
        final List<Material> materials =
                commonServer.listMaterials(sessionToken, displayCriteria.getListCriteria(), true);
        return materials;
    }
}
