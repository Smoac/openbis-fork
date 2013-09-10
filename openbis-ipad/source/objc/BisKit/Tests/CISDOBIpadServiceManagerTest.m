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
//  CISDOBIpadServiceManagerTest.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/30/12.
//
//

#import "CISDOBIpadServiceManagerTest.h"
#import "CISDOBIpadServiceManager.h"
#import "CISDOBIpadServiceManagerInternal.h"
#import "CISDOBIpadServiceInternal.h"
#import "CISDOBIpadService.h"
#import "CISDOBIpadServiceInternal.h"
#import "CISDOBConnection.h"
#import "CISDOBConnectionInternal.h"
#import "CISDOBIpadEntity.h"
#import "CISDOBIpadServerInfo.h"

static BOOL IsPermIdCompound(NSString *permId)
{
    return [permId hasSuffix: @"5HT_COMPOUND)"];
}

static BOOL IsPermIdTarget(NSString *permId)
{
    return [permId hasSuffix: @"5HT_TARGET)"];
}

@implementation CISDOBIpadServiceManagerTest

- (void)processNotification:(NSNotification *)note
{
    if ([CISDOBIpadServiceWillLoginNotification isEqualToString: [note name]]) {
        self.willLogin = YES;
    }
    if ([CISDOBIpadServiceDidLoginNotification isEqualToString: [note name]]) {
        self.didLogin = YES;
    }
    if ([CISDOBIpadServiceWillRetrieveRootLevelEntitiesNotification isEqualToString: [note name]]) {
        self.willRetrieveRootLevel = YES;
    }
    if ([CISDOBIpadServiceDidRetrieveRootLevelEntitiesNotification isEqualToString: [note name]]) {
        self.didRetrieveRootLevel = YES;
    }
    if ([CISDOBIpadServiceWillSynchEntitiesNotification isEqualToString: [note name]]) {
        self.willSynchEntities = YES;
    }
    if ([CISDOBIpadServiceDidSynchEntitiesNotification isEqualToString: [note name]]) {
        self.didSynchEntities = YES;
    }
    if ([CISDOBIpadServiceWillDrillOnEntityNotification isEqualToString: [note name]]) {
        self.willDrill = YES;
    }
    if ([CISDOBIpadServiceDidDrillOnEntityNotification isEqualToString: [note name]]) {
        self.didDrill = YES;
    }
    if ([CISDOBIpadServiceWillRetrieveDetailsForEntityNotification isEqualToString: [note name]]) {
        self.willRetrieveDetails = YES;
    }
    if ([CISDOBIpadServiceDidRetrieveDetailsForEntityNotification isEqualToString: [note name]]) {
        self.didRetrieveDetails = YES;
    }
    if ([CISDOBIpadServiceWillSearchForEntitiesNotification isEqualToString: [note name]]) {
        self.willSearch = YES;
    }
    if ([CISDOBIpadServiceDidSearchForEntitiesNotification isEqualToString: [note name]]) {
        self.didSearch = YES;
    }
}

- (void)registerForNotifications
{
    self.willLogin = NO;
    self.didLogin = NO;
    
    self.willRetrieveRootLevel = NO;
    self.didRetrieveRootLevel = NO;
    self.willSynchEntities = NO;
    self.didSynchEntities = NO;

    self.willDrill = NO;
    self.didDrill = NO;

    self.willRetrieveDetails = NO;
    self.didRetrieveDetails = NO;
    
    NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
    [notificationCenter
        addObserverForName: nil
        object: self.serviceManager
        queue: [NSOperationQueue mainQueue]
        usingBlock: ^(NSNotification *note) {
            [self processNotification: note];
        }];
}

- (void)unregisterForNotifications
{
    NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
    [notificationCenter removeObserver: self];
}

- (void)setUp
{
    [super setUp];
    NSURL *url = [NSURL URLWithString: @"https://localhost:8443"];
    NSURL *tempDir = [NSURL fileURLWithPath: NSTemporaryDirectory()];
    NSURL *databaseUrl = [tempDir URLByAppendingPathComponent: @"ipad-test.db"];
    NSError *error;
    self.serviceManager = [[CISDOBIpadServiceManager alloc] initWithStoreUrl: databaseUrl openbisUrl: url trusted: YES error: &error];
    STAssertNotNil(self.serviceManager, @"Service Manager file could not be created:\n%@", error);
    
    self.serviceManager.authenticationChallengeBlock = ^(CISDOBAsyncCall *call, NSURLAuthenticationChallenge *challange) {
        [call trustProtectionSpaceForAuthenticationChallenge: challange];
    };

    [self registerForNotifications];
}

- (void)tearDown
{
    [self unregisterForNotifications];
    [[NSFileManager defaultManager] removeItemAtURL: self.serviceManager.storeUrl error: nil];
    [super tearDown];
}

- (void)configureAndRunCallSynchronously:(CISDOBAsyncCall *)call
{
    [self configureCall: call];
    
    // The ipad service may make multiple calls, so take that into account.
    int waitTime = ((int) self.serviceManager.service.connection.timeoutInterval) * 2;
    [self waitSeconds: waitTime forCallToComplete: call];
}

- (NSArray *)targetsAndCompounds
{
	NSError *error;
	NSFetchRequest* request = [self.serviceManager fetchRequestForEntities];
    
   	NSArray *elements = [self.serviceManager executeFetchRequest: request error: &error];
    
    NSMutableArray *targetsAndCompounds = [NSMutableArray array];
    for (CISDOBIpadEntity *entity in elements) {
        if (IsPermIdCompound(entity.permId) || IsPermIdTarget(entity.permId)) {
            [targetsAndCompounds addObject: entity];
        }
    }
    
    return targetsAndCompounds;
}

- (NSArray *)entitiesWithChildren
{
	NSError *error;
	NSFetchRequest* request = [self.serviceManager fetchRequestForEntities];
    
   	NSArray *elements = [self.serviceManager executeFetchRequest: request error: &error];
    
    NSMutableArray *entitiesWithChildren = [NSMutableArray array];
    for (CISDOBIpadEntity *entity in elements) {
        if ([entity.childrenPermIds count] > 0 && ![@"Navigation" isEqualToString: entity.category]) {
            [entitiesWithChildren addObject: entity];
        }
    }
    
    return entitiesWithChildren;
}

- (NSArray *)navigationEntities
{
	NSError *error;
	NSFetchRequest* request = [self.serviceManager fetchRequestForEntities];
    
   	NSArray *elements = [self.serviceManager executeFetchRequest: request error: &error];
    
    NSMutableArray *navigationEntities = [NSMutableArray array];
    for (CISDOBIpadEntity *entity in elements) {
        if ([@"Navigation" isEqualToString: entity.category]) {
            [navigationEntities addObject: entity];
        }
    }
    
    return navigationEntities;
}

- (void)checkFindingChildren
{
	NSError* error;
	NSArray* allEntities = [self.serviceManager allIpadEntitiesOrError: &error];
    
    for (CISDOBIpadEntity *entity in allEntities) {
        if ([entity.childrenPermIds count] > 0) {
            NSArray *children = [self.serviceManager entitiesByPermId: entity.childrenPermIds error: &error];
            STAssertTrue([entity.childrenPermIds count] == [children count], @"Entity children %@ should resolve correctly. Found instead the following %@", entity.childrenPermIds, children);
        }
    }

}

- (void)assertNotLoggedIn
{
    STAssertFalse(self.willLogin, @"Should not be logged in yet");
    STAssertFalse(self.didLogin, @"Should not be logged in yet");
}

- (void)assertLoggedIn
{
    STAssertTrue(self.willLogin, @"Should be logged in.");
    STAssertTrue(self.didLogin, @"Should be logged in.");
}

- (void)assertBeforeRootLevelCall
{
    STAssertFalse(self.willRetrieveRootLevel, @"Should not have retrieved root level");
    STAssertFalse(self.didRetrieveRootLevel, @"Should not have retrieved root level");
    STAssertFalse(self.willSynchEntities, @"Should not have processed root level");
    STAssertFalse(self.didSynchEntities, @"Should not have processed root level");
}

- (void)assertAfterRootLevelCall
{
    STAssertTrue(self.willRetrieveRootLevel, @"Should have retrieved root level");
    STAssertTrue(self.didRetrieveRootLevel, @"Should have retrieved root level");
    STAssertTrue(self.willSynchEntities, @"Should have processed root level");
    STAssertTrue(self.didSynchEntities, @"Should have processed root level");
}

- (void)assertBeforeDrill
{
    STAssertFalse(self.willDrill, @"Should not have drilled");
    STAssertFalse(self.didDrill, @"Should not have drilled");
}

- (void)assertAfterDrill
{
    STAssertTrue(self.willDrill, @"Should have drilled");
    STAssertTrue(self.didDrill, @"Should have drilled");
}

- (void)assertBeforeDetails
{
    STAssertFalse(self.willRetrieveDetails, @"Should not have retrieved details");
    STAssertFalse(self.didRetrieveDetails, @"Should not have retrieved details");
}

- (void)assertAfterDetails
{
    STAssertTrue(self.willRetrieveDetails, @"Should have retrieved details");
    STAssertTrue(self.didRetrieveDetails, @"Should have retrieved details");
}

- (void)assertBeforeSearch
{
    STAssertFalse(self.willSearch, @"Should not have searched");
    STAssertFalse(self.didSearch, @"Should not have searched");
}

- (void)assertAfterSearch
{
    STAssertTrue(self.willSearch, @"Should have searched");
    STAssertTrue(self.didSearch, @"Should have searched");
}

- (void)performLogin
{
    CISDOBAsyncCall *call;
    [self assertNotLoggedIn];
    call = [self.serviceManager loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    [self assertLoggedIn];
}

- (void)performRootLevelCall
{
    NSDate *startTime = [NSDate date];
    NSDate *originalSyncDate = self.serviceManager.serverInfo.lastSyncDate;
    CISDOBAsyncCall *call;
    [self assertBeforeRootLevelCall];
    call = [self.serviceManager retrieveRootLevelEntities];
    [self configureAndRunCallSynchronously: call];
    
    // If there was an originalSyncDate, then no new call was made
    if (!originalSyncDate) [self assertAfterRootLevelCall];

    STAssertNotNil(_callResult, @"The service manager should have returned some entities.");
    
    NSDate *endTime = [NSDate date];
    STAssertNotNil(self.serviceManager.serverInfo, @"The service manager should have a serverInfo");
    NSDate *lastSyncDate = self.serviceManager.serverInfo.lastSyncDate;
    STAssertNotNil(lastSyncDate, @"The server info should have a sync date");
    
    if (!originalSyncDate) {
        STAssertTrue([lastSyncDate isGreaterThan: startTime], @"The sync date should be later than the start time");
        STAssertTrue([endTime isGreaterThan: lastSyncDate], @"The sync date should be earlier than the end time");
    } else {
        STAssertTrue([lastSyncDate isEqualToDate: originalSyncDate], @"The sync date should not have been updated");
    }
}

/** Bypass the logic that decides if a call is necessary or not. */
- (void)performRootLevelCallDirectlyToServer
{
    NSDate *startTime = [NSDate date];
    CISDOBAsyncCall *call;
    [self assertBeforeRootLevelCall];
    call = [self.serviceManager retrieveRootLevelEntitiesFromServer];
    [self configureAndRunCallSynchronously: call];
    [self assertAfterRootLevelCall];

    STAssertNotNil(_callResult, @"The service manager should have returned some entities.");
    
    NSDate *endTime = [NSDate date];
    STAssertNotNil(self.serviceManager.serverInfo, @"The service manager should have a serverInfo");
    NSDate *lastSyncDate = self.serviceManager.serverInfo.lastSyncDate;
    STAssertNotNil(lastSyncDate, @"The server info should have a sync date");
    
    STAssertTrue([lastSyncDate isGreaterThan: startTime], @"The sync date should be later than the start time");
    STAssertTrue([endTime isGreaterThan: lastSyncDate], @"The sync date should be earlier than the end time");
}

- (void)performDrill:(CISDOBIpadEntity *)drillEntity
{
    [self performDrill: drillEntity retry: YES];
}

- (void)performDrill:(CISDOBIpadEntity *)drillEntity retry:(BOOL)shouldRetry
{
    CISDOBAsyncCall *call;
    [self assertBeforeDrill];
    call = [self.serviceManager drillOnEntity: drillEntity];
    if (!shouldRetry) {
        ((CISDOBIpadServiceManagerCall *) call).retryCount = 2;
    }
    [self configureAndRunCallSynchronously: call];
    
    STAssertTrue(_callResult != nil || _callError != nil, @"The iPad service should have returned some entities or an error.");
    [self assertAfterDrill];
}

- (void)performDetails:(CISDOBIpadEntity *)detailsEntity
{
    CISDOBAsyncCall *call;
    call = [self.serviceManager detailsForEntity:  detailsEntity];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    [self assertAfterDetails];
}

- (void)performDrillOnCollection:(NSArray *)drillEntities
{
    CISDOBAsyncCall *call;
    [self assertBeforeDrill];
    call = [self.serviceManager drillOnEntities: drillEntities];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    [self assertAfterDrill];
}

- (void)performDetailsOnCollection:(NSArray *)detailsEntities
{
    CISDOBAsyncCall *call;
    [self assertBeforeDetails];
    call = [self.serviceManager detailsForEntities:  detailsEntities];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    [self assertAfterDetails];
}


- (void)performSearch:(NSString *)searchText domain:(id)searchDomain
{
    CISDOBAsyncCall *call;
    [self assertBeforeSearch];
    call = [self.serviceManager searchForText: searchText domain: searchDomain];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    [self assertAfterSearch];
}

- (void)testPersistEntities
{
    [self performLogin];
    STAssertTrue([self.serviceManager shouldRefreshRootLevelEntitiesCall], @"We have not yet initialized the root level entities, so we should do so now");
    [self performRootLevelCall];
    STAssertFalse([self.serviceManager shouldRefreshRootLevelEntitiesCall], @"We have initialized the root level entities recently, no need to do it again.");
    

    // Get drill information on some entity
    NSArray *entitiesWithChildren = [self entitiesWithChildren];
    STAssertTrue([entitiesWithChildren count] > 0, @"There should be some entities with children");    
    CISDOBIpadEntity *drillEntity = [entitiesWithChildren objectAtIndex: 0];
    [self performDrill: drillEntity];
   
    
    // Get detail information on some entities
    CISDOBIpadEntity *detailsEntity = [entitiesWithChildren objectAtIndex: 1];
    [self assertBeforeDetails];
    [self performDetails: detailsEntity];
    
    // Check that the children could be found
    [self checkFindingChildren];
}

- (void)testPersistDrillAndDetailsOnCollections
{
    [self performLogin];
    [self performRootLevelCall];
    

    // Get drill information on some entity
    NSArray *entitiesWithChildren = [self entitiesWithChildren];
    STAssertTrue([entitiesWithChildren count] > 0, @"There should be some entities with children");    
    CISDOBIpadEntity *drillEntity = [entitiesWithChildren objectAtIndex: 0];
    [self performDrill: drillEntity];
   
    
    // Get detail information on some entities
    CISDOBIpadEntity *detailsEntity = [entitiesWithChildren objectAtIndex: 1];
    [self assertBeforeDetails];
    [self performDetails: detailsEntity];
    
    // Check that the children could be found
    [self checkFindingChildren];
}

- (void)testRootLevelCallWithRefresh
{
    [self performLogin];
    [self performRootLevelCall];
    NSDate *firstSyncDate = self.serviceManager.serverInfo.lastSyncDate;
    
    // Fix test state before making this call again
    self.willRetrieveRootLevel = NO;
    self.didRetrieveRootLevel = NO;
    self.willSynchEntities = NO;
    self.didSynchEntities = NO;    
    
    [self performRootLevelCall];
    NSDate *secondSyncDate = self.serviceManager.serverInfo.lastSyncDate;
    STAssertEqualObjects(firstSyncDate, secondSyncDate, @"The second sync should have the same time as the first");

    // Fix test state before making this call again
    self.willRetrieveRootLevel = NO;
    self.didRetrieveRootLevel = NO;
    self.willSynchEntities = NO;
    self.didSynchEntities = NO;       
    [self performRootLevelCallDirectlyToServer];

    NSDate *thirdSyncDate = self.serviceManager.serverInfo.lastSyncDate;
    STAssertTrue([firstSyncDate isLessThan: thirdSyncDate], @"The third sync should be later than the first");
    
    NSArray *firstUpdatedentities = [self.serviceManager entitiesNotUpdatedSince: firstSyncDate error: nil];
    NSArray *thirdUpdatedEntities = [self.serviceManager entitiesNotUpdatedSince: thirdSyncDate error: nil];
    STAssertTrue([firstUpdatedentities count] < [thirdUpdatedEntities count], @"The earlier sync request should have returned more entities %u : %u",[firstUpdatedentities count], [thirdUpdatedEntities count]);
}

- (void)testSearch
{
    [self performLogin];
    [self performSearch: @"5-hydroxytryptamine 3" domain: nil];
}

- (void)testSearchWithNilDomain
{
    [self performLogin];
    [self performSearch: @"5-hydroxytryptamine 3" domain: _serviceManager.service.clientPreferences.defaultSearchDomain];
}

- (void)testInvalidSessionToken
{
    [self performLogin];
    [self performRootLevelCall];
    
    // Switch the session token
    [self.serviceManager.service.connection setSessionTokenForTesting: @"junk"];

    // Get drill information on some entity
    NSArray *entitiesWithChildren = [self entitiesWithChildren];
    STAssertTrue([entitiesWithChildren count] > 0, @"There should be some entities with children");    
    CISDOBIpadEntity *drillEntity = [entitiesWithChildren objectAtIndex: 0];
    [self performDrill: drillEntity retry: NO];
    STAssertNotNil(_callError, @"The request should have failed with an error");
}

- (void)testInvalidSessionTokenRetry
{
    [self performLogin];
    [self performRootLevelCall];
    
    // Switch the session token
    [self.serviceManager.service.connection setSessionTokenForTesting: @"junk"];

    // Get drill information on some entity
    NSArray *entitiesWithChildren = [self entitiesWithChildren];
    STAssertTrue([entitiesWithChildren count] > 0, @"There should be some entities with children");    
    CISDOBIpadEntity *drillEntity = [entitiesWithChildren objectAtIndex: 0];
    [self performDrill: drillEntity retry: YES];
    STAssertNotNil(_callResult, @"The request should have succeeded with an error");
}

- (void)retrieveRootLevelEntitiesSimulatingRemovalOfCategory:(CISDOBIpadEntity *)categoryToRemove
{
    // Make a root level call, but do have some entities removed from the list
    CISDOBAsyncCall *call;
    call = [self.serviceManager retrieveRootLevelEntitiesFromServer];
    
    NSArray *removedEntities = [self.serviceManager.service convertToEntitiesPermIds: [NSArray arrayWithObject: categoryToRemove.permId] refcons: [NSArray arrayWithObject: categoryToRemove.refcon] count: 1];
    CISDOBIpadServiceCall *serviceCall = (CISDOBIpadServiceCall *)((CISDOBIpadServiceManagerCall *)call).serviceCall;
    CISDOBConnectionCall *connectionCall = (CISDOBConnectionCall *) serviceCall.connectionCall;
    NSArray *oldParams = connectionCall.params;
    NSMutableArray *params = [NSMutableArray arrayWithArray: oldParams];
    // The service parameters are always in the 4th position
    NSDictionary *oldServiceParams = [params objectAtIndex: 3];
    NSMutableDictionary *serviceParams = [NSMutableDictionary dictionaryWithDictionary: oldServiceParams];
        // Force the root request, bypassing the timing checks
    [serviceParams setObject: removedEntities forKey: @"HIDE"];
    [params replaceObjectAtIndex: 3 withObject: serviceParams];
    connectionCall.params = params;
    
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The service manager should have returned some entities.");
}

- (void)testDeletedEntities
{
    [self performLogin];
    [self performRootLevelCall];
    
    // Pick an entity to remove from the next result set to simulate deletion
    NSArray *navigationEntities = [self navigationEntities];
    CISDOBIpadEntity *categoryToRemove;
    for (CISDOBIpadEntity *entity in navigationEntities) {
        if ([@"TARGETS AND COMPOUNDS" isEqualToString: entity.permId]) {
            categoryToRemove = entity;
            break;
        }
    }
    // Remember the permId before we refresh because the entity will be deleted
    NSMutableArray *removedPermIds = [NSMutableArray array];
    self.serviceManager.mocSaveBlock = ^(CISDOBIpadServiceManager *manager, NSArray *entitiesToDelete) {
        // This block is invoked on each save. Not all saves will delete entities
        if ([entitiesToDelete count] < 1) return;
        
        [removedPermIds addObjectsFromArray: entitiesToDelete];
        STAssertEquals((NSUInteger) 1, [entitiesToDelete count], @"One navigational entity should have been deleted.");
        NSUInteger navCount = 0, compoundCount = 0, targetCount = 0;
        for (NSString *entityPermId in entitiesToDelete) {
            BOOL is5HTCompound = IsPermIdCompound(entityPermId);
            BOOL is5HTTarget = IsPermIdTarget(entityPermId);
            BOOL isNav = [@"TARGETS AND COMPOUNDS" isEqualToString: entityPermId];
            STAssertTrue(is5HTCompound || is5HTTarget || isNav, @"The deleted entities should be either targets or compounds");
            if (is5HTCompound) ++compoundCount;
            if (is5HTTarget) ++targetCount;
            if (isNav) ++navCount;
        }
        STAssertEquals((NSUInteger) 1, navCount, @"Only one navigational entity should have been deleted");
        STAssertEquals((NSUInteger) 0, compoundCount, @"No compound entities should have been deleted");
        STAssertEquals((NSUInteger) 0, targetCount, @"No target entities should have been deleted");
    };

    [self retrieveRootLevelEntitiesSimulatingRemovalOfCategory: categoryToRemove];
    
    // Check that the entityToRemove is no longer found
    NSError *error;
    NSArray *removedEntities = [self.serviceManager entitiesByPermId: removedPermIds error: &error];
    STAssertEquals([removedEntities count], (NSUInteger) 0, @"Removed entities should not be found anymore");

    // Check that entityToRemove is still accessible
    STAssertNil(categoryToRemove.permId, @"The Entity's fields should have been set to nil");
}

- (void)testImageRetrieval
{
    
    [self performLogin];
    [self performRootLevelCall];
    
    // Get drill information on some entity
    NSArray *entitiesWithChildren = [self entitiesWithChildren];
    CISDOBIpadEntity *entityWithImage = [entitiesWithChildren objectAtIndex: 0];
    STAssertNil(entityWithImage.imageUrlString, @"Entity should not yet have an image");
    
    // Check that getting an image for an entity without any images works correctly
    CISDOBAsyncCall *call = [self.serviceManager imagesForEntity: entityWithImage];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"Should have gotten an image object");
    CISDOBIpadImage *image = _callResult;
    STAssertEquals((NSUInteger) 0, [image.imageData length], @"Image data should be empty");

    // Initialize the image url and get the image
    [self performDetails: entityWithImage];
    STAssertNotNil(entityWithImage.imageUrlString, @"Should have found an entity with a local image");
    call = [self.serviceManager imagesForEntity: entityWithImage];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"Should have gotten an image object");
    
    image = _callResult;
    STAssertTrue([image.imageData length] > 0, @"Image data should not be empty");
    STAssertEqualObjects(@"image/jpeg", image.MIMEType, @"Mime type should be image/jpeg");
    STAssertNil(image.textEncodingName, @"Text encoding should be nil");
}

- (void)testUnicode
{
    
    [self performLogin];
    [self performRootLevelCall];
    
    NSArray *targetsAndCompounds = [self targetsAndCompounds];
    CISDOBIpadEntity *targetOrCompound = [targetsAndCompounds objectAtIndex: 0];
    
    [self performDetails: targetOrCompound];
    STAssertNotNil(targetOrCompound.properties, @"Properties should have been retrieved");
    NSDictionary *unicodeProperty = [targetOrCompound.properties objectAtIndex: 4];
    NSString *unicodeValue = [unicodeProperty objectForKey: @"value"];
    NSString *expectedValue = [NSString stringWithFormat: @"A pr%Cperty w%Cth accents.", (unsigned short) 0x00F6, (unsigned short) 0x00EF];
    STAssertEqualObjects(expectedValue, unicodeValue, @"Property should equal");
}



- (void)testNilUrl
{
    [self.serviceManager setOpenbisUrl: nil trusted: YES];
    
    CISDOBAsyncCall *call;
    
    [self assertNotLoggedIn];
    call = [self.serviceManager loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    [self assertLoggedIn];
    
    [self assertBeforeRootLevelCall];
    call = [self.serviceManager retrieveRootLevelEntities];
    [self configureAndRunCallSynchronously: call];
    STAssertTrue(self.willRetrieveRootLevel, @"Should have retrieved root level");
    STAssertTrue(self.didRetrieveRootLevel, @"Should have retrieved root level");
    // No synch should have happened
    
    STAssertNotNil(_callError, @"The service manager should have failed to return entities.");
}


@end
