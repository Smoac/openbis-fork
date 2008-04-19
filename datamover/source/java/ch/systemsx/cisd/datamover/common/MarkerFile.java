/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.common;

import java.io.File;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.StoreItem;

/**
 * Manipulations on marker files. Should not use string types in the interface.
 * 
 * @author Tomasz Pylak
 */
public class MarkerFile
{

    private static String getCopyFinishedMarkerName(String originalFileName)
    {
        return Constants.IS_FINISHED_PREFIX + originalFileName;
    }

    public static StoreItem createDeletionInProgressMarker(StoreItem originalItem)
    {
        return new StoreItem(getDeletionInProgressMarkerName(originalItem.getName()));
    }

    private static String getDeletionInProgressMarkerName(String originalFileName)
    {
        return Constants.DELETION_IN_PROGRESS_PREFIX + originalFileName;
    }

    public static boolean isCopyFinishedMarker(File file)
    {
        return file.getName().startsWith(Constants.IS_FINISHED_PREFIX);
    }

    public static boolean isDeletionInProgressMarker(File file)
    {
        return file.getName().startsWith(Constants.DELETION_IN_PROGRESS_PREFIX);
    }

    public static File extractOriginalFromCopyFinishedMarker(File markerFile)
    {
        assert isCopyFinishedMarker(markerFile);
        return FileUtilities.removePrefixFromFileName(markerFile, Constants.IS_FINISHED_PREFIX);
    }

    public static File createCopyFinishedMarker(File originalFile)
    {
        return createCopyFinishedMarker(originalFile.getParentFile(), originalFile.getName());
    }

    public static File createCopyFinishedMarker(File parent, String originalFileName)
    {
        return new File(parent, getCopyFinishedMarkerName(originalFileName));
    }

    public static StoreItem createCopyFinishedMarker(StoreItem originalItem)
    {
        return new StoreItem(getCopyFinishedMarkerName(originalItem.getName()));
    }

    public static StoreItem createRequiresDeletionBeforeCreationMarker()
    {
        return new StoreItem(".requiresDeletionBeforeCreation");
    }
}
