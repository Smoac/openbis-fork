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

import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.ant.task.subversion.SVNInfoRecord.NodeKind;
import ch.systemsx.cisd.ant.task.subversion.SVNInfoRecord.Updater;
import ch.systemsx.cisd.ant.task.subversion.SVNUtilities.ProcessInfo;

/**
 * 
 *
 * @author felmer
 */
class SVNInfoRecordExtractor
{
    private static interface ValueHandler
    {
        public void handle(SVNInfoRecord record, String value);
    }
    
    private static abstract class FirstValueHandler implements ValueHandler
    {
        private boolean first = true;

        public void handle(SVNInfoRecord record, String value)
        {
            if (first)
            {
                update(record.getUpdater(), value);
                first = false;
            }
        }
        
        protected abstract void update(Updater updater, String value);
    }
    
    private static final class PathHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String value)
        {
            updater.setWorkingCopyPath(value);
        }
    }
    
    private static final class RepositoryUrlHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String value)
        {
            updater.setRepositoryUrl(value);
        }
    }
    
    private static final class RepositoryRootHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String value)
        {
            updater.setRepositoryRootUrl(value);
        }
    }
    
    private static final class RepositoryUUIDHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String value)
        {
            updater.setRepositoryUUID(value);
        }
    }
    
    private static final class ScheduleHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String value)
        {
            updater.setSchedule(value);
        }
    }
    
    private static final class LastChangedAuthorHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String value)
        {
            updater.setLastChangedAuthor(value);
        }
    }
    
    private static final class LastChangedDateHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String value)
        {
            updater.setLastChangedDate(value);
        }
    }
    
    private static final class NodeKindHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String value)
        {
            if ("directory".equalsIgnoreCase(value))
            {
                updater.setNodeKind(NodeKind.DIRECTORY);
            } else if ("file".equalsIgnoreCase(value))
            {
                updater.setNodeKind(NodeKind.FILE);
            } else
            {
                throw SVNException.fromTemplate("Subversion reports invalid node kind '%s'.", value);
            }
        }
    }
    
    private static final class RevisionHandler implements ValueHandler
    {
        public void handle(SVNInfoRecord record, String value)
        {
            try
            {
                Updater updater = record.getUpdater();
                updater.setRevision(Math.max(record.getRevision(), Integer.parseInt(value)));
            } catch (NumberFormatException ex)
            {
                throw SVNException.fromTemplate("Subversion reports invalid revision number '%s'.", value);
            }
        }
    }

    private static final class LastChangedRevisionHandler implements ValueHandler
    {
        public void handle(SVNInfoRecord record, String value)
        {
            try
            {
                Updater updater = record.getUpdater();
                updater.setLastChangedRevision(Math.max(record.getLastChangedRevision(), Integer.parseInt(value)));
            } catch (NumberFormatException ex)
            {
                throw SVNException.fromTemplate("Subversion reports invalid last changed revision number '%s'.", value);
            }
        }
    }

    private final Map<String, ValueHandler> valueHandlerMap;

    SVNInfoRecordExtractor()
    {
        valueHandlerMap = new LinkedHashMap<String, ValueHandler>();
        valueHandlerMap.put("Path", new PathHandler());
        valueHandlerMap.put("URL", new RepositoryUrlHandler());
        valueHandlerMap.put("Repository Root", new RepositoryRootHandler());
        valueHandlerMap.put("Repository UUID", new RepositoryUUIDHandler());
        valueHandlerMap.put("Revision", new RevisionHandler());
        valueHandlerMap.put("Node Kind", new NodeKindHandler());
        valueHandlerMap.put("Schedule", new ScheduleHandler());
        valueHandlerMap.put("Last Changed Author", new LastChangedAuthorHandler());
        valueHandlerMap.put("Last Changed Rev", new LastChangedRevisionHandler());
        valueHandlerMap.put("Last Changed Date", new LastChangedDateHandler());
    }
    
    void fillInfoRecord(final SVNInfoRecord record, final ProcessInfo subversionProcessInfo)
    {
        for (String line : subversionProcessInfo.getLines())
        {
            int index = line.indexOf(':');
            if (index > 0)
            {
                ValueHandler valueHandler = valueHandlerMap.get(line.substring(0, index));
                if (valueHandler != null)
                {
                    valueHandler.handle(record, line.substring(index + 1).trim());
                }
            }
        }
    }


}
