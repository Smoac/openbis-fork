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

    static final String DATA_COMPLETED_SCRIPT = "data-completed-script";

    static final String DATA_COMPLETED_SCRIPT_TIMEOUT = "data-completed-script-timeout";

    static final String CLEANSING_REGEX = "cleansing-regex";

    /** The local directory where we create additional copy of the incoming data. */
    static final String EXTRA_COPY_DIR = "extra-copy-dir";

    /**
     * Time period (in seconds) to wait after a failure has occurred before the operation is
     * re-tried.
     */
    static final String FAILURE_INTERVAL = "failure-interval";

    static final String HARD_LINK_EXECUTABLE = "hard-link-executable";

    static final String INCOMING_HOST_FIND_EXECUTABLE = "incoming-host-find-executable";

    static final String INCOMING_HOST_LASTCHANGED_EXECUTABLE =
            "incoming-host-lastchanged-executable";

    static final String OUTGOING_HOST_FIND_EXECUTABLE = "outgoing-host-find-executable";

    static final String OUTGOING_HOST_LASTCHANGED_EXECUTABLE =
            "outgoing-host-lastchanged-executable";

    static final String INACTIVITY_PERIOD = "inactivity-period";

    /**
     * The directory to monitor for new files and directories to move to outgoing.
     */
    static final String INCOMING_TARGET = "incoming-target";

    /** The local directory to store paths that need manual intervention. */
    static final String MANUAL_INTERVENTION_DIR = "manual-intervention-dir";

    static final String MANUAL_INTERVENTION_REGEX = "manual-intervention-regex";

    /**
     * Maximal number of re-tries of a failed operation before giving up on it.
     */
    static final String MAX_RETRIES = "max-retries";

    /** The remote directory (or host and directory) to move the data to. */
    static final String OUTGOING_TARGET = "outgoing-target";

    static final String PREFIX_FOR_INCOMING = "prefix-for-incoming";

    static final String QUIET_PERIOD = "quiet-period";

    static final String RSYNC_EXECUTABLE = "rsync-executable";

    static final String INCOMING_HOST_RSYNC_EXECUTABLE = "incoming-host-rsync-executable";

    static final String OUTGOING_HOST_RSYNC_EXECUTABLE = "outgoing-host-rsync-executable";

    static final String RSYNC_OVERWRITE = "rsync-overwrite";

    static final String USE_RSYNC_FOR_EXTRA_COPIES = "use-rsync-for-extra-copies";

    static final String SSH_EXECUTABLE = "ssh-executable";

    static final String TRANSFER_FINISHED_EXECUTABLE = "transfer-finished-executable";

    static final String TREAT_INCOMING_AS_REMOTE = "treat-incoming-as-remote";

    static final String SKIP_ACCESSIBILITY_TEST_ON_INCOMING = "skip-accessibility-test-on-incoming";

    static final String SKIP_ACCESSIBILITY_TEST_ON_OUTGOING = "skip-accessibility-test-on-outgoing";

    private PropertyNames()
    {
        // This class can not be instantiated.
    }
}
