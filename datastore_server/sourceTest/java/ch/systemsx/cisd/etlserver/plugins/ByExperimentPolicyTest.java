package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.AssertJUnit;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.properties.ExtendedProperties;
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

public class ByExperimentPolicyTest extends AssertJUnit
{
    private Mockery context;

    private ExecutionContext ctx;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        ctx = new ExecutionContext();
        context = new Mockery();
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        final IDataSetPathInfoProvider pathProviderMock = ServiceProviderTestWrapper.mock(context, IDataSetPathInfoProvider.class);
        final ISingleDataSetPathInfoProvider singleDsProviderMock = ServiceProviderTestWrapper.mock(context, ISingleDataSetPathInfoProvider.class);

        ServiceProviderTestWrapper.addMock(context, IDataSetPathInfoProvider.class,
                pathProviderMock);

        context.checking(new Expectations()
            {
                {
                    allowing(pathProviderMock).tryGetSingleDataSetPathInfoProvider("dsNoSize");
                    will(returnValue(singleDsProviderMock));

                    allowing(singleDsProviderMock).getRootPathInfo();
                    will(returnValue(new DataSetPathInfo()
                        {
                            {
                                setSizeInBytes(42);
                            }
                        }));
                }
            });

    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        context.assertIsSatisfied();
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testNothingFoundForNoInputAndNoConfig()
    {
        ExtendedProperties properties = new ExtendedProperties();
        ByExpermientPolicy policy = new ByExpermientPolicy(properties);

        List<AbstractExternalData> filtered = policy.filter(new ArrayList<AbstractExternalData>());

        assertEquals(0, filtered.size());

        context.assertIsSatisfied();
    }

    private class ExecutionContext
    {
        private Map<String, Project> projects = new HashMap<String, Project>();

        private Map<String, Experiment> experiments = new HashMap<String, Experiment>();

        private Map<String, DataSetType> datasets = new HashMap<String, DataSetType>();

        public AbstractExternalData createDataset(String projectCode, String experimentCode, String datasetType, String dsCode, Long size)
        {
            Project project = projects.get(projectCode);
            if (project == null)
            {
                project = new Project();
                project.setIdentifier(projectCode);

                projects.put(projectCode, project);
            }

            Experiment exp = experiments.get(experimentCode);
            if (exp == null)
            {
                exp = new Experiment();
                exp.setIdentifier(project.getIdentifier() + "/" + experimentCode);
                experiments.put(experimentCode, exp);
            }

            DataSetType dataSetType = datasets.get(datasetType);
            if (dataSetType == null)
            {
                dataSetType = new DataSetType();
                dataSetType.setCode(datasetType);

                datasets.put(datasetType, dataSetType);
            }

            PhysicalDataSet ds = new PhysicalDataSet();
            ds.setCode(dsCode);
            ds.setExperiment(exp);
            ds.setSize(size);
            ds.setDataSetType(dataSetType);

            return ds;
        }
    }

    @Test
    public void testDatasetSizeIsPatched()
    {
        ExtendedProperties properties = new ExtendedProperties();
        ByExpermientPolicy policy = new ByExpermientPolicy(properties);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "234", "t1", "dsNoSize", null));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals(1, filtered.size());
        assertEquals(42, filtered.get(0).getSize().longValue());

        context.assertIsSatisfied();
    }

    @Test
    public void testEverythingFromProjectIsReturnedIfDatasetsAreSmall()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "14");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "100");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 2L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt2", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e3", "dt3", "ds3", 13L));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals("[ds1, ds2, ds3]", extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "15");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "25");
        
        ByExpermientPolicy policy = new ByExpermientPolicy(props);
        
        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1-1", "e112", "t2", "ds1", 10L));
        dataSets.add(ctx.createDataset("p2-1", "e122", "t1", "ds2", 10L));
        dataSets.add(ctx.createDataset("p2-1", "e121", "t2", "ds3", 10L));
        
        List<AbstractExternalData> filtered = policy.filter(dataSets);
        
        assertEquals("[ds2, ds3]", extractCodes(filtered).toString());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testSubsetIsReturnedIfDatasetsAreTooBig()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "6");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "10");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds3", 9L));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals("[ds1]", extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testTooSmallSetsAreNotArchived()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "500");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "1000");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds3", 9L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds4", 9L));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals("[]", extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testTooBigDataSetsAreArchivedOnTheirOwn()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "500");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "1000");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7000L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 8000L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds3", 9000L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds4", 9000L));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals("[ds1]", extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testSameDatatypeIsGroupedSmalls()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "10");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "1000");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt2", "ds3", 9L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds4", 9L));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals("[ds1, ds2]", extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testSameDatatypeIsGroupedBigs()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "6");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "1000");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();

        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 17L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds2", 18L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt2", "ds3", 19L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt1", "ds4", 19L));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals("[ds1, ds2]", extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }
    
    @Test
    public void testSameExperimentIsGroupedSmalls()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "10");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "1000");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 7L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt2", "ds2", 8L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt3", "ds3", 9L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt4", "ds4", 9L));
        dataSets.add(ctx.createDataset("p2", "e3", "dt1", "ds5", 9L));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals("[ds1, ds2, ds3]", extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testSameExperimentIsGroupedBigs()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "10");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "1000");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();

        dataSets.add(ctx.createDataset("p1", "e1", "dt1", "ds1", 17L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt2", "ds2", 18L));
        dataSets.add(ctx.createDataset("p1", "e1", "dt3", "ds3", 19L));
        dataSets.add(ctx.createDataset("p1", "e2", "dt4", "ds4", 19L));
        dataSets.add(ctx.createDataset("p2", "e3", "dt1", "ds5", 19L));

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals("[ds3]", extractCodes(filtered).toString());

        context.assertIsSatisfied();
    }

    @Test
    public void testDatasetTypeIsSplitIfTooMany()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty(BaseGroupingPolicy.MINIMAL_ARCHIVE_SIZE, "20");
        props.setProperty(BaseGroupingPolicy.MAXIMAL_ARCHIVE_SIZE, "45");

        ByExpermientPolicy policy = new ByExpermientPolicy(props);

        ArrayList<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();

        Sample s1 = new Sample();
        s1.setIdentifier("s1");

        Sample s2 = new Sample();
        s1.setIdentifier("s2");

        Sample s3 = new Sample();
        s1.setIdentifier("s3");

        Sample s4 = new Sample();
        s1.setIdentifier("s4");

        Sample s5 = new Sample();
        s1.setIdentifier("s5"); // this one will be sorted out

        AbstractExternalData ds;
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds1", 10L));
        ds.setSample(s5);
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds2", 10L));
        ds.setSample(s4);
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds3", 10L));
        ds.setSample(s3);
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds4", 10L));
        ds.setSample(s2);
        dataSets.add(ds = ctx.createDataset("p1", "e1", "dt1", "ds5", 10L));
        ds.setSample(s1);

        List<AbstractExternalData> filtered = policy.filter(dataSets);

        assertEquals(4, filtered.size());
        for (AbstractExternalData dsTest : filtered)
        {
            assertNotSame(s5, dsTest.getSampleIdentifier());
        }

        context.assertIsSatisfied();
    }
    
    private List<String> extractCodes(List<AbstractExternalData> dataSets)
    {
        List<String> codes = new ArrayList<String>();
        for (AbstractExternalData dataSet : dataSets)
        {
            codes.add(dataSet.getCode());
        }
        Collections.sort(codes);
        return codes;
    }

}
