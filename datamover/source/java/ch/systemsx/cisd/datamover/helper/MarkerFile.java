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

package ch.systemsx.cisd.datamover.helper;

import java.io.File;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Manipulations on marker files.
 * 
 * @author Tomasz Pylak on Aug 27, 2007
 */
public class MarkerFile
{

    public static String getCopyFinishedMarkerName(String originalFileName)
    {
        return Constants.IS_FINISHED_PREFIX + originalFileName;
    }

    public static String getDeletionInProgressMarkerName(File originalFile)
    {
        return Constants.DELETION_IN_PROGRESS_PREFIX + originalFile.getName();
    }

    public static String getDeletionInProgressMarkerName(String originalFileName)
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
        return new File(originalFile.getParent(), getCopyFinishedMarkerName(originalFile.getName()));
    }

}
