/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.resource;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author pkupczyk
 */
public class Resources
{

    private Logger logger;

    public Set<IReleasable> resources = new LinkedHashSet<IReleasable>();

    public Resources(Logger logger)
    {
        this.logger = logger;
    }

    public void add(IReleasable resource)
    {
        resources.add(resource);
    }

    public void release()
    {
        if (resources.size() > 0)
        {
            for (IReleasable resource : resources)
            {
                try
                {
                    resource.release();

                    if (logger != null)
                    {
                        logger.debug("Successfully released a resource: " + resource);
                    }

                } catch (Exception e)
                {
                    if (logger != null)
                    {
                        logger.debug("Couldn't release a resource: " + resource, e);
                    }
                }
            }
        } else
        {
            if (logger != null)
            {
                logger.debug("Didn't have to release any resources");
            }
        }
    }

    public void clear()
    {
        resources.clear();
    }

}
