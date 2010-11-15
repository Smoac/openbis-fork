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

package ch.systemsx.cisd.datamover;

/**
 * Global constants used in datamover. 
 *
 * @author Bernd Rinn
 */
public class DatamoverConstants
{
    static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";
    
    public static final String RSYNC_PASSWORD_FILE_INCOMING = "etc/rsync_incoming.passwd"; 

    public static final String RSYNC_PASSWORD_FILE_OUTGOING = "etc/rsync_outgoing.passwd";

    public static final int IGNORED_ERROR_COUNT_BEFORE_NOTIFICATION = 3;
    
    /** A remote connection must not take longer than 20s to be established. */
    public static final long TIMEOUT_REMOTE_CONNECTION_MILLIS = 20 * 1000L;
    
}
