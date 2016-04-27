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

package ch.systemsx.cisd.ant.task.subversion;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.ant.task.subversion.SVNInfoRecord.NodeKind;

/**
 * 
 *
 * @author felmer
 */
public class SVNInfoRecordExtractorTest
{
    private static final List<String> INFO1 = Collections.unmodifiableList(Arrays.asList("Path: .",  
            "URL: svn+ssh://cisd-hal.ethz.ch/internal/cisd/ant_tasks/trunk",  
            "Repository Root: svn+ssh://cisd-hal.ethz.ch/internal",  
            "Repository UUID: e2bb8bc7-4a29-0410-9445-bbee19c747d1",  
            "Revision: 290",  
            "Node Kind: directory",  
            "Schedule: add",  
            "Last Changed Author: felmer",  
            "Last Changed Rev: 255",  
            "Last Changed Date: 2007-05-07 16:27:11 +0200 (Mon, 07 May 2007)",  
            ""));
    private static final List<String> INFO2 = Collections.unmodifiableList(Arrays.asList("Path: build",  
            "URL: svn+ssh://cisd-hal.ethz.ch/internal/cisd/ant_tasks/trunk/build",  
            "Repository Root: svn+ssh://cisd-hal.ethz.ch/internal",  
            "Repository UUID: e2bb8bc7-4a29-0410-9445-bbee19c747d1",  
            "Revision: 290",  
            "Node Kind: directory",  
            "Schedule: normal",  
            "Last Changed Author: felmer",  
            "Last Changed Rev: 288",  
            "Last Changed Date: 2007-05-07 16:21:00 +0200 (Mon, 07 May 2007)",  
            ""));
    private static final List<String> INFO3 = Collections.unmodifiableList(Arrays.asList("Path: build\\build.xml",  
            "Name: build.xml",  
            "URL: svn+ssh://cisd-hal.ethz.ch/internal/cisd/ant_tasks/trunk/build/build.xml",  
            "Repository Root: svn+ssh://cisd-hal.ethz.ch/internal",  
            "Repository UUID: e2bb8bc7-4a29-0410-9445-bbee19c747d1",  
            "Revision: 291",  
            "Node Kind: file",  
            "Schedule: normal",  
            "Last Changed Author: fje",  
            "Last Changed Rev: 287",  
            "Last Changed Date: 2007-05-07 16:22:00 +0200 (Mon, 07 May 2007)",  
            "Text Last Updated: 2007-05-07 16:20:04 +0200 (Mon, 07 May 2007)",  
            "Checksum: 9236ea7a96fc9aa92c1b4ba8cabe1ae3",  
            ""));

    @Test
    public void testExtractingOfInfo3()
    {
        ArrayList<String> lines = new ArrayList<String>();
        lines.addAll(INFO3);
        SVNInfoRecord infoRecord = extract(lines);
        
        assertEquals("build\\build.xml", infoRecord.getWorkingCopyPath());
        assertEquals("svn+ssh://cisd-hal.ethz.ch/internal/cisd/ant_tasks/trunk/build/build.xml", 
                     infoRecord.getRepositoryUrl());
        assertEquals("svn+ssh://cisd-hal.ethz.ch/internal", infoRecord.getRepositoryRootUrl());
        assertEquals("e2bb8bc7-4a29-0410-9445-bbee19c747d1", infoRecord.getRepositoryUUID());
        assertEquals(291, infoRecord.getRevision());
        assertEquals(NodeKind.FILE, infoRecord.getNodeKind());
        assertEquals("normal", infoRecord.getSchedule());
        assertEquals("fje", infoRecord.getLastChangedAuthor());
        assertEquals(287, infoRecord.getLastChangedRevision());
        assertEquals("2007-05-07 16:22:00 +0200 (Mon, 07 May 2007)", infoRecord.getLastChangedDate());
    }
    
    @Test
    public void testExtractingOfInfo2Info3()
    {
        ArrayList<String> lines = new ArrayList<String>();
        lines.addAll(INFO2);
        lines.addAll(INFO3);
        SVNInfoRecord infoRecord = extract(lines);
        
        assertEquals("build", infoRecord.getWorkingCopyPath());
        assertEquals("svn+ssh://cisd-hal.ethz.ch/internal/cisd/ant_tasks/trunk/build", infoRecord.getRepositoryUrl());
        assertEquals("svn+ssh://cisd-hal.ethz.ch/internal", infoRecord.getRepositoryRootUrl());
        assertEquals("e2bb8bc7-4a29-0410-9445-bbee19c747d1", infoRecord.getRepositoryUUID());
        assertEquals(291, infoRecord.getRevision());
        assertEquals(NodeKind.DIRECTORY, infoRecord.getNodeKind());
        assertEquals("normal", infoRecord.getSchedule());
        assertEquals("felmer", infoRecord.getLastChangedAuthor());
        assertEquals(288, infoRecord.getLastChangedRevision());
        assertEquals("2007-05-07 16:21:00 +0200 (Mon, 07 May 2007)", infoRecord.getLastChangedDate());
    }
    
    
    @Test
    public void testExtractingOfInfo1Info2Info3()
    {
        ArrayList<String> lines = new ArrayList<String>();
        lines.addAll(INFO1);
        lines.addAll(INFO2);
        lines.addAll(INFO3);
        SVNInfoRecord infoRecord = extract(lines);
        
        assertEquals(".", infoRecord.getWorkingCopyPath());
        assertEquals("svn+ssh://cisd-hal.ethz.ch/internal/cisd/ant_tasks/trunk", infoRecord.getRepositoryUrl());
        assertEquals("svn+ssh://cisd-hal.ethz.ch/internal", infoRecord.getRepositoryRootUrl());
        assertEquals("e2bb8bc7-4a29-0410-9445-bbee19c747d1", infoRecord.getRepositoryUUID());
        assertEquals(291, infoRecord.getRevision());
        assertEquals(NodeKind.DIRECTORY, infoRecord.getNodeKind());
        assertEquals("add", infoRecord.getSchedule());
        assertEquals("felmer", infoRecord.getLastChangedAuthor());
        assertEquals(288, infoRecord.getLastChangedRevision());
        assertEquals("2007-05-07 16:27:11 +0200 (Mon, 07 May 2007)", infoRecord.getLastChangedDate());
    }
    
    private SVNInfoRecord extract(ArrayList<String> lines)
    {
        SVNUtilities.ProcessInfo processInfo = new SVNUtilities.ProcessInfo("info", lines, 0);
        SVNInfoRecord infoRecord = new SVNInfoRecord();
        new SVNInfoRecordExtractor().fillInfoRecord(infoRecord, processInfo);
        return infoRecord;
    }
}
