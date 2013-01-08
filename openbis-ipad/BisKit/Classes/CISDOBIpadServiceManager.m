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

NSString *const CISDOBIpadServiceManagerErrorDomain = @"CISDOBIpadServiceManagerErrorDomain";

// Internal class that synchronizes result data to the managed object context
@interface CISDOBBackgroundDataSynchronizer : NSObject

@property(readonly, weak) CISDOBIpadServiceManager *serviceManager;
@property(readonly, strong) CISDOBIpadServiceManagerCall *managerCall;
@property(readonly, strong) NSArray *rawEntities;
@property(readonly, strong) NSManagedObjectContext *managedObjectContext;
@property(nonatomic, copy) NSError *error;

@property(nonatomic) BOOL prune;

// Initialization
- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager managerCall:(CISDOBIpadServiceManagerCall *)call rawEntities:(NSArray *)rawEntities;

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
    
    CISDOBConnection *connection;
    if (openbisUrl) {
        connection = [[CISDOBLiveConnection alloc] initWithUrl: openbisUrl trusted: trusted];
    } else {
        connection = [[CISDOBDeadConnection alloc] init];
    }
    
    _storeUrl = [storeUrl copy];
    _service = [[CISDOBIpadService alloc] initWithConnection: connection];
    _managedObjectContext = GetMainThreadManagedObjectContext(self.storeUrl, error);
    _persistentStoreCoordinator = _managedObjectContext.persistentStoreCoordinator;
    if (!_managedObjectContext) return nil;
    
    _ipadEntityDescription = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: _managedObjectContext];
    _managedObjectModel = [_ipadEntityDescription managedObjectModel];
    
    _queue = [[NSOperationQueue alloc] init];
    
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
    
    _service = [[CISDOBIpadService alloc] initWithConnection: connection];
}

- (NSString *)sessionToken
{
   return ((CISDOBLiveConnection *)(self.service.connection)).sessionToken;
}

- (void)syncEntities:(NSArray *)rawEntities pruning:(BOOL)prune notifying:(CISDOBIpadServiceManagerCall *)managerCall
{
    void (^syncBlock)(void) = ^{
        [[NSNotificationCenter defaultCenter] postNotificationName: CISDOBIpadServiceWillSynchEntitiesNotification object: self];
        
        // Run the synchronizer in the background thread
        CISDOBBackgroundDataSynchronizer *synchronizer = [[CISDOBBackgroundDataSynchronizer alloc] initWithServiceManager: self managerCall: managerCall rawEntities: rawEntities];
        synchronizer.prune = prune;
        [synchronizer run];
        
        [[NSNotificationCenter defaultCenter] postNotificationName: CISDOBIpadServiceDidSynchEntitiesNotification object: self];         
        
        void (^notifyBlock)(void) = ^ {
            // Save the MOC and notifiy the client on the main thread
            CISDOBBackgroundDataSynchronizer *notifySynchronizer = synchronizer;
            if(!notifySynchronizer.error) {
                NSError *error;
                if (![self.managedObjectContext save: &error]) {
                    notifySynchronizer.error = error;
                }
            }
            [notifySynchronizer notifyCallOfResult];
        };
        [[NSOperationQueue mainQueue] addOperationWithBlock: notifyBlock];
    };
    [_queue addOperationWithBlock: syncBlock];
}

- (CISDOBIpadServiceManagerCall *)managerCallWrappingServiceCall:(CISDOBAsyncCall *)serviceCall pruning:(BOOL)prune
{
    CISDOBIpadServiceManagerCall *managerCall = [[CISDOBIpadServiceManagerCall alloc] initWithServiceManager: self serviceCall: serviceCall];
    
    serviceCall.success = ^(id result) {
        // Update the cache and call the managerCall success when done
        [self syncEntities: result pruning: prune notifying: managerCall];
    };    
    
    serviceCall.fail = ^(NSError *error) { [managerCall notifyFailure: error]; };
    
    return managerCall;
}

- (CISDOBIpadServiceManagerCall *)managerCallWrappingServiceCall:(CISDOBAsyncCall *)serviceCall
{
    return [self managerCallWrappingServiceCall: serviceCall pruning: NO];
}

- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password
{
    CISDOBAsyncCall *call = [self.service loginUser: user password: password];
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call pruning: NO];
    call.success = ^(id result) { [managerCall notifySuccess: result]; };
    managerCall.willCallNotificationName = CISDOBIpadServiceWillLoginNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidLoginNotification;
    return managerCall;
}

- (CISDOBAsyncCall *)retrieveRootLevelEntities
{
    CISDOBAsyncCall *call = [self.service listRootLevelEntities];
        // get rid of entities not mentioned in the original call
    CISDOBIpadServiceManagerCall *managerCall = [self managerCallWrappingServiceCall: call pruning: YES];
    
    managerCall.willCallNotificationName = CISDOBIpadServiceWillRetrieveRootLevelEntitiesNotification;
    managerCall.didCallNotificationName = CISDOBIpadServiceDidRetrieveRootLevelEntitiesNotification;
    
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

- (NSArray *)allIpadEntitiesOrError:(NSError **)error;
{
	NSFetchRequest* request = [self fetchRequestForEntities];
	return [self executeFetchRequest: request error: error];
}

- (NSArray *)entitiesByPermId:(NSArray *)permIds error:(NSError **)error
{
    NSFetchRequest *request = [self fetchRequestForEntitiesByPermId: permIds];
    return [self executeFetchRequest: request error: error];
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

@end

@implementation CISDOBIpadServiceManagerCall

- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager serviceCall:(CISDOBAsyncCall *)call
{
    if (!(self = [super init])) return nil;
 
    _serviceManager = serviceManager;
    _serviceCall = call;
    self.timeoutInterval = call.timeoutInterval;
    
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
    _prune = NO;
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
    // If pruning is requested, remove entities that cannot be reached from the server result set.
    // NOTE: This is a simplified implementation. A better solution would be to treat the intial results as a root set and trace out to do a gc, but the simpler implementation is just to remove everything that is not mentioned. We do the latter here.
    if (_prune) {
        // Remove all entities that were not mentioned
        NSFetchRequest *fetchRequest = [self.serviceManager fetchRequestForEntitiesNotUpdatedSince: lastUpdateDate];
        NSArray *entitiesToDelete = [self.managedObjectContext executeFetchRequest: fetchRequest error: &error];
        for (CISDOBIpadEntity *entity in entitiesToDelete) {
            [self.managedObjectContext deleteObject: entity];
        }
    }
    
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

@implementation CISDOBIpadImage

- (id)initWithImageData:(NSData *)imageData
{
    if (!(self = [super init])) return nil;
    
    _imageData = imageData;
    
    return self;
}
@end

@implementation CISDOBImageRetrievalCall
// This implementation of the CISDOBImageRetrievalCall is designed to just get one
// image. In the future, it will need to be modified to get many images, by storing a collection of responseData and connections, one for each image to retrieve.

- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager entity:(CISDOBIpadEntity *)entity
{
    if (!(self = [super init])) return nil;
 
    self.serviceManager = serviceManager;
    self.entity = entity;
        // Default timeout interval to 60sandrei
    self.timeoutInterval = 60.0;
    
    _responseData = [[NSMutableData alloc] init];
    
    return self;
}

- (void)couldNotCreateConnection
{
    NSDictionary *userInfo =
        [NSDictionary dictionaryWithObjectsAndKeys: @"Could not connect to server", NSLocalizedDescriptionKey, nil];
    NSError *error = [NSError errorWithDomain: CISDOBIpadServiceManagerErrorDomain code: kCISDOBIpadServiceManagerError_ImageRetrievalCouldNotConnectToServer userInfo: userInfo];
    if (_fail) _fail(error);
}

- (void)start
{
    NSURL *url = [NSURL URLWithString: self.entity.imageUrlString];
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
    // TODO: check with the service if the server can be trusted
//        if (SHOULD_CALL_DELEGATE_SELECTOR(jsonRpcCall:canTrustHost:))
//        {
//            if ([_delegate jsonRpcCall: self canTrustHost: challenge.protectionSpace.host]) {
//            // Tell the connection to trust this host
			NSURLCredential *credential = [NSURLCredential credentialForTrust: challenge.protectionSpace.serverTrust];
			[challenge.sender useCredential: credential forAuthenticationChallenge: challenge];
// }
//		}
	}
    [challenge.sender continueWithoutCredentialForAuthenticationChallenge: challenge]; 
}

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    [_responseData setLength: 0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    [_responseData appendData: data];
}


- (void)connectionDidFinishLoading:(NSURLConnection *)aConnection
{
    CISDOBIpadImage *image = [[CISDOBIpadImage alloc] initWithImageData: _responseData];
    if (_success) _success(image);
}


- (void)connection:(NSURLConnection *)aConnection didFailWithError:(NSError *)error
{
    if (_fail) _fail(error);
    _connection = nil;
}


@end
