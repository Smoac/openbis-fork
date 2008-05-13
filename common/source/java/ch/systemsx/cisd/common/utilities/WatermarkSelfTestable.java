/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.io.IOException;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.WatermarkWatcher.WatermarkState;

/**
 * An <code>ISelfTestable</code> implementation based on {@link WatermarkWatcher}.
 * 
 * @author Christian Ribeaud
 */
public final class WatermarkSelfTestable implements ISelfTestable
{

    private final File path;

    private final WatermarkWatcher watermarkWatcher;

    public WatermarkSelfTestable(final File path, final WatermarkWatcher watermarkWatcher)
    {
        this.path = path;
        this.watermarkWatcher = watermarkWatcher;
    }

    //
    // ISelfTestable
    //

    public final void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        try
        {
            final WatermarkState watermarkState = watermarkWatcher.getWatermarkState(path);
            if (WatermarkWatcher.isBelow(watermarkState))
            {
                final String freeSpaceDisplayed =
                        WatermarkWatcher.displayKilobyteValue(watermarkState.getFreeSpace());
                final String watermarkDisplayed =
                        WatermarkWatcher.displayKilobyteValue(watermarkState.getWatermark());
                throw ConfigurationFailureException.fromTemplate(
                        "Free space (%s) lies below given watermark (%s).", freeSpaceDisplayed,
                        watermarkDisplayed);
            }
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException(ex.getMessage());
        }
    }
}