/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.startup;

public enum AtomicFileSystemServerParameter
{
    //
    // Logging
    //
    logFactoryClass,
    logConfigFile,
    serverObserver,
    //
    // Parameters from the AFS Library
    //
    jsonObjectMapperClass,
    writeAheadLogRoot,
    storageRoot,
    storageUuid,
    storageIncomingShareId,
    //
    // Parameters for the HTTP server
    //
    httpServerClass,
    httpServerUri,
    httpServerPort,
    httpMaxContentLength, // This is a low level package size used by the netty layer, helps to avoid DOS attacks discarding packages
    //
    // Parameters for the API server
    //
    maxReadSizeInBytes, // This is the chunk size used by the API, sizes between 1 and 6 megabytes are typical, anything bigger is unlikely to provide performance benefits because we are limited by the http package sizes
    authenticationInfoProviderClass,
    authenticationProxyCacheIdleTimeout,
    authorizationInfoProviderClass,
    authorizationProxyCacheIdleTimeout,
    poolSize,
    connectionFactoryClass,
    workerFactoryClass,
    publicApiInterface,
    apiServerInteractiveSessionKey,
    apiServerTransactionManagerKey,
    apiServerWorkerTimeout,
    apiServerObserver,
    //
    // openBIS connection
    //
    openBISUrl,
    openBISTimeout,
    openBISUser,
    openBISPassword,
    openBISLastSeenDeletionFile,
    openBISLastSeenDeletionBatchSize,
    openBISLastSeenDeletionIntervalInSeconds
}