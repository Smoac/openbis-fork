/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;

/**
 * Test cases for corresponding {@link AttachmentDAO} class.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups =
    { "db" })
public final class AttachmentDAOTest extends AbstractDAOTest
{

    private static final String FILE_NAME = "attachment-test.file";

    private static final String ATT_CONTENTS_TABLE = "attachment_contents";

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public final void testDeleteAttachment()
    {
        final int NUM_ATTACHMENTS = 2;

        IAttachmentDAO attachmentDAO = daoFactory.getAttachmentDAO();
        AttachmentPE attachment1 = createTestAttachment(1);
        AttachmentPE attachment2 = createTestAttachment(2);
        ExperimentPE owner = selectFirstExperiment();

        int rowsInAttachmentContents = countRowsInTable(ATT_CONTENTS_TABLE);

        // create
        attachmentDAO.createAttachment(attachment1, owner);
        attachmentDAO.createAttachment(attachment2, owner);

        AttachmentPE persisted =
                attachmentDAO.tryFindAttachmentByOwnerAndFileName(owner, FILE_NAME);
        assertNotNull(persisted);
        assertEquals(rowsInAttachmentContents + NUM_ATTACHMENTS,
                countRowsInTable(ATT_CONTENTS_TABLE));

        // delete and test
        attachmentDAO.deleteByOwnerAndFileName(owner, FILE_NAME);

        persisted = attachmentDAO.tryFindAttachmentByOwnerAndFileName(owner, FILE_NAME);
        assertNull(persisted);
        assertEquals(rowsInAttachmentContents, countRowsInTable(ATT_CONTENTS_TABLE));

    }

    private AttachmentPE createTestAttachment(int version)
    {
        AttachmentPE result = new AttachmentPE();
        result.setFileName(FILE_NAME);
        result.setVersion(version);

        AttachmentContentPE content = new AttachmentContentPE();
        content.setValue("sample-attachment-content".getBytes());
        result.setAttachmentContent(content);

        result.setRegistrator(getTestPerson());

        return result;

    }

}