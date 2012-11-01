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
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import javax.activation.DataHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.trigger.AsynchronousTrigger;
import ch.systemsx.cisd.cifex.server.trigger.ITrigger;
import ch.systemsx.cisd.cifex.server.trigger.ITriggerConsole;
import ch.systemsx.cisd.cifex.server.trigger.ITriggerRequest;
import ch.systemsx.cisd.cifex.server.trigger.SingletonTrigger;
import ch.systemsx.cisd.cifex.server.util.FilenameUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.From;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.reflection.ClassUtils;

/**
 * A class that manages the initialization and call of {@link ITrigger}s.
 * 
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
            triggerRequest.setToBeDeletedOrNull(toBeDeleted);
            this.fileManager = fileManager;
            this.triggerRequest = triggerRequest;
            this.requestUser = requestUser;
            this.triggerUser = triggerUser;
        }

        @Override
        public List<ITriggerRequest> getAllPendingRequests()
        {
            return getPendingRequests(null, null);
        }

        @Override
        public List<ITriggerRequest> getAllPendingRequests(String fileNameWildCard)
        {
            return getPendingRequests(null, fileNameWildCard);
        }

        @Override
        public List<ITriggerRequest> getPendingRequests()
        {
            return getPendingRequests(requestUser.getID(), null);
        }

        @Override
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
                if (userIdOrNull != null && userIdOrNull != fileDTO.getOwnerId())
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

        @Override
        public void upload(File fileToUpload, String[] recipients)
        {
            upload(fileToUpload, recipients, "Upload by trigger "
                    + trigger.getClass().getSimpleName());
        }

        @Override
        public void upload(File fileToUpload, String[] recipients, String comment)
        {
            upload(fileToUpload, FilenameUtilities.getMimeType(fileToUpload.getName()), recipients,
                    comment);
        }

        @Override
        public void upload(File fileToUpload, String mimeType, String[] recipients, String comment)
        {
            final File uploadedFile = copy(fileManager, triggerUser, fileToUpload);
            final int crc32Value = checksumCRC32(uploadedFile);
            fileManager.registerFileLinkAndInformRecipients(triggerUser, uploadedFile.getName(),
                    comment, mimeType, uploadedFile, crc32Value, recipients, url, userActionLog);
        }

        @Override
        public void sendMessage(String subject, String content, String replyTo, From fromOrNull,
                String... recipients) throws EnvironmentFailureException
        {
            mailClient.sendMessage(subject, content, replyTo, null, recipients);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void sendMessageWithAttachment(final String subject, final String content,
                final String filename, final DataHandler attachmentContent,
                final String replyToOrNull, final From fromOrNull, final String... recipients)
                throws EnvironmentFailureException
        {
            mailClient.sendMessageWithAttachment(subject, content, filename, attachmentContent,
                    replyToOrNull, fromOrNull, recipients);
        }

        @Override
        public void sendEmailMessage(String subject, String content, EMailAddress replyToOrNull,
                EMailAddress fromOrNull, EMailAddress... recipients)
                throws EnvironmentFailureException
        {
            mailClient.sendEmailMessage(subject, content, replyToOrNull, fromOrNull, recipients);
        }

        @Override
        public void sendEmailMessageWithAttachment(String subject, String content, String filename,
                DataHandler attachmentContent, EMailAddress replyToOrNull, EMailAddress fromOrNull,
                EMailAddress... recipients) throws EnvironmentFailureException
        {
            mailClient.sendEmailMessageWithAttachment(subject, content, filename,
                    attachmentContent, replyToOrNull, fromOrNull, recipients);
        }

        @Override
        public void sendTestEmail()
        {
            mailClient.sendTestEmail();
        }

        void deleteDismissables()
        {
            for (FileDTO fileDTO : toBeDeleted)
            {
                boolean success = false;
                try
                {
                    fileManager.deleteFile(fileDTO);
                    success = true;
                } finally
                {
                    userActionLog.logDeleteFile(fileDTO, success);
                }
            }
        }

    }

    private static class TriggerRequest implements ITriggerRequest
    {
        private final FileDTO fileDTO;

        private final File file;

        private Set<FileDTO> toBeDeletedOrNull;

        private boolean dismiss;

        private Date timeOfRequest;

        private Date timeOfExpiration;

        TriggerRequest(FileDTO fileDTO, File file, Set<FileDTO> toBeDeletedOrNull)
        {
            this.fileDTO = fileDTO;
            this.file = file;
            this.toBeDeletedOrNull = toBeDeletedOrNull;
            this.timeOfRequest = fileDTO.getRegistrationDate();
            this.timeOfExpiration = fileDTO.getExpirationDate();
            dismiss = false;
        }

        void setToBeDeletedOrNull(Set<FileDTO> toBeDeletedOrNull)
        {
            this.toBeDeletedOrNull = toBeDeletedOrNull;
        }

        @Override
        public String getComment()
        {
            return fileDTO.getComment();
        }

        @Override
        public File getFile()
        {
            return file;
        }

        @Override
        public String getFileName()
        {
            return fileDTO.getName();
        }

        @Override
        public String getUploadingUserEmail()
        {
            return fileDTO.getOwner().getEmail();
        }

        @Override
        public String getUploadingUserId()
        {
            return fileDTO.getOwner().getUserCode();
        }

        @Override
        public String getUploadingUserFullName()
        {
            return fileDTO.getOwner().getUserFullName();
        }

        public long getFileID()
        {
            return fileDTO.getID();
        }

        @Override
        public void dismiss()
        {
            dismiss = true;
            if (toBeDeletedOrNull != null)
            {
                toBeDeletedOrNull.add(fileDTO);
            }
        }

        public void setDismiss(boolean dismiss)
        {
            this.dismiss = dismiss;
            if (toBeDeletedOrNull != null)
            {
                if (dismiss)
                {
                    toBeDeletedOrNull.add(fileDTO);
                } else
                {
                    toBeDeletedOrNull.remove(fileDTO);
                }
            }
        }

        public boolean isDismissed()
        {
            return dismiss;
        }

        @Override
        public Date getRequestTime()
        {
            return timeOfRequest;
        }

        @Override
        public Date getExpirationTime()
        {
            return timeOfExpiration;
        }

    }

    private static class TriggerDescription
    {
        private final String triggerClassName;

        private final String triggerPropertyFileOrNull;

        private final ITrigger triggerObjectOrNull;

        private final boolean asynchronous;

        private final boolean willDismiss;

        private final int permitsNeeded;

        static boolean hasAsyncTriggers(Map<String, TriggerDescription> triggerMap)
        {
            for (TriggerDescription desc : triggerMap.values())
            {
                if (desc.isAsync())
                {
                    return true;
                }
            }
            return false;
        }

        TriggerDescription(String triggerClassName, String triggerPropertyFileOrNull)
        {
            this.triggerClassName = triggerClassName;
            this.triggerPropertyFileOrNull = triggerPropertyFileOrNull;
            try
            {
                final Class<?> triggerClass = Class.forName(triggerClassName);
                if (triggerClass.isAnnotationPresent(SingletonTrigger.class))
                {
                    this.triggerObjectOrNull = createTrigger();
                } else
                {
                    createTrigger(); // We create the trigger anyway to check whether it works.
                    this.triggerObjectOrNull = null;
                }
                final AsynchronousTrigger asyncTrigger =
                        triggerClass.getAnnotation(AsynchronousTrigger.class);
                if (asyncTrigger != null)
                {
                    asynchronous = true;
                    willDismiss = asyncTrigger.willDismissRequest();
                    permitsNeeded = asyncTrigger.triggerPermits();
                } else
                {
                    asynchronous = false;
                    willDismiss = false;
                    permitsNeeded = 0;
                }
            } catch (ClassNotFoundException ex)
            {
                throw new ConfigurationFailureException("Class '" + triggerClassName
                        + "' not found", ex);
            }
        }

        String getTriggerClassName()
        {
            return triggerClassName;
        }

        String getTriggerPropertyFileOrNull()
        {
            return triggerPropertyFileOrNull;
        }

        ITrigger getTrigger()
        {
            if (triggerObjectOrNull != null)
            {
                return triggerObjectOrNull;
            } else
            {
                return createTrigger();
            }
        }

        boolean isAsync()
        {
            return asynchronous;
        }

        boolean willDismissRequest()
        {
            return willDismiss;
        }

        int getPermitsNeeded()
        {
            return permitsNeeded;
        }

        private ITrigger createTrigger()
        {
            try
            {
                if (getTriggerPropertyFileOrNull() == null)
                {
                    return ClassUtils.create(ITrigger.class, getTriggerClassName());
                } else
                {
                    final Properties props = getProperties(getTriggerPropertyFileOrNull());
                    return ClassUtils.create(ITrigger.class, getTriggerClassName(), props);
                }
            } catch (Exception ex)
            {
                throw new ConfigurationFailureException("Cannot create trigger '"
                        + getTriggerClassName() + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
            }
        }

        private Properties getProperties(String triggerPropertyFile)
        {
            final File propFile = new File(triggerPropertyFile);
            if (propFile.exists())
            {
                return PropertyIOUtils.loadProperties(propFile.getPath());
            } else
            {
                final InputStream is =
                        TriggerManager.class.getResourceAsStream("/" + triggerPropertyFile);
                if (is == null)
                {
                    throw new ConfigurationFailureException(
                            "Cannot find trigger configuration file '" + triggerPropertyFile + "'.");
                }
                return PropertyIOUtils.loadProperties(is, triggerPropertyFile);
            }
        }

    }

    /**
     * ExecutorService for calling the triggers in a non-blocking way.
     */
    private final ExecutorService triggerExecutor;

    private final int maxTriggerPermits;

    private final Semaphore permits;

    private final Map<String, TriggerDescription> triggerMap;

    private final IMailClient mailClient;

    private final String url;

    private final IUserActionLog userActionLog;

    TriggerManager(BusinessContext context)
    {
        this.mailClient = context.getMailClient();
        this.maxTriggerPermits = context.getTriggerPermits();
        this.userActionLog = context.getUserActionLogHttp();
        this.triggerMap = getTriggers();
        if (TriggerDescription.hasAsyncTriggers(triggerMap))
        {
            this.triggerExecutor =
                    new NamingThreadPoolExecutor("Triggers").corePoolSize(3).daemonize();
            this.permits = new Semaphore(maxTriggerPermits);
        } else
        {
            this.triggerExecutor = null;
            this.permits = null;
        }
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
    }

    private Map<String, TriggerDescription> getTriggers()
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
            return Collections.<String, TriggerDescription> emptyMap();
        }

        final Map<String, TriggerDescription> triggers = new HashMap<String, TriggerDescription>();
        for (String triggerLine : triggerLines)
        {
            if (StringUtils.isBlank(triggerLine) || triggerLine.startsWith("#"))
            {
                continue;
            }
            String[] splitted = StringUtils.split(triggerLine.trim(), '\t');
            if (splitted.length == 2)
            {
                triggers.put(splitted[0], new TriggerDescription(splitted[1], null));
            } else if (splitted.length == 3)
            {
                triggers.put(splitted[0], new TriggerDescription(splitted[1], splitted[2]));
            } else
            {
                throw new ConfigurationFailureException("Illegal line in file " + TRIGGERS_RESOURCE
                        + ": lines need to have 2 or 3 columns, separated by TAB, this line has "
                        + splitted.length + " columns: '" + triggerLine.trim() + "'.");
            }
        }
        return triggers;
    }

    @Override
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
        final TriggerDescription triggerDesc = triggerMap.get(triggerUser.getUserCode());
        final ITrigger trigger = triggerDesc.getTrigger();
        final TriggerConsole console =
                new TriggerConsole(trigger, fileManager, request, fileDTO.getOwner(), triggerUser);
        if (triggerDesc.isAsync())
        {
            if (triggerDesc.getPermitsNeeded() > maxTriggerPermits)
            {
                throw ConfigurationFailureException
                        .fromTemplate(
                                "Number of permits needed to run trigger '%s' (%d) is more than all available permits (%d).",
                                triggerDesc.getTriggerClassName(), triggerDesc.getPermitsNeeded(),
                                maxTriggerPermits);
            }
            triggerExecutor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        int permitsHeld = 0;
                        try
                        {
                            permits.acquire(triggerDesc.getPermitsNeeded());
                            permitsHeld = triggerDesc.getPermitsNeeded();
                            trigger.handle(request, console);
                        } catch (Throwable th)
                        {
                            final String msg =
                                    "Your request '" + request.getFileName() + "' to user '"
                                            + triggerUser.getUserCode() + "' failed:\n"
                                            + th.getClass().getSimpleName() + ": "
                                            + th.getMessage();
                            mailClient.sendEmailMessage("Your request '" + request.getFileName()
                                    + "'", msg, null, null, new EMailAddress(request
                                    .getUploadingUserEmail()));
                            throw CheckedExceptionTunnel.wrapIfNecessary(th);
                        } finally
                        {
                            if (permitsHeld > 0)
                            {
                                permits.release(permitsHeld);
                            }
                            request.setDismiss(triggerDesc.willDismissRequest());
                            console.deleteDismissables();
                        }
                    }
                });
            return triggerDesc.willDismissRequest();
        } else
        {
            try
            {
                trigger.handle(request, console);
            } finally
            {
                console.deleteDismissables();
            }
            return request.isDismissed();
        }
    }

    @Override
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
            FileOperations.getInstance().copy(fileToUpload, uploadedFile);
        }
        return uploadedFile;
    }

    private int checksumCRC32(final File uploadedFile)
    {
        final int crc32Value;
        try
        {
            crc32Value = (int) FileUtils.checksumCRC32(uploadedFile);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return crc32Value;
    }

}
