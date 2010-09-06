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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongArrayMapper;
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

    final String WELLS_FOR_MATERIAL_ID_SELECT =
            "select "
                    + "      pl.id as plate_id,"
                    + "      exp.id as exp_id,"
                    + "      exp.code as exp_code,"
                    + "      exp.perm_id as exp_perm_id,"
                    + "      exp_type.code as exp_type_code,"
                    + "      projects.code as proj_code,"
                    + "      groups.code as space_code,"
                    + "      pl.perm_id as plate_perm_id,"
                    + "      pl.code as plate_code,"
                    + "      pl_type.code as plate_type_code,"
                    + "      well.id as well_id,"
                    + "      well.perm_id as well_perm_id,"
                    + "      well.code as well_code,"
                    + "      well_type.code as well_type_code,"
                    + "      well_material.id as material_content_id,"
                    + "      well_material.code as material_content_code,"
                    + "      well_material_type.code as material_content_type_code"
                    + " from samples pl"
                    + "   join samples well on pl.id = well.samp_id_part_of"
                    + "   join experiments exp on pl.expe_id = exp.id"
                    + "   join experiment_types exp_type on exp.exty_id = exp_type.id"
                    + "   join projects on exp.proj_id = projects.id"
                    + "   join groups on projects.grou_id = groups.id"
                    + "   join sample_types pl_type on pl.saty_id = pl_type.id"
                    + "   join sample_types well_type on well.saty_id = well_type.id"
                    + "   join sample_properties well_props on well.id = well_props.samp_id"
                    + "   join materials well_material on well_props.mate_prop_id = well_material.id"
                    + "   join material_types well_material_type on well_material.maty_id = well_material_type.id";

    /**
     * @return well locations which belong to a parent plate connected to a specified experiment.
     *         Each well will have a material property (e.g. gene) with the specified id.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = ?{1} and exp.id = ?{2}")
    public DataIterator<WellContent> getPlateLocationsForMaterialId(long materialId,
            long experimentId);

    /**
     * @return well locations which are connected to a given material (e.g. gene) with the specified
     *         id.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = ?{1}")
    public DataIterator<WellContent> getPlateLocationsForMaterialId(long materialId);

    /**
     * @return well locations which belong to a parent plate connected to a specified experiment.
     *         Each well will have a material property (e.g. gene) with one of the specified codes.
     *         The connected material will have one of the specified types.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = any(?{1}) and "
            + "well_material_type.code = any(?{2}) and exp.id = ?{3}", parameterBindings =
        { LongArrayMapper.class, StringArrayMapper.class, TypeMapper.class /* default mapper */}, fetchSize = FETCH_SIZE)
    public DataIterator<WellContent> getPlateLocationsForMaterialCodes(long[] materialIds,
            String[] materialTypeCodes, long experimentId);

    /**
     * @return well locations with the specified materials, from any experiment. Each well will have
     *         a material property (e.g. gene) with one of the specified codes. The connected
     *         material will have one of the specified types.
     */
    @Select(sql = WELLS_FOR_MATERIAL_ID_SELECT + " where well_material.id = any(?{1}) and "
            + "well_material_type.code = any(?{2})", parameterBindings =
        { LongArrayMapper.class, StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<WellContent> getPlateLocationsForMaterialCodes(long[] materialIds,
            String[] materialTypeCodes);

    /**
     * @return the material to well plate mapping for the given <var>platePermId</var>. Only
     *         consider materials of <var>materialTypeCode</var>. Only fills <var>well_code</var>
     *         and <var>material_content_code</var>. Note that this may return more than one row per
     *         well.
     */
    @Select(sql = "select "
            + "      well.code as well_code,"
            + "      well_material.code as material_content_code"
            + " from samples well"
            + "   join samples pl on pl.id = well.samp_id_part_of"
            + "   join sample_properties well_props on well.id = well_props.samp_id"
            + "   join materials well_material on well_props.mate_prop_id = well_material.id"
            + "   join material_types well_material_type on well_material.maty_id = well_material_type.id"
            + " where well_material_type.code=?{2} and pl.perm_id=?{1}")
    public DataIterator<WellContent> getPlateMappingForMaterialType(String platePermId,
            String materialTypeCode);

    /**
     * @return the material to well plate mapping for the given <var>platePermId</var>. Consider all
     *         material types. Only fills <var>well_code</var>, <var>material_content_code</var> and
     *         <var>material_content_code</var>. Note that this may return more than one row per
     *         well.
     */
    @Select(sql = "select "
            + "      well.code as well_code,"
            + "      well_material_type.code as material_content_type_code,"
            + "      well_material.code as material_content_code"
            + " from samples well"
            + "   join samples pl on pl.id = well.samp_id_part_of"
            + "   join sample_properties well_props on well.id = well_props.samp_id"
            + "   join materials well_material on well_props.mate_prop_id = well_material.id"
            + "   join material_types well_material_type on well_material.maty_id = well_material_type.id"
            + " where pl.perm_id=?{1} order by material_content_type_code")
    public DataIterator<WellContent> getPlateMapping(String platePermId);

    /**
     * @return the material to well plate mapping for the given <var>spaceCode</var> and
     *         <var>plateCode</var>. Only consider materials of <var>materialTypeCode</var>. Only
     *         fills <var>well_code</var> and <var>material_content_code</var>. Note that this may
     *         return more than one row per well.
     */
    @Select(sql = "select "
            + "      well.code as well_code,"
            + "      well_material.code as material_content_code"
            + " from samples well"
            + "   join samples pl on pl.id = well.samp_id_part_of"
            + "   join groups sp on pl.grou_id = sp.id"
            + "   join sample_properties well_props on well.id = well_props.samp_id"
            + "   join materials well_material on well_props.mate_prop_id = well_material.id"
            + "   join material_types well_material_type on well_material.maty_id = well_material_type.id"
            + " where well_material_type.code = ?{3} and pl.code = ?{2} and sp.code = ?{1}")
    public DataIterator<WellContent> getPlateMappingForMaterialType(String spaceCode,
            String plateCode, String materialTypeCode);

    /**
     * @return the material to well plate mapping for the given <var>spaceCode</var> and
     *         <var>plateCode</var>. Consider all material types. Only fills <var>well_code</var>,
     *         <var>material_content_code</var> and <var>material_content_code</var>. Note that this
     *         may return more than one row per well.
     */
    @Select(sql = "select "
            + "      well.code as well_code,"
            + "      well_material_type.code as material_content_type_code,"
            + "      well_material.code as material_content_code"
            + " from samples well"
            + "   join samples pl on pl.id = well.samp_id_part_of"
            + "   join groups sp on pl.grou_id = sp.id"
            + "   join sample_properties well_props on well.id = well_props.samp_id"
            + "   join materials well_material on well_props.mate_prop_id = well_material.id"
            + "   join material_types well_material_type on well_material.maty_id = well_material_type.id"
            + " where sp.code = ?{1} and pl.code = ?{2} order by material_content_type_code")
    public DataIterator<WellContent> getPlateMapping(String spaceCode, String plateCode);

    /**
     * Returns the plate geometry string for the plate with given <var>platePermId</var>, or
     * <code>null</code>, if no plate with that perm id can be found.
     */
    @Select(sql = "select space.code as space_code, pl.code as plate_code, cvte.code as plate_geometry "
            + "      from sample_properties sp "
            + "         join samples pl on pl.id = sp.samp_id "
            + "         join controlled_vocabulary_terms cvte on cvte.id = sp.cvte_id "
            + "         join sample_type_property_types stpt on stpt.id = sp.stpt_id "
            + "         join property_types pt on pt.id = stpt.prty_id "
            + "         join groups space on pl.grou_id = space.id"
            + "      where pt.code = 'PLATE_GEOMETRY' "
            + "         and pt.is_internal_namespace = true and pl.perm_id = ?{1}")
    public PlateGeometryContainer tryGetPlateGeometry(String platePermId);

    /**
     * Returns the plate geometry string for the plate with given <var>spaceCode</var> and
     * <var>plateCode</var>, or <code>null</code>, if no plate with that code can be found.
     */
    @Select(sql = "select pl.perm_id, cvte.code as plate_geometry "
            + "      from sample_properties sp "
            + "         join samples pl on pl.id = sp.samp_id "
            + "         join controlled_vocabulary_terms cvte on cvte.id = sp.cvte_id "
            + "         join sample_type_property_types stpt on stpt.id = sp.stpt_id "
            + "         join property_types pt on pt.id = stpt.prty_id "
            + "         join groups space on pl.grou_id = space.id"
            + "      where pt.code = 'PLATE_GEOMETRY' "
            + "         and pt.is_internal_namespace = true and space.code = ?{1} and pl.code = ?{2}")
    public PlateGeometryContainer tryGetPlateGeometry(String spaceCode, String plateCode);

    /**
     * Returns the ids of materials of specified type and used in specified experiment.
     */
    @Select(sql = "SELECT distinct well_material.id            "
            + "      FROM samples pl                           "
            + "      JOIN samples well ON well.samp_id_part_of = pl.id"
            + "      JOIN sample_properties well_props ON well_props.samp_id = well.id"
            + "      JOIN materials well_material ON well_material.id = well_props.mate_prop_id"
            + "     WHERE pl.expe_id = ?{1}                   "
            + "       AND well_material.maty_id = ?{2}", fetchSize = FETCH_SIZE)
    public DataIterator<Long> getMaterialsForExperimentWells(long experimentId, long materialTypeId);

}
