/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.transformation;

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.compression.tiff.TiffCompressor;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * {@link ITransformator} that compresses TIFF files.
 * 
 * @author Piotr Buczek
 */
public class TiffCompressorTransformator implements ITransformator
{

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, TiffCompressorTransformator.class);

    public Status transform(File path)
    {
        // NOTE:
        // TiffCompressor performs compression in-place and doesn't change files that are already
        // compressed so there is no additional recovery mechanism needs to be implemented.

        String errorMsgOrNull;
        try
        {
            errorMsgOrNull = TiffCompressor.compress(path.getAbsolutePath());
            if (errorMsgOrNull != null)
            {
                notificationLog.error(errorMsgOrNull);
            }
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
            return Status.createError("Tiff compression was interrupted:" + ex.getMessage());
        } catch (EnvironmentFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }

        // Even if compression of some files failed they can still be moved further.
        return Status.OK;
    }
}
