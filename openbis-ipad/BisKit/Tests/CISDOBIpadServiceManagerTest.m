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

- (void)setUp
{
    [super setUp];
    NSURL *url = [NSURL URLWithString: @"https://localhost:8443"];
    NSURL *tempDir = [NSURL fileURLWithPath: NSTemporaryDirectory()];
    NSURL *databaseUrl = [tempDir URLByAppendingPathComponent: @"ipad-test.db"];
    NSError *error;
    self.serviceManager = [[CISDOBIpadServiceManager alloc] initWithStoreUrl: databaseUrl openbisUrl: url trusted: YES error: &error];
    STAssertNotNil(self.serviceManager, @"Service Manager file could not be created:\n%@", error);
}

- (void)tearDown
{
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
	NSFetchRequest* request = self.serviceManager.entityFetchRequest;
    
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

- (void)testPersistEntities
{
    CISDOBAsyncCall *call;
    call = [self.serviceManager loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    
    call = [self.serviceManager retrieveRootLevelEntities];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The service manager should have returned some entities.");

    // Get drill information on some entity
    NSArray *entitiesWithChildren = [self entitiesWithChildren];
    STAssertTrue([entitiesWithChildren count] > 0, @"There should be some entities with children");
   
    call = [self.serviceManager drillOnEntity: [entitiesWithChildren objectAtIndex: 0]];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    
    // Get detail information on some entities
    call = [self.serviceManager detailsForEntity: [entitiesWithChildren objectAtIndex: 1]];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");

    
    // Check that the children could be found
    [self checkFindingChildren];
}

@end
