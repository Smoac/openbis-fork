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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class ProtXMLTestCase extends AbstractFileSystemTestCase
{

    protected static final String EXAMPLE = "<?xml version='1.0' encoding='UTF-8'?>\n"
            + "<protein_summary xmlns='http://regis-web.systemsbiology.net/protXML' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
            		"xsi:schemaLocation='http://regis-web.systemsbiology.net/protXML http://sashimi.sourceforge.net/schema_revision/protXML/protXML_v5.xsd'>\n"
            + "<protein_summary_header reference_database='some/path/uniprot.HUMAN.v125.fasta' residue_substitution_list='I -> L' " +
            		"source_files='/home/lars/tmp/interact.pep.xml' source_files_alt='/home/lars/tmp/interact.pep.xml' min_peptide_probability='0.20' min_peptide_weight='0.50' num_predicted_correct_prots='755.6' num_input_1_spectra='0' num_input_2_spectra='77824' num_input_3_spectra='38201' num_input_4_spectra='0' num_input_5_spectra='0' initial_min_peptide_prob='0.05' total_no_spectrum_ids='104665.2' sample_enzyme='trypsin'>\n"
            + " <program_details analysis='' time='2009-08-12T09:08:07'>\n"
            + " <proteinprophet_details occam_flag='true' groups_flag='Y' degen_flag='Y' nsp_flag='Y' initial_peptide_wt_iters='2' " +
            		"nsp_distribution_iters='2' final_peptide_wt_iters='4' run_options='XML'>\n"
            + " <nsp_information neighboring_bin_smoothing='Y'>\n"
            + "   <nsp_distribution bin_no='0' nsp_lower_bound_incl='0.00' nsp_upper_bound_incl='0.00' pos_freq='0.003' neg_freq='0.320' pos_to_neg_ratio='0.01'/>\n"
            + " </nsp_information>\n"
            + " <ni_information>\n"
            + "   <ni_distribution bin_no='1' pos_freq='42' neg_freq='42' pos_to_neg_ratio='1'/>\n"
            + " </ni_information>\n"
            + " <protein_summary_data_filter min_probability='0.25' sensitivity='1' false_positive_error_rate='0.5' predicted_num_correct='9' " +
            		"predicted_num_incorrect='4'/>\n"
            + " </proteinprophet_details>\n" 
            + " </program_details>\n"
            + "</protein_summary_header>\n"
            + "<dataset_derivation generation_no='1'/>\n"
            + "<protein_group probability='1' group_number='1'>\n"
            + " <protein probability='1' protein_name='a' n_indistinguishable_proteins='3' group_sibling_id='0'>\n"
            + "  <parameter name='key1' value='value1' type='type1'/>\n"
            + "  <parameter name='key2' value='value2' type='type2'/>\n"
            + "  <annotation protein_description='P42'/>\n"
            + "  <indistinguishable_protein protein_name='a'><annotation protein_description='P43'/></indistinguishable_protein>\n"
            + "  <indistinguishable_protein protein_name='a'><annotation protein_description='P44'/></indistinguishable_protein>\n"
            + "  <peptide peptide_sequence='VYQIDGNYSR' charge='1' initial_probability='0' is_nondegenerate_evidence='true' n_enzymatic_termini='1' n_instances='1' is_contributing_evidence='N'>\n"
            + "   <modification_info mod_nterm_mass='42' mod_cterm_mass='24.25'>\n"
            + "    <mod_aminoacid_mass position='1' mass='115.25'/>\n" 
            + "    <mod_aminoacid_mass position='4' mass='31.75'/>\n" 
            + "   </modification_info>\n"
            + "  </peptide>\n"
            + "  <peptide peptide_sequence='ITSN' charge='1' initial_probability='0' is_nondegenerate_evidence='Y' n_enzymatic_termini='1' n_instances='1' is_contributing_evidence='N'/>\n"
            + " </protein>\n"
            + " <protein probability='0' protein_name='a' n_indistinguishable_proteins='1' group_sibling_id='1'>\n"
            + "  <annotation protein_description='Q42'/>\n"
            + "  <peptide peptide_sequence='YSR' charge='1' initial_probability='0' is_nondegenerate_evidence='Y' n_enzymatic_termini='1' n_instances='1' is_contributing_evidence='N'/>\n"
            + " </protein>\n"
            + "</protein_group>\n"
            + "<protein_group probability='0.75' group_number='2'>\n"
            + " <protein probability='0.75' protein_name='a' n_indistinguishable_proteins='1' group_sibling_id='2'>\n"
            + "  <annotation protein_description='R42'/>\n"
            + "  <peptide peptide_sequence='IYSR' charge='1' initial_probability='0' is_nondegenerate_evidence='Y' n_enzymatic_termini='1' n_instances='1' is_contributing_evidence='N'/>\n"
            + " </protein>\n"
            + "</protein_group>\n"
            + "</protein_summary>\n";

    /**
     *
     *
     */
    public ProtXMLTestCase()
    {
        super();
    }

    /**
     *
     *
     * @param cleanAfterMethod
     */
    public ProtXMLTestCase(boolean cleanAfterMethod)
    {
        super(cleanAfterMethod);
    }

}