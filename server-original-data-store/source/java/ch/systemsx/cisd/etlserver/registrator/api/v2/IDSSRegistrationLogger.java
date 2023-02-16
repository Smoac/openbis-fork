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
package ch.systemsx.cisd.etlserver.registrator.api.v2;

/**
 * Logging interface exposed through a dropbox api.
 * 
 * @author Jakub Straszewski
 */
public interface IDSSRegistrationLogger
{
    public void info(String message);

    public void warn(String message);

    public void error(String message);

    public void info(String message, Throwable ex);

    public void warn(String message, Throwable ex);

    public void error(String message, Throwable ex);
}
