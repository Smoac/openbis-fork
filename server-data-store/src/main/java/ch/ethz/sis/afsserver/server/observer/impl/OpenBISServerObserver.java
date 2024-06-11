package ch.ethz.sis.afsserver.server.observer.impl;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

    private static final int BATCH_SIZE = 1000;

    private String openBISUser;

    private String openBISPassword;

    private String openBISLastSeenDeletionFile;

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

                LastSeenEvent lastSeenEvent = loadLastSeenEvent();

                EventSearchCriteria criteria = new EventSearchCriteria();
                criteria.withEventType().thatEquals(EventType.DELETION);
                criteria.withEntityType().thatEquals(EntityType.DATA_SET);

                if (lastSeenEvent != null)
                {
                    logger.info("Last seen event found with id: " + lastSeenEvent.getId() + " and registration date: "
                            + lastSeenEvent.getRegistrationDate() + ". Only newer events will be processed.");
                    criteria.withRegistrationDate().thatIsLaterThanOrEqualTo(lastSeenEvent.getRegistrationDate());
                } else
                {
                    logger.info("No last seen event found. All events will be processed.");
                }

                EventFetchOptions fo = new EventFetchOptions();
                fo.sortBy().id().asc();
                fo.count(BATCH_SIZE);

                SearchResult<Event> searchResult = applicationServerApi.searchEvents(sessionToken, criteria, new EventFetchOptions());

                if (searchResult.getObjects().isEmpty())
                {
                    logger.info("No data set deletion events found.");
                    return;
                }

                logger.info("Found " + searchResult.getObjects().size()
                        + " data set deletion event(s)." + (searchResult.getTotalCount() > searchResult.getObjects().size() ?
                        " Total number of deletion events: " + searchResult.getTotalCount() : ""));

                LastSeenEvent newLastSeenEvent = lastSeenEvent;

                for (Event event : searchResult.getObjects())
                {
                    EventTechId eventTechId = (EventTechId) event.getId();

                    if (lastSeenEvent != null && eventTechId.getTechId() <= lastSeenEvent.getId())
                    {
                        // there can be multiple events with the same registration date, therefore we need to check the ids as well
                        continue;
                    }

                    processApplicationServerDeletionEvent(sessionToken, event);

                    newLastSeenEvent = new LastSeenEvent(eventTechId.getTechId(), event.getRegistrationDate());
                }

                if (newLastSeenEvent != null)
                {
                    storeLastSeenEvent(newLastSeenEvent);
                }

                if (searchResult.getTotalCount() <= searchResult.getObjects().size())
                {
                    logger.info("No more events to process. Existing.");
                    return;
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

    private void processApplicationServerDeletionEvent(String sessionToken, Event event)
    {
        try
        {
            logger.info("Deleting '" + event.getIdentifier() + "' data set from the AFS server. The data set was deleted at the AS server on "
                    + event.getRegistrationDate() + " in event " + event.getId());

            ApiRequest apiRequest =
                    new ApiRequest("1", "delete", Map.of("owner", event.getIdentifier(), "source", ""), sessionToken,
                            null, null);

            apiServer.processOperation(apiRequest, new ApiResponseBuilder(), new PerformanceAuditor());
        } catch (APIServerException e)
        {
            logger.throwing(e);
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
