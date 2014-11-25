/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.test.annotation.ExpectedException;
import org.testng.AssertJUnit;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collection.SimpleComparator;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ISingleDataSetPathInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetPathInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * @author Jakub Straszewski
 */
public abstract class ByPoliceAbstractTest extends AssertJUnit
{
    protected Mockery context;

    protected ExecutionContext ctx;

    private IDataSetPathInfoProvider pathProviderMock;

    private ISingleDataSetPathInfoProvider singleDsProviderMock;

    private AtomicInteger counter;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        ctx = new ExecutionContext();
        context = new Mockery();
        counter = new AtomicInteger();
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        pathProviderMock = ServiceProviderTestWrapper.mock(context, IDataSetPathInfoProvider.class);
        singleDsProviderMock = ServiceProviderTestWrapper.mock(context, ISingleDataSetPathInfoProvider.class);

        ServiceProviderTestWrapper.addMock(context, IDataSetPathInfoProvider.class,
                pathProviderMock);

    }

    /**
     * Expect that the data set with given code will have no size, and operation to retrieve it will return given size.
     */
    protected void prepareDataSetWithNoSize(final String code, final Long sizeInBytes)
    {
        context.checking(new Expectations()
            {
                {
                    one(pathProviderMock).tryGetSingleDataSetPathInfoProvider(code);
                    if (sizeInBytes != null)
                    {
                        will(returnValue(singleDsProviderMock));

                        one(singleDsProviderMock).getRootPathInfo();
                        will(returnValue(new DataSetPathInfo()
                            {
                                {
                                    setSizeInBytes(42);
                                }
                            }));
                    }
                    else
                    {
                        will(returnValue(null));
                    }
                }
            });
    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        context.assertIsSatisfied();
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    protected class ExecutionContext
    {
        public AbstractExternalData createDataset(String projectCode, String experimentCode, String datasetType, String dsCode, Long size)
        {
            return createDataset("___space", projectCode, experimentCode, datasetType, dsCode, size);
        }

        /**
         * If datasetCode is null then it gets assigned unique code containing the word "generated"
         */
        public AbstractExternalData createDataset(String spaceCode, String projectCode, String experimentCode, String datasetType, String dsCode,
                Long size)
        {
            return createDataset(spaceCode, projectCode, experimentCode, datasetType, null, dsCode, size);
        }

        /**
         * If datasetCode is null then it gets assigned unique code containing the word "generated"
         */
        public AbstractExternalData createDataset(String spaceCode, String projectCode, String experimentCode, String datasetType, String sampleCode,
                String dsCode,
                Long size)
        {
            Space space = new Space();
            space.setCode(spaceCode);
            space.setIdentifier("/" + space.getCode());

            Project project = new Project();
            project.setCode(projectCode);
            project.setIdentifier("/" + spaceCode + "/" + projectCode);
            project.setSpace(space);

            Experiment exp = new Experiment();
            exp.setProject(project);
            exp.setCode(experimentCode);
            exp.setIdentifier(project.getIdentifier() + "/" + experimentCode);

            Sample sample = null;
            if (sampleCode != null)
            {
                sample = new Sample();
                sample.setCode(sampleCode);
                sample.setIdentifier(space.getIdentifier() + "/" + sample.getCode());
                sample.setExperiment(exp);
                sample.setSpace(space);
            }

            DataSetType dataSetType = new DataSetType();
            dataSetType.setCode(datasetType);

            PhysicalDataSet ds = new PhysicalDataSet();
            if (dsCode != null)
            {
                ds.setCode(dsCode);
            }
            else
            {
                ds.setCode("generated-" + counter.incrementAndGet());
            }
            ds.setExperiment(exp);
            ds.setSample(sample);
            ds.setSize(size);
            ds.setDataSetType(dataSetType);

            return ds;
        }
    }

    protected List<String> extractCodes(List<AbstractExternalData> dataSets)
    {
        List<String> codes = new ArrayList<String>();
        for (AbstractExternalData dataSet : dataSets)
        {
            codes.add(dataSet.getCode());
        }
        Collections.sort(codes);
        return codes;
    }

    // Some general tests for all policies

    protected abstract IAutoArchiverPolicy getPolicy(int min, int max);

    /**
     * Creates the policy with given min and max value. To improve the robusntess of the test this method shuffles the incoming dataset and sorts the
     * result.
     */
    protected List<AbstractExternalData> filter(int min, int max, ArrayList<AbstractExternalData> dataSets)
    {
        return filter(min, max, dataSets, true);
    }

    /**
     * Creates the policy with given min and max value. The input to the filtering is not shuffled.
     */
    protected List<AbstractExternalData> filterWithoutShuffling(int min, int max, ArrayList<AbstractExternalData> dataSets)
    {
        return filter(min, max, dataSets, false);
    }

    private List<AbstractExternalData> filter(int min, int max, ArrayList<AbstractExternalData> dataSets, boolean shuffle)
    {
        IAutoArchiverPolicy policy = getPolicy(min, max);

        if (shuffle)
        {
            Collections.shuffle(dataSets, new Random(445332075L));
        }

        List<AbstractExternalData> result = new ArrayList<AbstractExternalData>(policy.filter(dataSets));

        Collections.sort(result, new SimpleComparator<AbstractExternalData, String>()
            {
                @Override
                public String evaluate(AbstractExternalData item)
                {
                    return item.getCode();
                }
            });

        return result;
    }

    @Test
    public void testNothingFoundForNoInputAndNoConfig()
    {
        IAutoArchiverPolicy policy = getPolicy(40, 100);

        List<AbstractExternalData> filtered = policy.filter(new ArrayList<AbstractExternalData>());

        assertEquals(0, filtered.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testDatasetSizeIsPatched()
    {
        IAutoArchiverPolicy policy = getPolicy(40, 100);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "234", "t1", "dsNoSizeButWillGetIt", null));

        prepareDataSetWithNoSize("dsNoSizeButWillGetIt", 42L);

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals(1, filtered.size());
        assertEquals(42, filtered.get(0).getSize().longValue());

        context.assertIsSatisfied();
    }

    @Test
    @ExpectedException(value = EnvironmentFailureException.class)
    public void testDatasetSizeIsNotPatched()
    {
        IAutoArchiverPolicy policy = getPolicy(40, 100);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "234", "t1", "dsNoSize", null));

        prepareDataSetWithNoSize("dsNoSize", null);

        try
        {
            policy.filter(dataSets);
            fail("Expected failure of filtering in case of unknonwn data set size");
        } catch (EnvironmentFailureException ef)
        {
            // ok
        }

        context.assertIsSatisfied();
    }

    protected void assertAllDataSetsAreNotGenerated(List<AbstractExternalData> filtered)
    {
        if (extractCodes(filtered).toString().contains("generated"))
        {
            fail(getErrorMessage(filtered));
        }
    }

    protected String getErrorMessage(List<AbstractExternalData> filtered)
    {
        return "Unexpected data sets in result of filtering data sets." + extractCodes(filtered).toString();
    }

    protected void assertTotalDataSetsSize(long expectedSize, List<AbstractExternalData> dataSets)
    {
        long actualSize = 0;
        for (AbstractExternalData data : dataSets)
        {
            actualSize += data.getSize();
        }
        assertEquals(getErrorMessage(dataSets), expectedSize, actualSize);
    }

}
