/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.sql;

/**
 * @author pkupczyk
 */
public class PropertyQueryGenerator
{

    public static void main(String[] args)
    {
        createSamplePropertyQuery();
        createSamplePropertyHistoryQuery();
        createMaterialPropertyQuery();
        createMaterialPropertyHistoryQuery();
    }

    private static void createSamplePropertyQuery()
    {
        PropertyQueryParams params = new PropertyQueryParams();
        params.propertyTable = "sample_properties";
        params.propertyTableEntityIdColumn = "samp_id";
        params.propertyTableEntityTypePropertyTypeIdColumn = "stpt_id";
        params.entityTypePropertyTypeTable = "sample_type_property_types";
        System.out.println("Sample property: \n" + createPropertyQuery(params));
        System.out.println("Sample material property: \n" + createMaterialPropertyQuery(params));
    }

    private static void createSamplePropertyHistoryQuery()
    {
        PropertyHistoryQueryParams params = new PropertyHistoryQueryParams();
        params.propertyHistoryTable = "sample_properties_history";
        params.propertyHistoryTableEntityIdColumn = "samp_id";
        params.propertyHistoryTableEntityTypePropertyTypeIdColumn = "stpt_id";
        params.entityTypePropertyTypeTable = "sample_type_property_types";
        System.out.println("Sample property history: \n" + createPropertyHistoryQuery(params));
    }

    private static void createMaterialPropertyQuery()
    {
        PropertyQueryParams params = new PropertyQueryParams();
        params.propertyTable = "material_properties";
        params.propertyTableEntityIdColumn = "mate_id";
        params.propertyTableEntityTypePropertyTypeIdColumn = "mtpt_id";
        params.entityTypePropertyTypeTable = "material_type_property_types";
        System.out.println("Material property: \n" + createPropertyQuery(params));
        System.out.println("Material material property: \n" + createMaterialPropertyQuery(params));
    }

    private static void createMaterialPropertyHistoryQuery()
    {
        PropertyHistoryQueryParams params = new PropertyHistoryQueryParams();
        params.propertyHistoryTable = "material_properties_history";
        params.propertyHistoryTableEntityIdColumn = "mate_id";
        params.propertyHistoryTableEntityTypePropertyTypeIdColumn = "mtpt_id";
        params.entityTypePropertyTypeTable = "material_type_property_types";
        System.out.println("Material property history: \n" + createPropertyHistoryQuery(params));
    }

    public static String createPropertyQuery(PropertyQueryParams params)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append("p." + params.propertyTableEntityIdColumn + " as entityId, ");
        sb.append("pt.code as propertyCode, ");
        sb.append("p.value as propertyValue, ");
        sb.append("m.code as materialPropertyValueCode, ");
        sb.append("mt.code as materialPropertyValueTypeCode, ");
        sb.append("cvt.code as vocabularyPropertyValue \n");
        sb.append("from ");
        sb.append(params.propertyTable + " p \n");
        sb.append("left outer join materials m on p.mate_prop_id = m.id \n");
        sb.append("left outer join controlled_vocabulary_terms cvt on p.cvte_id = cvt.id \n");
        sb.append("left join material_types mt on m.maty_id = mt.id \n");
        sb.append("left join " + params.entityTypePropertyTypeTable + " etpt on p." + params.propertyTableEntityTypePropertyTypeIdColumn
                + " = etpt.id \n");
        sb.append("left join property_types pt on etpt.prty_id = pt.id \n");
        sb.append("where p." + params.propertyTableEntityIdColumn + " = any(?{1})\n");
        return sb.toString();
    }

    public static String createMaterialPropertyQuery(PropertyQueryParams params)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append("p." + params.propertyTableEntityIdColumn + " as entityId, ");
        sb.append("pt.code as propertyCode, ");
        sb.append("p.mate_prop_id as propertyValue \n");
        sb.append("from ");
        sb.append(params.propertyTable + " p \n");
        sb.append("left join " + params.entityTypePropertyTypeTable + " etpt on p." + params.propertyTableEntityTypePropertyTypeIdColumn
                + " = etpt.id \n");
        sb.append("left join property_types pt on etpt.prty_id = pt.id \n");
        sb.append("where p.mate_prop_id is not null and p." + params.propertyTableEntityIdColumn + " = any(?{1})\n");
        return sb.toString();
    }

    public static String createPropertyHistoryQuery(PropertyHistoryQueryParams params)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append("ph." + params.propertyHistoryTableEntityIdColumn + " as entityId, ");
        sb.append("ph.pers_id_author as authorId, ");
        sb.append("pt.code as propertyCode, ");
        sb.append("ph.value as propertyValue, ");
        sb.append("ph.material as materialPropertyValue, ");
        sb.append("ph.vocabulary_term as vocabularyPropertyValue, ");
        sb.append("ph.valid_from_timestamp as validFrom, ");
        sb.append("ph.valid_until_timestamp as validTo \n");
        sb.append("from ");
        sb.append(params.propertyHistoryTable + " ph \n");
        sb.append("left join " + params.entityTypePropertyTypeTable + " etpt on ph." + params.propertyHistoryTableEntityTypePropertyTypeIdColumn
                + " = etpt.id ");
        sb.append("left join property_types pt on etpt.prty_id = pt.id \n");
        sb.append("where ph." + params.propertyHistoryTableEntityIdColumn + " = any(?{1})\n");
        return sb.toString();
    }

    public static class PropertyQueryParams
    {
        public String propertyTable;

        public String propertyTableEntityIdColumn;

        public String propertyTableEntityTypePropertyTypeIdColumn;

        public String entityTypePropertyTypeTable;

    }

    public static class PropertyHistoryQueryParams
    {
        public String propertyHistoryTable;

        public String propertyHistoryTableEntityIdColumn;

        public String propertyHistoryTableEntityTypePropertyTypeIdColumn;

        public String entityTypePropertyTypeTable;
    }

}
