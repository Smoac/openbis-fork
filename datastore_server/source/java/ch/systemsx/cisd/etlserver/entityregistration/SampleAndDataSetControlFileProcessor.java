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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.parser.GlobalProperties;
import ch.systemsx.cisd.openbis.generic.shared.parser.GlobalPropertiesLoader;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetControlFileProcessor
{
    private final SampleAndDataSetRegistrationGlobalState globalState;

    @SuppressWarnings("unused")
    private final File folder;

    private final File controlFile;

    @SuppressWarnings("unused")
    private final HashMap<File, Exception> errorMap = new HashMap<File, Exception>();

    /**
     * Utility class for accessing the properties defined in a control file.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class ControlFileOverrideProperties
    {
        private final GlobalProperties properties;

        /**
         * Creates a wrapper on the properties. The FileNotFoundExcecption cannot happen in
         * practice, since we've already verified that the file exists.
         * 
         * @param controlFile
         * @throws FileNotFoundException
         */
        ControlFileOverrideProperties(File controlFile) throws FileNotFoundException
        {
            properties = GlobalPropertiesLoader.load(controlFile);
        }

        public String trySpaceCode()
        {
            return properties.get(SampleAndDataSetRegistrationHandler.DATA_SPACE_CONTROL_FILE_KEY);
        }

        public SpaceIdentifier trySpaceIdentifier()
        {
            String spaceCode = trySpaceCode();
            if (null == spaceCode)
            {
                return null;
            }
            return new SpaceIdentifier(DatabaseInstanceIdentifier.createHome(), spaceCode);
        }

        public String trySampleTypeCode()
        {
            return properties.get(SampleAndDataSetRegistrationHandler.SAMPLE_TYPE_CONTROL_FILE_KEY);
        }

        public SampleType trySampleType()
        {
            String sampleTypeCode = trySampleTypeCode();
            if (null == sampleTypeCode)
            {
                return null;
            }
            SampleType sampleType = new SampleType();
            sampleType.setCode(sampleTypeCode);
            return sampleType;
        }

        public String tryUserString()
        {
            return properties.get(SampleAndDataSetRegistrationHandler.USER_CONTROL_FILE_KEY);
        }
    }

    /**
     * Utility class for accessing the properties defined in a control file.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class ControlFileRegistrationProperties
    {
        private final ControlFileOverrideProperties overrideProperties;

        private final SampleAndDataSetRegistrationGlobalState globalProperties;

        /**
         * An object that gets the definitive properties for registration. The properties are
         * created by taking overrides when available, and defaults otherwise.
         */
        ControlFileRegistrationProperties(ControlFileOverrideProperties overrideProperties,
                SampleAndDataSetRegistrationGlobalState globalProperties)
                throws FileNotFoundException
        {
            this.overrideProperties = overrideProperties;
            this.globalProperties = globalProperties;
        }

        public SpaceIdentifier getSpaceIdentifier()
        {
            SpaceIdentifier spaceIdentifier = overrideProperties.trySpaceIdentifier();
            if (null == spaceIdentifier)
            {
                spaceIdentifier = globalProperties.trySpaceIdentifier();
            }
            return spaceIdentifier;
        }

        public SampleType getSampleType()
        {
            SampleType sampleType = overrideProperties.trySampleType();
            if (null == sampleType)
            {
                sampleType = globalProperties.trySampleType();
            }
            return sampleType;
        }

        public String getUserString()
        {
            return overrideProperties.tryUserString();
        }

        public void checkValidity()
        {
            SpaceIdentifier spaceIdentifier = getSpaceIdentifier();
            SampleType sampleType = getSampleType();
            String user = getUserString();

            StringBuilder sb = new StringBuilder();
            boolean hasError = false;
            if (null == spaceIdentifier)
            {
                hasError = true;
                sb.append("\tNo default space identifier has been specified, and no space identifier was specified in the control file.");
            }
            if (null == sampleType)
            {
                hasError = true;
                sb.append("\tNo default sample type has been specified, and no sample type was specified in the control file");
            }
            if (null == user)
            {
                hasError = true;
                sb.append("\tNo user has been specified");
            }
            if (hasError)
            {
                throw new UserFailureException(sb.toString());
            }
        }
    }

    SampleAndDataSetControlFileProcessor(SampleAndDataSetRegistrationGlobalState globalState,
            File folder, File controlFile)
    {
        this.globalState = globalState;
        this.folder = folder;
        this.controlFile = controlFile;
    }

    /**
     * Register the entities defined in the control file. Collect all the errors that happen and
     * notify the user. If the file itself has errors, then throw an exception to the parent.
     */
    public void register() throws UserFailureException, EnvironmentFailureException,
            FileNotFoundException
    {
        ControlFileOverrideProperties overrideProperties;
        overrideProperties = new ControlFileOverrideProperties(controlFile);

        logControlFileOverridePropertiesExtracted(overrideProperties);

        ControlFileRegistrationProperties properties =
                new ControlFileRegistrationProperties(overrideProperties, globalState);

        try
        {
            properties.checkValidity();
        } catch (UserFailureException e)
        {
            // If we don't know which user to send the email to, don't handle this error -- leave it
            // to higher levels.
            if (null == properties.getUserString())
            {
                throw e;
            }

            sendEmailWithErrorMessage(e.getMessage());
            return;
        }

    }

    private void logControlFileOverridePropertiesExtracted(ControlFileOverrideProperties properties)
    {
        String message =
                String.format(
                        "Global properties extracted from file '%s': SAMPLE_TYPE(%s) DEFAULT_SPACE(%s) USER(%s)",
                        controlFile.getName(), properties.trySampleType(),
                        properties.trySpaceCode(), properties.tryUserString());
        globalState.getOperationLog().debug(message);
    }

    private void sendEmailWithErrorMessage(String message)
    {
        // Log it
        globalState.getOperationLog().error(message);

        // Create an email and send it.
    }
}
