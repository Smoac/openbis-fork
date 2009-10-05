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
            + "<protein_summary xmlns='http://regis-web.systemsbiology.net/protXML' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://sashimi.sourceforge.net/schema_revision/protXML/protXML_v3.xsd'>\n"
            + "<protein_summary_header reference_database='some/path/uniprot.HUMAN.v125.fasta'>\n"
            + " <program_details>\n"
            + " <proteinprophet_details >\n"
            + " <protein_summary_data_filter min_probability='0.25' sensitivity='1' false_positive_error_rate='0.5' predicted_num_correct='9' predicted_num_incorrect='4'/>\n"
            + " </proteinprophet_details >\n" 
            + " </program_details>\n"
            + "</protein_summary_header>\n"
            + "<protein_group probability='1'>\n"
            + " <protein probability='1'>\n"
            + "  <parameter name='key1' value='value1' type='type1'/>\n"
            + "  <parameter name='key2' value='value2' type='type2'/>\n"
            + "  <annotation protein_description='P42'/>\n"
            + "  <indistinguishable_protein><annotation protein_description='P43'/></indistinguishable_protein>\n"
            + "  <indistinguishable_protein><annotation protein_description='P44'/></indistinguishable_protein>\n"
            + "  <peptide peptide_sequence='VYQIDGNYSR'>\n"
            + "   <modification_info mod_nterm_mass='42' mod_cterm_mass='24.25'>\n"
            + "    <mod_aminoacid_mass position='1' mass='115.25'/>\n" 
            + "    <mod_aminoacid_mass position='4' mass='31.75'/>\n" 
            + "   </modification_info>\n"
            + "  </peptide>\n"
            + "  <peptide peptide_sequence='ITSN'/>\n"
            + " </protein>\n"
            + " <protein probability='0'>\n"
            + "  <annotation protein_description='Q42'/>\n"
            + "  <peptide peptide_sequence='YSR'/>\n"
            + " </protein>\n"
            + "</protein_group>\n"
            + "<protein_group probability='0.75'>\n"
            + " <protein probability='0.75'>\n"
            + "  <annotation protein_description='R42'/>\n"
            + "  <peptide peptide_sequence='IYSR'/>\n"
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