/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.server.shuffling;

import java.util.Properties;

/**
 * Abstract super class of all implementations of {@link IPostRegistrationTask}.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractPostRegistrationTask implements IPostRegistrationTask
{
    protected final Properties properties;

    protected final IEncapsulatedOpenBISService service;

    public AbstractPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this.properties = properties;
        this.service = service;
    }

    @Override
    public void clearCache()
    {
    }

}