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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.utilities.UnicodeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.parser.GlobalProperties;
import ch.systemsx.cisd.openbis.generic.shared.parser.GlobalPropertiesLoader;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetControlFileProcessor extends AbstractSampleAndDataSetProcessor
{
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

        public SampleType trySampleType()
        {
            String sampleTypeCode =
                    properties
                            .get(SampleAndDataSetRegistrationHandler.SAMPLE_TYPE_CONTROL_FILE_KEY);
            if (null == sampleTypeCode)
            {
                return null;
            }
            SampleType sampleType = new SampleType();
            sampleType.setCode(sampleTypeCode);
            return sampleType;
        }

        public DataSetType tryDataSetType()
        {
            String dataSetTypeCode =
                    properties
                            .get(SampleAndDataSetRegistrationHandler.DATA_SET_TYPE_CONTROL_FILE_KEY);
            if (null == dataSetTypeCode)
            {
                return null;
            }
            DataSetType dataSetType = new DataSetType();
            dataSetType.setCode(dataSetTypeCode);
            return dataSetType;
        }

        public String tryUserString()
        {
            return properties.get(SampleAndDataSetRegistrationHandler.USER_CONTROL_FILE_KEY);
        }
    }

    /**
     * Utility class for accessing the properties defined in a control file.
     * <p>
     * After instantiating this object, you should call check validity to ensure that none of the
     * methods return null. If the object is invalid, you should check for null values, otherwise
     * all methods will return non-null values unless otherwise noted.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class ControlFileRegistrationProperties
    {
        private final ControlFileOverrideProperties overrideProperties;

        private final SampleAndDataSetRegistrationGlobalState globalProperties;

        private final Person user;

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

            String userIdOrEmail = overrideProperties.tryUserString();
            if (null == userIdOrEmail)
            {
                user = null;
            } else
            {
                user =
                        globalProperties.getOpenbisService().tryPersonWithUserIdOrEmail(
                                userIdOrEmail);
            }
        }

        /**
         * Returns a space identifier. Call checkValidity first.
         */
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

        public DataSetType getDataSetType()
        {
            DataSetType dataSetType = overrideProperties.tryDataSetType();
            if (null == dataSetType)
            {
                dataSetType = globalProperties.tryDataSetType();
            }
            return dataSetType;
        }

        public Person getUser()
        {
            return user;
        }

        public void checkValidity()
        {
            SpaceIdentifier spaceIdentifier = getSpaceIdentifier();
            SampleType sampleType = getSampleType();
            DataSetType dataSetType = getDataSetType();
            Person theUser = getUser();

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
            if (null == dataSetType)
            {
                hasError = true;
                sb.append("\tNo default data set type has been specified, and no data set type was specified in the control file");
            }
            if (null == theUser)
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
        super(globalState, folder);
        this.controlFile = controlFile;
    }

    /**
     * Register the entities defined in the control file. Collect all the errors that happen and
     * notify the user. If the file itself has errors, then throw an exception to the parent.
     */
    public void register() throws UserFailureException, EnvironmentFailureException,
            FileNotFoundException
    {
        ControlFileOverrideProperties overrideProperties =
                new ControlFileOverrideProperties(controlFile);

        logControlFileOverridePropertiesExtracted(overrideProperties);

        ControlFileRegistrationProperties properties =
                new ControlFileRegistrationProperties(overrideProperties, globalState);

        BisTabFileLoader<SampleDataSetPair> controlFileLoader =
                new BisTabFileLoader<SampleDataSetPair>(
                        SampleDataSetPairParserObjectFactory.createFactoryFactory(
                                properties.getSampleType(), properties.getDataSetType()), false);

        List<SampleDataSetPair> loadedSampleDataSetPairs = null;

        try
        {
            properties.checkValidity();
            Reader reader = UnicodeUtils.createReader(new FileInputStream(controlFile));
            loadedSampleDataSetPairs =
                    controlFileLoader.load(new DelegatedReader(reader, controlFile.getName()));
        } catch (UserFailureException e)
        {
            // If we don't know which user to send the email to, don't handle this error -- leave it
            // to higher levels.
            if (null == properties.getUser())
            {
                throw e;
            }

            sendEmailWithErrorMessage(properties, e.getMessage());
            return;
        }

        // If we are here, we have sucessfuly parsed the file
        for (SampleDataSetPair sampleDataSet : loadedSampleDataSetPairs)
        {
            SampleAndDataSetRegistrator registrator =
                    new SampleAndDataSetRegistrator(globalState, sampleDataSet);
            registrator.register();
        }

    }

    private void logControlFileOverridePropertiesExtracted(ControlFileOverrideProperties properties)
    {
        String message =
                String.format(
                        "Global properties extracted from file '%s': SAMPLE_TYPE(%s) DEFAULT_SPACE(%s) USER(%s)",
                        controlFile.getName(), properties.trySampleType(),
                        properties.trySpaceCode(), properties.tryUserString());
        logInfo(message);
    }

    /**
     * Send an email message to the person who uploaded the file. This method is only called if we
     * have a valid email address to contact. Otherwise, errors are forwarded to a higher level for
     * handling.
     */
    private void sendEmailWithErrorMessage(ControlFileRegistrationProperties properties,
            String message)
    {
        // Log it
        logError(message);

        // Create an email and send it.
        try
        {
            String subject = createErrorEmailSubject();
            String content = createErrorEmailContent();
            String filename = SampleAndDataSetFolderProcessor.ERRORS_FILENAME;
            EMailAddress recipient = new EMailAddress(properties.getUser().getEmail());
            DataSource dataSource = new ByteArrayDataSource(message, "text/plain");

            globalState.getMailClient().sendEmailMessageWithAttachment(subject, content, filename,
                    new DataHandler(dataSource), null, null, recipient);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private String createErrorEmailSubject()
    {
        return String.format("Sample / Data Set Registration Error -- %s", controlFile);
    }

    private String createErrorEmailContent()
    {
        return String
                .format("When trying to register the samples and data sets specified in the control file, %s, errors were encountered. These errors are detailed in the attachment.",
                        controlFile);
    }
}
