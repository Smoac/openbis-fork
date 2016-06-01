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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.SoftLinkMaker;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
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
public class HierarchicalStorageUpdater implements IDataStoreLockingMaintenanceTask
{
    public static final String STOREROOT_DIR_LINK_PATH_KEY = "storeroot-dir-link-path";

    public static final String HIERARCHY_ROOT_DIR_KEY = "hierarchy-root-dir";

    public static final String HIERARCHY_LINK_NAMING_STRATEGY = "link-naming-strategy";

    public static final String LINK_SOURCE_SUBFOLDER = "link-source-subpath";

    public static final String LINK_FROM_FIRST_CHILD = "link-from-first-child";

    private static final String REBUILDING_HIERARCHICAL_STORAGE = "Rebuilding hierarchical storage";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HierarchicalStorageUpdater.class);

    private static class LinkSourceDescriptor
    {
        private final String subFolder;

        private final boolean linkFromFirstChild;

        public LinkSourceDescriptor(String subFolder, boolean linkFromFirstChild)
        {
            this.subFolder = subFolder;
            this.linkFromFirstChild = linkFromFirstChild;
        }

        public String getSubFolder()
        {
            return subFolder;
        }

        public boolean isLinkFromFirstChild()
        {
            return linkFromFirstChild;
        }
    }

    private IEncapsulatedOpenBISService openBISService;

    private IHierarchicalStorageLinkNamingStrategy linkNamingStrategy;

    private File storeRoot;

    private File hierarchyRoot;

    private Map<String /* data set type */, LinkSourceDescriptor> linkSourceDescriptors;

    @Override
    public void setUp(String pluginName, Properties pluginProperties)
    {
        LogInitializer.init();
        String storeRootFileName = pluginProperties.getProperty(STOREROOT_DIR_LINK_PATH_KEY);
        if (storeRootFileName == null)
        {
            storeRootFileName = PropertyUtils.getMandatoryProperty(pluginProperties, DssPropertyParametersUtil.STOREROOT_DIR_KEY);
        }
        String hierarchyRootFileName =
                PropertyUtils.getMandatoryProperty(pluginProperties, HIERARCHY_ROOT_DIR_KEY);

        openBISService = ServiceProvider.getOpenBISService();
        linkNamingStrategy = createLinkNamingStrategy(pluginProperties);
        storeRoot = new File(storeRootFileName);
        hierarchyRoot = new File(hierarchyRootFileName);
        linkSourceDescriptors = initializeLinkSourceDescriptors(pluginProperties);

        operationLog.info("Plugin initialized with: store root = " + storeRootFileName
                + ", hierarchy root = " + hierarchyRootFileName);
    }

    @Override
    public void execute()
    {
        rebuildHierarchy();
    }

    /**
     * @see IDataStoreLockingMaintenanceTask#requiresDataStoreLock()
     */
    @Override
    public boolean requiresDataStoreLock()
    {
        return true;
    }

    private IHierarchicalStorageLinkNamingStrategy createLinkNamingStrategy(Properties properties)
    {
        String linkNamingStrategyClassName =
                PropertyUtils.getProperty(properties, HIERARCHY_LINK_NAMING_STRATEGY,
                        TemplateBasedLinkNamingStrategy.class.getName());
        ExtendedProperties strategyProperties =
                ExtendedProperties
                        .getSubset(properties, HIERARCHY_LINK_NAMING_STRATEGY + ".", true);
        try
        {
            return ClassUtils.create(IHierarchicalStorageLinkNamingStrategy.class,
                    Class.forName(linkNamingStrategyClassName), strategyProperties);
        } catch (ClassNotFoundException ex)
        {
            throw ConfigurationFailureException.fromTemplate("Wrong '%s' property: %s",
                    HIERARCHY_LINK_NAMING_STRATEGY, ex.getMessage());
        }
    }

    private Map<String, LinkSourceDescriptor> initializeLinkSourceDescriptors(
            Properties pluginProperties)
    {
        HashMap<String, LinkSourceDescriptor> result = new HashMap<String, LinkSourceDescriptor>();

        ExtendedProperties subFolderProps =
                ExtendedProperties.getSubset(pluginProperties, LINK_SOURCE_SUBFOLDER + ".", true);
        ExtendedProperties linkFromFirstChildProps =
                ExtendedProperties.getSubset(pluginProperties, LINK_FROM_FIRST_CHILD + ".", true);

        HashSet<Object> dataSetTypes = new HashSet<Object>();
        dataSetTypes.addAll(subFolderProps.keySet());
        dataSetTypes.addAll(linkFromFirstChildProps.keySet());
        for (Object o : dataSetTypes)
        {
            String dataSetType = (String) o;
            String subFolder = subFolderProps.getProperty(dataSetType);
            boolean linkFromFirstChild =
                    Boolean.parseBoolean(linkFromFirstChildProps.getProperty(dataSetType));
            LinkSourceDescriptor descriptor =
                    new LinkSourceDescriptor(subFolder, linkFromFirstChild);
            result.put(dataSetType, descriptor);
        }

        return result;
    }

    /**
     * Refreshes the hierarchy of the data inside hierarchical storage accordingly to the database content.
     */
    private void rebuildHierarchy()
    {
        operationLog.info(REBUILDING_HIERARCHICAL_STORAGE);
        Map<String, String> newLinkMappings = convertDataToLinkMappings();
        Set<String> toCreate = new HashSet<String>(newLinkMappings.keySet());
        Set<String> toDelete = linkNamingStrategy.extractPaths(hierarchyRoot);
        Set<String> dontTouch = intersection(toCreate, toDelete);
        toCreate.removeAll(dontTouch);
        toDelete.removeAll(dontTouch);
        removeUnnecessaryMappings(newLinkMappings, toCreate);
        deleteObsoleteLinks(toDelete);
        createLinksForChangedData(newLinkMappings);
    }

    /**
     * Extracts a {@link Map}: (target,source) from a collection of data sets.
     */
    private Map<String, String> convertDataToLinkMappings()
    {
        Collection<SimpleDataSetInformationDTO> dataSets = openBISService.listPhysicalDataSets();
        Map<String, String> linkMappings = new HashMap<String, String>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            File targetFile =
                    new File(hierarchyRoot, linkNamingStrategy.createHierarchicalPath(dataSet));
            File share = new File(storeRoot, dataSet.getDataSetShareId());
            File dataSetLocationRoot = new File(share, dataSet.getDataSetLocation());
            File linkSource = determineLinkSource(dataSetLocationRoot, dataSet.getDataSetType());
            if (linkSource != null)
            {
                linkMappings.put(targetFile.getAbsolutePath(), linkSource.getAbsolutePath());
            } else
            {

                String logMessage =
                        String.format("Can not determine the link source file for data set '%s', "
                                + "dataSetType='%s'. Link creation will be skipped.",
                                dataSetLocationRoot, dataSet.getDataSetType());
                operationLog.warn(logMessage);
            }
        }
        return linkMappings;
    }

    private File determineLinkSource(File dataSetLocationRoot, String dataSetType)
    {
        LinkSourceDescriptor linkSourceDescriptor = getLinkSourceDescriptor(dataSetType);
        File source = dataSetLocationRoot;

        if (linkSourceDescriptor != null)
        {
            String subPath = linkSourceDescriptor.getSubFolder();
            if (StringUtils.isBlank(subPath) == false)
            {
                source = new File(source.getAbsolutePath() + File.separator + subPath);
                if (source.exists() == false)
                {
                    String logMessage =
                            String.format("Invalid '%s' configuration for "
                                    + "data set '%s'. Subfolder '%s' does not exist",
                                    LINK_SOURCE_SUBFOLDER, dataSetLocationRoot, source);
                    operationLog.warn(logMessage);
                    return null;
                }
            }
            if (linkSourceDescriptor.isLinkFromFirstChild() && source.isDirectory())
            {
                File[] rootChildren = source.listFiles();
                if (rootChildren == null || rootChildren.length == 0)
                {
                    String logMessage =
                            String.format("Invalid '%s' configuration for "
                                    + "data set '%s'. Subfolder '%s' has no children",
                                    LINK_FROM_FIRST_CHILD, dataSetLocationRoot, source);
                    operationLog.warn(logMessage);
                    return null;
                }
                source = rootChildren[0];
            }
        }

        return source;
    }

    private LinkSourceDescriptor getLinkSourceDescriptor(String dataSetType)
    {
        return linkSourceDescriptors.get(dataSetType);
    }

    /**
     * Removes from the <code>linkMappings</code> map all the elements with keys not belonging to <code>keep</code> set.
     */
    private void removeUnnecessaryMappings(Map<String, String> linkMappings, Set<String> keep)
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
    private Set<String> intersection(Set<String> setA, Set<String> setB)
    {
        Set<String> toBeUntouched = new HashSet<String>(setA);
        toBeUntouched.retainAll(setB);
        return toBeUntouched;
    }

    /**
     * Recursively removes from the file system files with paths defined in <code>toBeDeleted</code> {@link Set}.
     */
    private void deleteObsoleteLinks(Set<String> toBeDeleted)
    {
        for (String pathToDelete : toBeDeleted)
        {
            File toDelete = new File(pathToDelete);
            File parent = toDelete.getParentFile();
            deleteWithSymbolicLinks(toDelete);
            while (parent != null && false == isSameFile(parent, hierarchyRoot))
            {
                if (parent.list().length == 0)
                {
                    toDelete = parent;
                    parent = toDelete.getParentFile();
                    delete(toDelete);
                } else
                {
                    break;
                }
            }
        }
    }

    private boolean isSameFile(File file1, File file2)
    {
        try
        {
            return file1.getCanonicalPath().equals(file2.getCanonicalPath());
        } catch (IOException ioex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
        }
    }

    private void deleteWithSymbolicLinks(File toDelete)
    {
        if (FileUtilities.isSymbolicLink(toDelete) == false && toDelete.isDirectory())
        {
            for (File file : toDelete.listFiles())
            {
                // all these files should be symbolic links to a dataset directory.
                // We cannot delete recursively here, it would remove the original files.
                boolean ok = delete(file);
                if (ok == false)
                {
                    operationLog.error("Cannot delete the file: " + file.getPath());
                }
            }
        }

        delete(toDelete);
    }

    private boolean delete(File file)
    {
        if (canBeDeleted(file))
        {
            operationLog.info("Deleting " + file.getAbsolutePath());
            return file.delete();
        } else
        {
            operationLog.error(file.getPath() + " is not a symbolic link and will not be deleted.");
            return false;
        }

    }

    private boolean canBeDeleted(File file)
    {
        if (isUnderHierarchyRoot(file))
        {
            return FileUtilities.isSymbolicLink(file) || file.isDirectory();
        } else
        {
            operationLog.warn("Aborting an attempt to delete content outside of hierarchy root : "
                    + file.getAbsolutePath() + ". Please analyze, this is a programming error.");
            return false;
        }
    }

    /**
     * we cannot use cannonical paths here, because they resolve symbolic links.
     */
    private boolean isUnderHierarchyRoot(File file)
    {
        return file.getAbsolutePath().startsWith(hierarchyRoot.getAbsolutePath());
    }

    /**
     * Creates the soft links for files with paths defined in <code>linkMappings</code> {@link Map}.
     */
    private void createLinksForChangedData(Map<String, String> linkMappings)
    {
        for (String targetPath : linkMappings.keySet())
        {
            File targetDir = new File(targetPath);
            String sourcePath = linkMappings.get(targetPath);
            File sourceFile = new File(sourcePath);
            targetDir.getParentFile().mkdirs();
            SoftLinkMaker.createSymbolicLink(sourceFile, targetDir);
        }
    }

}
