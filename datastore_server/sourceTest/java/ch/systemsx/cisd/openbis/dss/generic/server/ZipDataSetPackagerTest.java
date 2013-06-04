/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.IOUtilities;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.HierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.DssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.IContentCache;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DataSetExistenceChecker;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;

import de.schlichtherle.io.FileInputStream;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ZipDataSetPackagerTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_CODE = "ds1";
    private static final String DSS_CODE = "DSS";
    private static final String SHARE_ID = "1";
    private Mockery context;
    private IEncapsulatedOpenBISService openbisService;
    private IShareIdManager shareIdManager;
    private IConfigProvider configProvider;
    private HierarchicalContentProvider contentProvider;
    private DataSetExistenceChecker existenceChecker;
    private IHierarchicalContentFactory contentProviderFactory;
    private File rootFolder;

    @BeforeMethod
    public void prepareTestFixture()
    {
        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        configProvider = context.mock(IConfigProvider.class);
        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getStoreRoot();
                    will(returnValue(workingDirectory));
                    
                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DSS_CODE));
                }
            });
        IContentCache contentCache = null;
        ISessionTokenProvider sessionTokenProvider = null;
        ExposablePropertyPlaceholderConfigurer infoProvider = null;
        IDataSetDirectoryProvider dataSetDirectoryProvider = new DataSetDirectoryProvider(workingDirectory, shareIdManager);
        contentProviderFactory = new DefaultFileBasedHierarchicalContentFactory();
        contentProvider = new HierarchicalContentProvider(openbisService, dataSetDirectoryProvider, contentProviderFactory,
                new DssServiceRpcGenericFactory(), contentCache, sessionTokenProvider, DSS_CODE, infoProvider);
        existenceChecker = new DataSetExistenceChecker(dataSetDirectoryProvider, TimingParameters.getDefaultParameters());
        File share = new File(workingDirectory, SHARE_ID);
        share.mkdirs();
        rootFolder = new File(share, DATA_SET_CODE);
        File subFolder = new File(rootFolder, "subfolder");
        subFolder.mkdirs();
        FileUtilities.writeToFile(new File(rootFolder, "read.me"), "Don't read me!");
        FileUtilities.writeToFile(new File(subFolder, "change-log.txt"), "nothing changed");
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateCompressed() throws Exception
    {
        PhysicalDataSet dataSet = new DataSetBuilder().store(new DataStoreBuilder(DSS_CODE).getStore())
                .code(DATA_SET_CODE).type("MY-DATASET").fileFormat("XML").location(DATA_SET_CODE).property("AGE", "42")
                .experiment(new ExperimentBuilder().identifier("/S/P/E1").type("MY-EXPERIMENT").property("GREETINGS", "Hello").getExperiment())
                .getDataSet();
        prepareShareIdManager(DATA_SET_CODE);
        prepareGetDataSetLocation(dataSet);
        File zipFile = new File(workingDirectory, "archived1.zip");
        ZipDataSetPackager packager = new ZipDataSetPackager(zipFile, true, contentProvider, existenceChecker);
        
        packager.addDataSetTo("", dataSet);
        packager.close();
        
        checkZipFileContent(zipFile, dataSet, "data_set\tcode\tds1\n" + 
                "data_set\tproduction_timestamp\t\n" + 
                "data_set\tproducer_code\t\n" + 
                "data_set\tdata_set_type\tMY-DATASET\n" + 
                "data_set\tis_measured\tTRUE\n" + 
                "data_set\tis_complete\tFALSE\n" + 
                "data_set\tAGE\t42\n" + 
                "data_set\tparent_codes\t\n" + 
                "experiment\tspace_code\tS\n" + 
                "experiment\tproject_code\tP\n" + 
                "experiment\texperiment_code\tE1\n" + 
                "experiment\texperiment_type_code\tMY-EXPERIMENT\n" + 
                "experiment\tregistration_timestamp\t\n" + 
                "experiment\tregistrator\t\n" + 
                "experiment\tGREETINGS\tHello\n");
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCreateUncompressed() throws Exception
    {
        PhysicalDataSet dataSet = new DataSetBuilder().store(new DataStoreBuilder(DSS_CODE).getStore())
                .code(DATA_SET_CODE).type("MY-DATASET").fileFormat("XML").location(DATA_SET_CODE).property("AGE", "24")
                .experiment(new ExperimentBuilder().identifier("/S/P/E1").type("MY-EXPERIMENT").getExperiment())
                .sample(new SampleBuilder().identifier("/S/S1").type("MY-SAMPLE").property("GREETINGS", "Hi").getSample())
                .parent(new DataSetBuilder().code("P1").getDataSet())
                .getDataSet();
        prepareShareIdManager(DATA_SET_CODE);
        prepareGetDataSetLocation(dataSet);
        File zipFile = new File(workingDirectory, "archived2.zip");
        ZipDataSetPackager packager = new ZipDataSetPackager(zipFile, false, contentProvider, existenceChecker);
        
        packager.addDataSetTo("", dataSet);
        packager.close();
        
        checkZipFileContent(zipFile, dataSet, "data_set\tcode\tds1\n" +
                "data_set\tproduction_timestamp\t\n" +
                "data_set\tproducer_code\t\n" +
                "data_set\tdata_set_type\tMY-DATASET\n" +
                "data_set\tis_measured\tTRUE\n" +
                "data_set\tis_complete\tFALSE\n" +
                "data_set\tAGE\t24\n" +
                "data_set\tparent_codes\tP1\n" +
                "sample\ttype_code\tMY-SAMPLE\n" +
                "sample\tcode\tS1\n" +
                "sample\tspace_code\tS\n" +
                "sample\tregistration_timestamp\t\n" +
                "sample\tregistrator\t\n" +
                "sample\tGREETINGS\tHi\n" +
                "experiment\tspace_code\tS\n" +
                "experiment\tproject_code\tP\n" +
                "experiment\texperiment_code\tE1\n" +
                "experiment\texperiment_type_code\tMY-EXPERIMENT\n" +
                "experiment\tregistration_timestamp\t\n" +
                "experiment\tregistrator\t\n");
        context.assertIsSatisfied();
    }

    private void checkZipFileContent(File zipFile, PhysicalDataSet dataSet, String metaData) throws Exception
    {
        assertEquals(true, zipFile.exists());
        ZipFile zFile = new ZipFile(zipFile);
        for (ZipEntry zipEntry : Collections.list(zFile.entries()))
        {
            String relativePath = zipEntry.getName();
            if (relativePath.equals(AbstractDataSetPackager.META_DATA_FILE_NAME))
            {
                checkContent(zFile, zipEntry, new ByteArrayInputStream(metaData.getBytes()));
            } else
            {
                checkZipEntry(zFile, zipEntry);
            }
        }
    }

    private void checkZipEntry(ZipFile zf, ZipEntry entry) throws Exception
    {
        String relativePath = entry.getName();
        File file = new File(rootFolder, relativePath);
        assertEquals(relativePath + " exists", true, file.exists());
        assertEquals(relativePath + " folder or file", file.isDirectory(), entry.isDirectory());
        if (entry.isDirectory())
        {
            return;
        }
        assertEquals(relativePath + " size", file.length(), entry.getSize());
        assertEquals(relativePath + " checksum", calculateChecksum(file), (int) entry.getCrc());
        checkContent(zf, entry, new FileInputStream(file));
    }

    private void checkContent(ZipFile zf, ZipEntry entry, InputStream inputStream) throws Exception
    {
        String expectedContent = getContentAndClose(inputStream);
        String actualContent = getContentAndClose(zf.getInputStream(entry));
        assertEquals(entry.getName() + " content", expectedContent, actualContent);
    }
    
    private String getContentAndClose(InputStream inputStream) throws Exception
    {
        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, output);
            return output.toString();
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private int calculateChecksum(File file) throws Exception
    {
        InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(file);
            return IOUtilities.getChecksumCRC32(inputStream);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }
    
    private void prepareGetDataSetLocation(final PhysicalDataSet dataSet)
    {
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetDataSetLocation(dataSet.getCode());
                    will(returnValue(new DatasetLocationNode(dataSet)));
                }
            });
    }

    private void prepareShareIdManager(final String dataSetCode)
    {
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(shareIdManager).getShareId(dataSetCode);
                    will(returnValue(SHARE_ID));
                    
                    one(shareIdManager).lock(dataSetCode);
                    
                    one(shareIdManager).releaseLock(dataSetCode);
                }
            });
    }

}
