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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * A generic material <i>Business Object</i>.
 * 
 * @author Izabela Adamczyk
 */
public interface IMaterialBO extends IEntityBusinessObject
{
    /** Returns the material which has been loaded. */
    MaterialPE getMaterial();

    /** Adds properties */
    public void enrichWithProperties();

    /**
     * Changes given materials. Currently allowed changes: properties.
     * 
     * @param deleteUntouchedProperties if true all old properties which have not been mentioned in
     *            the update list will be deleted.
     */
    public void update(List<MaterialUpdateDTO> materialsUpdate, boolean deleteUntouchedProperties);

    /**
     * Changes given material. Currently allowed changes: properties.
     */
    public void update(MaterialUpdateDTO materialUpdate);

    /**
     * Deletes material for specified reason.
     * 
     * @param materialId material technical identifier
     * @throws UserFailureException if material with given technical identifier is not found.
     */
    void deleteByTechId(TechId materialId, String reason);

    /** Describes the material update operation, currently only properties can be changed. */
    public static class MaterialUpdateDTO extends AbstractHashable
    {
        private final TechId materialId;

        private final List<IEntityProperty> properties;

        private final Date version;

        public MaterialUpdateDTO(TechId materialId, List<IEntityProperty> properties, Date version)
        {
            this.materialId = materialId;
            this.properties = properties;
            this.version = version;
        }

        public TechId getMaterialId()
        {
            return materialId;
        }

        public List<IEntityProperty> getProperties()
        {
            return properties;
        }

        public Date getVersion()
        {
            return version;
        }
    }

}
