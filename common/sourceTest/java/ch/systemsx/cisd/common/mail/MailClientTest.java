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

package ch.systemsx.cisd.common.mail;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MailClientTest extends AbstractFileSystemTestCase
{
    @Test
    public void test()
    {
        String path = workingDirectory.getPath() + "/emails";
        File emailFolder = new File(path);
        assert emailFolder.exists() == false;
        
        MailClient mailClient = new MailClient("sender", "file://" + path);
        mailClient.sendMessage("some message", "Hello world\nHow are you today?", "a@b.c", "d@e.f");
        
        assert emailFolder.exists();
        assert emailFolder.isDirectory();
        File[] files = emailFolder.listFiles();
        assertEquals(1, files.length);
        assertEquals("email", files[0].getName());
        assertEquals("Subj: some message\n" + "From: sender\n" + "To:   a@b.c, d@e.f\n" + "Content:\n"
                + "Hello world\nHow are you today?\n", FileUtilities.loadToString(files[0]));
        
        // second mail
        mailClient.sendMessage("Greetings", "Hello world!");
        files = emailFolder.listFiles();
        assertEquals(2, files.length);
        assertEquals("email", files[0].getName());
        assertEquals("email1", files[1].getName());
        assertEquals("Subj: Greetings\n" + "From: sender\n" + "To:   \n" + "Content:\n"
                + "Hello world!\n", FileUtilities.loadToString(files[1]));
    }
}
