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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
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
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;

/**
 * @author Franz-Josef Elmer
 */
public interface IPhosphoNetXClientServiceAsync extends IClientServiceAsync
{
    /** @see IPhosphoNetXClientService#listParentlessMsInjectionSamples(DefaultResultSetConfig) */
    public void listParentlessMsInjectionSamples(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> criteria,
            AsyncCallback<TypedTableResultSet<Sample>> callback);
    
    /** @see IPhosphoNetXClientService#listBiologicalSamples(DefaultResultSetConfig) */
    public void listBiologicalSamples(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> criteria,
            AsyncCallback<TypedTableResultSet<Sample>> callback);
    
    /** @see IPhosphoNetXClientService#linkSamples(Sample, List) */
    public void linkSamples(Sample parentSample, List<Sample> childSamples, AsyncCallback<Void> callback);
    
    /** @see IPhosphoNetXClientService#createAndLinkSamples(NewSample, List) */
    public void createAndLinkSamples(NewSample newBiologicalSample,
            List<Sample> msInjectionSamples, AsyncCallback<Void> callback);
    
    /** @see IPhosphoNetXClientService#getTreatmentTypeVocabulary() */
    public void getTreatmentTypeVocabulary(AsyncCallback<Vocabulary> callback);
    
    /**
     * @see IPhosphoNetXClientService#getAbundanceColumnDefinitionsForProteinByExperiment(TechId,
     *      String)
     */
    public void getAbundanceColumnDefinitionsForProteinByExperiment(TechId experimentID,
            String treatmentTypeOrNull,
            AsyncCallback<List<AbundanceColumnDefinition>> callback);

    /** @see IPhosphoNetXClientService#listProteinsByExperiment(ListProteinByExperimentCriteria) */
    public void listProteinsByExperiment(ListProteinByExperimentCriteria criteria,
            AsyncCallback<TypedTableResultSet<ProteinInfo>> callback);

    /** @see IPhosphoNetXClientService#prepareExportProteins(TableExportCriteria) */
    public void prepareExportProteins(
            TableExportCriteria<TableModelRowWithObject<ProteinInfo>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see IPhosphoNetXClientService#listProteinSummariesByExperiment(ListProteinSummaryByExperimentCriteria) */
    public void listProteinSummariesByExperiment(ListProteinSummaryByExperimentCriteria criteria,
            AsyncCallback<TypedTableResultSet<ProteinSummary>> callback);

    /** @see IPhosphoNetXClientService#prepareExportProteinSummary(TableExportCriteria) */
    public void prepareExportProteinSummary(
            TableExportCriteria<TableModelRowWithObject<ProteinSummary>> exportCriteria,
            AsyncCallback<String> callback);
    
    /** @see IPhosphoNetXClientService#getProteinByExperiment(TechId, TechId) */
    public void getProteinByExperiment(TechId experimentID, TechId proteinReferenceID,
            AsyncCallback<ProteinByExperiment> callback);

    /** @see IPhosphoNetXClientService#listSequencesByProteinReference(ListProteinSequenceCriteria) */
    public void listSequencesByProteinReference(ListProteinSequenceCriteria criteria,
            AsyncCallback<TypedTableResultSet<ProteinSequence>> callback);

    /** @see IPhosphoNetXClientService#prepareExportProteinSequences(TableExportCriteria) */
    public void prepareExportProteinSequences(
            TableExportCriteria<TableModelRowWithObject<ProteinSequence>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see IPhosphoNetXClientService#listProteinsByExperimentAndReference(ListProteinByExperimentAndReferenceCriteria) */
    public void listProteinsByExperimentAndReference(
            ListProteinByExperimentAndReferenceCriteria criteria,
            AsyncCallback<TypedTableResultSet<DataSetProtein>> callback);

    /** @see IPhosphoNetXClientService#prepareExportDataSetProteins(TableExportCriteria) */
    public void prepareExportDataSetProteins(TableExportCriteria<TableModelRowWithObject<DataSetProtein>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see IPhosphoNetXClientService#listProteinRelatedSamplesByProtein(ListSampleAbundanceByProteinCriteria) */
    public void listProteinRelatedSamplesByProtein(ListSampleAbundanceByProteinCriteria criteria,
            AsyncCallback<TypedTableResultSet<ProteinRelatedSample>> callback);

    /** @see IPhosphoNetXClientService#prepareExportProteinRelatedSamples(TableExportCriteria) */
    public void prepareExportProteinRelatedSamples(
            TableExportCriteria<TableModelRowWithObject<ProteinRelatedSample>> exportCriteria,
            AsyncCallback<String> callback);

    /** @see IPhosphoNetXClientService#listRawDataSamples(IResultSetConfig) */
    public void listRawDataSamples(IResultSetConfig<String, TableModelRowWithObject<Sample>> criteria,
            AsyncCallback<TypedTableResultSet<Sample>> callback);
    
    /** @see IPhosphoNetXClientService#prepareExportRawDataSamples(TableExportCriteria) */
    public void prepareExportRawDataSamples(TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria,
            AsyncCallback<String> callback);
    
    /** @see IPhosphoNetXClientService#processRawData(String, long[], String) */
    public void processRawData(String dataSetProcessingKey, long[] rawDataSampleIDs, String dataSetType, AsyncCallback<Void> callback);
}