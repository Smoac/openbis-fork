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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private static final MessageFormat EXPIRATION_TEMPLATE =
            new MessageFormat("\nExpiration: Files will be removed  " + DATE_TEMPLATE);

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

        StringBuilder builder = new StringBuilder();
        addGreeting(builder);
        builder.append(getLongRegistratorDescription());
        builder.append(" has stored ").append(createFileText());
        builder.append(" on our server for you to download. File information appears below.\n\n");
        addRegistratorDetails(builder);
        builder.append("\n\n").append(StringUtils.capitalize(createFileText())).append(
                " (click to download):\n\n");
        Date minExpirationDate = new Date(Long.MAX_VALUE);
        for (final FileDTO file : files)
        {
            builder.append(file.getName()).append(" ");
            builder.append(url).append("/index.html");
            appendURLParam(builder, Constants.FILE_ID_PARAMETER, file.getID(), true);
            appendURLParam(builder, Constants.USERCODE_PARAMETER, userCode, false);
            // Append line separator as String as not as Character. On MacOS with Entourage I got a
            // not-so-correctly
            // formatted email.
            builder.append("\n");
            Date expirationDate = file.getExpirationDate();
            if (expirationDate.getTime() < minExpirationDate.getTime())
            {
                minExpirationDate = expirationDate;
            }
        }
        builder.append(EXPIRATION_TEMPLATE.format(new Object[]
            { minExpirationDate }));
        builder.append("\n");
        if (password != null)
        {
            builder.append("\nFor downloading you have to enter the following password: ").append(
                    password);
            builder.append("\n");
        }
        builder.append("\nTo login to the system: ").append(url).append("/index.html");
        appendURLParam(builder, Constants.USERCODE_PARAMETER, userCode, true);
        builder.append("\n\n------------------------------------------------------------\n\n");
        builder
                .append("We recommend that you install the latest version of your antivirus software ");
        builder.append("prior to downloading any files over the Internet.\n");
        return builder.toString();
    }

    @Override
    protected String createSubject()
    {
        assert files.size() > 0 : "No files to upload.";
        return StringUtils.capitalize(createFileText()) + " available for download from "
                + getShortRegistratorDescription();
    }

    private String createFileText()
    {
        return files.size() == 1 ? "a file" : "files";
    }

}
