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

package ch.systemsx.cisd.cina.dss.info;

import java.io.File;

import javax.activation.DataHandler;

import ch.systemsx.cisd.cina.dss.info.FolderOracle.FolderType;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Create the subject and content for an email that is sent when an experiment or sample is
 * registered.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class EntityRegistrationSuccessEmail
{
    private static final String MARKER_FILE_MIME_TYPE = "application/octet-stream";

    private final DataSetInformation dataSetInformation;

    private final EntityEmailDetails entityEmailDetails;

    private static final String EMAIL_SUBJECT_PREFIX = "[CINA]";

    private static final String SAMPLE_CODE_PREFIX = "S";

    public EntityRegistrationSuccessEmail(DataSetInformation dataSetInformation,
            File incomingDataSetDirectory)
    {
        this.dataSetInformation = dataSetInformation;

        FolderType folderType = new FolderOracle().getTypeForFolder(incomingDataSetDirectory);
        switch (folderType)
        {
            case DATA_SET:
                entityEmailDetails = new DatasetEmailDetails();
                break;
            case EXPERIMENT:
                entityEmailDetails = new ExperimentEmailDetails();
                break;
            case SAMPLE:
                entityEmailDetails = new SampleEmailDetails();
                break;
            case UNKNOWN:
                entityEmailDetails = new UnknownEmailDetails();
                break;
            default:
                entityEmailDetails = new UnknownEmailDetails();
                break;
        }
    }

    /**
     * Return true if the incoming data set requires that an email be sent on registration.
     */
    public boolean shouldSendEmail()
    {
        return entityEmailDetails.shouldSendEmail();
    }

    /**
     * Return the subject of the email
     */
    public String getSubject()
    {
        StringBuffer subject = new StringBuffer();
        subject.append(EMAIL_SUBJECT_PREFIX);
        subject.append(" Registered ");
        entityEmailDetails.appendEntityName(subject);
        subject.append(" ");
        entityEmailDetails.appendSubjectDetails(subject);
        return subject.toString();
    }

    /**
     * Return the body of the email for a text-only (no attachments) email. Used if the mail client
     * does no support sending attachments.
     */
    public String getContentTextOnly()
    {
        StringBuffer content = new StringBuffer();
        entityEmailDetails.appendEntityName(content);
        content.append(" was successfully registered. Use the following metadata file to register");
        entityEmailDetails.appendFollowOnEntityName(content);
        content.append(":\n");
        content.append("------");
        entityEmailDetails.appendMetadataFileName(content);
        content.append("------");
        content.append("\n");
        entityEmailDetails.appendMetadataFileContent(content);

        return content.toString();
    }

    /**
     * Return the body of the email for a MIME email. In a MIME email, the marker file is sent as a
     * file attachment, not part of the text body of the email.
     */
    public String getContentMimeText()
    {
        StringBuffer content = new StringBuffer();
        entityEmailDetails.appendEntityName(content);
        content.append(" was successfully registered. Use the attached metadata file to register ");
        entityEmailDetails.appendFollowOnEntityName(content);

        return content.toString();
    }

    /**
     * Return the file name of the MIME file attachment.
     */
    public String getContentMimeAttachmentFileName()
    {
        StringBuffer content = new StringBuffer();
        entityEmailDetails.appendMetadataFileName(content);

        return content.toString();
    }

    public DataHandler getContentMimeAttachmentContent()
    {
        StringBuffer content = new StringBuffer();
        entityEmailDetails.appendMetadataFileContent(content);
        DataHandler attachment = new DataHandler(content.toString(), MARKER_FILE_MIME_TYPE);

        return attachment;
    }

    private abstract class EntityEmailDetails
    {
        boolean shouldSendEmail()
        {
            return false;
        }

        abstract void appendEntityName(StringBuffer sb);

        void appendFollowOnEntityName(StringBuffer sb)
        {
        }

        void appendSubjectDetails(StringBuffer subject)
        {
        }

        void appendMetadataFileName(StringBuffer sb)
        {
        }

        void appendMetadataFileContent(StringBuffer sb)
        {

        }
    }

    private class ExperimentEmailDetails extends EntityEmailDetails
    {
        @Override
        boolean shouldSendEmail()
        {
            return true;
        }

        @Override
        void appendEntityName(StringBuffer sb)
        {
            sb.append("Experiment");
        }

        @Override
        void appendFollowOnEntityName(StringBuffer sb)
        {
            sb.append("Samples");
        }

        @Override
        void appendSubjectDetails(StringBuffer subject)
        {
            subject.append(dataSetInformation.getExperimentIdentifier());
        }

        @Override
        void appendMetadataFileContent(StringBuffer sb)
        {
            sb.append(SampleMetadataExtractor.EXPERIMENT_IDENTIFIER_KEY);
            sb.append("=");
            sb.append(dataSetInformation.getExperimentIdentifier());
            sb.append("\n");

            sb.append(SampleMetadataExtractor.EXPERIMENT_OWNER_EMAIL_KEY);
            sb.append("=");
            sb.append(dataSetInformation.tryGetUploadingUserEmail());
            sb.append("\n");

            sb.append(SampleMetadataExtractor.SAMPLE_CODE_PREFIX_KEY);
            sb.append("=");
            sb.append(SAMPLE_CODE_PREFIX);
            sb.append("\n");
        }

        @Override
        void appendMetadataFileName(StringBuffer sb)
        {
            sb.append(FolderOracle.SAMPLE_METADATA_FILENAME);

        }
    }

    private class SampleEmailDetails extends EntityEmailDetails
    {
        @Override
        boolean shouldSendEmail()
        {
            return true;
        }

        @Override
        void appendEntityName(StringBuffer sb)
        {
            sb.append("Sample");
        }

        @Override
        void appendFollowOnEntityName(StringBuffer sb)
        {
            sb.append("Data Sets");
        }

        @Override
        void appendSubjectDetails(StringBuffer subject)
        {
            subject.append(dataSetInformation.getSampleIdentifier());
        }

        @Override
        void appendMetadataFileContent(StringBuffer sb)
        {
        }

        @Override
        void appendMetadataFileName(StringBuffer sb)
        {
            sb.append(FolderOracle.DATA_SET_METADATA_FILENAME);

        }
    }

    private class DatasetEmailDetails extends EntityEmailDetails
    {
        @Override
        void appendEntityName(StringBuffer sb)
        {
            sb.append("Data Set");
        }

        @Override
        void appendSubjectDetails(StringBuffer subject)
        {
            subject.append(dataSetInformation.getDataSetCode());
        }
    }

    private class UnknownEmailDetails extends EntityEmailDetails
    {
        @Override
        void appendEntityName(StringBuffer sb)
        {
            sb.append("Unknown");
        }
    }
}
