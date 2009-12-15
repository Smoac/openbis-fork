/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;

/**
 * Screening specific queries on openbis database.
 * 
 * @author Tomasz Pylak
 */
@Private
@Friend(toClasses = WellLocation.class)
public interface IScreeningQuery extends BaseQuery
{
    /**
     * @return well locations which belong to a parent plate connected to a specified experiment.
     *         Each well will have a material property (e.g. oligo), which is connected through
     *         another material property to a material (e.g. gene) with a specified id.
     */
    @Select(sql = "select pl.id as plate_id, pl.code as plate_code, pl_type.code as plate_type_code, "
            + "      well.id as well_id, well.code as well_code, well_type.code as well_type_code, "
            + "      well_material.id as material_content_id, well_material.code as material_content_code, "
            + "      well_material_type.code as material_content_type_code                        "
            + "from experiments exp, samples pl, samples well,                                     "
            + "     sample_properties well_props, materials well_material, material_properties well_material_props, "
            + "     sample_types pl_type, sample_types well_type, material_types well_material_type "
            + "where                                                                               "
            + "-- find 'well' belonging to the plate which belongs to this experiment              "
            + "exp.id = ?{2} and pl.expe_id = exp.id and well.samp_id_part_of = pl.id and             "
            + "-- find 'well_material' assigned to the well                                        "
            + "well_props.samp_id = well.id and well_material.id = well_props.mate_prop_id and     "
            + "-- well content material property must point to the specified gene id               "
            + "well_material_props.mate_id = well_material.id and well_material_props.mate_prop_id = ?{1} and "
            + "-- additional joins to entity type tables                                           "
            + "pl_type.id = pl.saty_id and well_type.id = well.saty_id and                         "
            + "well_material_type.id = well_material.maty_id ")
    public DataIterator<WellLocation> getPlateLocations(long geneMaterialId, long experimentId);

}
