/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;

/**
 * Read-only table for materials. Holds a collection of instances of {@link MaterialPE}.
 * 
 * @author Izabela Adamczyk
 */
public interface IMaterialTable
{

    /** Returns the loaded {@link MaterialPE}. */
    public List<MaterialPE> getMaterials();

    /**
     * Defines new materials of specified type.<br>
     * Calls of this method cannot be mixed with calls to {@link #update}.
     */
    public void add(List<NewMaterial> newMaterials, MaterialTypePE materialTypePE);

    /**
     * Changes given materials. Currently allowed changes: properties.<br>
     * Calls of this method cannot be mixed with calls to {@link #add}.
     * 
     * @param deleteUntouchedProperties if true all old properties which have not been mentioned in
     *            the update list will be deleted.
     */
    public void update(List<MaterialUpdateDTO> materialsUpdate, boolean deleteUntouchedProperties);

    /**
     * Saves new materials in the database.
     */
    public void save();

}
