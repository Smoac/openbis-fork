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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;

/**
 * <i>Data Access Object</i> for {@link MaterialPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IMaterialDAO
{

    /**
     * Lists materials of given type. Fetches also properties and inhibitor.
     */
    public List<MaterialPE> listMaterialsWithPropertiesAndInhibitor(MaterialTypePE type);

    /** Inserts given {@link MaterialPE}s into the database. */
    public void createMaterials(List<MaterialPE> materials);

}
