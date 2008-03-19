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

package ch.systemsx.cisd.cifex.server;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Date;

import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.EMailBuilderForUploadedFiles;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * @author Franz-Josef Elmer
 */
public class EMailBuilderForUploadedFilesTest
{
    @Test
    public void testOneFileWithCommentAndPassword()
    {
        UserDTO registrator = new UserDTO();
        registrator.setUserCode("registrator");
        registrator.setEmail("reg@r.rr");
        MockMailClient mailClient = new MockMailClient();
        EMailBuilderForUploadedFiles builder =
                new EMailBuilderForUploadedFiles(mailClient, registrator, "a@a.aa");
        FileDTO file = new FileDTO(0L);
        file.setID(42L);
        file.setName("myData.zip");
        file.setExpirationDate(new Date(1234567890));
        builder.addFile(file);
        builder.setUserCode("userCode");
        builder.setURL("http://localhost/cifex");
        builder.setComment("Here are my data.");
        builder.setPassword("a1234");
        builder.sendEMail();

        assertEquals("[CIFEX] A file available for download from registrator", mailClient.subject);
        assertEquals(
                "Hello,\n"
                        + "\n"
                        + "registrator <reg@r.rr> has stored a file on our server for you to download. File information appears below.\n"
                        + "\n"
                        + "------------------------------------------------------------\n"
                        + "\n"
                        + "From:\tregistrator\n"
                        + "Email:\treg@r.rr\n"
                        + "Comment:\tHere are my data.\n"
                        + "\n"
                        + "A file (click to download):\n"
                        + "\n"
                        + "myData.zip http://localhost/cifex/index.html?fileId=42&user=userCode\n"
                        + "\n"
                        + "Expiration: Files will be removed  on 15-Jan-1970 at 07:56:07\n"
                        + "\n"
                        + "For downloading you have to enter the following password: a1234\n"
                        + "\n"
                        + "To login to the system: http://localhost/cifex/index.html?user=userCode\n"
                        + "\n"
                        + "------------------------------------------------------------\n"
                        + "\n"
                        + "We recommend that you install the latest version of your antivirus software prior "
                        + "to downloading any files over the Internet.\n" + "\n" + "\n"
                        + "--------------------------------------------------\n"
                        + "CIFEX - CISD File EXchanger\n"
                        + "Center for Information Sciences and Databases\n" + "ETH Zurich",
                mailClient.content);
        assertEquals(1, mailClient.recipients.length);
        assertEquals("a@a.aa", mailClient.recipients[0]);
    }

    @Test
    public void testTwoFilesNoComment()
    {
        UserDTO registrator = new UserDTO();
        registrator.setUserCode("registrator");
        registrator.setEmail("reg@r.rr");
        MockMailClient mailClient = new MockMailClient();
        EMailBuilderForUploadedFiles builder =
                new EMailBuilderForUploadedFiles(mailClient, registrator, "a@a.aa");
        FileDTO file1 = new FileDTO(0L);
        file1.setID(42L);
        file1.setName("myData.zip");
        file1.setExpirationDate(new Date(1234567890));
        builder.addFile(file1);
        FileDTO file2 = new FileDTO(0L);
        file2.setID(4711L);
        file2.setName("otherData.zip");
        file2.setExpirationDate(new Date(1234967890));
        builder.addFile(file2);
        builder.setUserCode("userCode");
        builder.setURL("http://localhost/cifex");
        builder.sendEMail();

        assertEquals("[CIFEX] Files available for download from registrator", mailClient.subject);
        assertEquals(
                "Hello,\n"
                        + "\n"
                        + "registrator <reg@r.rr> has stored files on our server for you to download. File information appears below.\n"
                        + "\n"
                        + "------------------------------------------------------------\n"
                        + "\n"
                        + "From:\tregistrator\n"
                        + "Email:\treg@r.rr\n"
                        + "\n"
                        + "Files (click to download):\n"
                        + "\n"
                        + "myData.zip http://localhost/cifex/index.html?fileId=42&user=userCode\n"
                        + "otherData.zip http://localhost/cifex/index.html?fileId=4711&user=userCode\n"
                        + "\n"
                        + "Expiration: Files will be removed  on 15-Jan-1970 at 07:56:07\n"
                        + "\n"
                        + "To login to the system: http://localhost/cifex/index.html?user=userCode"
                        + "\n\n"
                        + "------------------------------------------------------------\n"
                        + "\n"
                        + "We recommend that you install the latest version of your antivirus software prior "
                        + "to downloading any files over the Internet.\n" + "\n" + "\n"
                        + "--------------------------------------------------\n"
                        + "CIFEX - CISD File EXchanger\n"
                        + "Center for Information Sciences and Databases\n" + "ETH Zurich",
                mailClient.content);
        assertEquals(1, mailClient.recipients.length);
        assertEquals("a@a.aa", mailClient.recipients[0]);
    }

}
