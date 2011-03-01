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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.maintenance.IResourceContendingMaintenanceTask;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.Constants;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Creates the hierarchical structure of data sets registered in openBIS for given data store.
 * 
 * @author Izabela Adamczyk
 * @author Kaloyan Enimanev
 */
public class HierarchicalStorageUpdater implements IResourceContendingMaintenanceTask
{
    public static final String STOREROOT_DIR_KEY = "storeroot-dir";

    public static final String HIERARCHY_ROOT_DIR_KEY = "hierarchy-root-dir";

    public static final String HIERARCHY_LINK_NAMING_STRATEGY = "link-naming-strategy";

    private static final String REBUILDING_HIERARCHICAL_STORAGE = "Rebuilding hierarchical storage";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HierarchicalStorageUpdater.class);

    private IEncapsulatedOpenBISService openBISService;

    private IHierarchicalStorageLinkNamingStrategy linkNamingStrategy;

    private File storeRoot;

    private File hierarchyRoot;

    public void setUp(String pluginName, Properties pluginProperties)
    {
        LogInitializer.init();
        // TODO 2010-03-23, Piotr Buczek: pluginProperties contain all needed properties
        // There is no need to load service properties once again.
        Properties properties = DssPropertyParametersUtil.loadServiceProperties();
        String storeRootFileName =
                PropertyUtils.getMandatoryProperty(properties, STOREROOT_DIR_KEY);
        String hierarchyRootFileName =
                PropertyUtils.getMandatoryProperty(properties, pluginName + "."
                        + HIERARCHY_ROOT_DIR_KEY);

        openBISService = ServiceProvider.getOpenBISService();
        linkNamingStrategy = createLinkNamingStrategy(properties);
        storeRoot = new File(storeRootFileName);
        hierarchyRoot = new File(hierarchyRootFileName);

        operationLog.info("Plugin initialized with: store root = " + storeRootFileName
                + ", hierarchy root = " + hierarchyRootFileName);
    }

    public void execute()
    {
        rebuildHierarchy();
    }

    /**
     * requires an exclusive lock of the data store folder.
     */
    public String getRequiredResourceLock()
    {
        return Constants.DATA_STORE_RESOURCE_NAME;
    }

    private IHierarchicalStorageLinkNamingStrategy createLinkNamingStrategy(Properties properties)
    {
        String linkNamingStrategyClassName =
                PropertyUtils.getProperty(properties, HIERARCHY_LINK_NAMING_STRATEGY,
                        TemplateBasedLinkNamingStrategy.class.getName());
        try
        {
            return ClassUtils.create(IHierarchicalStorageLinkNamingStrategy.class,
                            Class.forName(linkNamingStrategyClassName), properties);
        } catch (ClassNotFoundException ex)
        {
            throw ConfigurationFailureException.fromTemplate("Wrong '%s' property: %s",
                    HIERARCHY_LINK_NAMING_STRATEGY, ex.getMessage());
        }
    }

    /**
     * Refreshes the hierarchy of the data inside hierarchical storage accordingly to the database
     * content.
     */
    private void rebuildHierarchy()
    {
        logInfo(REBUILDING_HIERARCHICAL_STORAGE);
        Map<String, String> newLinkMappings = convertDataToLinkMappings();
        Set<String> toCreate = new HashSet<String>(newLinkMappings.keySet());
        Set<String> toDelete = linkNamingStrategy.extractPaths(hierarchyRoot);
        Set<String> dontTouch = intersection(toCreate, toDelete);
        toCreate.removeAll(dontTouch);
        toDelete.removeAll(dontTouch);
        removeUnnecessaryMappings(newLinkMappings, toCreate);
        deleteObsoleteLinks(hierarchyRoot, toDelete);
        createLinksForChangedData(newLinkMappings);
    }

    /**
     * Extracts a {@link Map}: (target,source) from a collection of data sets.
     */
    private Map<String, String> convertDataToLinkMappings()
    {
        Collection<SimpleDataSetInformationDTO> dataSets = openBISService.listDataSets();
        Map<String, String> linkMappings = new HashMap<String, String>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            File targetFile =
                    new File(hierarchyRoot, linkNamingStrategy.createHierarchicalPath(dataSet));
            File share = new File(storeRoot, dataSet.getDataSetShareId());
            File dataSetLocationRoot = new File(share, dataSet.getDataSetLocation());
            File linkSource = determineLinkSource(dataSetLocationRoot);
            linkMappings.put(targetFile.getAbsolutePath(), linkSource.getAbsolutePath());
        }
        return linkMappings;
    }

    /**
     * Some storage processors keep the originally uploaded data sets in an "original" sub-folder.
     * We would like to link directly to the originally uploaded content and therefore we build
     * determine the file/folder which we want to link in the following way
     * <p>
     * <li>1. If the dataset location contains a single subdirectory named "original" with a single
     * sub-item F then we link to F (F can be file or directory).
     * <li>2. else If the dataset location contains a single sub-item F then we link F.
     * <li>3. else (the dataset location has multiple children) we link to the data set location
     */
    private File determineLinkSource(File sourceRoot)
    {
        File[] rootChildren = sourceRoot.listFiles();
        File result = sourceRoot;
        if (rootChildren != null && rootChildren.length == 1)
        {
            result = rootChildren[0];

            if (rootChildren[0].isDirectory()
                    && DefaultStorageProcessor.ORIGINAL_DIR.equals(rootChildren[0].getName()))
            {
                File[] originalChildren = rootChildren[0].listFiles();
                if (originalChildren != null && originalChildren.length == 1)
                {
                    result = originalChildren[0];
                }
            }
        }
        return result;
    }

    /**
     * Removes from the <code>linkMappings</code> map all the elements with keys not belonging to
     * <code>keep</code> set.
     */
    private static void removeUnnecessaryMappings(Map<String, String> linkMappings, Set<String> keep)
    {
        Set<String> keys = new HashSet<String>(linkMappings.keySet());
        for (String path : keys)
        {
            if (keep.contains(path) == false)
            {
                linkMappings.remove(path);
            }
        }
    }

    /**
     * Creates a new {@link Set} containing the elements that belong to both {@link Set}s.
     */
    private static Set<String> intersection(Set<String> setA, Set<String> setB)
    {
        Set<String> toBeUntouched = new HashSet<String>(setA);
        toBeUntouched.retainAll(setB);
        return toBeUntouched;
    }

    /**
     * Recursively removes from the file system files with paths defined in <code>toBeDeleted</code>
     * {@link Set}.
     */
    private static void deleteObsoleteLinks(File hierarchyRoot, Set<String> toBeDeleted)
    {
        for (String pathToDelete : toBeDeleted)
        {
            File toDelete = new File(pathToDelete);
            File parent = toDelete.getParentFile();
            deleteWithSymbolicLinks(toDelete);
            while (parent != null
                    && parent.getAbsolutePath().equals(hierarchyRoot.getAbsolutePath()) == false)
            {
                if (parent.list().length == 0)
                {
                    toDelete = parent;
                    parent = toDelete.getParentFile();
                    toDelete.delete();
                } else
                {
                    break;
                }
            }
        }
    }

    private static void deleteWithSymbolicLinks(File toDeleteParent)
    {
        if (toDeleteParent.isDirectory() == false)
        {
            operationLog
                    .error("Directory structure is different than expected. File '"
                            + toDeleteParent.getPath()
                            + "' should be a directory. It will not be cleared.");
            return;
        }
        for (File file : toDeleteParent.listFiles())
        {
            // all these files should be symbolic links to a dataset directory.
            // We cannot delete recursively here, it would remove the original files.
            boolean ok = file.delete();
            if (ok == false)
            {
                operationLog.error("Cannot delete the file: " + file.getPath());
            }
        }
        toDeleteParent.delete();
    }

    /**
     * Creates the soft links for files with paths defined in <code>linkMappings</code> {@link Map}.
     */
    private static void createLinksForChangedData(Map<String, String> linkMappings)
    {
        for (String targetPath : linkMappings.keySet())
        {
            File targetDir = new File(targetPath);
            String sourcePath = linkMappings.get(targetPath);
            File sourceFile = new File(sourcePath);
            targetDir.mkdirs();
            SoftLinkMaker.createSymbolicLink(sourceFile, targetDir);
        }
    }

    private static void logInfo(String info)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(info);
        }
    }
}
