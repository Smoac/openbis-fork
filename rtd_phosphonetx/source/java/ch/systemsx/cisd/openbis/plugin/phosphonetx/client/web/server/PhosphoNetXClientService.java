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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentAndReferenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSequenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSummaryByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListSampleAbundanceByProteinCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;

/**
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.PHOSPHONETX_PLUGIN_SERVICE)
public class PhosphoNetXClientService extends AbstractClientService implements
        IPhosphoNetXClientService
{
    @Resource(name = ResourceNames.PHOSPHONETX_PLUGIN_SERVER)
    private IPhosphoNetXServer server;
    
    @Resource(name = ResourceNames.PHOSPHONETX_RAW_DATA_SERVICE_WEB)
    private IProteomicsDataServiceInternal proteomicsDataService;

    public PhosphoNetXClientService()
    {
        super();
    }

    public PhosphoNetXClientService(IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
    }

    @Override
    protected IServer getServer()
    {
        return server;
    }

    @Override
    protected String getVersion()
    {
        return BuildAndEnvironmentInfo.INSTANCE.getFullVersion();
    }

    public Vocabulary getTreatmentTypeVocabulary()
    {
        final String sessionToken = getSessionToken();
        return server.getTreatmentTypeVocabulary(sessionToken);
    }

    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            TechId experimentID, String treatmentTypeOrNull)
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try
        {
            final String sessionToken = getSessionToken();
            return server.getAbundanceColumnDefinitionsForProteinByExperiment(sessionToken,
                    experimentID, treatmentTypeOrNull);
        } finally
        {
            operationLog.info(stopWatch.getTime() + " msec for getAbundanceColumnDefinitionsForProteinByExperiment");
        }
    }

    public ResultSet<ProteinInfo> listProteinsByExperiment(ListProteinByExperimentCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try
        {
            final String sessionToken = getSessionToken();
            TechId experimentID = criteria.getExperimentID();
            double fdr = criteria.getFalseDiscoveryRate();
            AggregateFunction aggregateFunction = criteria.getAggregateFunction();
            String treatmentTypeCode = criteria.getTreatmentTypeCode();
            boolean aggregateOnOriginal = criteria.isAggregateOriginal();
            return listEntities(criteria, new ListProteinOriginalDataProvider(server, sessionToken,
                    experimentID, fdr, aggregateFunction, treatmentTypeCode, aggregateOnOriginal));
        } finally
        {
            operationLog.info(stopWatch.getTime() + " msec for listProteinsByExperiment");
        }
    }

    public String prepareExportProteins(TableExportCriteria<ProteinInfo> exportCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }

    public ResultSet<ProteinSummary> listProteinSummariesByExperiment(
            ListProteinSummaryByExperimentCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try
        {
            final String sessionToken = getSessionToken();
            return listEntities(criteria, new ListProteinSummaryProvider(server, sessionToken,
                    criteria.getExperimentID()));
        } finally
        {
            operationLog.info(stopWatch.getTime() + " msec for listProteinSummariesByExperiment");
        }
    }
    
    public String prepareExportProteinSummary(TableExportCriteria<ProteinSummary> exportCriteria)
    throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }
    
    public ProteinByExperiment getProteinByExperiment(TechId experimentID, TechId proteinReferenceID)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        try
        {
            return server.getProteinByExperiment(sessionToken, experimentID, proteinReferenceID);
        } catch (final UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }
    }

    public ResultSet<ProteinSequence> listSequencesByProteinReference(
            ListProteinSequenceCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(criteria, new ListProteinSequenceDataProvider(server, sessionToken,
                criteria.getProteinReferenceID()));
    }

    public String prepareExportProteinSequences(TableExportCriteria<ProteinSequence> exportCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }

    public ResultSet<DataSetProtein> listProteinsByExperimentAndReference(
            ListProteinByExperimentAndReferenceCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(criteria, new ListDataSetProteinDataProvider(server, sessionToken,
                criteria.getExperimentID(), criteria.getProteinReferenceID()));
    }

    public String prepareExportDataSetProteins(TableExportCriteria<DataSetProtein> exportCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }

    public ResultSet<SampleWithPropertiesAndAbundance> listSamplesWithAbundanceByProtein(
            ListSampleAbundanceByProteinCriteria criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        return listEntities(criteria, new ListSampleAbundanceDataProvider(server, sessionToken,
                criteria.getExperimentID(), criteria.getProteinReferenceID()));
    }

    public String prepareExportSamplesWithAbundance(
            TableExportCriteria<SampleWithPropertiesAndAbundance> exportCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }

    public GenericTableResultSet listRawDataSamples(IResultSetConfig<String, GenericTableRow> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        RawDataSampleProvider rawDataSampleProvider = new RawDataSampleProvider(proteomicsDataService, sessionToken);
        ResultSet<GenericTableRow> resultSet = listEntities(criteria, rawDataSampleProvider);
        return new GenericTableResultSet(resultSet, rawDataSampleProvider.getHeaders());
    }

    public String prepareExportRawDataSamples(TableExportCriteria<GenericTableRow> exportCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }
    
    public void processRawData(String dataSetProcessingKey, long[] rawDataSampleIDs, String dataSetType)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        proteomicsDataService.processRawData(getSessionToken(), dataSetProcessingKey, rawDataSampleIDs, dataSetType);
    }
    
}
