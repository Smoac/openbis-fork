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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Kaloyan Enimanev
 */
public class HierarchicalStorageUpdaterTest extends AbstractFileSystemTestCase
{

    private final static File STORE_ROOT_TEMPLATE = new File("./resource/test-data/"
            + HierarchicalStorageUpdaterTest.class.getSimpleName() + "/store-root");

    private final static String DATASET_TYPE = "dataset-type";

    private final static String SHARE_ID = "1";

    private final static FileFilter ignoreSVNFiles = new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().equals(".svn") == false;
            }
        };

    private IEncapsulatedOpenBISService openBISService;

    private Mockery context;

    public HierarchicalStorageUpdaterTest()
    {
        super(false);
    }

    @BeforeMethod
    public void setUpMocks() throws Exception
    {

        prepareDirectoryStructures();

        context = new Mockery();
        openBISService = context.mock(IEncapsulatedOpenBISService.class);
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);

        context.checking(new Expectations()
            {
                {
                    allowing(beanFactory).getBean("openBIS-service");
                    will(returnValue(openBISService));

                    allowing(openBISService).listPhysicalDataSets();
                    will(returnValue(listDataSets()));
                }
            });
    }

    @AfterMethod(alwaysRun=true)
    public void tearDown()
    {
        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testDataIsNotDeletedAfterReconfig() throws Exception
    {

        updater().execute();

        // execute with different configuration and attempt to damage the data store
        reconfiguredUpdater().execute();

        assertDataStoreNotDamaged();

    }

    @Test
    public void testBrokenLinksAreDeleted() throws Exception
    {

        HierarchicalStorageUpdater storageUpdater = createUpdater(true);
        storageUpdater.execute();

        File shareRoot = new File(getStoreRoot(), SHARE_ID);
        File dataSetSource = new File(shareRoot, "ds2");
        assertTrue(dataSetSource.isDirectory());

        File symboliLink =
                new File(getHierarchyRoot().getAbsolutePath()
                        + "/space/project/experiment/dataset-type+sample+ds2");
        assertTrue("Symbolic links should be created", FileUtilities.isSymbolicLink(symboliLink));

        FileUtilities.deleteRecursively(dataSetSource);

        storageUpdater.execute();

        assertTrue("Broken symlinks should be deleted", false == symboliLink.exists());
    }

    private void prepareDirectoryStructures() throws IOException
    {
        FileUtils.copyDirectory(STORE_ROOT_TEMPLATE, getStoreRoot(), ignoreSVNFiles);
        getHierarchyRoot().mkdirs();
    }

    private File getStoreRoot()
    {
        return new File(workingDirectory, "store-root");
    }

    private File getHierarchyRoot()
    {
        return new File(workingDirectory, "hierarchy-root");
    }

    private void assertDataStoreNotDamaged() throws Exception
    {
        File cleanStorage = new File(workingDirectory, "clean-storage-nosvn");
        FileUtils.copyDirectory(STORE_ROOT_TEMPLATE, cleanStorage, ignoreSVNFiles);

        long templateSize = FileUtils.sizeOfDirectory(cleanStorage);
        long rootSize = FileUtils.sizeOfDirectory(getStoreRoot());

        String errMessage =
                String.format("The data-store root in '%s' has been damaged", getStoreRoot()
                        .getAbsolutePath());
        assertEquals(errMessage, templateSize, rootSize);
    }


    private HierarchicalStorageUpdater updater()
    {
        return createUpdater(false);
    }

    private HierarchicalStorageUpdater reconfiguredUpdater()
    {
        return createUpdater(true);
    }


    private HierarchicalStorageUpdater createUpdater(boolean linkFromFirstChild)
    {
        final String pluginName = "hierarchical-storage-updater";
        final String pluginPrefix = pluginName + ".";

        System.setProperty(DssPropertyParametersUtil.OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + HierarchicalStorageUpdater.STOREROOT_DIR_KEY, getStoreRoot().getAbsolutePath());
        System.setProperty(DssPropertyParametersUtil.OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + pluginPrefix + HierarchicalStorageUpdater.HIERARCHY_ROOT_DIR_KEY,
                getHierarchyRoot().getAbsolutePath());

        Properties properties = new Properties();
        if (linkFromFirstChild)
        {
            properties.put(HierarchicalStorageUpdater.LINK_SOURCE_SUBFOLDER + "." + DATASET_TYPE,
                    "original");
            properties.put(HierarchicalStorageUpdater.LINK_FROM_FIRST_CHILD + "." + DATASET_TYPE,
                    "" + true);
        }

        HierarchicalStorageUpdater updater = new HierarchicalStorageUpdater();
        updater.setUp(pluginName, properties);
        return updater;
    }

    private List<SimpleDataSetInformationDTO> listDataSets()
    {
        final File shareRoot = new File(getStoreRoot(), SHARE_ID);
        final List<SimpleDataSetInformationDTO> result = new ArrayList<SimpleDataSetInformationDTO>();
        for (File directory : FileUtilities.listDirectories(shareRoot, false))
        {
            SimpleDataSetInformationDTO dataset = new SimpleDataSetInformationDTO();
            result.add(dataset);
            dataset.setDataSetType(DATASET_TYPE);
            dataset.setDataSetCode(directory.getName());
            dataset.setDataSetLocation(directory.getName());
            dataset.setDataSetShareId(SHARE_ID);
            dataset.setExperimentCode("experiment");
            dataset.setSpaceCode("space");
            dataset.setProjectCode("project");
            dataset.setSampleCode("sample");
        }
        return result;
    }
}
