/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.trigger.ITrigger;
import ch.systemsx.cisd.cifex.server.trigger.ITriggerConsole;
import ch.systemsx.cisd.cifex.server.trigger.ITriggerRequest;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * @author Bernd Rinn
 */
class TriggerManager implements ITriggerManager
{
    private static final String TRIGGERS_RESOURCE = "/triggers.txt";

    private static final String TRIGGERS_FILE = "etc" + TRIGGERS_RESOURCE;

    private final class TriggerConsole implements ITriggerConsole
    {
        private final ITrigger trigger;

        private final IFileManager fileManager;

        private final TriggerRequest triggerRequest;

        private final UserDTO triggerUser;

        private final UserDTO requestUser;

        private final Set<FileDTO> toBeDeleted = new HashSet<FileDTO>();

        private TriggerConsole(ITrigger trigger, IFileManager fileManager,
                TriggerRequest triggerRequest, UserDTO requestUser, UserDTO triggerUser)
        {
            this.trigger = trigger;
            this.fileManager = fileManager;
            this.triggerRequest = triggerRequest;
            this.requestUser = requestUser;
            this.triggerUser = triggerUser;
        }

        public List<ITriggerRequest> getAllPendingRequests()
        {
            return getPendingRequests(null, null);
        }

        public List<ITriggerRequest> getAllPendingRequests(String fileNameWildCard)
        {
            return getPendingRequests(null, fileNameWildCard);
        }

        public List<ITriggerRequest> getPendingRequests()
        {
            return getPendingRequests(requestUser.getID(), null);
        }

        public List<ITriggerRequest> getPendingRequests(String fileNameWildCard)
        {
            return getPendingRequests(requestUser.getID(), fileNameWildCard);
        }

        private List<ITriggerRequest> getPendingRequests(Long userIdOrNull,
                String fileNameWildCardOrNull)
        {
            final List<FileDTO> files = fileManager.listDownloadFiles(triggerUser.getID());
            final List<ITriggerRequest> requests = new ArrayList<ITriggerRequest>(files.size());
            for (FileDTO fileDTO : files)
            {
                if (fileDTO.getID() == triggerRequest.getFileID())
                {
                    continue;
                }
                if (userIdOrNull != null && userIdOrNull != fileDTO.getRegistratorId())
                {
                    continue;
                }
                if (fileNameWildCardOrNull != null
                        && FilenameUtils.wildcardMatch(fileDTO.getName(), fileNameWildCardOrNull) == false)
                {
                    continue;
                }
                final File realFile = fileManager.getRealFile(fileDTO);
                requests.add(new TriggerRequest(fileDTO, realFile, toBeDeleted));
            }
            return requests;
        }

        public void upload(File fileToUpload, String[] recipients)
        {
            upload(fileToUpload, recipients, "Upload by trigger "
                    + trigger.getClass().getSimpleName());
        }

        public void upload(File fileToUpload, String[] recipients, String comment)
        {
            upload(fileToUpload, FilenameUtilities.getMimeType(fileToUpload.getName()), recipients,
                    comment);
        }

        public void upload(File fileToUpload, String mimeType, String[] recipients, String comment)
        {
            final File uploadedFile = copy(fileManager, triggerUser, fileToUpload);
            fileManager.registerFileLinkAndInformRecipients(triggerUser, uploadedFile.getName(),
                    comment, mimeType, uploadedFile, recipients, url);
        }

        public void sendMessage(String subject, String content, String replyTo,
                String... recipients) throws EnvironmentFailureException
        {
            mailClient.sendMessage(subject, content, replyTo, recipients);
        }

        void deleteDismissables()
        {
            for (FileDTO fileDTO : toBeDeleted)
            {
                fileManager.deleteFile(fileDTO);
            }
        }

    }

    private static class TriggerRequest implements ITriggerRequest
    {
        private final FileDTO fileDTO;

        private final File file;

        private final Set<FileDTO> toBeDeletedOrNull;

        private boolean dismiss;

        TriggerRequest(FileDTO fileDTO, File file, Set<FileDTO> toBeDeletedOrNull)
        {
            this.fileDTO = fileDTO;
            this.file = file;
            this.toBeDeletedOrNull = toBeDeletedOrNull;
            dismiss = false;
        }

        public String getComment()
        {
            return fileDTO.getComment();
        }

        public File getFile()
        {
            return file;
        }

        public String getFileName()
        {
            return fileDTO.getName();
        }

        public String getUploadingUserEmail()
        {
            return fileDTO.getRegisterer().getEmail();
        }

        public String getUploadingUserId()
        {
            return fileDTO.getRegisterer().getUserCode();
        }

        public String getUploadingUserFullName()
        {
            return fileDTO.getRegisterer().getUserFullName();
        }

        public long getFileID()
        {
            return fileDTO.getID();
        }

        public void dismiss()
        {
            dismiss = true;
            if (toBeDeletedOrNull != null)
            {
                toBeDeletedOrNull.add(fileDTO);
            }
        }

        public boolean isDismissed()
        {
            return dismiss;
        }

    }

    private static class TriggerDescription
    {
        final String triggerUser;

        final String triggerClassName;

        final String triggerPropertyFileOrNull;

        TriggerDescription(String triggerUser, String triggerClassName,
                String triggerPropertyFileOrNull)
        {
            this.triggerUser = triggerUser;
            this.triggerClassName = triggerClassName;
            this.triggerPropertyFileOrNull = triggerPropertyFileOrNull;
        }
    }

    private final Map<String, ITrigger> triggerMap = new HashMap<String, ITrigger>();

    private final IMailClient mailClient;

    private final String url;

    TriggerManager(BusinessContext context)
    {
        this.mailClient = context.getMailClient();
        try
        {
            this.url =
                    context.getOverrideURL() == null ? ("https://"
                            + InetAddress.getLocalHost().getCanonicalHostName() + "/index.html")
                            : context.getOverrideURL();
        } catch (UnknownHostException ex)
        {
            throw new EnvironmentFailureException("Cannot determine ip address of local host.", ex);
        }
        final List<TriggerDescription> triggers = getTriggerDescriptions();
        for (TriggerDescription triggerDescription : triggers)
        {
            triggerMap.put(triggerDescription.triggerUser, createTrigger(triggerDescription));
        }
    }

    private ITrigger createTrigger(TriggerDescription triggerDescription)
    {
        try
        {
            if (triggerDescription.triggerPropertyFileOrNull == null)
            {
                return ClassUtils.create(ITrigger.class, triggerDescription.triggerClassName);
            } else
            {
                final Properties props =
                        getProperties(triggerDescription.triggerPropertyFileOrNull);
                return ClassUtils
                        .create(ITrigger.class, triggerDescription.triggerClassName, props);
            }
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot create trigger '"
                    + triggerDescription.triggerClassName + "'", CheckedExceptionTunnel
                    .unwrapIfNecessary(ex));
        }
    }

    private Properties getProperties(String triggerPropertyFile)
    {
        final File propFile = new File(triggerPropertyFile);
        if (propFile.exists())
        {
            return PropertyUtils.loadProperties(propFile.getPath());
        } else
        {
            final InputStream is =
                    TriggerManager.class.getResourceAsStream("/" + triggerPropertyFile);
            if (is == null)
            {
                throw new ConfigurationFailureException("Cannot find trigger configuration file '"
                        + triggerPropertyFile + "'.");
            }
            return PropertyUtils.loadProperties(is, triggerPropertyFile);
        }
    }

    private List<TriggerDescription> getTriggerDescriptions()
    {
        List<String> triggerLines = null;
        final File triggersFile = new File(TRIGGERS_FILE);
        if (triggersFile.exists() == false)
        {
            final InputStream is = TriggerManager.class.getResourceAsStream(TRIGGERS_RESOURCE);
            if (is != null)
            {
                triggerLines = FileUtilities.loadToStringList(is);
            }
        } else
        {
            triggerLines = FileUtilities.loadToStringList(triggersFile);
        }
        if (triggerLines == null)
        {
            return Collections.<TriggerDescription> emptyList();
        }
        final ArrayList<TriggerDescription> triggers =
                new ArrayList<TriggerDescription>(triggerLines.size());
        for (String triggerLine : triggerLines)
        {
            if (StringUtils.isBlank(triggerLine) || triggerLine.startsWith("#"))
            {
                continue;
            }
            String[] splitted = StringUtils.split(triggerLine.trim(), '\t');
            if (splitted.length == 2)
            {
                triggers.add(new TriggerDescription(splitted[0], splitted[1], null));
            } else if (splitted.length == 3)
            {
                triggers.add(new TriggerDescription(splitted[0], splitted[1], splitted[2]));
            } else
            {
                throw new ConfigurationFailureException("Illegal line in file " + TRIGGERS_RESOURCE
                        + ": lines need to have 2 or 3 columns, separated by TAB, this line has "
                        + splitted.length + " columns: '" + triggerLine.trim() + "'.");
            }
        }
        return triggers;
    }

    public boolean handle(final UserDTO triggerUser, final FileDTO fileDTO,
            final IFileManager fileManager)
    {
        if (isTriggerUser(triggerUser) == false)
        {
            throw new IllegalArgumentException("User " + triggerUser.getUserCode()
                    + " is not a trigger user.");
        }
        final File file = fileManager.getRealFile(fileDTO);
        final TriggerRequest request = new TriggerRequest(fileDTO, file, null);
        final ITrigger trigger = triggerMap.get(triggerUser.getUserCode());
        final TriggerConsole console =
                new TriggerConsole(trigger, fileManager, request, fileDTO.getRegisterer(),
                        triggerUser);
        trigger.handle(request, console);
        console.deleteDismissables();
        return request.isDismissed();
    }

    public boolean isTriggerUser(UserDTO user)
    {
        return triggerMap.containsKey(user.getUserCode());
    }

    private File copy(final IFileManager fileManager, final UserDTO triggerUser, File fileToUpload)
    {
        final File uploadedFile = fileManager.createFile(triggerUser, fileToUpload.getName());
        boolean linked = false;
        if (Unix.isOperational())
        {
            try
            {
                Unix.createHardLink(fileToUpload.getPath(), uploadedFile.getPath());
                linked = true;
            } catch (IOExceptionUnchecked ex)
            {
                // We'll just copy the file instead of linking it.
            }
        }
        if (linked == false)
        {
            FileUtilities.copyFileTo(fileToUpload, uploadedFile, true);
        }
        return uploadedFile;
    }

}
