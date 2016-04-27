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

/**
 * A record as returned by {@link ISVNInfo}.
 * 
 * @author Bernd Rinn
 */
class SVNInfoRecord
{

    public enum NodeKind
    {
        FILE, DIRECTORY
    }
    
    /** The writing part of the interface. */
    interface Updater
    {
        public void setWorkingCopyPath(String workingCopyPath);
        public void setRepositoryUrl(String repositoryUrl);
        public void setRepositoryRootUrl(String repositoryRootUrl);
        public void setRepositoryUUID(String repositoryUUID);
        public void setRevision(int revision);
        public void setNodeKind(NodeKind nodeKind);
        public void setSchedule(String schedule);
        public void setLastChangedAuthor(String lastChangedAuthor);
        public void setLastChangedRevision(int lastChangedRevision);
        public void setLastChangedDate(String lastChangedDate);
    }

    private String workingCopyPath;

    private String repositoryUrl;

    private String repositoryRootUrl;

    private String repositoryUUID;

    private int revision;

    private NodeKind nodeKind;

    private String schedule;

    private String lastChangedAuthor;

    private  int lastChangedRevision;

    private String lastChangedDate;

    private Updater updater = new Updater()
                {
                    
                    public void setLastChangedAuthor(String lastChangedAuthor)
                    {
                        SVNInfoRecord.this.lastChangedAuthor = lastChangedAuthor;
                    }
            
                    public void setLastChangedRevision(int lastChangedRevision)
                    {
                        SVNInfoRecord.this.lastChangedRevision = lastChangedRevision;
                    }
            
                    public void setLastChangedDate(String lastChangedDate)
                    {
                        SVNInfoRecord.this.lastChangedDate = lastChangedDate;
                    }
            
                    public void setNodeKind(NodeKind nodeKind)
                    {
                        SVNInfoRecord.this.nodeKind = nodeKind;
                    }
            
                    public void setRepositoryRootUrl(String repositoryRootUrl)
                    {
                        SVNInfoRecord.this.repositoryRootUrl = repositoryRootUrl;
                    }
            
                    public void setRepositoryUUID(String repositoryUUID)
                    {
                        SVNInfoRecord.this.repositoryUUID = repositoryUUID;
                    }
            
                    public void setRepositoryUrl(String repositoryUrl)
                    {
                        SVNInfoRecord.this.repositoryUrl = repositoryUrl;
                    }
            
                    public void setRevision(int revision)
                    {
                        SVNInfoRecord.this.revision = revision;
                    }
            
                    public void setSchedule(String schedule)
                    {
                        SVNInfoRecord.this.schedule = schedule;
                    }
            
                    public void setWorkingCopyPath(String workingCopyPath)
                    {
                        SVNInfoRecord.this.workingCopyPath = workingCopyPath;
                    }
                    
                };
    
    public String getLastChangedAuthor()
    {
        return lastChangedAuthor;
    }

    public String getLastChangedDate()
    {
        return lastChangedDate;
    }

    public int getLastChangedRevision()
    {
        return lastChangedRevision;
    }

    public NodeKind getNodeKind()
    {
        return nodeKind;
    }

    public String getRepositoryRootUrl()
    {
        return repositoryRootUrl;
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public String getRepositoryUUID()
    {
        return repositoryUUID;
    }

    public int getRevision()
    {
        return revision;
    }

    public String getSchedule()
    {
        return schedule;
    }

    public String getWorkingCopyPath()
    {
        return workingCopyPath;
    }

    /**
     * @return The {@link Updater} object to use when writing the fields.
     */
    Updater getUpdater()
    {
        return updater;
    }

}
