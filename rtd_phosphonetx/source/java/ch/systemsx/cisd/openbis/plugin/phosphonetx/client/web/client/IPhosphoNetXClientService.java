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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentAndReferenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSequenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSummaryByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListSampleAbundanceByProteinCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IPhosphoNetXClientService extends IClientService
{
    public Vocabulary getTreatmentTypeVocabulary();
    
    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            TechId experimentID, String treatmentTypeOrNull);
    
    public ResultSet<ProteinInfo> listProteinsByExperiment(ListProteinByExperimentCriteria criteria)
            throws UserFailureException;

    public String prepareExportProteins(TableExportCriteria<ProteinInfo> exportCriteria)
            throws UserFailureException;
    
    public ResultSet<ProteinSummary> listProteinSummariesByExperiment(
            ListProteinSummaryByExperimentCriteria criteria) throws UserFailureException;
    
    public String prepareExportProteinSummary(TableExportCriteria<ProteinSummary> exportCriteria)
            throws UserFailureException;
    
    public ProteinByExperiment getProteinByExperiment(TechId experimentID, TechId proteinReferenceID)
            throws UserFailureException;
    
    public ResultSet<ProteinSequence> listSequencesByProteinReference(ListProteinSequenceCriteria criteria)
            throws UserFailureException;

    public String prepareExportProteinSequences(TableExportCriteria<ProteinSequence> exportCriteria)
            throws UserFailureException;
    
    public ResultSet<DataSetProtein> listProteinsByExperimentAndReference(
            ListProteinByExperimentAndReferenceCriteria criteria) throws UserFailureException;
    
    public String prepareExportDataSetProteins(TableExportCriteria<DataSetProtein> exportCriteria)
            throws UserFailureException;
    
    public ResultSet<SampleWithPropertiesAndAbundance> listSamplesWithAbundanceByProtein(
            ListSampleAbundanceByProteinCriteria criteria) throws UserFailureException; 

    public String prepareExportSamplesWithAbundance(
            TableExportCriteria<SampleWithPropertiesAndAbundance> exportCriteria)
            throws UserFailureException;

    public ResultSetWithEntityTypes<Sample> listRawDataSamples(IResultSetConfig<String, Sample> criteria)
            throws UserFailureException;

    public String prepareExportRawDataSamples(TableExportCriteria<Sample> exportCriteria)
            throws UserFailureException;
    
    public void copyRawData(long[] rawDataSampleIDs) throws UserFailureException;

}
