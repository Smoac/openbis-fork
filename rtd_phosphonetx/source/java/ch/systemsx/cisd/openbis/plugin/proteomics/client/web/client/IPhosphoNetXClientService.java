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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinByExperimentAndReferenceCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinSequenceCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinSummaryByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListSampleAbundanceByProteinCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSummary;

/**
 * * Service interface for the PhosphoNetX GWT client.
 * <p>
 * Each method should declare throwing {@link UserFailureException}. The authorization framework can throw it when the user has insufficient
 * privileges. If it is not marked, the GWT client will report unexpected exception.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public interface IPhosphoNetXClientService extends IClientService
{
    public TypedTableResultSet<Sample> listParentlessMsInjectionSamples(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> criteria)
            throws UserFailureException;

    public TypedTableResultSet<Sample> listBiologicalSamples(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> criteria)
            throws UserFailureException;

    public void linkSamples(Sample parentSample, List<Sample> childSamples) throws UserFailureException;

    public void createAndLinkSamples(NewSample newBiologicalSample,
            List<Sample> msInjectionSamples) throws UserFailureException;

    public Vocabulary getTreatmentTypeVocabulary() throws UserFailureException;

    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            TechId experimentID, String treatmentTypeOrNull) throws UserFailureException;

    public TypedTableResultSet<ProteinInfo> listProteinsByExperiment(ListProteinByExperimentCriteria criteria)
            throws UserFailureException;

    public String prepareExportProteins(TableExportCriteria<TableModelRowWithObject<ProteinInfo>> exportCriteria)
            throws UserFailureException;

    public TypedTableResultSet<ProteinSummary> listProteinSummariesByExperiment(
            ListProteinSummaryByExperimentCriteria criteria) throws UserFailureException;

    public String prepareExportProteinSummary(TableExportCriteria<TableModelRowWithObject<ProteinSummary>> exportCriteria)
            throws UserFailureException;

    public ProteinByExperiment getProteinByExperiment(TechId experimentID, TechId proteinReferenceID)
            throws UserFailureException;

    public TypedTableResultSet<ProteinSequence> listSequencesByProteinReference(
            ListProteinSequenceCriteria criteria) throws UserFailureException;

    public String prepareExportProteinSequences(
            TableExportCriteria<TableModelRowWithObject<ProteinSequence>> exportCriteria)
            throws UserFailureException;

    public TypedTableResultSet<DataSetProtein> listProteinsByExperimentAndReference(
            ListProteinByExperimentAndReferenceCriteria criteria) throws UserFailureException;

    public String prepareExportDataSetProteins(
            TableExportCriteria<TableModelRowWithObject<DataSetProtein>> exportCriteria)
            throws UserFailureException;

    public TypedTableResultSet<ProteinRelatedSample> listProteinRelatedSamplesByProtein(
            ListSampleAbundanceByProteinCriteria criteria) throws UserFailureException;

    public String prepareExportProteinRelatedSamples(
            TableExportCriteria<TableModelRowWithObject<ProteinRelatedSample>> exportCriteria)
            throws UserFailureException;

    public TypedTableResultSet<Sample> listRawDataSamples(
            IResultSetConfig<String, TableModelRowWithObject<Sample>> criteria) throws UserFailureException;

    public String prepareExportRawDataSamples(TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria)
            throws UserFailureException;

    public void processRawData(String dataSetProcessingKey, long[] rawDataSampleIDs,
            String dataSetType) throws UserFailureException;

}
