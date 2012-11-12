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
#import "CISDOBIpadService.h"
#import "CISDOBConnection.h"
#import "CISDOBIpadEntity.h"

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

- (NSArray *)entitiesWithChildren
{
	NSError *error;
	NSFetchRequest* request = [self.serviceManager fetchRequestForEntities];
    
   	NSArray *elements = [self.serviceManager executeFetchRequest: request error: &error];
    
    NSMutableArray *entitiesWithChildren = [NSMutableArray array];
    for (CISDOBIpadEntity *entity in elements) {
        if ([entity.childrenPermIds count] > 0) {
            [entitiesWithChildren addObject: entity];
        }
    }
    
    return entitiesWithChildren;
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

- (void)testPersistEntities
{
    CISDOBAsyncCall *call;
    
    [self assertNotLoggedIn];
    call = [self.serviceManager loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    [self assertLoggedIn];
    
    [self assertBeforeRootLevelCall];
    call = [self.serviceManager retrieveRootLevelEntities];
    [self configureAndRunCallSynchronously: call];
    [self assertAfterRootLevelCall];
    
    STAssertNotNil(_callResult, @"The service manager should have returned some entities.");

    // Get drill information on some entity
    NSArray *entitiesWithChildren = [self entitiesWithChildren];
    STAssertTrue([entitiesWithChildren count] > 0, @"There should be some entities with children");
   

    [self assertBeforeDrill];
    call = [self.serviceManager drillOnEntity: [entitiesWithChildren objectAtIndex: 0]];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    [self assertAfterDrill];
   
    
    // Get detail information on some entities
    [self assertBeforeDetails];
    call = [self.serviceManager detailsForEntity: [entitiesWithChildren objectAtIndex: 1]];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    [self assertAfterDetails];
  

    
    // Check that the children could be found
    [self checkFindingChildren];
}

@end
