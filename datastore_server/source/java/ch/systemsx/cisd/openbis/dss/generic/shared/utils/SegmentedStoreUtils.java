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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Utility methods for segmented stores.
 * 
 * @author Franz-Josef Elmer
 */
public class SegmentedStoreUtils
{
    private static final IFreeSpaceProvider DUMMY_FREE_SPACE_PROVIDER = new IFreeSpaceProvider()
        {
            public long freeSpaceKb(HostAwareFile path) throws IOException
            {
                return Long.MAX_VALUE;
            }
        };

    private static final String RSYNC_EXEC = "rsync";

    private static final Pattern SHARE_ID_PATTERN = Pattern.compile("[0-9]+");

    private static final Comparator<Share> SHARE_COMPARATOR = new Comparator<Share>()
        {
            public int compare(Share o1, Share o2)
            {
                return o1.getShareId().compareTo(o2.getShareId());
            }
        };

    private static final FileFilter FILTER_ON_SHARES = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                if (pathname.isDirectory() == false)
                {
                    return false;
                }
                String name = pathname.getName();
                return SHARE_ID_PATTERN.matcher(name).matches();
            }
        };

    /**
     * Lists all folders in specified store root directory which match share pattern.
     */
    public static File[] getShares(File storeRootDir)
    {
        File[] files = storeRootDir.listFiles(FILTER_ON_SHARES);
        if (files == null)
        {
            throw new ConfigurationFailureException(
                    "Store folder does not exist or cannot be accessed: " + storeRootDir);
        }
        Arrays.sort(files);
        return files;
    }

    /**
     * Returns first the id of the first incoming share folder of specified store root which allows
     * to move a file from specified incoming folder to the incoming share.
     */
    public static String findIncomingShare(File incomingFolder, File storeRoot, ISimpleLogger logger)
    {
        if (incomingFolder.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Incoming folder does not exist or is not a folder: " + incomingFolder);
        }
        if (storeRoot.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Store root does not exist or is not a folder: " + storeRoot);
        }
        File testFile = new File(incomingFolder, ".DDS_TEST");
        try
        {
            testFile.createNewFile();
        } catch (IOException ex)
        {
            throw new ConfigurationFailureException(
                    "Couldn't create a test file in the following incoming folder: "
                            + incomingFolder, ex);
        }
        File matchingShare = findShare(testFile, storeRoot, logger);
        return matchingShare.getName();
    }

    private static File findShare(File testFile, File storeRoot, ISimpleLogger logger)
    {
        for (File share : getShares(storeRoot))
        {
            File destination = new File(share, testFile.getName());
            if (testFile.renameTo(destination))
            {
                destination.delete();
                Share shareObject =
                        new ShareFactory().createShare(share, DUMMY_FREE_SPACE_PROVIDER, logger);
                if (shareObject.isWithdrawShare())
                {
                    logger.log(LogLevel.WARN, "Incoming folder [" + testFile.getParent()
                            + "] can not be assigned to share " + shareObject.getShareId()
                            + " because its property " + ShareFactory.WITHDRAW_SHARE_PROP
                            + " is set to true.");
                }
                return share;
            }
        }
        testFile.delete();
        throw new ConfigurationFailureException(
                "No share could be found for the following incoming folder: "
                        + testFile.getParentFile().getAbsolutePath());
    }

    /**
     * Gets a list of all shares of specified store root directory. As a side effect it calculates
     * and updates the size of all data sets if necessary.
     * 
     * @param dataStoreCode Code of the data store to which the root belongs.
     * @param incomingShares Set of IDs of incoming shares. Will be used to mark {@link Share}
     *            object in the returned list.
     * @param freeSpaceProvider Provider of free space used for all shares.
     * @param service Access to openBIS API in order to get all data sets and to update data set
     *            size.
     * @param log Logger for logging size calculations.
     */
    public static List<Share> getDataSetsPerShare(File storeRoot, String dataStoreCode,
            Set<String> incomingShares, IFreeSpaceProvider freeSpaceProvider,
            IEncapsulatedOpenBISService service, ISimpleLogger log)
    {
        final long start = System.currentTimeMillis();
        List<Share> shares =
                getDataSetsPerShare(storeRoot, dataStoreCode, freeSpaceProvider, service, log,
                        SystemTimeProvider.SYSTEM_TIME_PROVIDER);
        for (Share share : shares)
        {
            share.setIncoming(incomingShares.contains(share.getShareId()));
        }
        log.log(LogLevel.INFO,
                String.format("Obtained the list of all datasets in all shares in %.2f s.",
                        (System.currentTimeMillis() - start) / 1000.0));
        return shares;
    }

    static List<Share> getDataSetsPerShare(File storeRoot, String dataStoreCode,
            IFreeSpaceProvider freeSpaceProvider, IEncapsulatedOpenBISService service,
            ISimpleLogger log, ITimeProvider timeProvider)
    {
        Map<String, Share> shares = getShares(storeRoot, freeSpaceProvider, log);
        for (SimpleDataSetInformationDTO dataSet : service.listDataSets())
        {
            String shareId = dataSet.getDataSetShareId();
            if (dataStoreCode.equals(dataSet.getDataStoreCode()))
            {
                Share share = shares.get(shareId);
                String dataSetCode = dataSet.getDataSetCode();
                if (share == null)
                {
                    log.log(LogLevel.WARN, "Data set " + dataSetCode
                            + " not accessible because of unknown or unmounted share " + shareId
                            + ".");
                } else
                {
                    File dataSetInStore = new File(share.getShare(), dataSet.getDataSetLocation());
                    if (dataSetInStore.exists())
                    {
                        if (dataSet.getDataSetSize() == null)
                        {
                            log.log(LogLevel.INFO, "Calculating size of " + dataSetInStore);
                            long t0 = timeProvider.getTimeInMilliseconds();
                            long size = FileUtils.sizeOfDirectory(dataSetInStore);
                            log.log(LogLevel.INFO,
                                    dataSetInStore + " contains " + size + " bytes (calculated in "
                                            + (timeProvider.getTimeInMilliseconds() - t0)
                                            + " msec)");
                            service.updateShareIdAndSize(dataSetCode, shareId, size);
                            dataSet.setDataSetSize(size);
                        }
                        share.addDataSet(dataSet);
                    } else
                    {
                        log.log(LogLevel.WARN, "Data set " + dataSetCode
                                + " no longer exists in share " + shareId + ".");
                    }
                }
            }
        }
        List<Share> list = new ArrayList<Share>(shares.values());
        Collections.sort(list, SHARE_COMPARATOR);
        return list;
    }

    private static Map<String, Share> getShares(File storeRoot,
            IFreeSpaceProvider freeSpaceProvider, ISimpleLogger log)
    {
        Map<String, Share> shares = new HashMap<String, Share>();
        for (File file : getShares(storeRoot))
        {
            Share share = new ShareFactory().createShare(file, freeSpaceProvider, log);
            shares.put(share.getShareId(), share);
        }
        return shares;
    }

    /**
     * Moves the specified data set to the specified share. The data set is folder in the store its
     * name is the data set code. The destination folder is <code>share</code>. Its name is the
     * share id.
     * <p>
     * This method works as follows:
     * <ol>
     * <li>Copying data set to new share.
     * <li>Sanity check of successfully copied data set.
     * <li>Changing share id in openBIS AS.
     * <li>Deletes the data set at the old location after all locks on the data set have been
     * released.
     * </ol>
     * 
     * @param service to access openBIS AS.
     */
    public static void moveDataSetToAnotherShare(final File dataSetDirInStore, File share,
            IEncapsulatedOpenBISService service, final IShareIdManager shareIdManager,
            final ISimpleLogger logger)
    {
        final String dataSetCode = dataSetDirInStore.getName();
        ExternalData dataSet = service.tryGetDataSet(dataSetCode);
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set: " + dataSetCode);
        }
        shareIdManager.lock(dataSetCode);
        try
        {
            File oldShare =
                    dataSetDirInStore.getParentFile().getParentFile().getParentFile()
                            .getParentFile().getParentFile();
            String relativePath = FileUtilities.getRelativeFilePath(oldShare, dataSetDirInStore);
            if (share.getName().equals(oldShare.getName()))
            {
                return;
            }
            File dataSetDirInNewShare = new File(share, relativePath);
            dataSetDirInNewShare.mkdirs();
            copyToShare(dataSetDirInStore, dataSetDirInNewShare, logger);
            long size = assertEqualSizeAndChildren(dataSetDirInStore, dataSetDirInNewShare);
            String shareId = share.getName();
            service.updateShareIdAndSize(dataSetCode, shareId, size);
            shareIdManager.setShareId(dataSetCode, shareId);
        } finally
        {
            shareIdManager.releaseLock(dataSetCode);
        }
        deleteDataSet(dataSetCode, dataSetDirInStore, shareIdManager, logger);
    }

    /**
     * Deletes specified data set at specified location. This methods waits until any locks on the
     * specified data set have been released.
     */
    protected static void deleteDataSet(final String dataSetCode, final File dataSetDirInStore,
            final IShareIdManager shareIdManager, final ISimpleLogger logger)
    {
        logger.log(LogLevel.INFO, "Await for data set " + dataSetCode + " to be unlocked.");
        shareIdManager.await(dataSetCode);
        deleteDataSetInstantly(dataSetCode, dataSetDirInStore, logger);
    }

    /**
     * Deletes specified data set. This methods waits until any locks on the specified data set have
     * been released.
     */
    public static void deleteDataSet(final IDatasetLocation dataSet,
            final IDataSetDirectoryProvider dataSetDirectoryProvider,
            final IShareIdManager shareIdManager, final ISimpleLogger logger)
    {
        final String dataSetCode = dataSet.getDataSetCode();
        logger.log(LogLevel.INFO, "Await for data set " + dataSetCode + " to be unlocked.");
        shareIdManager.await(dataSetCode);
        File dataSetDirInStore = dataSetDirectoryProvider.getDataSetDirectory(dataSet);
        deleteDataSetInstantly(dataSetCode, dataSetDirInStore, logger);
    }

    /**
     * Deletes specified data set at specified location. This methods doesn't wait for any locks and
     * removes the data set instantly.
     */
    public static void deleteDataSetInstantly(final String dataSetCode,
            final File dataSetDirInStore, final ISimpleLogger logger)
    {
        logger.log(LogLevel.INFO, "Start deleting data set " + dataSetCode + " at "
                + dataSetDirInStore);
        boolean successful = FileUtilities.deleteRecursively(dataSetDirInStore);
        if (successful)
        {
            logger.log(LogLevel.INFO, "Data set " + dataSetCode + " at " + dataSetDirInStore
                    + " has been successfully deleted.");
        } else
        {
            logger.log(LogLevel.WARN, "Deletion of data set " + dataSetCode + " at "
                    + dataSetDirInStore + " failed.");
        }
    }

    /**
     * Deletes specified data set in the old share if it is already in the new one or in the new one
     * if it is still in the old one.
     * 
     * @param shareIdManager provides the current share.
     */
    public static void cleanUp(SimpleDataSetInformationDTO dataSet, File storeRoot,
            String newShareId, IShareIdManager shareIdManager, ISimpleLogger logger)
    {
        String dataSetCode = dataSet.getDataSetCode();
        String shareId = shareIdManager.getShareId(dataSetCode);
        String oldShareId = dataSet.getDataSetShareId();
        if (newShareId.equals(oldShareId))
        {
            logger.log(LogLevel.WARN, "No clean up will be performed because for data set "
                    + dataSetCode + " both shares are the same: " + oldShareId);
            return;
        }
        boolean currentIsOld = shareId.equals(oldShareId);
        boolean currentIsNew = shareId.equals(newShareId);
        if (currentIsOld == false && currentIsNew == false)
        {
            logger.log(LogLevel.WARN, "No clean up will be performed because data set "
                    + dataSetCode + " is neither in share " + oldShareId + " nor in share "
                    + newShareId + " but in share " + shareId + ".");
            return;
        }
        File shareFolder = new File(storeRoot, currentIsOld ? newShareId : oldShareId);
        String location = dataSet.getDataSetLocation();
        deleteDataSet(dataSetCode, new File(shareFolder, location), shareIdManager, logger);
    }

    private static void copyToShare(File file, File share, ISimpleLogger logger)
    {
        final long start = System.currentTimeMillis();
        logger.log(LogLevel.INFO, String.format("Start moving directory '%s' to new share '%s'",
                file.getPath(), share.getPath()));
        final RsyncCopier copier = new RsyncCopier(OSUtilities.findExecutable(RSYNC_EXEC));
        copier.copyContent(file, share);
        logger.log(LogLevel.INFO, String.format(
                "Finished moving directory '%s' to new share '%s' in %.2f s", file.getPath(),
                share.getPath(), (System.currentTimeMillis() - start) / 1000.0));
    }

    private static long assertEqualSizeAndChildren(File source, File destination)
    {
        assertSameName(source, destination);
        if (source.isFile())
        {
            assertFile(destination);
            return assertSameSizeAndCheckSum(source, destination);
        } else
        {
            assertDirectory(destination);
            File[] sourceFiles = getFiles(source);
            File[] destinationFiles = getFiles(destination);
            assertSameNumberOfChildren(source, sourceFiles, destination, destinationFiles);
            long sum = 0;
            for (int i = 0; i < sourceFiles.length; i++)
            {
                sum += assertEqualSizeAndChildren(sourceFiles[i], destinationFiles[i]);
            }
            return sum;
        }
    }

    private static void assertSameNumberOfChildren(File source, File[] sourceFiles,
            File destination, File[] destinationFiles)
    {
        if (sourceFiles.length != destinationFiles.length)
        {
            throw new EnvironmentFailureException("Destination directory '"
                    + destination.getAbsolutePath() + "' has " + destinationFiles.length
                    + " files but source directory '" + source.getAbsolutePath() + "' has "
                    + sourceFiles.length + " files.");
        }
    }

    private static long assertSameSizeAndCheckSum(File source, File destination)
    {
        long sourceSize = source.length();
        long destinationSize = destination.length();
        if (sourceSize != destinationSize)
        {
            throw new EnvironmentFailureException("Destination file '"
                    + destination.getAbsolutePath() + "' has size " + destinationSize
                    + " but source file '" + source.getAbsolutePath() + "' has size " + sourceSize
                    + ".");
        }
        long sourceChecksum = calculateCRC(source);
        long destinationChecksum = calculateCRC(destination);
        if (sourceChecksum != destinationChecksum)
        {
            throw new EnvironmentFailureException("Destination file '"
                    + destination.getAbsolutePath() + "' has checksum " + destinationChecksum
                    + " but source file '" + source.getAbsolutePath() + "' has checksum "
                    + sourceChecksum + ".");
        }
        return sourceSize;
    }

    private static long calculateCRC(File file)
    {
        try
        {
            return FileUtils.checksumCRC32(file);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private static void assertSameName(File source, File destination)
    {
        if (source.getName().equals(destination.getName()) == false)
        {
            throw new EnvironmentFailureException("Destination file '"
                    + destination.getAbsolutePath() + "' has a different name than source file '"
                    + source.getAbsolutePath() + ".");
        }
    }

    private static void assertFile(File file)
    {
        if (file.exists() == false)
        {
            throw new EnvironmentFailureException("File does not exist: " + file.getAbsolutePath());
        }
        if (file.isFile() == false)
        {
            throw new EnvironmentFailureException("File is a directory: " + file.getAbsolutePath());
        }
    }

    private static void assertDirectory(File file)
    {
        if (file.exists() == false)
        {
            throw new EnvironmentFailureException("Directory does not exist: "
                    + file.getAbsolutePath());
        }
        if (file.isDirectory() == false)
        {
            throw new EnvironmentFailureException("Directory is a file: " + file.getAbsolutePath());
        }
    }

    private static File[] getFiles(File file)
    {
        File[] files = file.listFiles();
        Arrays.sort(files);
        return files;
    }

}
