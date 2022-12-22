package ch.ethz.sis.afsserver.server;

import ch.ethz.sis.afsserver.http.HttpServer;
import ch.ethz.sis.afsserver.server.impl.ApiServerAdapter;
import ch.ethz.sis.afsserver.server.observer.ApiServerObserver;
import ch.ethz.sis.afsserver.server.observer.ServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsjson.jackson.JacksonObjectMapper;
import ch.ethz.sis.shared.log.LogFactory;
import ch.ethz.sis.shared.log.LogFactoryFactory;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import ch.ethz.sis.shared.pool.Factory;
import ch.ethz.sis.shared.pool.Pool;
import ch.ethz.sis.shared.startup.Configuration;
import lombok.NonNull;

public final class AfsServer<CONNECTION, API> {

    private final Logger logger;

    private final Pool<Configuration, CONNECTION> connectionsPool;

    private final ApiServer<CONNECTION, Request, Response, API> apiServer;

    private final HttpServer httpServer;

    private final ServerObserver<CONNECTION> observer;

    private boolean shutdown;

    public AfsServer(Configuration configuration,
                  @NonNull ServerObserver<CONNECTION> serverObserver,
                  @NonNull ApiServerObserver<CONNECTION> apiServerObserver) throws Exception {
        // 1. Load logging plugin, Initializing LogManager
        shutdown = false;
        observer = serverObserver;
        LogFactoryFactory logFactoryFactory = new LogFactoryFactory();
        LogFactory logFactory = logFactoryFactory.create(configuration.getStringProperty(AtomicFileSystemServerParameter.logFactoryClass));
        logFactory.configure(configuration.getStringProperty(AtomicFileSystemServerParameter.logConfigFile));
        LogManager.setLogFactory(logFactory);
        logger = LogManager.getLogger(AfsServer.class);

        logger.info("=== Server Bootstrap ===");
        logger.info("Running with java.version: " + System.getProperty("java.version"));

        // 2.1 Load DB plugin
        logger.info("Creating Connection Factory");
        Factory<Configuration, Configuration, CONNECTION> connectionFactory = configuration.getSharableInstance(AtomicFileSystemServerParameter.connectionFactoryClass);
        connectionFactory.init(configuration);

        logger.info("Creating Workers Factory");
        Factory<Configuration, Configuration, Worker<CONNECTION>> workerFactory = configuration.getSharableInstance(AtomicFileSystemServerParameter.workerFactoryClass);
        workerFactory.init(configuration);

        // 2.2 Creating workers pool
        logger.info("Creating server workers");
        int poolSize = configuration.getIntegerProperty(AtomicFileSystemServerParameter.poolSize);


        connectionsPool = new Pool<>(poolSize, configuration, connectionFactory);
        Pool<Configuration, Worker<CONNECTION>> workersPool = new Pool<>(poolSize, configuration, workerFactory);

        // 2.3 Init API Server observer
        apiServerObserver.init(configuration);

        // 2.4 Creating API Server
        logger.info("Creating API server");
        @SuppressWarnings("unchecked")
        Class<API> publicApiInterface = (Class<API>) configuration.getInterfaceClass(
                AtomicFileSystemServerParameter.publicApiInterface);
        String interactiveSessionKey = configuration.getStringProperty(AtomicFileSystemServerParameter.apiServerInteractiveSessionKey);
        String transactionManagerKey = configuration.getStringProperty(AtomicFileSystemServerParameter.apiServerTransactionManagerKey);
        int apiServerWorkerTimeout = configuration.getIntegerProperty(AtomicFileSystemServerParameter.apiServerWorkerTimeout);
        apiServer = new ApiServer<>(connectionsPool, workersPool, publicApiInterface, interactiveSessionKey, transactionManagerKey, apiServerWorkerTimeout, apiServerObserver);

        // 2.5 Creating JSON RPC Service
        logger.info("Creating API Server adaptor");
        JacksonObjectMapper jsonObjectMapper = configuration.getSharableInstance(AtomicFileSystemServerParameter.jsonObjectMapperClass);
        ApiServerAdapter<CONNECTION, API> apiServerAdapter = new ApiServerAdapter<>(apiServer, jsonObjectMapper);

        // 2.6 Creating HTTP Service
        int httpServerPort = configuration.getIntegerProperty(AtomicFileSystemServerParameter.httpServerPort);
        int maxContentLength = configuration.getIntegerProperty(AtomicFileSystemServerParameter.httpMaxContentLength);
        String httpServerPath = configuration.getStringProperty(AtomicFileSystemServerParameter.httpServerPath);
        logger.info(String.format("Starting HTTP Service on port %d with URI %s with maxContentLength %d",
                httpServerPort, httpServerPath, maxContentLength));
        httpServer = configuration.getSharableInstance(AtomicFileSystemServerParameter.httpServerClass);
        httpServer.start(httpServerPort, maxContentLength, httpServerPath, apiServerAdapter);

        // 2.7 Init observer
        observer.init(apiServer, configuration);
        observer.beforeStartup();

        // 3 Startup
        logger.info("=== Server ready ===");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown(true);
            } catch (Exception e) {
                logger.catching(e);
            }
        }));
    }

    public void shutdown(boolean gracefully) throws Exception {
        if (!shutdown) {
            observer.beforeShutdown();
            shutdown = true;
            logger.info("Shutting down - http server");
            httpServer.shutdown(gracefully);
            logger.info("Shutting down - api server");
            apiServer.shutdown();
            logger.info("Shutting down - waiting for api server workers to finish");
            if (gracefully) {
                while (apiServer.hasWorkersInUse()) {
                    Thread.sleep(100);
                }
            }
            logger.info("Shutting down - connection pool");
            connectionsPool.shutdown();
        }
    }
}
