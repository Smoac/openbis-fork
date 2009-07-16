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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentAndReferenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSequenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListSampleAbundanceByProteinCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IPhosphoNetXClientServiceAsync extends IClientServiceAsync
{
    /** @see IPhosphoNetXClientService#listProteinsByExperiment(ListProteinByExperimentCriteria) */
    public void listProteinsByExperiment(ListProteinByExperimentCriteria criteria,
            AsyncCallback<ResultSet<ProteinInfo>> callback);

    /** @see IPhosphoNetXClientService#prepareExportProteins(TableExportCriteria) */
    public void prepareExportProteins(TableExportCriteria<ProteinInfo> exportCriteria,
            AsyncCallback<String> callback);

    /** @see IPhosphoNetXClientService#getProteinByExperiment(TechId, TechId) */
    public void getProteinByExperiment(TechId experimentID, TechId proteinReferenceID,
            AsyncCallback<ProteinByExperiment> callback);

    /** @see IPhosphoNetXClientService#listSequencesByProteinReference(ListProteinSequenceCriteria) */
    public void listSequencesByProteinReference(ListProteinSequenceCriteria criteria,
            AsyncCallback<ResultSet<ProteinSequence>> callback);

    /** @see IPhosphoNetXClientService#prepareExportProteinSequences(TableExportCriteria) */
    public void prepareExportProteinSequences(TableExportCriteria<ProteinSequence> exportCriteria,
            AsyncCallback<String> callback);

    /** @see IPhosphoNetXClientService#listProteinsByExperimentAndReference(ListProteinByExperimentAndReferenceCriteria) */
    public void listProteinsByExperimentAndReference(
            ListProteinByExperimentAndReferenceCriteria criteria,
            AsyncCallback<ResultSet<DataSetProtein>> callback);
    
    /** @see IPhosphoNetXClientService#prepareExportDataSetProteins(TableExportCriteria) */
    public void prepareExportDataSetProteins(TableExportCriteria<DataSetProtein> exportCriteria,
            AsyncCallback<String> callback);
    
    /** @see IPhosphoNetXClientService#listSamplesWithAbundanceByProtein(ListSampleAbundanceByProteinCriteria) */
    public void listSamplesWithAbundanceByProtein(ListSampleAbundanceByProteinCriteria criteria,
            AsyncCallback<ResultSet<SampleWithPropertiesAndAbundance>> callback);

    /** @see IPhosphoNetXClientService#prepareExportSamplesWithAbundance(TableExportCriteria) */
    public void prepareExportSamplesWithAbundance(
            TableExportCriteria<SampleWithPropertiesAndAbundance> exportCriteria,
            AsyncCallback<String> callback);
}