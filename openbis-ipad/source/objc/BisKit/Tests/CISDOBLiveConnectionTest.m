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
//  CISDOBLive_connectionTest.m
//  BisMac
//
//  Created by cramakri on 19.09.12.
//
//

#import "CISDOBLiveConnectionTest.h"
#import "CISDOBAsyncCall.h"


@implementation CISDOBLiveConnectionTest

- (void)setUp
{
    [super setUp];
    NSURL *url = [NSURL URLWithString: @"https://localhost:8443"];
    _connection = [[CISDOBLiveConnection alloc] initWithUrl: url trusted: YES];
    _connection.delegate = self;
}

- (void)tearDown
{
    // Tear-down code here.
    [_connection release], _connection = nil;
    [super tearDown];
}

- (void)configureAndRunCallSynchronously:(CISDOBAsyncCall *)call
{
    [self configureCall: call];
    
    int waitTime = ((int) _connection.timeoutInterval) + 1;
    [self waitSeconds: waitTime forCallToComplete: call];
}

- (NSDictionary *)extractIpadService:(NSArray *)services
{
    for (NSDictionary *service in services) {
        if ([@"ipad-read-service-v1" isEqualToString: [service objectForKey: @"serviceKey"]]) {
            return service;
        }
    }
    
    return nil;
}

- (void)testLoginAndListServices
{
    CISDOBAsyncCall *call;
    call = [_connection loginUser: GetDefaultUserName() password: GetDefaultUserPassword()];
    [self configureAndRunCallSynchronously: call];
    
    call = [_connection listAggregationServices];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"There should be some aggregation services on the server");
    NSDictionary *service = [self extractIpadService: _callResult];
    [service retain];
    STAssertNotNil(service, @"There should be a service with key \"ipad-read-service-v1\". Services: %@", _callResult);
    
    // Call without any parameters
    call =
        [_connection
            createReportFromDataStore: [service objectForKey: @"dataStoreCode"]
            aggregationService: [service objectForKey: @"serviceKey"]
            parameters: nil];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The ipad-read-service-v1 should have returned some data.");
    NSArray *rows = [_callResult objectForKey: @"rows"];
    STAssertTrue([rows count] == 0, @"The ipad-read-service-v1 should have returned empty data.");
    
    // Call with the correct parameters
    NSDictionary *parameters = [NSDictionary dictionaryWithObject: @"ROOT" forKey: @"requestKey"];
    call =
        [_connection
            createReportFromDataStore: [service objectForKey: @"dataStoreCode"]
            aggregationService: [service objectForKey: @"serviceKey"]
            parameters: parameters];
    [self configureAndRunCallSynchronously: call];
    STAssertNotNil(_callResult, @"The ipad-read-service-v1 should have returned some data.");
    rows = [_callResult objectForKey: @"rows"];
    STAssertTrue([rows count] > 0, @"The ipad-read-service-v1 should have returned some data.");
    [service release];
}

// CISDOBAsyncCallDelegate
- (void)asyncCall:(CISDOBAsyncCall *)call didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)authenticationChallenge
{
    [call trustProtectionSpaceForAuthenticationChallenge: authenticationChallenge];
}

@end
