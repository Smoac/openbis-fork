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
 * A static class containing all the property names found in the <code>service.properties</code>.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyNames
{

    /**
     * The directory for local files and directories manipulations.
     */
    static final String BUFFER_DIR = "buffer-dir";

    static final String CHECK_INTERVAL = "check-interval";

    static final String CHECK_INTERVAL_INTERNAL = "check-interval-internal";

    static final String CLEANSING_REGEX = "cleansing-regex";

    /** The local directory where we create additional copy of the incoming data. */
    static final String EXTRA_COPY_DIR = "extra-copy-dir";

    static final String FAILURE_INTERVAL = "failure-interval";

    static final String HARD_LINK_EXECUTABLE = "hard-link-executable";

    static final String INACTIVITY_PERIOD = "inactivity-period";

    /**
     * The directory to monitor for new files and directories to move to outgoing.
     */
    static final String INCOMING_DIR = "incoming-dir";

    static final String INCOMING_HOST = "incoming-host";

    /** The local directory to store paths that need manual intervention. */
    static final String MANUAL_INTERVENTION_DIR = "manual-intervention-dir";

    static final String MANUAL_INTERVENTION_REGEX = "manual-intervention-regex";

    static final String MAX_RETRIES = "max-retries";

    /** The remote directory to move the data to. */
    static final String OUTGOING_DIR = "outgoing-dir";

    static final String OUTGOING_HOST = "outgoing-host";

    static final String PREFIX_FOR_INCOMING = "prefix-for-incoming";

    static final String QUIET_PERIOD = "quiet-period";

    static final String RSYNC_EXECUTABLE = "rsync-executable";

    static final String RSYNC_OVERWRITE = "rsync-overwrite";

    static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    static final String SSH_EXECUTABLE = "ssh-executable";

    static final String TREAT_INCOMING_AS_REMOTE = "treat-incoming-as-remote";

    private PropertyNames()
    {
        // This class can not be instantiated.
    }
}
