/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.WebClientConfigurationProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DefaultResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IOriginalDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.IResultSetManager;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.util.CacheManager;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.Constants;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Treatment;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses={AbstractClientService.class, PhosphoNetXClientService.class, CacheManager.class})
public class PhosphoNetXClientServiceTest extends AbstractFileSystemTestCase
{
    private static final String SESSION_TOKEN = "session-token";
    
    private static final class SimpleResultSetManager<K> implements IResultSetManager<K>
    {

        public <T> IResultSet<K, T> getResultSet(String sessionToken,
                IResultSetConfig<K, T> resultConfig, IOriginalDataProvider<T> dataProvider)
                throws UserFailureException
        {
            List<TableModelColumnHeader> headers = dataProvider.getHeaders();
            List<T> originalData = dataProvider.getOriginalData();
            List<GridRowModel<T>> rows = new ArrayList<GridRowModel<T>>();
            for (int i = 0; i < originalData.size(); i++)
            {
                T rowData = originalData.get(i);
                rows.add(new GridRowModel<T>(rowData, null));
            }
            return new DefaultResultSet<K, T>(resultConfig.getCacheConfig().tryGetResultSetKey(),
                    new GridRowModels<T>(rows, headers, null, null), rows.size());
        }

        public void removeResultSet(K resultSetKey) throws UserFailureException
        {
        }
        
    }
    
    private Mockery context;
    private IPhosphoNetXServer server;

    private PhosphoNetXClientService clientService;

    private IRequestContextProvider requestContextProvider;

    private HttpServletRequest request;

    private HttpSession httpSession;

    private File cacheFolder;

    @BeforeMethod
    public final void beforeMethod() throws Exception
    {
        context = new Mockery();
        server = context.mock(IPhosphoNetXServer.class);
        requestContextProvider = context.mock(IRequestContextProvider.class);
        request = context.mock(HttpServletRequest.class);
        httpSession = context.mock(HttpSession.class);
        clientService = new PhosphoNetXClientService();
        clientService.server = server;
        clientService.requestContextProvider = requestContextProvider;
        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(request));

                    allowing(request).getSession(false);
                    will(returnValue(httpSession));

                    allowing(httpSession).getAttribute("openbis-session-token");
                    will(returnValue(SESSION_TOKEN));
                    
                    allowing(httpSession).getAttribute(SessionConstants.OPENBIS_RESULT_SET_MANAGER);
                    will(returnValue(new SimpleResultSetManager<String>()));
                }
            });
        Properties properties = new Properties();
        cacheFolder = new File(workingDirectory, "cache");
        FileUtilities.deleteRecursively(cacheFolder);
        properties.setProperty("technologies", Constants.TECHNOLOGY_NAME);
        properties.setProperty(Constants.TECHNOLOGY_NAME + "." + CacheManager.CACHE_FOLDER_KEY,
                cacheFolder.getPath());
        clientService.webClientConfigurationProvider =
                new WebClientConfigurationProvider(properties);
        clientService.afterPropertiesSet();
    }

    @AfterMethod
    public final void afterMethod()
    {
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetAbundanceColumnDefinitionsForProteinByExperiment()
    {
        final TechId experimentID = new TechId(42L);
        final AbundanceColumnDefinition colDef = new AbundanceColumnDefinition();
        colDef.setSampleCode("S1");
        Treatment treatment = new Treatment();
        treatment.setType("type1");
        treatment.setTypeCode("code");
        colDef.setTreatments(Arrays.asList(treatment));
        context.checking(new Expectations()
            {
                {
                    one(server).getAbundanceColumnDefinitionsForProteinByExperiment(SESSION_TOKEN,
                            experimentID, null);
                    will(returnValue(Arrays.asList(colDef)));
                }
            });

        List<AbundanceColumnDefinition> colDefs =
                clientService.getAbundanceColumnDefinitionsForProteinByExperiment(experimentID,
                        null);
        colDefs =
                clientService.getAbundanceColumnDefinitionsForProteinByExperiment(experimentID,
                        null);
        assertEquals(colDef.getSampleCode(), colDefs.get(0).getSampleCode());
        assertEquals(treatment.getType(), colDefs.get(0).getTreatments().get(0).getType());
        assertEquals(1, colDefs.size());
        assertEquals(3, cacheFolder.listFiles().length);

        context.assertIsSatisfied();
    }
    
    @Test
    public void testListProteinsByExperiment()
    {
        final TechId experimentID1 = new TechId(42L);
        final TechId experimentID2 = new TechId(4711L);
        final double fdr1 = 0.125;
        final double fdr2 = 0.25;
        final AggregateFunction f1 = AggregateFunction.MAX;
        final AggregateFunction f2 = AggregateFunction.MIN;
        final String treatment1 = "t1";
        final String treatment2 = "t2";
        final ProteinInfo p1 = createProtein(1);
        final ProteinInfo p2 = createProtein(2);
        final ProteinInfo p3 = createProtein(3);
        final ProteinInfo p4 = createProtein(4);
        final ProteinInfo p5 = createProtein(5);
        final ProteinInfo p6 = createProtein(6);
        context.checking(new Expectations()
            {
                {
                    one(server).listProteinsByExperiment(SESSION_TOKEN, experimentID1, fdr1, f1,
                            treatment1, false);
                    will(returnValue(Arrays.asList(p1)));
                    one(server).listProteinsByExperiment(SESSION_TOKEN, experimentID2, fdr1, f1,
                            treatment1, false);
                    will(returnValue(Arrays.asList(p2)));
                    one(server).listProteinsByExperiment(SESSION_TOKEN, experimentID1, fdr2, f1,
                            treatment1, false);
                    will(returnValue(Arrays.asList(p3)));
                    one(server).listProteinsByExperiment(SESSION_TOKEN, experimentID1, fdr1, f2,
                            treatment1, false);
                    will(returnValue(Arrays.asList(p4)));
                    one(server).listProteinsByExperiment(SESSION_TOKEN, experimentID1, fdr1, f1,
                            treatment2, false);
                    will(returnValue(Arrays.asList(p5)));
                    one(server).listProteinsByExperiment(SESSION_TOKEN, experimentID1, fdr1, f1,
                            treatment1, true);
                    will(returnValue(Arrays.asList(p6)));
                    
                }
            });
        
        listAndCheckProteins(p1, experimentID1, fdr1, f1, treatment1, false);
        listAndCheckProteins(p2, experimentID2, fdr1, f1, treatment1, false);
        listAndCheckProteins(p3, experimentID1, fdr2, f1, treatment1, false);
        listAndCheckProteins(p4, experimentID1, fdr1, f2, treatment1, false);
        listAndCheckProteins(p5, experimentID1, fdr1, f1, treatment2, false);
        listAndCheckProteins(p6, experimentID1, fdr1, f1, treatment1, true);
        listAndCheckProteins(p1, experimentID1, fdr1, f1, treatment1, false);
        listAndCheckProteins(p2, experimentID2, fdr1, f1, treatment1, false);
        listAndCheckProteins(p3, experimentID1, fdr2, f1, treatment1, false);
        listAndCheckProteins(p4, experimentID1, fdr1, f2, treatment1, false);
        listAndCheckProteins(p5, experimentID1, fdr1, f1, treatment2, false);
        listAndCheckProteins(p6, experimentID1, fdr1, f1, treatment1, true);
        assertEquals(13, cacheFolder.listFiles().length);
        
        context.assertIsSatisfied();
    }
    
    private void listAndCheckProteins(ProteinInfo protein, TechId experimentId,
            double falseDiscoveryRate, AggregateFunction function, String treatmentTypeCode,
            boolean aggregateOnOriginal)
    {
        ListProteinByExperimentCriteria criteria = new ListProteinByExperimentCriteria();
        criteria.setCacheConfig(ResultSetFetchConfig.createFetchFromCache("key"));
        criteria.setExperimentID(experimentId);
        criteria.setFalseDiscoveryRate(falseDiscoveryRate);
        criteria.setAggregateFunction(function);
        criteria.setTreatmentTypeCode(treatmentTypeCode);
        criteria.setAggregateOriginal(aggregateOnOriginal);
        ResultSet<ProteinInfo> rs = clientService.listProteinsByExperiment(criteria);
        ProteinInfo actualProtein = rs.getList().get(0).getOriginalObject();
        assertEquals(protein.getId(), actualProtein.getId());
        assertEquals(protein.getDescription(), actualProtein.getDescription());
        assertEquals(1, rs.getTotalLength());
    }

    private ProteinInfo createProtein(long id)
    {
        ProteinInfo protein = new ProteinInfo();
        protein.setId(new TechId(id));
        protein.setDescription("Protein " + id);
        return protein;
    }

}
