package ch.ethz.sis.afsserver.server.observer.impl;

import java.io.ByteArrayInputStream;
import java.nio.file.NoSuchFileException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsjson.JsonObjectMapper;
import ch.ethz.sis.afsserver.server.APIServer;
import ch.ethz.sis.afsserver.server.APIServerException;
import ch.ethz.sis.afsserver.server.impl.ApiRequest;
import ch.ethz.sis.afsserver.server.impl.ApiResponse;
import ch.ethz.sis.afsserver.server.impl.ApiResponseBuilder;
import ch.ethz.sis.afsserver.server.observer.ServerObserver;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameterUtil;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.EventType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.id.EventTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import ch.ethz.sis.shared.startup.Configuration;
import lombok.Value;

public class OpenBISServerObserver implements ServerObserver<TransactionConnection>
{

    private static final Logger logger = LogManager.getLogger(OpenBISServerObserver.class);

    private static final String THREAD_NAME = "openbis-server-observer-task";

    private String openBISUser;

    private String openBISPassword;

    private String openBISLastSeenDeletionFile;

    private Integer openBISLastSeenDeletionBatchSize;

    private Integer openBISLastSeenDeletionIntervalInSeconds;

    private IApplicationServerApi applicationServerApi;

    private APIServer<TransactionConnection, ApiRequest, ApiResponse, ?> apiServer;

    private JsonObjectMapper jsonObjectMapper;

    @Override
    public void init(APIServer<TransactionConnection, ?, ?, ?> apiServer, Configuration configuration) throws Exception
    {
        this.openBISUser = AtomicFileSystemServerParameterUtil.getOpenBISUser(configuration);
        this.openBISPassword = AtomicFileSystemServerParameterUtil.getOpenBISPassword(configuration);
        this.openBISLastSeenDeletionFile = AtomicFileSystemServerParameterUtil.getOpenBISLastSeenDeletionFile(configuration);
        this.openBISLastSeenDeletionBatchSize =
                AtomicFileSystemServerParameterUtil.getOpenBISLastSeenDeletionBatchSize(configuration);
        this.openBISLastSeenDeletionIntervalInSeconds =
                AtomicFileSystemServerParameterUtil.getOpenBISLastSeenDeletionIntervalInSeconds(configuration);
        this.applicationServerApi = AtomicFileSystemServerParameterUtil.getApplicationServerApi(configuration);
        this.apiServer = (APIServer<TransactionConnection, ApiRequest, ApiResponse, ?>) apiServer;
        this.jsonObjectMapper = AtomicFileSystemServerParameterUtil.getJsonObjectMapper(configuration);
    }

    @Override
    public void beforeStartup() throws Exception
    {
        new Timer(THREAD_NAME, true).schedule(new TimerTask()
                                              {
                                                  @Override public void run()
                                                  {
                                                      processApplicationServerDeletionEvents();
                                                  }
                                              },
                0,
                openBISLastSeenDeletionIntervalInSeconds * 1000L);
    }

    @Override
    public void beforeShutdown() throws Exception
    {
    }

    private void processApplicationServerDeletionEvents()
    {
        while (true)
        {
            String sessionToken = null;

            try
            {
                sessionToken = applicationServerApi.login(openBISUser, openBISPassword);

                if (sessionToken == null)
                {
                    throw new RuntimeException(
                            "Could not login to the AS server. Please check openBIS user and openBIS password in the AFS server configuration.");
                }

                final LastSeenEvent lastSeenEvent = loadLastSeenEvent();
                LastSeenEvent newLastSeenEvent = null;

                EventSearchCriteria criteria = new EventSearchCriteria();
                criteria.withEventType().thatEquals(EventType.DELETION);
                criteria.withEntityType().thatEquals(EntityType.DATA_SET);

                EventFetchOptions fo = new EventFetchOptions();
                fo.sortBy().id().asc();
                fo.count(openBISLastSeenDeletionBatchSize);

                if (lastSeenEvent != null)
                {
                    logger.info("Last seen event file found with id: " + lastSeenEvent.getId() + " and registration date: "
                            + lastSeenEvent.getRegistrationDate() + ". Only newer deletion events will be processed.");
                    criteria.withRegistrationDate().thatIsLaterThanOrEqualTo(lastSeenEvent.getRegistrationDate());
                } else
                {
                    logger.info("No last seen event file found. All deletion events will be processed.");
                }

                SearchResult<Event> foundEvents = applicationServerApi.searchEvents(sessionToken, criteria, fo);

                if (foundEvents.getObjects().isEmpty())
                {
                    logger.info("No new deletion events found. Exiting.");
                    return;
                }

                List<Event> newEvents = foundEvents.getObjects().stream().filter(event ->
                {
                    EventTechId eventId = (EventTechId) event.getId();
                    return lastSeenEvent == null || eventId.getTechId() > lastSeenEvent.getId();
                }).collect(Collectors.toList());

                if (newEvents.isEmpty())
                {
                    Event lastEvent = foundEvents.getObjects().get(foundEvents.getObjects().size() - 1);
                    newLastSeenEvent = new LastSeenEvent(((EventTechId) lastEvent.getId()).getTechId(), lastEvent.getRegistrationDate());
                } else
                {
                    logger.info("Found " + newEvents.size() + " new deletion event(s).");

                    Event lastEvent = null;

                    try
                    {
                        for (Event event : newEvents)
                        {
                            processEvent(sessionToken, event);
                            lastEvent = event;
                        }
                    } finally
                    {
                        if (lastEvent != null)
                        {
                            newLastSeenEvent =
                                    new LastSeenEvent(((EventTechId) lastEvent.getId()).getTechId(), lastEvent.getRegistrationDate());
                        }
                    }
                }

                storeLastSeenEvent(newLastSeenEvent);

                if (foundEvents.getTotalCount() <= foundEvents.getObjects().size())
                {
                    logger.info("No new deletion events found. Exiting.");
                    return;
                } else
                {
                    if (lastSeenEvent != null && lastSeenEvent.getRegistrationDate().equals(newLastSeenEvent.getRegistrationDate()))
                    {
                        throw new RuntimeException(
                                "The processing of deletion events could not progress from last seen id: " + lastSeenEvent.getId()
                                        + " and registration date: " + lastSeenEvent.getRegistrationDate()
                                        + ". Try increasing the batch size to a higher value than " + openBISLastSeenDeletionBatchSize + ".");
                    }
                }
            } catch (Exception e)
            {
                logger.throwing(e);
                return;
            } finally
            {
                if (sessionToken != null)
                {
                    applicationServerApi.logout(sessionToken);
                }
            }
        }
    }

    private void processEvent(String sessionToken, Event event) throws APIServerException
    {
        try
        {
            ApiRequest apiRequest =
                    new ApiRequest("1", "delete", Map.of("owner", event.getIdentifier(), "source", ""), sessionToken,
                            null, null);

            apiServer.processOperation(apiRequest, new ApiResponseBuilder(), new PerformanceAuditor());

            logger.info("Data set " + event.getIdentifier() + " has been successfully deleted from the store.");
        } catch (APIServerException e)
        {
            if (e.getMessage().contains(NoSuchFileException.class.getSimpleName()))
            {
                logger.info("Data set " + event.getIdentifier() + " does not exist in the store. Nothing to delete.");
            } else
            {
                throw new RuntimeException("Deletion of data set " + event.getIdentifier() + " has failed.", e);
            }
        }
    }

    private LastSeenEvent loadLastSeenEvent() throws Exception
    {
        try
        {
            if (IOUtils.exists(openBISLastSeenDeletionFile))
            {
                byte[] bytes = IOUtils.readFully(openBISLastSeenDeletionFile);
                return jsonObjectMapper.readValue(new ByteArrayInputStream(bytes), LastSeenEvent.class);
            } else
            {
                return null;
            }
        } catch (Exception e)
        {
            throw new RuntimeException("Could not load the last seen event from file " + openBISLastSeenDeletionFile, e);
        }
    }

    private void storeLastSeenEvent(LastSeenEvent lastSeenEvent)
    {
        try
        {
            String tempFile = openBISLastSeenDeletionFile + ".tmp";
            if (IOUtils.exists(tempFile))
            {
                IOUtils.delete(tempFile);
            }
            IOUtils.createFile(tempFile);
            byte[] bytes = jsonObjectMapper.writeValue(lastSeenEvent);
            IOUtils.write(tempFile, 0, bytes);
            IOUtils.move(tempFile, openBISLastSeenDeletionFile);
        } catch (Exception e)
        {
            throw new RuntimeException("Could not store the last seen event in file " + openBISLastSeenDeletionFile, e);
        }
    }

    @Value
    private static class LastSeenEvent
    {
        Long id;

        Date registrationDate;
    }

}
