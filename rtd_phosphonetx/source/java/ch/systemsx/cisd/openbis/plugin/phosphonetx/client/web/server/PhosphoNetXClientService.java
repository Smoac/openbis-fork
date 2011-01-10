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

import static ch.systemsx.cisd.common.utilities.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Resource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DataProviderAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.util.CacheManager;
import ch.systemsx.cisd.openbis.generic.shared.util.ICacheManager;
import ch.systemsx.cisd.openbis.generic.shared.util.Key;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.Constants;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentAndReferenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSequenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSummaryByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListSampleAbundanceByProteinCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.CacheData;
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
        IPhosphoNetXClientService, InitializingBean
{
    private static final String CACHE_VERSION = "1"; // Sprint S97

    @Resource(name = ResourceNames.PHOSPHONETX_PLUGIN_SERVER)
    @Private
    IPhosphoNetXServer server;

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

    public void afterPropertiesSet() throws Exception
    {
        WebClientConfiguration webClientConfiguration = getWebClientConfiguration();
        IFreeSpaceProvider freeSpaceProvider = new SimpleFreeSpaceProvider();
        final ICacheManager cacheManager =
                new CacheManager(webClientConfiguration, Constants.TECHNOLOGY_NAME,
                        SYSTEM_TIME_PROVIDER, freeSpaceProvider, CACHE_VERSION);
        ProxyFactory proxyFactory = new ProxyFactory(server);
        proxyFactory.addInterface(IPhosphoNetXServer.class);
        AnnotationMatchingPointcut pointcut =
                AnnotationMatchingPointcut.forMethodAnnotation(CacheData.class);
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(pointcut, new MethodInterceptor()
            {
                public Object invoke(MethodInvocation methodInvocation) throws Throwable
                {
                    // assuming first argument is sessionToken which shouldn't be a part of the key
                    Object[] arguments = methodInvocation.getArguments();
                    Object[] keyArguments = new Serializable[arguments.length];
                    Method method = methodInvocation.getMethod();
                    keyArguments[0] = method.getName();
                    for (int i = 1; i < keyArguments.length; i++)
                    {
                        keyArguments[i] = arguments[i];
                    }
                    Key key = new Key(keyArguments);
                    Object data = cacheManager.tryToGetData(key);
                    if (data == null)
                    {
                        Object serverObject = methodInvocation.getThis();
                        data = method.invoke(serverObject, arguments);
                        cacheManager.storeData(key, data);
                    }
                    return data;
                }
            }));
        server = (IPhosphoNetXServer) proxyFactory.getProxy();
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
            operationLog.info(stopWatch.getTime()
                    + " msec for getAbundanceColumnDefinitionsForProteinByExperiment");
        }
    }

    public ResultSet<ProteinInfo> listProteinsByExperiment(ListProteinByExperimentCriteria criteria)
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
    {
        return prepareExportEntities(exportCriteria);
    }

    public ResultSet<ProteinSummary> listProteinSummariesByExperiment(
            ListProteinSummaryByExperimentCriteria criteria)
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
    {
        return prepareExportEntities(exportCriteria);
    }

    public ProteinByExperiment getProteinByExperiment(TechId experimentID, TechId proteinReferenceID)
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
    {
        final String sessionToken = getSessionToken();
        return listEntities(criteria, new ListProteinSequenceDataProvider(server, sessionToken,
                criteria.getProteinReferenceID()));
    }

    public String prepareExportProteinSequences(TableExportCriteria<ProteinSequence> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    public ResultSet<DataSetProtein> listProteinsByExperimentAndReference(
            ListProteinByExperimentAndReferenceCriteria criteria)
    {
        final String sessionToken = getSessionToken();
        return listEntities(criteria, new ListDataSetProteinDataProvider(server, sessionToken,
                criteria.getExperimentID(), criteria.getProteinReferenceID()));
    }

    public String prepareExportDataSetProteins(TableExportCriteria<DataSetProtein> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    public ResultSet<SampleWithPropertiesAndAbundance> listSamplesWithAbundanceByProtein(
            ListSampleAbundanceByProteinCriteria criteria)
    {
        final String sessionToken = getSessionToken();
        return listEntities(criteria, new ListSampleAbundanceDataProvider(server, sessionToken,
                criteria.getExperimentID(), criteria.getProteinReferenceID()));
    }

    public String prepareExportSamplesWithAbundance(
            TableExportCriteria<SampleWithPropertiesAndAbundance> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    public TypedTableResultSet<Sample> listRawDataSamples(
            IResultSetConfig<String, TableModelRowWithObject<Sample>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        final String sessionToken = getSessionToken();
        RawDataSampleProvider rawDataSampleProvider =
                new RawDataSampleProvider(proteomicsDataService, sessionToken);
        DataProviderAdapter<Sample> dataProvider =
                new DataProviderAdapter<Sample>(rawDataSampleProvider);
        ResultSet<TableModelRowWithObject<Sample>> resultSet = listEntities(criteria, dataProvider);
        return new TypedTableResultSet<Sample>(resultSet);
    }

    public String prepareExportRawDataSamples(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    @SuppressWarnings("deprecation")
    public void processRawData(String dataSetProcessingKey, long[] rawDataSampleIDs,
            String dataSetType)
    {
        proteomicsDataService.processRawData(getSessionToken(), dataSetProcessingKey,
                rawDataSampleIDs, dataSetType);
    }

}
