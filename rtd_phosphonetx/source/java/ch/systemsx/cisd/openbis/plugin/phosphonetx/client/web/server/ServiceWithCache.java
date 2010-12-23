/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientService;
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
class ServiceWithCache implements IPhosphoNetXClientService
{
    private final IPhosphoNetXClientService service;

    ServiceWithCache(IPhosphoNetXClientService service)
    {
        this.service = service;
    }
        
    public ApplicationInfo getApplicationInfo()
    {
        return service.getApplicationInfo();
    }

    public SessionContext tryToGetCurrentSessionContext()
    {
        return service.tryToGetCurrentSessionContext();
    }

    public SessionContext tryToLogin(String userID, String password) throws UserFailureException
    {
        return service.tryToLogin(userID, password);
    }

    public void setBaseURL(String baseURL)
    {
        service.setBaseURL(baseURL);
    }

    public void updateDisplaySettings(DisplaySettings displaySettings)
    {
        service.updateDisplaySettings(displaySettings);
    }

    public DisplaySettings resetDisplaySettings()
    {
        return service.resetDisplaySettings();
    }

    public void changeUserHomeGroup(TechId groupIdOrNull)
    {
        service.changeUserHomeGroup(groupIdOrNull);
    }

    public void logout(DisplaySettings displaySettings)
    {
        service.logout(displaySettings);
    }

    public Vocabulary getTreatmentTypeVocabulary() throws UserFailureException
    {
        return service.getTreatmentTypeVocabulary();
    }

    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            TechId experimentID, String treatmentTypeOrNull) throws UserFailureException
    {
        return service.getAbundanceColumnDefinitionsForProteinByExperiment(experimentID,
                treatmentTypeOrNull);
    }

    public ResultSet<ProteinInfo> listProteinsByExperiment(ListProteinByExperimentCriteria criteria)
            throws UserFailureException
    {
        return service.listProteinsByExperiment(criteria);
    }

    public String prepareExportProteins(TableExportCriteria<ProteinInfo> exportCriteria)
            throws UserFailureException
    {
        return service.prepareExportProteins(exportCriteria);
    }

    public ResultSet<ProteinSummary> listProteinSummariesByExperiment(
            ListProteinSummaryByExperimentCriteria criteria) throws UserFailureException
    {
        return service.listProteinSummariesByExperiment(criteria);
    }

    public String prepareExportProteinSummary(TableExportCriteria<ProteinSummary> exportCriteria)
            throws UserFailureException
    {
        return service.prepareExportProteinSummary(exportCriteria);
    }

    public ProteinByExperiment getProteinByExperiment(TechId experimentID, TechId proteinReferenceID)
            throws UserFailureException
    {
        return service.getProteinByExperiment(experimentID, proteinReferenceID);
    }

    public ResultSet<ProteinSequence> listSequencesByProteinReference(
            ListProteinSequenceCriteria criteria) throws UserFailureException
    {
        return service.listSequencesByProteinReference(criteria);
    }

    public String prepareExportProteinSequences(TableExportCriteria<ProteinSequence> exportCriteria)
            throws UserFailureException
    {
        return service.prepareExportProteinSequences(exportCriteria);
    }

    public ResultSet<DataSetProtein> listProteinsByExperimentAndReference(
            ListProteinByExperimentAndReferenceCriteria criteria) throws UserFailureException
    {
        return service.listProteinsByExperimentAndReference(criteria);
    }

    public String prepareExportDataSetProteins(TableExportCriteria<DataSetProtein> exportCriteria)
            throws UserFailureException
    {
        return service.prepareExportDataSetProteins(exportCriteria);
    }

    public ResultSet<SampleWithPropertiesAndAbundance> listSamplesWithAbundanceByProtein(
            ListSampleAbundanceByProteinCriteria criteria) throws UserFailureException
    {
        return service.listSamplesWithAbundanceByProtein(criteria);
    }

    public String prepareExportSamplesWithAbundance(
            TableExportCriteria<SampleWithPropertiesAndAbundance> exportCriteria)
            throws UserFailureException
    {
        return service.prepareExportSamplesWithAbundance(exportCriteria);
    }

    public TypedTableResultSet<Sample> listRawDataSamples(
            IResultSetConfig<String, TableModelRowWithObject<Sample>> criteria)
            throws UserFailureException
    {
        return service.listRawDataSamples(criteria);
    }

    public String prepareExportRawDataSamples(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria)
            throws UserFailureException
    {
        return service.prepareExportRawDataSamples(exportCriteria);
    }

    public void processRawData(String dataSetProcessingKey, long[] rawDataSampleIDs,
            String dataSetType) throws UserFailureException
    {
        service.processRawData(dataSetProcessingKey, rawDataSampleIDs, dataSetType);
    }

}
