package ch.ethz.sis.afsserver.server;

import ch.ethz.sis.afs.api.TwoPhaseTransactionAPI;
import ch.ethz.sis.afs.api.dto.ExceptionReason;
import ch.ethz.sis.afsserver.exception.ApiExceptions;
import ch.ethz.sis.afsserver.server.observer.ApiServerObserver;
import ch.ethz.sis.afsserver.server.performance.PerformanceAuditor;
import ch.ethz.sis.shared.exception.ThrowableReason;
import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import ch.ethz.sis.shared.pool.Pool;
import ch.ethz.sis.shared.reflect.Reflect;
import ch.ethz.sis.shared.startup.Configuration;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ch.ethz.sis.afsserver.server.ApiServerErrorType.IncorrectParameters;
import static ch.ethz.sis.afsserver.server.ApiServerErrorType.MethodNotFound;

/*
 * This class should be used as delegate by specific server transport classes
 */
public class ApiServer<CONNECTION, INPUT extends Request, OUTPUT extends Response, API> {

    private static final Logger logger = LogManager.getLogger(ApiServer.class);
    private static final long IDLE_WORKER_TIMEOUT_CHECK_INTERVAL_IN_MILLIS = 1000;
    private final Pool<Configuration, CONNECTION> connectionsPool;
    private final Pool<Configuration, Worker<CONNECTION>> workersPool;

    private final Map<Worker<CONNECTION>, Long> workersLastAccessed = new ConcurrentHashMap<>();
    private final Map<String, Worker<CONNECTION>> workersInUse = new ConcurrentHashMap<>();

    private final Map<String, Method> apiMethods = new ConcurrentHashMap<>();
    private final Map<Method, Parameter[]> apiMethodParameters = new ConcurrentHashMap<>();

    private final String interactiveSessionKey;
    private final String transactionManagerKey;
    private final int apiServerWorkerTimeout; // Maximum amount of time allowed for a request to do a piece of work, when exceeded, the server cancels the request.

    private Timer idleWorkerCleanupTask;
    private boolean shutdown;
    private final ApiServerObserver<CONNECTION> observer;

    public ApiServer(
            @NonNull Pool<Configuration, CONNECTION> connectionsPool,
            @NonNull Pool<Configuration, Worker<CONNECTION>> workersPool,
            @NonNull Class<API> apiClassDefinition,
            @NonNull String interactiveSessionKey,
            @NonNull String transactionManagerKey,
            int apiServerWorkerTimeout,
            @NonNull ApiServerObserver<CONNECTION> observer) {
        this.shutdown = false;
        this.connectionsPool = connectionsPool;
        this.workersPool = workersPool;

        for (Method method : apiClassDefinition.getMethods()) {
            apiMethods.put(method.getName(), method);
            apiMethodParameters.put(method, method.getParameters());
        }

        this.apiServerWorkerTimeout = apiServerWorkerTimeout;
        this.interactiveSessionKey = interactiveSessionKey;
        this.transactionManagerKey = transactionManagerKey;
        this.observer = observer;

        scheduleIdleWorkerCleanupTask();
    }

    public void shutdown() {
        idleWorkerCleanupTask.cancel();
        shutdown = true;
    }

    public boolean hasWorkersInUse() {
        return !workersInUse.isEmpty();
    }

    private void scheduleIdleWorkerCleanupTask() {
        idleWorkerCleanupTask = new Timer();
        idleWorkerCleanupTask.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        for (String sessionToken : workersInUse.keySet()) {
                            try {
                                Worker<CONNECTION> worker = workersInUse.get(sessionToken);
                                if (worker != null) {
                                    boolean isTimeout = workersLastAccessed.get(worker) +
                                            apiServerWorkerTimeout < System.currentTimeMillis();

                                    if (isTimeout) {
                                        checkIn(true,
                                                false,
                                                false,
                                                true,
                                                true,
                                                sessionToken,
                                                worker);
                                    }
                                }
                            } catch (Exception ex) {
                                logger.catching(ex);
                            }
                        }
                    }
                }, 0, IDLE_WORKER_TIMEOUT_CHECK_INTERVAL_IN_MILLIS
        );
    }

    private static final Set<String> twoPhaseTransactionAPIMethods = Reflect.getMethodNames(TwoPhaseTransactionAPI.class);

    private boolean isValidNonInteractiveSession(INPUT request) {
        return !twoPhaseTransactionAPIMethods.contains(request.getMethod());
    }

    private boolean isValidInteractiveSessionFinished(INPUT request) {
        return request.getMethod().equals("commit") || request.getMethod().equals("rollback");
    }

    private boolean sameSessionToken(List<INPUT> requests) {
        String sessionToken = requests.get(0).getSessionToken();
        for (Request request : requests) {
            if ((request.getSessionToken() == null && sessionToken == null) ||
                    (request.getSessionToken() != null && request.getSessionToken().equals(sessionToken))) {
                //Equals
            } else {
                return false;
            }
        }
        return true;
    }

    public OUTPUT processOperation(INPUT request, ResponseBuilder<OUTPUT> responseBuilder, PerformanceAuditor performanceAuditor) throws
            ApiServerException
    {
        logger.traceAccess(null, request);

        // Shutting down?
        if (shutdown) {
            throw new ApiServerException(null, ApiServerErrorType.InternalError, ApiExceptions.SHUTTING_DOWN.getCause());
        }

        // Requests validation
        // begin/rollback can only be called if the session token is present
        String sessionToken = request.getSessionToken();
        boolean sessionTokenFound = sessionToken != null;
        boolean isValidTransactionManagerMode = transactionManagerKey.equals(request.getTransactionManagerKey());
        boolean isValidInteractiveSession = interactiveSessionKey.equals(request.getInteractiveSessionKey());

        boolean isValidInteractiveSessionFinished = false;
        if (isValidInteractiveSession) {
            isValidInteractiveSessionFinished = isValidInteractiveSessionFinished(request) || !sessionTokenFound;
        }

        boolean isValidNonInteractiveSession = false;
        if (!isValidInteractiveSession) {
            isValidNonInteractiveSession = isValidNonInteractiveSession(request);
        }

        if (!isValidInteractiveSession && !isValidNonInteractiveSession) {
            throw new ApiServerException(null, IncorrectParameters, ApiExceptions.NON_INTERACTIVE_WITH_TRANSACTION_CONTROL.getCause());
        }

        // Process requests separately
        Worker<CONNECTION> worker = null;
        String currentRequestId = null;
        OUTPUT response = null;
        boolean errorFound = false;

        try {
            worker = checkOut(performanceAuditor,
                                isValidTransactionManagerMode,
                                isValidInteractiveSession,
                                isValidInteractiveSessionFinished,
                                isValidNonInteractiveSession,
                                sessionTokenFound,
                                sessionToken);

            response = dispatcher(worker, request, responseBuilder);
            currentRequestId = request.getId();
            currentRequestId = null;
            errorFound = response.getError() != null;
        } catch (Exception exception) {
            errorFound = true;
            logger.catching(exception);
            ApiServerException apiException;
            if (exception instanceof ApiServerException) {
                apiException = (ApiServerException) exception;
            } else if(exception.getCause() != null && (exception.getCause() instanceof ThrowableReason)) {
                ThrowableReason throwableReason = (ThrowableReason) exception.getCause();
                apiException = new ApiServerException(currentRequestId, ApiServerErrorType.InternalError, throwableReason.getReason());
            } else if (exception instanceof InvocationTargetException) { // When calling methods using reflection the real cause is wrapped
                Throwable originalException = exception.getCause();
                ExceptionReason reason;
                if ((originalException != null) && (originalException.getCause() instanceof ThrowableReason)) {
                    ThrowableReason throwableReason = (ThrowableReason) originalException.getCause();
                    reason = (ExceptionReason) throwableReason.getReason();
                } else if(originalException != null) {
                    reason = ApiExceptions.UNKNOWN.getCause(originalException.getClass().getSimpleName(), originalException.getMessage());
                } else { // This error branch has never been hit during testing
                    reason = ApiExceptions.UNKNOWN.getCause(exception.getClass().getSimpleName(), exception.getMessage());
                }
                apiException = new ApiServerException(currentRequestId, ApiServerErrorType.InternalError, reason);
            } else { // This error branch has never been hit during testing
                ExceptionReason cause = ApiExceptions.UNKNOWN.getCause(exception.getClass().getSimpleName(), exception.getMessage());
                apiException = new ApiServerException(currentRequestId, ApiServerErrorType.InternalError, cause);
            }
            logger.throwing(apiException);
            throw apiException;
        } finally {
            checkIn(isValidInteractiveSession,
                    isValidInteractiveSessionFinished,
                    isValidNonInteractiveSession,
                    errorFound,
                    sessionTokenFound,
                    sessionToken,
                    worker);
        }

        return logger.traceExit(response);
    }

    private Worker<CONNECTION> checkOut(PerformanceAuditor performanceAuditor,
                                        boolean isValidTransactionManagerMode,
                                        boolean isValidInteractiveSession,
                                        boolean isValidInteractiveSessionFinished,
                                        boolean isValidNonInteractiveSession,
                                        boolean sessionTokenFound,
                                        String sessionToken) throws Exception {
        Worker<CONNECTION> worker = null;

        try {
            if (isValidInteractiveSession &&
                    sessionTokenFound &&
                    workersInUse.containsKey(sessionToken)) {
                worker = workersInUse.get(sessionToken);
                workersLastAccessed.put(worker, System.currentTimeMillis());
            } else {
                CONNECTION connection = connectionsPool.checkOut();
                worker = workersPool.checkOut();
                worker.createContext(performanceAuditor);
                worker.setConnection(connection);
                worker.setTransactionManagerMode(isValidTransactionManagerMode);

                if (sessionTokenFound) {
                    worker.setSessionToken(sessionToken);
                    workersInUse.put(sessionToken, worker);
                    workersLastAccessed.put(worker, System.currentTimeMillis());
                    if (isValidNonInteractiveSession) {
                        worker.begin(UUID.randomUUID());
                    }
                }
            }
        } catch (Exception exceptionAtCheckout) {
            checkIn(isValidInteractiveSession,
                    isValidInteractiveSessionFinished,
                    isValidNonInteractiveSession,
                    true,
                    sessionTokenFound,
                    sessionToken,
                    worker);
            throw exceptionAtCheckout;
        }

        return worker;
    }

    private void checkIn(boolean isValidInteractiveSession,
                             boolean isValidInteractiveSessionFinished,
                             boolean isValidNonInteractiveSession,
                             boolean errorFound,
                             boolean sessionTokenFound,
                             String sessionToken,
                             Worker<CONNECTION> worker) {
        try {
            if (sessionTokenFound) {
                if (isValidInteractiveSession) {
                    if (isValidInteractiveSessionFinished || errorFound) {
                        workersInUse.remove(sessionToken);
                        workersLastAccessed.remove(worker);
                    }
                }

                if (isValidNonInteractiveSession) {
                    workersInUse.remove(sessionToken);
                    workersLastAccessed.remove(worker);
                }
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        try {
            if (isValidNonInteractiveSession && !errorFound && sessionTokenFound) {
                worker.commit();
            }
        } catch (Exception ex) {
            logger.catching(ex);
        }

        boolean doCleanAndReturnWorker = isValidInteractiveSessionFinished ||
                isValidNonInteractiveSession ||
                errorFound;
        if (doCleanAndReturnWorker) {
            CONNECTION connection = null;

            try {
                connection = worker.getConnection(); // Connection saved before clean it
            } catch (Exception ex) {
                logger.catching(ex);
            }

            try {
                worker.cleanConnection();
            } catch (Exception ex) {
                logger.catching(ex);
            }

            try {
                connectionsPool.checkIn(connection);
            } catch (Exception ex) {
                logger.catching(ex);
            }

            try {
                worker.cleanContext();
            } catch (Exception ex) {
                logger.catching(ex);
            }

            try {
                workersPool.checkIn(worker);
            } catch (Exception ex) {
                logger.catching(ex);
            }
        }
    }

    //
    // Dispatcher, picks the correct handler and executes the method
    //

    private OUTPUT dispatcher(Worker<CONNECTION> api, Request request, ResponseBuilder<OUTPUT> responseBuilder) throws Exception {
        Method apiMethod = apiMethods.get(request.getMethod());
        Object[] requestParamsForApiMethod = null;

        if (apiMethod != null) {
            Parameter[] apiParams = apiMethodParameters.get(apiMethod);

            Map<String, Object> requestParams = request.getParams();

            // Parameters size check
            if ((requestParams == null && apiParams.length != 0) ||
                    (requestParams != null && apiParams.length != requestParams.size())) {
                throw new ApiServerException(request.getId(), IncorrectParameters, ApiExceptions.WRONG_PARAMETER_LIST_LENGTH.getCause());
            }

            // Parameters present check
            requestParamsForApiMethod = new Object[apiParams.length];
            for (int pIdx = 0; pIdx < apiParams.length; pIdx++) {
                Parameter parameter = apiParams[pIdx];
                Object requestParam = requestParams.get(parameter.getName());

                // Parameter present
                if (requestParam == null) {
                    throw new ApiServerException(request.getId(), IncorrectParameters, ApiExceptions.MISSING_METHOD_PARAMETER.getCause(parameter.getName(), apiMethod));
                }

                // Parameter of the expected type
                if (!parameter.getType().isInstance(requestParam)) {
                    throw new ApiServerException(request.getId(), IncorrectParameters, ApiExceptions.METHOD_PARAMETER_WRONG_TYPE.getCause(parameter.getName(), apiMethod));
                }

                requestParamsForApiMethod[pIdx] = requestParam;
            }

            observer.beforeAPICall(api, request.getMethod(), request.getParams());
            Object result = apiMethod.invoke(api, requestParamsForApiMethod);
            observer.afterAPICall(api, request.getMethod(), request.getParams());

            return responseBuilder.build(request.getId(), result);
        } else {
            throw new ApiServerException(request.getId(), MethodNotFound, ApiExceptions.METHOD_NOT_FOUND.getCause(request.getMethod()));
        }
    }

    //
    // Public API to request workers to the ApiServer by extensions
    //

    public Worker<CONNECTION> checkOut() throws Exception {
        PerformanceAuditor performanceAuditor = new PerformanceAuditor();
        performanceAuditor.start();
        return checkOut(performanceAuditor,
                false,
                false,
                false,
                true,
                false,
                null);
    }

    public void checkIn(boolean errorFound,
                        Worker<CONNECTION> worker) {
        checkIn(false,
                false,
                true,
                errorFound,
                worker.getSessionToken() != null,
                worker.getSessionToken(),
                worker);
    }

    private static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}