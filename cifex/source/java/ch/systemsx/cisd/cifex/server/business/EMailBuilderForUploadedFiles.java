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

package ch.systemsx.cisd.cifex.server.business;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * Class which creates and sends an e-mail informing someone that some files are available for
 * download.
 * 
 * @author Franz-Josef Elmer
 */
public class EMailBuilderForUploadedFiles extends AbstractEMailBuilder
{
    private static final String FILE_FOR_DOWNLOAD_SUBJECT_LINE = "file-for-download-subject";

    private static final String FILE_FOR_DOWNLOAD_EMAIL_TEMPLATE_FILE_NAME =
            "etc/file-for-download-email.template";

    private final List<FileDTO> files = new ArrayList<FileDTO>();

    private String userCode;

    /**
     * Creates an new instance for the specified mail client, registrator of the new files, and the
     * email of the recipient.
     */
    public EMailBuilderForUploadedFiles(IMailClient mailClient, UserDTO registrator, String email)
    {
        super(mailClient, registrator, email);
    }

    /**
     * Adds a file. At least one file has to be added before sending the e-mail.
     */
    public void addFile(FileDTO file)
    {
        files.add(file);
    }

    /**
     * Sets the user name. Has to be set before sending the e-mail.
     */
    public final void setUserCode(String userCode)
    {
        this.userCode = userCode;
    }

    @Override
    protected String createContent()
    {
        assert files.size() > 0 : "No files to upload.";
        assert userCode != null : "Missing user code";

        return createContent(FILE_FOR_DOWNLOAD_EMAIL_TEMPLATE_FILE_NAME);
    }

    private StringBuilder addFileSection(StringBuilder builder)
    {
        for (final FileDTO file : files)
        {
            builder.append(file.getName()).append("  ");
            addURL(builder, Constants.FILE_ID_PARAMETER, file.getID(),
                    Constants.USERCODE_PARAMETER, userCode);
            builder.append("\n");
        }
        return builder;
    }

    @Override
    protected Date tryGetExpirationDate()
    {
        Date minExpirationDate = new Date(Long.MAX_VALUE);
        for (final FileDTO file : files)
        {
            Date expirationDate = file.getExpirationDate();
            if (expirationDate.getTime() < minExpirationDate.getTime())
            {
                minExpirationDate = expirationDate;
            }
        }
        return minExpirationDate;
    }

    @Override
    protected String createSubject()
    {
        assert files.size() > 0 : "No files to upload.";
        return StringUtils.capitalize(emailDict.get(FILE_FOR_DOWNLOAD_SUBJECT_LINE));
    }

    @Override
    protected String getUserCode()
    {
        return userCode;
    }

    @Override
    protected void addToDict(Properties emailProps, DateFormat dateFormat)
    {
        if (files.size() == 1)
        {
            emailDict.put("one-or-more-files", emailProps.getProperty("one-file"));
            emailDict.put("one-or-more-files2", emailProps.getProperty("one-file2"));
            emailDict.put("one-or-more-files3", emailProps.getProperty("one-file3"));
        } else
        {
            emailDict.put("one-or-more-files", emailProps.getProperty("multiple-files"));
            emailDict.put("one-or-more-files2", emailProps.getProperty("multiple-files2"));
            emailDict.put("one-or-more-files3", emailProps.getProperty("multiple-files3"));
        }
        emailDict.put("file-section", addFileSection(new StringBuilder()).toString());
    }
}
