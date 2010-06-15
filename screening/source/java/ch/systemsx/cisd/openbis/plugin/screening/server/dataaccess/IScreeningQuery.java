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
import net.lemnik.eodsql.TypeMapper;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.StringArrayMapper;

/**
 * Screening specific queries on openbis database.
 * 
 * @author Tomasz Pylak
 */
@Private
@Friend(toClasses = WellContent.class)
public interface IScreeningQuery extends BaseQuery
{

    public static final int FETCH_SIZE = 1000;

    /**
     * @return well locations which belong to a parent plate connected to a specified experiment.
     *         Each well will have a material property (e.g. oligo), which is connected through
     *         another material property to a material (e.g. gene) with a specified id.
     */
    @Select(sql = "select pl.id as plate_id, pl.perm_id as plate_perm_id, pl.code as plate_code, pl_type.code as plate_type_code, "
            + "      well.id as well_id, well.perm_id as well_perm_id, well.code as well_code, well_type.code as well_type_code, "
            + "      well_material.id as material_content_id, well_material.code as material_content_code, "
            + "      well_material_type.code as material_content_type_code,                     "
            // nested material information will be the same for all rows - no fetching here
            + "      null as nested_well_material_id, null as nested_well_material_code, null as nested_well_material_type_code "
            + "from experiments exp, samples pl, samples well,                                     "
            + "     sample_properties well_props, materials well_material, material_properties well_material_props, "
            + "     sample_types pl_type, sample_types well_type, material_types well_material_type "
            + "where                                                                               "
            // find 'well' belonging to the plate which belongs to this experiment
            + "exp.id = ?{2} and pl.expe_id = exp.id and well.samp_id_part_of = pl.id and             "
            // find 'well_material' assigned to the well
            + "well_props.samp_id = well.id and well_material.id = well_props.mate_prop_id and     "
            // well content material property must point to the specified gene id
            + "well_material_props.mate_id = well_material.id and well_material_props.mate_prop_id = ?{1} and "
            // additional joins to entity type tables
            + "pl_type.id = pl.saty_id and well_type.id = well.saty_id and                         "
            + "well_material_type.id = well_material.maty_id ")
    public DataIterator<WellContent> getPlateLocationsForNestedMaterialId(long geneMaterialId,
            long experimentId);

    // well content with "first-level" materials (like oligos or controls)
    static final String PLATE_LOCATIONS_FIRST_LEVEL_SELECT =
            "pl.id as plate_id, pl.perm_id as plate_perm_id, pl.code as plate_code, pl_type.code as plate_type_code, "
                    + "well.id as well_id, well.perm_id as well_perm_id, well.code as well_code, well_type.code as well_type_code, "
                    + "well_material.id as material_content_id, well_material.code as material_content_code, "
                    + "well_material_type.code as material_content_type_code ";

    static final String PLATE_LOCATIONS_FIRST_LEVEL_FROM =
            "experiments exp, samples pl, samples well,                   "
                    + "sample_properties well_props, materials well_material, material_properties well_material_props, "
                    + "sample_types pl_type, sample_types well_type, material_types well_material_type ";

    static final String PLATE_LOCATIONS_COMMON_WHERE =
    // -- find 'well' belonging to the plate which belongs to this experiment
            "exp.id = ?{1} and pl.expe_id = exp.id and well.samp_id_part_of = pl.id and "
                    // -- find 'well_material' assigned to the well
                    + "well_props.samp_id = well.id and well_material.id = well_props.mate_prop_id and "
                    // -- additional joins to entity type tables
                    + "pl_type.id = pl.saty_id and well_type.id = well.saty_id and     "
                    + "well_material_type.id = well_material.maty_id ";

    // well content for "second-level" materials (like genes)
    static final String PLATE_LOCATIONS_SECOND_LEVEL_SELECT =
            "nested_well_material.id as nested_well_material_id, nested_well_material.code as nested_well_material_code, "
                    + "nested_well_material_type.code as nested_well_material_type_code ";

    static final String PLATE_LOCATIONS_SECOND_LEVEL_FROM =
            "materials nested_well_material, material_types nested_well_material_type ";

    static final String PLATE_LOCATIONS_SECOND_LEVEL_WHERE =
    // -- well content material property
            "well_material_props.mate_id = well_material.id and "
                    // -- material connected to the material in the well (e.g. gene)
                    + "well_material_props.mate_prop_id = nested_well_material.id and "
                    + "nested_well_material.code = any(?{2}) and "
                    + "nested_well_material_type.id = nested_well_material.maty_id";

    @Select(sql = "select " + PLATE_LOCATIONS_FIRST_LEVEL_SELECT + ", "
            + PLATE_LOCATIONS_SECOND_LEVEL_SELECT

            + " from " + PLATE_LOCATIONS_FIRST_LEVEL_FROM + ", "
            + PLATE_LOCATIONS_SECOND_LEVEL_FROM

            + " where " + PLATE_LOCATIONS_COMMON_WHERE + " and "
            + PLATE_LOCATIONS_SECOND_LEVEL_WHERE, parameterBindings =
        { TypeMapper.class/* default */, StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<WellContent> getPlateLocationsForNestedMaterialCodes(long experimentId,
            String[] nestedMaterialCodes);

}
