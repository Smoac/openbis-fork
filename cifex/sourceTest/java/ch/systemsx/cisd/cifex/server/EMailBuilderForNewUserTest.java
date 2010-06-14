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

import ch.systemsx.cisd.cifex.server.business.EMailBuilderForNewUser;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * @author Franz-Josef Elmer
 */
public class EMailBuilderForNewUserTest
{

    @Test
    public void testNewAdminUser()
    {
        UserDTO registrator = new UserDTO();
        registrator.setUserCode("registrator");
        registrator.setEmail("reg@r.rr");
        UserDTO newUser = new UserDTO();
        newUser.setAdmin(true);
        newUser.setUserCode("a");
        newUser.setEmail("a@a.aa");
        MockMailClient mailClient = new MockMailClient();
        EMailBuilderForNewUser builder =
                new EMailBuilderForNewUser(mailClient, registrator, newUser);
        builder.setURL("http://localhost/cifex");
        builder.setPassword("a1234");
        builder.setComment("Hello new user!");
        builder.sendEMail();

        assertEquals("[CIFEX] Registrator has requested an administrator account for you",
                mailClient.subject);
        assertEquals(
                "Hello a,\n" + 
                "\n" + 
                "registrator <reg@r.rr> has requested an administrator account on our server for you.\n" + 
                "\n" + 
                "------------------------------------------------------------\n" + 
                "Information about the person who requested the account:\n" + 
                "------------------------------------------------------------\n" + 
                "\n" + 
                "From:     registrator\n" + 
                "Email:    reg@r.rr\n" + 
                "Comment:  Hello new user!\n" + 
                "\n" + 
                "-------------------------------------------------\n" + 
                "Here's how to login:\n" + 
                "-------------------------------------------------\n" + 
                "\n" + 
                "Visit:    http://localhost/cifex/?user=a\n" + 
                "User:     a\n" + 
                "Password: a1234\n" + 
                "\n" + 
                "--------------------------------------------------\n" + 
                "CIFEX - CISD File EXchanger\n" + 
                "Center for Information Sciences and Databases\n" + 
                "ETH Zurich",
                mailClient.content);
        assertEquals(1, mailClient.recipients.length);
        assertEquals("a@a.aa", mailClient.recipients[0].tryGetEmailAddress());
    }

    @Test
    public void testNewPermanentUser()
    {
        UserDTO registrator = new UserDTO();
        registrator.setUserCode("registrator");
        registrator.setEmail("reg@r.rr");
        UserDTO newUser = new UserDTO();
        newUser.setUserCode("p");
        newUser.setEmail("p@p.pp");
        MockMailClient mailClient = new MockMailClient();
        EMailBuilderForNewUser builder =
                new EMailBuilderForNewUser(mailClient, registrator, newUser);
        builder.setURL("http://localhost/cifex");
        builder.setPassword("p1234");
        builder.sendEMail();

        assertEquals("[CIFEX] Registrator has requested a regular account for you",
                mailClient.subject);
        assertEquals(
                "Hello p,\n" + 
                "\n" + 
                "registrator <reg@r.rr> has requested a regular account on our server for you.\n" + 
                "\n" + 
                "------------------------------------------------------------\n" + 
                "Information about the person who requested the account:\n" + 
                "------------------------------------------------------------\n" + 
                "\n" + 
                "From:     registrator\n" + 
                "Email:    reg@r.rr\n" + 
                "\n" + 
                "-------------------------------------------------\n" + 
                "Here's how to login:\n" + 
                "-------------------------------------------------\n" + 
                "\n" + 
                "Visit:    http://localhost/cifex/?user=p\n" + 
                "User:     p\n" + 
                "Password: p1234\n" + 
                "\n" + 
                "--------------------------------------------------\n" + 
                "CIFEX - CISD File EXchanger\n" + 
                "Center for Information Sciences and Databases\n" + 
                "ETH Zurich",
                mailClient.content);
        assertEquals(1, mailClient.recipients.length);
        assertEquals("p@p.pp", mailClient.recipients[0].tryGetEmailAddress());
    }

    @Test
    public void testNewTemporaryUser()
    {
        UserDTO registrator = new UserDTO();
        registrator.setUserCode("registrator");
        registrator.setEmail("reg@r.rr");
        UserDTO newUser = new UserDTO();
        newUser.setUserCode("t");
        newUser.setEmail("t@t.tt");
        newUser.setExpirationDate(new Date(1234567890));
        MockMailClient mailClient = new MockMailClient();
        EMailBuilderForNewUser builder =
                new EMailBuilderForNewUser(mailClient, registrator, newUser);
        builder.setURL("http://localhost/cifex");
        builder.setPassword("t1234");
        builder.sendEMail();

        assertEquals("[CIFEX] Registrator has requested a temporary account for you",
                mailClient.subject);
        assertEquals(
                "Hello t,\n" + 
                "\n" + 
                "registrator <reg@r.rr> has requested a temporary account on our server for you.\n" + 
                "\n" + 
                "------------------------------------------------------------\n" + 
                "Information about the person who requested the account:\n" + 
                "------------------------------------------------------------\n" + 
                "\n" + 
                "From:     registrator\n" + 
                "Email:    reg@r.rr\n" + 
                "\n" + 
                "-------------------------------------------------\n" + 
                "Here's how to login:\n" + 
                "-------------------------------------------------\n" + 
                "\n" + 
                "Visit:    http://localhost/cifex/?user=t\n" + 
                "User:     t\n" + 
                "Password: t1234\n" + 
                "\n" + 
                "This login account expires on 1970-01-15 at 07:56:07.\n" + 
                "\n" + 
                "--------------------------------------------------\n" + 
                "CIFEX - CISD File EXchanger\n" + 
                "Center for Information Sciences and Databases\n" + 
                "ETH Zurich",
                mailClient.content);
        assertEquals(1, mailClient.recipients.length);
        assertEquals("t@t.tt", mailClient.recipients[0].tryGetEmailAddress());
    }
}
