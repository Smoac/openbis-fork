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
//  CISDOBIpadServiceTest.m
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 9/26/12.
//
//

#import "CISDOBIpadServiceTest.h"
#import "CISDOBConnection.h"
#import "CISDOBIpadService.h"
#import "CISDOBIpadServiceInternal.h"
#import "CISDOBAsyncCall.h"


@implementation CISDOBIpadServiceTest

- (void)setUp
{
    [super setUp];
    NSURL *url = [NSURL URLWithString: @"https://localhost:8443"];
    CISDOBLiveConnection *connection = [[CISDOBLiveConnection alloc] initWithUrl: url trusted: YES];
    connection.delegate = self;
    _service = [[CISDOBIpadService alloc] initWithConnection: connection];
    [connection release];
}

- (void)tearDown
{
    [_service release], _service = nil;
    [super tearDown];
}

- (void)configureAndRunCallSynchronously:(CISDOBAsyncCall *)call
{
    [self configureCall: call];
    
    // The ipad service may make multiple calls, so take that into account.
    int waitTime = ((int) _service.connection.timeoutInterval) * 2;
    [self waitSeconds: waitTime forCallToComplete: call];
}

- (CISDOBIpadRawEntity *)entityForRootCall:(NSArray *)navigationalEntities
{
    return [navigationalEntities objectAtIndex: 1];
}

- (void)testListNavigationalEntities
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    call = [_service listNavigationalEntities];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    NSArray *rawEntities = _callResult;
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");
    
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        NSString *summaryHeader = rawEntity.summaryHeader;
        STAssertNotNil(summaryHeader, @"The summary header should not be nil");
        STAssertNotNil(rawEntity.permId, @"PermId should not be nil");
        STAssertNotNil(rawEntity.refcon, @"Refcon should not be nil");
//        STAssertNotNil(rawEntity.category, @"Group should not be nil");
        STAssertTrue([summaryHeader length], @"Summary header should not be empty");
        STAssertNotNil(rawEntity.summary, @"Summary should not be nil");
        STAssertNotNil(rawEntity.rootLevel, @"RootLevel should not be nil");
        STAssertEquals([rawEntity.rootLevel boolValue], YES, @"RootLevel should not be true");
    }
}

- (void)testListRootEntities
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_service.clientPreferences, @"The client preferences should have been initialized");
    STAssertEquals(_service.clientPreferences.rootSetRefreshInterval, 60. * 30., @"The default root refresh interval should be 30 min");

    call = [_service listNavigationalEntities];
    [self configureAndRunCallSynchronously: call];
    CISDOBIpadRawEntity *entityForRoot = [self entityForRootCall: _callResult];
    call = [_service listRootLevelEntities: [NSArray arrayWithObject: entityForRoot.permId] refcons: [NSArray arrayWithObject: entityForRoot.refcon]];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    NSArray *rawEntities = _callResult;
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");
    
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        NSString *summaryHeader = rawEntity.summaryHeader;
        STAssertNotNil(summaryHeader, @"The summary header should not be nil");
        STAssertNotNil(rawEntity.permId, @"PermId should not be nil");
        STAssertNotNil(rawEntity.refcon, @"Refcon should not be nil");
        STAssertNotNil(rawEntity.category, @"Group should not be nil");
        STAssertTrue([summaryHeader length], @"Summary header should not be empty");
        STAssertNotNil(rawEntity.summary, @"Summary should not be nil");
        STAssertNotNil(rawEntity.children, @"Children should not be nil");
        STAssertNotNil(rawEntity.rootLevel, @"RootLevel should not be nil");
        STAssertNil(rawEntity.identifier, @"Identifier should be nil");
        STAssertNil(rawEntity.imageUrl, @"Image url should be nil");
        STAssertNil(rawEntity.properties, @"Properties should be nil");
    }
}

- (void)testDrill
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    
    call = [_service listNavigationalEntities];
    [self configureAndRunCallSynchronously: call];
    CISDOBIpadRawEntity *entityForRoot = [self entityForRootCall: _callResult];
    call = [_service listRootLevelEntities: [NSArray arrayWithObject: entityForRoot.permId] refcons: [NSArray arrayWithObject: entityForRoot.refcon]];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    NSArray *rawEntities = _callResult;
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");
    
    
    // Find an entity with children and drill on it
    CISDOBIpadRawEntity *entityWithChildren = nil;
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        if ([@"5HT_PROBE" isEqualToString: rawEntity.category] && [rawEntity.children length] > 2) {
            entityWithChildren = [rawEntity retain];
            break;
        }
    }
    
    // Drill
    NSError *error;
    id refconObject = [NSJSONSerialization JSONObjectWithData: [entityWithChildren.refcon dataUsingEncoding: NSASCIIStringEncoding] options: 0 error: &error];
    STAssertNotNil(refconObject, @"Could not parse refcon string %@ : %@", entityWithChildren.refcon, error);
    call = [_service drillOnEntityWithPermId: entityWithChildren.permId refcon: refconObject];
    [self configureAndRunCallSynchronously: call];
    
    rawEntities = _callResult;
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");
}

- (void)collectFromEntities:(NSArray *)rawEntities permIds:(NSMutableArray *)permIds refcons:(NSMutableArray *)refcons error:(NSError **)error
{
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        id refconObject = [NSJSONSerialization JSONObjectWithData: [rawEntity.refcon dataUsingEncoding: NSASCIIStringEncoding] options: 0 error: error];
        [permIds addObject: rawEntity.permId];
        [refcons addObject: refconObject];
    }
}

- (void)testDetails
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    
    call = [_service listNavigationalEntities];
    [self configureAndRunCallSynchronously: call];
    CISDOBIpadRawEntity *entityForRoot = [self entityForRootCall: _callResult];
    call = [_service listRootLevelEntities: [NSArray arrayWithObject: entityForRoot.permId] refcons: [NSArray arrayWithObject: entityForRoot.refcon]];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    NSArray *rawEntities = _callResult;
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");
    
    
    // Find an entity with children and drill on it
    CISDOBIpadRawEntity *entityWithChildren = nil;
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        if ([@"5HT_PROBE" isEqualToString: rawEntity.category] && [rawEntity.children length] > 2) {
            entityWithChildren = [rawEntity retain];
            break;
        }
    }
    STAssertNotNil(entityWithChildren, @"Should have found an entity with children");
    // Drill
    NSError *error;
    id refconObject = [NSJSONSerialization JSONObjectWithData: [entityWithChildren.refcon dataUsingEncoding: NSASCIIStringEncoding] options: 0 error: &error];
    STAssertNotNil(refconObject, @"Could not parse refcon string %@ : %@", entityWithChildren.refcon, error);
    call = [_service drillOnEntityWithPermId: entityWithChildren.permId refcon: refconObject];
    [self configureAndRunCallSynchronously: call];
    rawEntities = _callResult;
    STAssertEquals([rawEntities count], (NSUInteger) 3, @"The Pad service should have returned three entity for drill.");
    
    // Details
    NSMutableArray *permIds = [NSMutableArray array];
    NSMutableArray *refcons = [NSMutableArray array];
    [self collectFromEntities: rawEntities permIds: permIds refcons: refcons error: &error];
    call = [_service detailsForEntities: permIds refcons: refcons];
    [self configureAndRunCallSynchronously: call];
    
    rawEntities = _callResult;
    STAssertEquals([rawEntities count], (NSUInteger) 3, @"The Pad service should have returned three entities with details.");
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        STAssertNotNil(rawEntity.properties, @"After a details request, properties should not be nil");
    }    
    
    [entityWithChildren release];
}

- (void)testSearch
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    call = [_service searchForText:@"5-hydroxytryptamine 3"];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    NSArray *rawEntities = _callResult;
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");
    
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        NSString *summaryHeader = rawEntity.summaryHeader;
        STAssertNotNil(summaryHeader, @"The summary header should not be nil");
        STAssertNotNil(rawEntity.permId, @"PermId should not be nil");
        STAssertNotNil(rawEntity.refcon, @"Refcon should not be nil");
//        STAssertNotNil(rawEntity.category, @"Group should not be nil");
        STAssertTrue([summaryHeader length], @"Summary header should not be empty");
        STAssertNotNil(rawEntity.summary, @"Summary should not be nil");
    }
}

- (void)testEmptySearch
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    call = [_service searchForText:@""];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    NSArray *rawEntities = _callResult;
    STAssertEquals([rawEntities count], (NSUInteger) 0, @"An empty search should have returned no entities.");    
}

// CISDOBAsyncCallDelegate
- (void)asyncCall:(CISDOBAsyncCall *)call didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)authenticationChallenge
{
    [call trustProtectionSpaceForAuthenticationChallenge: authenticationChallenge];
}

@end
