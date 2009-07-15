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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Creates in {@link #HIERARCHY_ROOT_DIR_KEY} the hierarchical structure of data sets registered in
 * openBIS for given data store and located in {@link #STOREROOT_DIR_KEY}. The structure building
 * procedure is run for the first time after {@link #HIERARCHY_BUILD_DELAY_KEY} seconds and is
 * executed every {@link #HIERARCHY_REBUILD_INTERVAL_KEY} seconds.
 * 
 * @author Izabela Adamczyk
 */
public class HierarchicalStorageDaemon
{
    private static final String REBUILDING_HIERARCHICAL_STORAGE = "Rebuilding hierarchical storage";

    private static final String DAEMON_NOT_STARTED_FORMAT =
            "No '%s' property defined - hierarchical storage daemon not started.";

    private static final String DEAMON_STARTED_FORMAT =
            "Hierarchical storage daemon is running (rebuild interval = %s, first build delay = %s, store root = '%s', hierarchy root = '%s')";

    private static final int ONE_DAY_IN_SEC = 60 * 60 * 24;

    public static final String STOREROOT_DIR_KEY = "storeroot-dir";

    public static final String HIERARCHY_REBUILD_INTERVAL_KEY = "hierarchy-rebuild-interval";

    public static final String HIERARCHY_BUILD_DELAY_KEY = "hierarchy-build-delay";

    public static final String HIERARCHY_ROOT_DIR_KEY = "hierarchy-root-dir";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HierarchicalStorageDaemon.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, HierarchicalStorageDaemon.class);

    /**
     * Creates a thread refreshing hierarchical storage.
     */
    public static void main(String[] args)
    {
        LogInitializer.init();
        Properties properties = PropertyParametersUtil.loadServiceProperties();
        final String storeRoot = PropertyUtils.getProperty(properties, STOREROOT_DIR_KEY);
        if (storeRoot == null)
        {
            logInfo(String.format(DAEMON_NOT_STARTED_FORMAT, STOREROOT_DIR_KEY));
            return;
        }
        final String hierarchyRoot = PropertyUtils.getProperty(properties, HIERARCHY_ROOT_DIR_KEY);
        if (hierarchyRoot == null)
        {
            logInfo(String.format(DAEMON_NOT_STARTED_FORMAT, HIERARCHY_ROOT_DIR_KEY));
            return;
        }
        long rebuildInterval =
                PropertyUtils.getLong(properties, HIERARCHY_REBUILD_INTERVAL_KEY, ONE_DAY_IN_SEC);
        long buildDelay =
                PropertyUtils.getLong(properties, HIERARCHY_BUILD_DELAY_KEY, ONE_DAY_IN_SEC);
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable()
            {
                public void run()
                {
                    rebuildHierarchy(new File(storeRoot), openBISService, new File(hierarchyRoot));
                }
            }, buildDelay, rebuildInterval, TimeUnit.SECONDS);
        logInfo(String.format(DEAMON_STARTED_FORMAT, rebuildInterval, buildDelay, storeRoot,
                hierarchyRoot));
    }

    /**
     * Refreshes the hierarchy of the data inside hierarchical storage accordingly to the database
     * content.
     */
    private static void rebuildHierarchy(File storeRoot,
            IEncapsulatedOpenBISService openBISService, File hierarchyRoot)
    {
        logInfo(REBUILDING_HIERARCHICAL_STORAGE);
        Collection<SimpleDataSetInformationDTO> dataSets = openBISService.listDataSets();
        Map<String, String> newLinkMappings =
                convertDataToLinkMappings(storeRoot, hierarchyRoot, dataSets);
        Set<String> toCreate = new HashSet<String>(newLinkMappings.keySet());
        Set<String> toDelete = DataSetHierarchyHelper.extractPaths(hierarchyRoot);
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
    private static Map<String, String> convertDataToLinkMappings(File storeRoot,
            File hierarchyRoot, Collection<SimpleDataSetInformationDTO> dataSets)
    {
        Map<String, String> linkMappings = new HashMap<String, String>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            File targetFile =
                    new File(hierarchyRoot, DataSetHierarchyHelper.createHierarchicalPath(dataSet));
            File sourceFile = new File(storeRoot, dataSet.getDataSetLocation());
            linkMappings.put(targetFile.getAbsolutePath(), sourceFile.getAbsolutePath());
        }
        return linkMappings;
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
            FileUtilities.deleteRecursively(toDelete);
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
            ProcessExecutionHelper.runAndLog(new SoftLinkMaker().createCommand(sourceFile,
                    targetDir), operationLog, machineLog);
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
