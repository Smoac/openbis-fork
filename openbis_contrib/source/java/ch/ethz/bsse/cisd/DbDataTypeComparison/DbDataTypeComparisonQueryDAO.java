package ch.ethz.bsse.cisd.DbDataTypeComparison;

/**
 * @author Manuel Kohler
 */

import java.util.List;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

interface DbDataTypeComparisonDAO extends TransactionQuery
{
    public static final int FETCH_SIZE = 1000;

    // types:
    @Select("select code as sample_types from sample_types order by code")
    public String[] listSampleTypeCodes();

    @Select("select code as data_set_types from data_set_types order by code")
    public String[] listDataSetTypeCodes();

    @Select("select code as experiment_types from experiment_types order by code")
    public String[] listExperimentTypeCodes();

    @Select("select code as material_types from material_types order by code")
    public String[] listMaterialTypeCodes();

    @Select("select code as property_types from property_types order by code;")
    public String[] listPropertyTypeCodes();

    // which data_type for a property_type?
    @Select("select pt.code as ptCode, dt.code as dtCode from property_types pt join data_types dt on pt.daty_id=dt.id order by pt.code;")
    public List<PropertyTypeAndDataTypeDTO> getPropertyTypeAndDataType();

    // property assignments:
    // samples
    @Select("select st.code as sampleType, pt.code as propertyType from property_types pt"
            + " join sample_type_property_types stpt on pt.id = stpt.prty_id"
            + " join sample_types st on stpt.saty_id=st.id order by st.code;")
    public List<SamplePropertyAssignmentsDTO> getSamplePropertyAssignments();

    // data sets
    @Select("select dst.code as dataSet, pt.code as propertyType from property_types pt"
            + " join data_set_type_property_types dstpt on pt.id = dstpt.prty_id"
            + " join data_set_types dst on dstpt.dsty_id=dst.id order by dst.code;")
    public List<DataSetPropertyAssignmentsDTO> getDataSetPropertyAssignments();

    // experiments
    @Select("select ep.code as experimentType, pt.code as propertyType from property_types pt"
            + " join experiment_type_property_types etpt on pt.id=etpt.exty_id"
            + " join experiment_types ep on etpt.exty_id=ep.id order by ep.code;")
    public List<ExperimentPropertyAssignmentsDTO> getExperimentPropertyAssignments();

    // materials
    @Select("select mt.code as materialType, pt.code as propertyType from property_types pt"
            + " join material_type_property_types mtpt on pt.id=mtpt.prty_id"
            + " join material_types mt on mt.id = mtpt.maty_id order by mt.code;")
    public List<MaterialPropertyAssignmentDTO> getMaterialPropertyAssignment();

    // controlled vocabularies
    @Select("select code as controlledVocabularies from controlled_vocabularies;")
    public List<ControlledVocabulariesDTO> getControlledVocabularies();

    // controlled vocabularies with terms
    @Select("select cv.code as controlledVocabularies, cvt.code as controlledVocabulariesTerms"
            + " from controlled_vocabularies cv join controlled_vocabulary_terms cvt"
            + " on cv.id=cvt.covo_id order by cv.code;")
    public List<TermsOfControlledVocabulariesDTO> getTermsOfControlledVocabularies();

}