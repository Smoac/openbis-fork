/*
 * Copyright 2012 ETH Zuerich, CISD
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
//
//  CISDOBIpadServiceManager.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/30/12.
//
//


#import "CISDOBIpadServiceManager.h"
#import "CISDOBIpadServiceManagerInternal.h"
#import "CISDOBIpadService.h"
#import "CISDOBIpadServiceInternal.h"
#import "CISDOBIpadEntity.h"
#import "CISDOBConnection.h"

NSString *const CISDOBIpadServiceWillLoginNotification = @"CISDOBIpadServiceWillLoginNotification";
NSString *const CISDOBIpadServiceDidLoginNotification = @"CISDOBIpadServiceDidLoginNotification";
NSString *const CISDOBIpadServiceWillRetrieveRootLevelEntitiesNotification = @"CISDOBIpadServiceWillRetrieveRootLevelEntitiesNotification";
NSString *const CISDOBIpadServiceDidRetrieveRootLevelEntitiesNotification = @"CISDOBIpadServiceDidRetrieveRootLevelEntitiesNotification";
NSString *const CISDOBIpadServiceWillDrillOnEntityNotification = @"CISDOBIpadServiceWillDrillOnEntityNotification";
NSString *const CISDOBIpadServiceDidDrillOnEntityNotification = @"CISDOBIpadServiceDidDrillOnEntityNotification";
NSString *const CISDOBIpadServiceWillRetrieveDetailsForEntityNotification = @"CISDOBIpadServiceWillRetrieveDetailsForEntityNotification";
NSString *const CISDOBIpadServiceDidRetrieveDetailsForEntityNotification = @"CISDOBIpadServiceDidRetrieveDetailsForEntityNotification";
NSString *const CISDOBIpadServiceWillSynchEntitiesNotification = @"CISDOBIpadServiceWillSynchEntitiesNotification";
NSString *const CISDOBIpadServiceDidSynchEntitiesNotification = @"CISDOBIpadServiceDidSynchEntitiesNotification";
NSString *const CISDOBIpadServiceWillSynchPruningEntitiesNotification = @"CISDOBIpadServiceWillSynchPruningEntitiesNotification";
NSString *const CISDOBIpadServiceDidSynchPruningEntitiesNotification = @"CISDOBIpadServiceDidSynchPruningEntitiesNotification";
NSString *const CISDOBIpadServiceWillSearchForEntitiesNotification = @"CISDOBIpadServiceWillSearchForEntitiesNotification";
NSString *const CISDOBIpadServiceDidSearchForEntitiesNotification = @"CISDOBIpadServiceDidSearchForEntitiesNotification";

NSString *const CISDOBIpadServiceManagerErrorDomain = @"CISDOBIpadServiceManagerErrorDomain";

// Internal class that synchronizes result data to the managed object context
@interface CISDOBBackgroundDataSynchronizer : NSObject

@property(weak, readonly) CISDOBIpadServiceManager *serviceManager;
@property(strong, readonly) CISDOBIpadServiceManagerCall *managerCall;
@property(strong, readonly) NSArray *rawEntities;
@property(strong, readonly) NSManagedObjectContext *managedObjectContext;
@property(copy, nonatomic) NSError *error;

// Initialization
- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager managerCall:(CISDOBIpadServiceManagerCall *)call rawEntities:(NSArray *)rawEntities;

// Actions
- (void)run;
- (void)notifyCallOfResult;

@end

// This class is not yet used, but is a sketch for seperating updates from pruning
@interface CISDOBBackgroundDataPruner : NSObject

@property(weak, readonly) CISDOBIpadServiceManager *serviceManager;
@property(strong, readonly) CISDOBIpadServiceManagerCall *managerCall;
@property(strong, readonly) NSManagedObjectContext *managedObjectContext;
@property(copy, nonatomic) NSError *error;

@property(strong, nonatomic) NSDate *pruneCutoffDate;
@property(readonly) NSArray *deletedEntityPermIds;

// Initialization
- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager managerCall:(CISDOBIpadServiceManagerCall *)call;

// Actions
- (void)run;
- (void)notifyCallOfResult;

@end

static NSManagedObjectContext* GetMainThreadManagedObjectContext(NSURL* storeUrl, NSError** error)
{
	// Explicitly specify which db schema we want to use
	NSBundle* bundle = [NSBundle bundleForClass: [CISDOBIpadEntity class]];
	NSString* momPath = [bundle pathForResource: @"persistent-data-model" ofType: @"momd"];
	NSManagedObjectModel* mom = [[NSManagedObjectModel alloc] initWithContentsOfURL: [NSURL fileURLWithPath: momPath]];

	NSManagedObjectContext* moc = [[NSManagedObjectContext alloc] initWithConcurrencyType: NSMainQueueConcurrencyType];
	NSPersistentStoreCoordinator* coordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel: mom];
	[moc setPersistentStoreCoordinator: coordinator];	
	NSPersistentStore* store = 
		[coordinator 
			addPersistentStoreWithType: NSSQLiteStoreType 
			configuration: nil 
			URL: storeUrl
			options: nil 
			error: error];
	if (!store) {
		return nil;
	}
	return moc;
}


@implementation CISDOBIpadServiceManager


- (id)initWithStoreUrl:(NSURL *)storeUrl openbisUrl:(NSURL *)openbisUrl trusted:(BOOL)trusted error:(NSError **)error
{
    if (!(self = [super init])) return nil;
    
    [self setOpenbisUrl: openbisUrl trusted: trusted];
    _storeUrl = [storeUrl copy];
    _managedObjectContext = GetMainThreadManagedObjectContext(self.storeUrl, error);
    _persistentStoreCoordinator = _managedObjectContext.persistentStoreCoordinator;
    if (!_managedObjectContext) return nil;
    
    _ipadEntityDescription = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: _managedObjectContext];
    _managedObjectModel = [_ipadEntityDescription managedObjectModel];
    
    _queue = [[NSOperationQueue alloc] init];
        // Restrict this queue to processing operations serially
    [_queue setMaxConcurrentOperationCount: 1];
    self.online = NO;
    self.syncDone = NO;
    return self;
}

- (NSURL *)openbisUrl
{
    if (![_service.connection isKindOfClass: [CISDOBLiveConnection class]]) return nil;
    
    return ((CISDOBLiveConnection *) _service.connection).url;

}

- (void)setOpenbisUrl:(NSURL *)openbisUrl trusted:(BOOL)trusted;
{
    if ([self.openbisUrl isEqual: openbisUrl]) return;
    
    CISDOBConnection *connection;
    if (openbisUrl) {
        connection = [[CISDOBLiveConnection alloc] initWithUrl: openbisUrl trusted: trusted];
    } else {
        connection = [[CISDOBDeadConnection alloc] init];
    }
    connection.delegate = self;
    
    _service = [[CISDOBIpadService alloc] initWithConnection: connection];
}

- (NSString *)sessionToken
{
   return ((CISDOBLiveConnection *)(self.service.connection)).sessionToken;
}

// Save the MOC. The deletedEntities array should be non-nil
- (BOOL)saveManagedObjectContextDeleting:(NSArray *)deletedEntities error:(NSError **)error
{
    if (self.mocSaveBlock) self.mocSaveBlock(self, deletedEntities);
    BOOL success = [self.managedObjectContext save: error];
    if (success) {
        if (self.mocPostSaveBlock) self.mocPostSaveBlock(self);
    }
    return success;
}

- (void)pruneEntitiesNotifying:(CISDOBIpadServiceManagerCall *)managerCall
{
    void (^syncBlock)(void) = ^{
        [[NSNotificationCenter defaultCenter] postNotificationName: CISDOBIpadServiceWillSynchEntitiesNotification object: self];
        
        CISDOBBackgroundDataPruner *pruner = [[CISDOBBackgroundDataPruner alloc] initWithServiceManager: self managerCall: managerCall];
        pruner.pruneCutoffDate = self.lastRootSetUpdateDate;
        [pruner run];

        
        [[NSNotificationCenter defaultCenter] postNotificationName: CISDOBIpadServiceDidSynchEntitiesNotification object: self];         
        
        void (^notifyBlock)(void) = ^ {
            // Save the MOC and notifiy the client on the main thread
            if(!pruner.error) {
                NSError *error = nil;
                [self saveManagedObjectContextDeleting: pruner.deletedEntityPermIds error: &error];
                pruner.error = error;
            }
            [pruner notifyCallOfResult];
        };
        [[NSOperationQueue mainQueue] addOperationWithBlock: notifyBlock];
    };
    [_queue addOperationWithBlock: syncBlock];
}

- (void)syncEntities:(NSArray *)rawEntities notifying:(CISDOBIpadServiceManagerCall *)managerCall
{
    void (^syncBlock)(void) = ^{
        [[NSNotificationCenter defaultCenter] postNotificationName: CISDOBIpadServiceWillSynchEntitiesNotification object: self];
        
        // Run the synchronizer in the background thread
        CISDOBBackgroundDataSynchronizer *synchronizer = [[CISDOBBackgroundDataSynchronizer alloc] initWithServiceManager: self managerCall: managerCall rawEntities: rawEntities];
        [synchronizer run];
        
        [[NSNotificationCenter defaultCenter] postNotificationName: CISDOBIpadServiceDidSynchEntitiesNotification object: self];         
        
        void (^notifyBlock)(void) = ^ {
            // Save the MOC and notifiy the client on the main thread
            if(!synchronizer.error) {
                NSError *error = nil;
                [self saveManagedObjectContextDeleting: [NSArray array] error: &error];
                synchronizer.error = error;
            }
            [synchronizer notifyCallOfResult];
        };
        [[NSOperationQueue mainQueue] addOperationWithBlock: notifyBlock];
    };
    [_queue addOperationWithBlock: syncBlock];
}

- (BOOL)shouldRetryCall:(CISDOBIpadServiceManagerCall *)managerCall onError:(NSError *)error
{
    // We can retry JsonRpc errors caused by invalid session tokens
    if (![CISOBJsonRpcErrorDomain isEqualToString: error.domain]) return false;

    NSDictionary *jsonErrorObject = [[error userInfo] objectForKey: CISOBJsonRpcErrorObjectKey];
    if (!jsonErrorObject) return false;
    NSDictionary *errorData = [jsonErrorObject objectForKey: @"data"];
    if (!errorData) return false;
    NSString *exceptionTypeName = [errorData objectForKey: @"exceptionTypeName"];
    if (!exceptionTypeName) return false;
    if (![@"ch.systemsx.cisd.common.exceptions.InvalidSessionException" isEqualToString: exceptionTypeName]) return false;
    
    return managerCall.retryCount < 1;
}

- (void)loginAndRetryCall:(CISDOBIpadServiceManagerCall *)managerCall
{
    if (!_username || !_password) return;
    
    NSString *oldSessionToken = self.service.connection.sessionToken;
    
    // Login and then retry the call
    managerCall.retryCount =  managerCall.retryCount + 1;
    CISDOBAsyncCall *call = [self.service.connection loginUser: _username password: _password];
    managerCall.loginRetryCall = call;
    call.success = ^(id result) {
        // Fix the session token
        CISDOBIpadServiceCall *serviceCall = (CISDOBIpadServiceCall *)managerCall.serviceCall;
        [serviceCall replaceSessionToken: oldSessionToken with: result];
        [managerCall start];
    };
    call.fail = ^(NSError *error) { [managerCall notifyFailure: error]; };
    [call start];
}

- (void)initializeFailureBlockOnServiceCall:(CISDOBAsyncCall *)serviceCall managerCall:(CISDOBIpadServiceManagerCall *)managerCall
{
    __weak CISDOBIpadServiceManager *weakSelf = self;
    
    serviceCall.fail = ^(NSError *error) {
        if ([weakSelf shouldRetryCall: managerCall onError: error]) {
            [self loginAndRetryCall: managerCall];
            return;
        }
        
        // Check the error -- the server could be unavailable
        if (IsSomeKindOfNetworkConnectionError(error)) {
            // "Could not connect to the server"
            weakSelf.online = NO;
        }
        [managerCall notifyFailure: error];
    };
}

- (CISDOBIpadServiceManagerCall *)managerCallWrappingServiceCall:(CISDOBAsyncCall *)serviceCall
{
    CISDOBIpadServiceManagerCall *managerCall = [[CISDOBIpadServiceManagerCall alloc] initWithServiceManager: self serviceCall: serviceCall];
    
    __weak CISDOBIpadServiceManager *weakSelf = self;
    
    serviceCall.success = ^(id result) {
        weakSelf.online = YES;
        // Update the cache and call the managerCall success when done
        [weakSelf syncEntities: result notifying: managerCall];
    };    
    
    [self initializeFailureBlockOnServiceCall: serviceCall managerCall: managerCall];
    
    return managerCall;
}

- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password
{
    CISDOBAsyncCall *call = [self.service loginUser: user password: password];
    // Remember the username and password so we can reauthenticate if necessary
    _username = user;
    _password = password;
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call];
    
    __weak CISDOBIpadServiceManager *weakSelf = self;
    call.success = ^(id result) { weakSelf.online = YES; [managerCall notifySuccess: result]; };
    managerCall.willCallNotificationName = CISDOBIpadServiceWillLoginNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidLoginNotification;
    return managerCall;
}

- (BOOL)shouldRefreshRootLevelEntitiesCall
{
    if (!self.lastRootSetUpdateDate) return YES;
    if (!self.service.clientPreferences) return YES;
    NSTimeInterval rootSetRefreshInterval = self.service.clientPreferences.rootSetRefreshInterval;
    if ([[NSDate date] timeIntervalSinceDate: self.lastRootSetUpdateDate] < rootSetRefreshInterval) return NO;
    return YES;
}

- (CISDOBAsyncCall *)retrieveRootLevelEntitiesFromServer
{
    CISDOBAsyncCall *call = [self.service listNavigationalEntities];
    
    CISDOBIpadServiceManagerCall *managerCall = [[CISDOBIpadServiceManagerCall alloc] initWithServiceManager: self serviceCall: call];
    // Save the update date to be the time the call is constructed (i.e., before the call is made)
    NSDate *timeOfCallConstruction = [NSDate date];
    
    __weak CISDOBIpadServiceManager *weakSelf = self;
    
    call.success = ^(id result) {
        weakSelf.online = YES;
        self.lastRootSetUpdateDate = timeOfCallConstruction;
        CISDOBIpadServiceManagerRetrieveRootSetCommand *command = [[CISDOBIpadServiceManagerRetrieveRootSetCommand alloc] init];
        command.serviceManager = weakSelf;
        command.serviceManagerCall = managerCall;
        command.topLevelNavigationEntities = result;
        [command run];
    };    
    
    [self initializeFailureBlockOnServiceCall: call managerCall: managerCall];
    
    managerCall.willCallNotificationName = CISDOBIpadServiceWillRetrieveRootLevelEntitiesNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidRetrieveRootLevelEntitiesNotification;
    
    return managerCall;
}

- (CISDOBAsyncCall *)retrieveRootLevelEntities
{
    if ([self shouldRefreshRootLevelEntitiesCall]) return [self retrieveRootLevelEntitiesFromServer];
    
    // Make up a dummy call
    CISDOBAsyncCall *call = [self.service heartbeat];
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call];

    return managerCall;
}

- (CISDOBAsyncCall *)drillOnEntity:(CISDOBIpadEntity *)entity
{
    CISDOBAsyncCall *call = [self.service drillOnEntityWithPermId: entity.permId refcon: entity.refcon];
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call];
    
    managerCall.willCallNotificationName = CISDOBIpadServiceWillDrillOnEntityNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidDrillOnEntityNotification;
    
    return managerCall;
}

- (CISDOBAsyncCall *)detailsForEntity:(CISDOBIpadEntity *)entity
{
    CISDOBAsyncCall *call = [self.service detailsForEntityWithPermId: entity.permId refcon: entity.refcon];
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call];
    
    managerCall.willCallNotificationName = CISDOBIpadServiceWillRetrieveDetailsForEntityNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidRetrieveDetailsForEntityNotification;
    
    return managerCall;
}

- (CISDOBAsyncCall *)imagesForEntity:(CISDOBIpadEntity *)entity
{
    CISDOBImageRetrievalCall *call = [[CISDOBImageRetrievalCall alloc] initWithServiceManager: self entity: entity];
    return call;
}

- (CISDOBAsyncCall *)drillOnEntities:(NSArray *)entities
{
    NSMutableArray *refcons = [NSMutableArray arrayWithCapacity: [entities count]];
    for (CISDOBIpadEntity *entity in entities) {
        [refcons addObject: entity.refcon];
    }
    
    CISDOBAsyncCall *call = [self.service drillOnEntities: entities refcons: refcons];
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call];
    
    managerCall.willCallNotificationName = CISDOBIpadServiceWillDrillOnEntityNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidDrillOnEntityNotification;
    
    return managerCall;
}

- (CISDOBAsyncCall *)detailsForEntities:(NSArray *)entities
{
    NSMutableArray *refcons = [NSMutableArray arrayWithCapacity: [entities count]];
    for (CISDOBIpadEntity *entity in entities) {
        [refcons addObject: entity.refcon];
    }
    
    CISDOBAsyncCall *call = [self.service detailsForEntities: entities refcons: refcons];
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call];
    
    managerCall.willCallNotificationName = CISDOBIpadServiceWillRetrieveDetailsForEntityNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidRetrieveDetailsForEntityNotification;
    
    return managerCall;
}

- (CISDOBAsyncCall *)searchForText:(NSString *)searchText domain:(id)searchDomain
{
    CISDOBAsyncCall *call = [self.service searchForText: searchText domain: nil];
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call];
    
    managerCall.willCallNotificationName = CISDOBIpadServiceWillSearchForEntitiesNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidSearchForEntitiesNotification;
    
    return managerCall;
}

- (NSArray *)allIpadEntitiesOrError:(NSError **)error;
{
	NSFetchRequest* request = [self fetchRequestForEntities];
	return [self executeFetchRequest: request error: error];
}

- (NSArray *)entitiesByPermId:(NSArray *)permIds error:(NSError **)error
{
    NSFetchRequest *request = [self fetchRequestForEntitiesByPermId: permIds];
    NSArray *results = [self executeFetchRequest: request error: error];
    
    //1. Put results on a map with the format <permId, result>
    NSMutableDictionary *resultsWithKeys = [NSMutableDictionary dictionaryWithCapacity:permIds.count];
    
    for (CISDOBIpadEntity *entity in results) {
        [resultsWithKeys setObject:entity forKey:entity.permId];
    }

    //2. Iterate over permIds to get the things from the map in order to create a new array with the correct order
    NSMutableArray *sortedResults = [NSMutableArray arrayWithCapacity:permIds.count];
    for (id key in permIds) {
        CISDOBIpadEntity *entity = [resultsWithKeys objectForKey:key];
        [sortedResults addObject:entity];
    }
    
    return sortedResults;
}

- (NSArray *)entitiesNotUpdatedSince:(NSDate *)date error:(NSError **)error
{
    NSFetchRequest *request = [self fetchRequestForEntitiesNotUpdatedSince: date];
    return [self executeFetchRequest: request error: error];
}

- (NSFetchRequest *)fetchRequestForEntities
{
	NSFetchRequest *request = [[NSFetchRequest alloc] init];
    [request setEntity: self.ipadEntityDescription];
    return request;
}

- (NSArray *)executeFetchRequest:(NSFetchRequest *)fetchRequest error:(NSError **)error
{
    return [self.managedObjectContext executeFetchRequest: fetchRequest error: error];
}

- (NSFetchRequest *)fetchRequestForEntitiesByPermId:(NSArray *)permIds
{
    NSDictionary *fetchVariables = [NSDictionary dictionaryWithObject: permIds forKey: @"PERM_IDS"];
    NSFetchRequest *request = [self.managedObjectModel fetchRequestFromTemplateWithName: @"EntitiesByPermIds" substitutionVariables: fetchVariables];
    return request;
}

- (NSFetchRequest *)fetchRequestForEntitiesNotUpdatedSince:(NSDate *)date
{
    NSDictionary *fetchVariables = [NSDictionary dictionaryWithObject: date forKey: @"LAST_UPDATE_DATE"];
    NSFetchRequest *request = [self.managedObjectModel fetchRequestFromTemplateWithName: @"EntitiesNotUpdatedSince" substitutionVariables: fetchVariables];
    return request;
}

// CISDOBAsyncCallDelegate
- (void)asyncCall:(CISDOBAsyncCall *)call didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
    if (self.authenticationChallengeBlock) {
        self.authenticationChallengeBlock(call, challenge);
    } else {
        [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge];
    }
}

@end

@implementation CISDOBIpadServiceManagerCall

- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager serviceCall:(CISDOBAsyncCall *)call
{
    if (!(self = [super init])) return nil;
 
    _serviceManager = serviceManager;
    _serviceCall = call;
    self.timeoutInterval = call.timeoutInterval;
    self.retryCount = 0;
    
    return self;
}

- (void)sendCompletionNotification:(NSError *)errorOrNil
{
    if (self.didCallNotificationName) {
        NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
        if (errorOrNil) [userInfo setValue: errorOrNil forKey: NSUnderlyingErrorKey];
        [[NSNotificationCenter defaultCenter] postNotificationName: self.didCallNotificationName object: self.serviceManager userInfo: userInfo];
    }
}

- (void)notifySuccess:(id)result
{
    [self sendCompletionNotification: nil];
    if (self.success) self.success(result);
}

- (void)notifyFailure:(NSError *)error
{
    [self sendCompletionNotification: error];
    if (self.fail) self.fail(error);
}

- (void)start
{
    if (self.willCallNotificationName) {
        [[NSNotificationCenter defaultCenter] postNotificationName: self.willCallNotificationName object: self.serviceManager];
    }
    _serviceCall.timeoutInterval = self.timeoutInterval;
    [_serviceCall start];
}

@end

@implementation CISDOBBackgroundDataSynchronizer

// Initialization
- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager managerCall:(CISDOBIpadServiceManagerCall *)call rawEntities:(NSArray *)rawEntities
{
    if (!(self = [super init])) return nil;
    
    _serviceManager = serviceManager;
    _managerCall = call;
    _rawEntities = rawEntities;
    _managedObjectContext = [[NSManagedObjectContext alloc] initWithConcurrencyType: NSConfinementConcurrencyType];
    _managedObjectContext.parentContext = _serviceManager.managedObjectContext;
    _error = nil;
    
    return self;
}

- (BOOL)synchEntity:(CISDOBIpadRawEntity *)rawEntity lastUpdateDate:(NSDate *)date error:(NSError **)error
{
    // Create new entities in the moc, and store them.
    CISDOBIpadEntity *entity;
    NSFetchRequest *fetchRequest = [self.serviceManager fetchRequestForEntitiesByPermId: [NSArray arrayWithObject: rawEntity.permId]];
    // Run the fetch request against our own MOC -- running it against the serviceManager's MOC will cause problems (deadlocks)
    NSArray *matchedEntities = [self.managedObjectContext executeFetchRequest: fetchRequest error: error];
    if (!matchedEntities) return NO;
    if ([matchedEntities count] > 0) {
        entity = [matchedEntities objectAtIndex: 0];
        [entity updateFromRawEntity: rawEntity];
    } else {
        entity = [NSEntityDescription insertNewObjectForEntityForName: @"CISDOBIpadEntity" inManagedObjectContext: self.managedObjectContext];
        [entity initializeFromRawEntity: rawEntity];
    }
    entity.lastUpdateDate = date;
    entity.serverUrlString =  [((CISDOBLiveConnection *)(self.serviceManager.service.connection)).url absoluteString];
    
    return YES;
}


- (void)run
{
    NSError *error;
    NSDate *lastUpdateDate = [NSDate date];
    BOOL success;
    for (CISDOBIpadRawEntity *rawEntity in self.rawEntities) {
        success = [self synchEntity: rawEntity lastUpdateDate: lastUpdateDate error: &error];
        if (!success) {
            self.error = error;
            return;
        }
    }
    // If pruning is requested, remove entities that were not updated since the prune cutoff date
//    if (_prune && self.pruneCutoffDate) {
//        // Remove all entities that were not mentioned
//        NSFetchRequest *fetchRequest = [self.serviceManager fetchRequestForEntitiesNotUpdatedSince: self.pruneCutoffDate];
//        NSArray *entitiesToDelete = [self.managedObjectContext executeFetchRequest: fetchRequest error: &error];
//        for (CISDOBIpadEntity *entity in entitiesToDelete) {
//            [(NSMutableArray *)_deletedEntities addObject: entity.permId];
//            [self.managedObjectContext deleteObject: entity];
//        }
//    }
    
    success = [self.managedObjectContext save: &error];
    if (!success) {
        self.error = error;
        return;
    }
    
    self.error = nil;
}

- (void)notifyCallOfResult
{
    if (self.error) {
        [self.managerCall notifyFailure: self.error];
    } else if (self.managerCall.success) {
        [self.managerCall notifySuccess: self.rawEntities];
    }
    
}

@end

// This class is not yet used.
@implementation CISDOBBackgroundDataPruner

// Initialization
- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager managerCall:(CISDOBIpadServiceManagerCall *)call
{
    if (!(self = [super init])) return nil;
    
    _serviceManager = serviceManager;
    _managerCall = call;
    _managedObjectContext = [[NSManagedObjectContext alloc] initWithConcurrencyType: NSConfinementConcurrencyType];
    _managedObjectContext.parentContext = _serviceManager.managedObjectContext;
    _error = nil;
    _deletedEntityPermIds = [NSMutableArray array];
    
    return self;
}

// Actions
- (void)run
{
    if (!self.pruneCutoffDate) return;
    [[NSNotificationCenter defaultCenter] postNotificationName: CISDOBIpadServiceWillSynchPruningEntitiesNotification object: self];
    BOOL success;
    NSError *error;
    // Remove entities that were not updated since the prune cutoff date
    // Remove all entities that were not mentioned
    
    // TODO Do not pune any more (change still in progress...)
//    NSFetchRequest *fetchRequest = [self.serviceManager fetchRequestForEntitiesNotUpdatedSince: self.pruneCutoffDate];
//    NSArray *entitiesToDelete = [self.managedObjectContext executeFetchRequest: fetchRequest error: &error];
//    for (CISDOBIpadEntity *entity in entitiesToDelete) {
//        [(NSMutableArray *)_deletedEntityPermIds addObject: entity.permId];
//        [self.managedObjectContext deleteObject: entity];
//    }
    
    // Could be an issue -- synchDone variable not updated in the main thread
    self.serviceManager.syncDone = YES;
    [[NSNotificationCenter defaultCenter] postNotificationName: CISDOBIpadServiceDidSynchPruningEntitiesNotification object: self];
    success = [self.managedObjectContext save: &error];
    if (!success) {
        self.error = error;
        return;
    }
    
    self.error = nil;
}

- (void)notifyCallOfResult
{
    if (self.error) {
        [self.managerCall notifyFailure: self.error];
    } else if (self.managerCall.success) {
        [self.managerCall notifySuccess: [NSArray array]];
    }
    
}

@end

@implementation CISDOBIpadImage


@end

@implementation CISDOBImageRetrievalCall
// This implementation of the CISDOBImageRetrievalCall is designed to just get one
// image. In the future, it will need to be modified to get many images, by storing a collection of responseData and connections, one for each image to retrieve.

- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager entity:(CISDOBIpadEntity *)entity
{
    if (!(self = [super init])) return nil;
 
    self.serviceManager = serviceManager;
    self.entity = entity;
        // Default timeout interval to 60s
    self.timeoutInterval = 60.0;
    
    _image = [[CISDOBIpadImage alloc] init];
    _image.imageData = [[NSMutableData alloc] init];
    
    return self;
}

- (NSMutableData *)responseData { return (NSMutableData *)_image.imageData; }

- (void)couldNotCreateConnection
{
    NSDictionary *userInfo =
        [NSDictionary dictionaryWithObjectsAndKeys: @"Could not connect to server", NSLocalizedDescriptionKey, nil];
    NSError *error = [NSError errorWithDomain: CISDOBIpadServiceManagerErrorDomain code: kCISDOBIpadServiceManagerError_ImageRetrievalCouldNotConnectToServer userInfo: userInfo];
    if (_fail) _fail(error);
}

- (void)start
{
    NSString *urlString = self.entity.imageUrlString;
    if (!urlString || [urlString length] < 1) {
        [[self responseData] setLength: 0];
        if (_success) _success(_image);
        return;
    }
    
    NSURL *url;
    // if this is a datastore_server url, add the session token
    NSRange dataStoreServerRange = [urlString rangeOfString: @"datastore_server"];
    if (dataStoreServerRange.length == 0) {
        url = [NSURL URLWithString: urlString];
    } else {
        NSMutableString *urlStringWithSession = [NSMutableString stringWithString: urlString];
        [urlStringWithSession appendFormat: @"?sessionID=%@", self.serviceManager.sessionToken];
        url = [NSURL URLWithString: urlStringWithSession];
    }

    NSMutableURLRequest *request = 
        [NSMutableURLRequest requestWithURL: url cachePolicy: NSURLRequestUseProtocolCachePolicy timeoutInterval: self.timeoutInterval];

    // Check that the connection can be created
    if (![NSURLConnection canHandleRequest: request]) {
        [self couldNotCreateConnection];
        return;
    } 
    
    _connection =
        [NSURLConnection connectionWithRequest: request delegate: self];
        
    if (!_connection) {
        [self couldNotCreateConnection];
        return;
    }
}

@end


@implementation CISDOBImageRetrievalCall (NSURLConnectionDelegate)

- (BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace
{
    return [protectionSpace.authenticationMethod isEqualToString: NSURLAuthenticationMethodServerTrust];
}

- (void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
    if ([challenge.protectionSpace.authenticationMethod isEqualToString: NSURLAuthenticationMethodServerTrust])
	{
        [self.serviceManager asyncCall: self didReceiveAuthenticationChallenge: challenge];
        return;
	}
    [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge]; 
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    _image.MIMEType = [response MIMEType];
    _image.textEncodingName = [response textEncodingName];
    _image.url = [response URL];
    [[self responseData] setLength: 0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    [[self responseData] appendData: data];
}


- (void)connectionDidFinishLoading:(NSURLConnection *)aConnection
{
    if (_success) _success(_image);
}


- (void)connection:(NSURLConnection *)aConnection didFailWithError:(NSError *)error
{
    if (_fail) _fail(error);
    _connection = nil;
}

@end

@implementation CISDOBIpadServiceManagerRetrieveRootSetCommand

- (void)runNextCall
{
    NSUInteger currentIndex = self.currentIndex, count = [self.topLevelNavigationEntities count];
    CISDOBIpadEntity *navEntity = [self.topLevelNavigationEntities objectAtIndex:  currentIndex];

    NSArray *permIds = [NSArray arrayWithObject: navEntity.permId];
    NSArray *refcons = [NSArray arrayWithObject: navEntity.refcon];
    CISDOBAsyncCall *call = [self.serviceManager.service listChangesSince: self.serviceManager.lastRootSetUpdateDate rootLevelEntity: permIds refcons: refcons];
    call.success = ^(id result) {
        [self.serviceManager syncEntities: result notifying: nil];
        if (currentIndex+1 == count) {
            [self.serviceManager pruneEntitiesNotifying: self.serviceManagerCall];
        } else {
            [self runNextCall];
        }
    };    
    [self.serviceManager initializeFailureBlockOnServiceCall: call managerCall: self.serviceManagerCall];
    self.currentIndex = currentIndex + 1;
    [call start];
}

- (void)run
{
    self.currentIndex = 0;
    if ([self.topLevelNavigationEntities count] < 1) {
        // Did not retrieve the topLevelNavigationEntities. Just call
        // listRootLevelEntites without arguments
        NSArray *permIds = [NSArray array];
        NSArray *refcons = [NSArray array];
        CISDOBAsyncCall *call = [self.serviceManager.service listChangesSince: self.serviceManager.lastRootSetUpdateDate rootLevelEntity: permIds refcons: refcons];
    
        call.success = ^(id result) {
           [self.serviceManager syncEntities: result notifying: nil];
           [self.serviceManager pruneEntitiesNotifying: self.serviceManagerCall];
        };
        [self.serviceManager initializeFailureBlockOnServiceCall: call managerCall: self.serviceManagerCall];
        [call start];
        return;
    }
    
    // First add the navigation entities
    [self.serviceManager syncEntities: self.topLevelNavigationEntities notifying: nil];
    
    // Go through each of the navigation entities and get the roots for them
    [self runNextCall];
}

@end
