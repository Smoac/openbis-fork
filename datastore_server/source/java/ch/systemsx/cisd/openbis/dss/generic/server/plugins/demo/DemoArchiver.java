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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Piotr Buczek
 */
public class DemoArchiver extends AbstractArchiverProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    public DemoArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    @Override
    protected void archive(DatasetDescription dataset) throws UserFailureException
    {
        System.out.println("Archived: " + dataset);
    }

    @Override
    protected void unarchive(DatasetDescription dataset) throws UserFailureException
    {
        System.out.println("Unarchived: " + dataset);
    }

}
