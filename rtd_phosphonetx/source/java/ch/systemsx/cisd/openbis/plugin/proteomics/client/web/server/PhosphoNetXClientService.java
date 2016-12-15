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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server;

import static ch.systemsx.cisd.common.utilities.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
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
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DataProviderAdapter;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.CacheManager;
import ch.systemsx.cisd.openbis.generic.shared.util.ICacheManager;
import ch.systemsx.cisd.openbis.generic.shared.util.Key;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.Constants;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientService;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinByExperimentAndReferenceCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinSequenceCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinSummaryByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListSampleAbundanceByProteinCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset.BiologicalSampleProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset.DataSetProteinProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset.ParentlessMsInjectionSampleProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset.ProteinProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset.ProteinRelatedSampleProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset.ProteinSequenceProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset.ProteinSummaryProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.CacheData;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSummary;

/**
 * @author Franz-Josef Elmer
 */
@Component(value = ResourceNames.PROTEOMICS_PLUGIN_SERVICE)
public class PhosphoNetXClientService extends AbstractClientService implements
        IPhosphoNetXClientService, InitializingBean
{
    private static final String CACHE_VERSION = Integer.toString(ServiceVersionHolder.VERSION);

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer commonServer;

    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    @Resource(name = ResourceNames.PROTEOMICS_PLUGIN_SERVER)
    @Private
    IPhosphoNetXServer server;

    @Resource(name = ResourceNames.PROTEOMICS_RAW_DATA_SERVICE_WEB)
    private IProteomicsDataServiceInternal proteomicsDataService;

    @Private
    ITimeProvider timeProvider = SYSTEM_TIME_PROVIDER;

    public PhosphoNetXClientService()
    {
        super();
    }

    public PhosphoNetXClientService(IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        WebClientConfiguration webClientConfiguration = getWebClientConfiguration();
        IFreeSpaceProvider freeSpaceProvider = new SimpleFreeSpaceProvider();
        final ICacheManager cacheManager =
                new CacheManager(webClientConfiguration, Constants.TECHNOLOGY_NAME,
                        timeProvider, freeSpaceProvider, CACHE_VERSION);
        ProxyFactory proxyFactory = new ProxyFactory(server);
        proxyFactory.addInterface(IPhosphoNetXServer.class);
        AnnotationMatchingPointcut pointcut =
                AnnotationMatchingPointcut.forMethodAnnotation(CacheData.class);
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(pointcut, new MethodInterceptor()
            {
                @Override
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

    @Override
    public TypedTableResultSet<Sample> listParentlessMsInjectionSamples(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        String sessionToken = getSessionToken();
        return listEntities(new ParentlessMsInjectionSampleProvider(commonServer, sessionToken),
                criteria);
    }

    @Override
    public TypedTableResultSet<Sample> listBiologicalSamples(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> criteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        String sessionToken = getSessionToken();
        return listEntities(new BiologicalSampleProvider(commonServer, sessionToken), criteria);
    }

    @Override
    public void linkSamples(Sample parentSample, List<Sample> childSamples)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        String sessionToken = getSessionToken();
        linkSamples(sessionToken, parentSample.getIdentifier(), childSamples);
    }

    @Override
    public void createAndLinkSamples(NewSample newBiologicalSample, List<Sample> msInjectionSamples)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        String sessionToken = getSessionToken();
        List<NewAttachment> noAttachments = Collections.<NewAttachment> emptyList();
        genericServer.registerSample(sessionToken, newBiologicalSample, noAttachments);
        linkSamples(sessionToken, newBiologicalSample.getIdentifier(), msInjectionSamples);
    }

    private void linkSamples(String sessionToken, String identifier, List<Sample> childSamples)
    {
        String[] parents = new String[]
        { identifier };
        for (Sample childSample : childSamples)
        {
            SampleIdentifier childSampleIdentifier =
                    SampleIdentifierFactory.parse(childSample.getIdentifier());
            SampleUpdatesDTO update =
                    new SampleUpdatesDTO(new TechId(childSample),
                            Collections.<IEntityProperty> emptyList(), null, null,
                            Collections.<NewAttachment> emptyList(),
                            childSample.getVersion(), childSampleIdentifier, null, parents);
            update.setUpdateExperimentLink(false);
            genericServer.updateSample(sessionToken, update);
        }
    }

    @Override
    public Vocabulary getTreatmentTypeVocabulary()
    {
        final String sessionToken = getSessionToken();
        return server.getTreatmentTypeVocabulary(sessionToken);
    }

    @Override
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

    @Override
    public TypedTableResultSet<ProteinInfo> listProteinsByExperiment(
            ListProteinByExperimentCriteria criteria)
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
            return listEntities(new ProteinProvider(server, sessionToken, experimentID, fdr,
                    aggregateFunction, treatmentTypeCode, aggregateOnOriginal), criteria);
        } finally
        {
            operationLog.info(stopWatch.getTime() + " msec for listProteinsByExperiment");
        }
    }

    @Override
    public String prepareExportProteins(TableExportCriteria<TableModelRowWithObject<ProteinInfo>> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    @Override
    public TypedTableResultSet<ProteinSummary> listProteinSummariesByExperiment(
            ListProteinSummaryByExperimentCriteria criteria)
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try
        {
            final String sessionToken = getSessionToken();
            return listEntities(
                    new ProteinSummaryProvider(server, sessionToken, criteria.getExperimentID()),
                    criteria);
        } finally
        {
            operationLog.info(stopWatch.getTime() + " msec for listProteinSummariesByExperiment");
        }
    }

    @Override
    public String prepareExportProteinSummary(
            TableExportCriteria<TableModelRowWithObject<ProteinSummary>> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    @Override
    public ProteinByExperiment getProteinByExperiment(TechId experimentID, TechId proteinReferenceID)
    {
        final String sessionToken = getSessionToken();
        return server.getProteinByExperiment(sessionToken, experimentID, proteinReferenceID);
    }

    @Override
    public TypedTableResultSet<ProteinSequence> listSequencesByProteinReference(
            ListProteinSequenceCriteria criteria)
    {
        final String sessionToken = getSessionToken();
        return listEntities(
                new ProteinSequenceProvider(server, sessionToken, criteria.getExperimentID(), criteria.getProteinReferenceID()),
                criteria);
    }

    @Override
    public String prepareExportProteinSequences(
            TableExportCriteria<TableModelRowWithObject<ProteinSequence>> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    @Override
    public TypedTableResultSet<DataSetProtein> listProteinsByExperimentAndReference(
            ListProteinByExperimentAndReferenceCriteria criteria)
    {
        final String sessionToken = getSessionToken();
        return listEntities(
                new DataSetProteinProvider(server, sessionToken, criteria.getExperimentID(),
                        criteria.getProteinReferenceID()), criteria);
    }

    @Override
    public String prepareExportDataSetProteins(
            TableExportCriteria<TableModelRowWithObject<DataSetProtein>> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    @Override
    public TypedTableResultSet<ProteinRelatedSample> listProteinRelatedSamplesByProtein(
            ListSampleAbundanceByProteinCriteria criteria)
    {
        final String sessionToken = getSessionToken();
        ProteinRelatedSampleProvider provider =
                new ProteinRelatedSampleProvider(server, sessionToken, criteria.getExperimentID(),
                        criteria.getProteinReferenceID());
        return listEntities(provider, criteria);
    }

    @Override
    public String prepareExportProteinRelatedSamples(
            TableExportCriteria<TableModelRowWithObject<ProteinRelatedSample>> exportCriteria)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException
    {
        return prepareExportEntities(exportCriteria);
    }

    @Override
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

    @Override
    public String prepareExportRawDataSamples(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria)
    {
        return prepareExportEntities(exportCriteria);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void processRawData(String dataSetProcessingKey, long[] rawDataSampleIDs,
            String dataSetType)
    {
        proteomicsDataService.processRawData(getSessionToken(), dataSetProcessingKey,
                rawDataSampleIDs, dataSetType);
    }

}
