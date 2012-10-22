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


@implementation CISDOBIpadServiceTest

- (void)setUp
{
    [super setUp];
    NSURL *url = [NSURL URLWithString: @"https://localhost:8443"];
    CISDOBLiveConnection *connection = [[CISDOBLiveConnection alloc] initWithUrl: url trusted: YES];
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

- (void)testListAllEntities
{
    CISDOBAsyncCall *call;
    call = [_service loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    
    call = [_service listRootLevelEntities];
    [self configureAndRunCallSynchronously: call];
    
    STAssertNotNil(_callResult, @"The iPad service should have returned some entities.");
    NSArray *rawEntities = _callResult;
    STAssertTrue([rawEntities count] > 0, @"The Pad service should have returned some entities.");
    
    for (CISDOBIpadRawEntity *rawEntity in rawEntities) {
        NSString *summaryHeader = rawEntity.summaryHeader;
        STAssertNotNil(summaryHeader, @"The summary header should not be nil");
        STAssertNotNil(rawEntity.permId, @"PermId should not be nil");
        STAssertNotNil(rawEntity.refcon, @"Ref con should not be nil");
        STAssertNotNil(rawEntity.category, @"Group should not be nil");
        STAssertTrue([summaryHeader length], @"Summary header should not be empty");
        STAssertNotNil(rawEntity.summary, @"Summary should not be nil");
//        STAssertNotNil(rawEntity.identifier, @"Identifier should not be nil");
//        STAssertNotNil(rawEntity.imageUrl, @"Image url should not be nil");
//        STAssertNotNil(rawEntity.children, @"Children should not be nil");
//        STAssertNotNil(rawEntity.properties, @"Properties type should not be nil");
    }
}

@end
