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
 * @author felmer
 */
class SVNInfoRecordExtractor
{
    private static final String LAST_CHANGED_DATE = "Last Changed Date";
    private static final String LAST_CHANGED_REV = "Last Changed Rev";
    private static final String LAST_CHANGED_AUTHOR = "Last Changed Author";

    private static interface ValueHandler
    {
        public void handle(SVNInfoRecord record, String key, String value);
        
        public void commit(SVNInfoRecord record);
    }

    private static abstract class FirstValueHandler implements ValueHandler
    {
        private boolean first = true;

        public void handle(SVNInfoRecord record, String key, String value)
        {
            if (first)
            {
                update(record.getUpdater(), key, value);
                first = false;
            }
        }
        
        public void commit(SVNInfoRecord record)
        {
        }

        protected abstract void update(Updater updater, String key, String value);
        
    }

    private static final class PathHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String key, String value)
        {
            updater.setWorkingCopyPath(value);
        }
    }

    private static final class RepositoryUrlHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String key, String value)
        {
            updater.setRepositoryUrl(value);
        }
    }

    private static final class RepositoryRootHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String key, String value)
        {
            updater.setRepositoryRootUrl(value);
        }
    }

    private static final class RepositoryUUIDHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String key, String value)
        {
            updater.setRepositoryUUID(value);
        }
    }

    private static final class ScheduleHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String key, String value)
        {
            updater.setSchedule(value);
        }
    }

    private static final class LastChangedHandler implements ValueHandler
    {
        private String lastChangedAuthor;
        
        private int lastChangedRev;
        
        private String lastChangedDate;

        private LastChangedHandler()
        {
            reset();
        }
        
        public void handle(SVNInfoRecord record, String key, String value)
        {
            if (LAST_CHANGED_REV.equals(key))
            {
                if (lastChangedRev > -1)
                {
                    commit(record);
                } else
                {
                    try
                    {
                        lastChangedRev = Integer.parseInt(value);
                    } catch (NumberFormatException ex)
                    {
                        throw SVNException.fromTemplate(
                                "Subversion reports invalid last changed revision number '%s'.", value);
                    }
                }
            } else if (LAST_CHANGED_AUTHOR.equals(key))
            {
                if (lastChangedAuthor != null)
                {
                    commit(record);
                } else
                {
                    lastChangedAuthor = value;
                }
            } else if (LAST_CHANGED_DATE.equals(key))
            {
                if (lastChangedDate != null)
                {
                    commit(record);
                } else
                {
                    lastChangedDate = value;
                }
            }
        }

        private void reset()
        {
            lastChangedAuthor = null;
            lastChangedDate = null;
            lastChangedRev = -1;
        }
        
        public void commit(SVNInfoRecord record)
        {
            if (record.getLastChangedRevision() < lastChangedRev)
            {
                final Updater updater = record.getUpdater();
                updater.setLastChangedAuthor(lastChangedAuthor);
                updater.setLastChangedDate(lastChangedDate);
                updater.setLastChangedRevision(lastChangedRev);
            }
            reset();
        }

    }

    private static final class NodeKindHandler extends FirstValueHandler
    {
        @Override
        protected void update(Updater updater, String key, String value)
        {
            if ("directory".equalsIgnoreCase(value))
            {
                updater.setNodeKind(NodeKind.DIRECTORY);
            } else if ("file".equalsIgnoreCase(value))
            {
                updater.setNodeKind(NodeKind.FILE);
            } else
            {
                throw SVNException
                        .fromTemplate("Subversion reports invalid node kind '%s'.", value);
            }
        }
    }

    private static final class RevisionHandler implements ValueHandler
    {
        public void handle(SVNInfoRecord record, String key, String value)
        {
            try
            {
                Updater updater = record.getUpdater();
                updater.setRevision(Math.max(record.getRevision(), Integer.parseInt(value)));
            } catch (NumberFormatException ex)
            {
                throw SVNException.fromTemplate("Subversion reports invalid revision number '%s'.",
                        value);
            }
        }

        public void commit(SVNInfoRecord record)
        {
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
        final LastChangedHandler lastChangedHandler = new LastChangedHandler();
        valueHandlerMap.put(LAST_CHANGED_AUTHOR, lastChangedHandler);
        valueHandlerMap.put(LAST_CHANGED_REV, lastChangedHandler);
        valueHandlerMap.put(LAST_CHANGED_DATE, lastChangedHandler);
    }

    void fillInfoRecord(final SVNInfoRecord record, final ProcessInfo subversionProcessInfo)
    {
        for (String line : subversionProcessInfo.getLines())
        {
            int index = line.indexOf(':');
            if (index > 0)
            {
                final String key = line.substring(0, index);
                ValueHandler valueHandler = valueHandlerMap.get(key);
                if (valueHandler != null)
                {
                    final String value = line.substring(index + 1).trim();
                    valueHandler.handle(record, key, value);
                }
            }
        }
        for (ValueHandler handler : valueHandlerMap.values())
        {
            handler.commit(record);
        }
    }

}
