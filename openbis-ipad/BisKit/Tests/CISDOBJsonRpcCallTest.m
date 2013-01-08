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
//  CISDOBJsonRpcCallTest.m
//  BisMac
//
//  Created by cramakri on 28.08.12.
//
//

#import "CISDOBJsonRpcCallTest.h"
#import "CISDOBJsonRpcCall.h"


@implementation CISDOBJsonRpcCallTest

- (void)setUp
{
    [super setUp];
    
    _initialRetainCount = [self retainCount];
    
    _jsonRpcCall = [[CISDOBJsonRpcCall alloc] init];
    _jsonRpcCall.timeoutInterval = 10.0;
    _jsonRpcCall.delegate = self;
    
    [self configureCall: _jsonRpcCall];
}

- (void)tearDown
{
    // Check that we are not leaking memory
    STAssertEquals([_jsonRpcCall retainCount], (NSUInteger) 1, @"jsonRpcCall retain count : %i", [_jsonRpcCall retainCount]);
    
    // Don't check the retain counts of NSNumbers -- they are strange
    if (nil != _callResult && ![_callResult isKindOfClass: [NSNumber class]]) {
        STAssertEquals([_callResult retainCount], (NSUInteger) 1, @"callResult retain count : %i", [_callResult retainCount]);
    }
    
    if (nil != _callError) {
        STAssertEquals([_callError retainCount], (NSUInteger) 1, @"callError retain count : %i", [_callError retainCount]);
    }
        
    // Tear-down code here.
    [_jsonRpcCall release], _jsonRpcCall = nil;    
    [_callResult release], _callResult = nil;
    [_callError release], _callError = nil;
    [super tearDown];
    
    STAssertEquals([self retainCount], _initialRetainCount, @"self retain count : %i", [self retainCount]);    
}

- (void)waitForCallToComplete
{
    int waitTime = ((int) _jsonRpcCall.timeoutInterval) + 1;
    [self waitSeconds: waitTime forCallToComplete: _jsonRpcCall];
}

- (void)assertErrorWasNetworkUnreachable
{
    STAssertEqualObjects([_callError domain] , NSURLErrorDomain, @"If there was an error, it should have been a network error : %@", _callError);
    
    NSInteger errorCode = [_callError code];
    // -1009 means no internet connection, -1004 is could not connect to server
    STAssertTrue(errorCode == -1009 || errorCode == -1004, @"If there was an error, it should have been a network error, not %li : %@", errorCode, _callError);
}

@end

@implementation CISDOBJsonRpcCallTest (CISDOBAsyncCallDelegate)

- (BOOL)asyncCall:(CISDOBAsyncCall *)call canTrustProtectionSpace:(NSString *)host
{
    // Allow local self-signed certificates
    return [host isEqualToString: @"localhost"];
}

@end

@implementation CISDOBJsonRpcCallGenericTest

- (void)setUp
{
    [super setUp];
    _jsonRpcCall.url = [NSURL URLWithString: @"http://www.raboof.com/projects/jayrock/demo.ashx"];
    _jsonRpcCall.method = @"add";    
}

- (void)testJsonRpcCall
{ 

    _jsonRpcCall.params = [NSArray arrayWithObjects: @"1", @"2", nil];    
    [self waitForCallToComplete];
    
    if (_callSucceeded) {
        STAssertEqualObjects(_callResult, [NSNumber numberWithInt: 3], @"1 + 2 = 3");
    } else {
        [self assertErrorWasNetworkUnreachable];
    }

}

- (void)testJsonRpcCallWithError
{ 
    _jsonRpcCall.params = [NSArray arrayWithObjects: @"a", @"2", nil];    
    [self waitForCallToComplete];
    
    STAssertFalse(_callSucceeded, @"The call should have resulted in an error %@", _callResult);
    if ([[_callError domain] isEqualToString: NSURLErrorDomain]) {
        [self assertErrorWasNetworkUnreachable];
    } else {
        STAssertEqualObjects([_callError domain], CISOBJsonRpcErrorDomain, @"The call should have resulted with an error in the JsonRpcErrorDomain : %@", _callError);
        STAssertEquals([_callError code], (NSInteger) kCISOBJsonRpcError_CallReturnedError, @"The error code should equal kCISOBJsonRpcError_CallReturnedError : %@", _callError);
        NSString *errorDesc = [[_callError userInfo] objectForKey: NSLocalizedDescriptionKey];
        STAssertEqualObjects(errorDesc, @"Input string was not in a correct format.", @"%@", errorDesc); 
    }
}

@end

@implementation CISDOBJsonRpcCallOpenBisTest

- (void)setUp
{
    [super setUp];
    // This should be an instance of openBIS that includes the ipad core-plugin.
    _jsonRpcCall.url = [NSURL URLWithString: @"https://localhost:8443/openbis/openbis/rmi-general-information-v1.json"];
}


- (void)assertDataSetTypeUnknownExists:(NSArray *)dataSetTypes
{
    BOOL foundUnknown = NO;
    for (NSDictionary *dataSetType in dataSetTypes) {
        if ([@"UNKNOWN" isEqualToString: [dataSetType objectForKey: @"code"]]) {
            foundUnknown = YES;
            break;
        }
    }
    
    STAssertTrue(foundUnknown, @"There should be a data set type with code UNKNOWN. Types: %@", dataSetTypes);
}

- (void)testOpenBisCalls
{ 
    
    _jsonRpcCall.method = @"tryToAuthenticateForAllServices";
    _jsonRpcCall.params = [NSArray arrayWithObjects: @"admin", @"password", nil];
    [self waitForCallToComplete];
    
    NSString *sessionToken = nil;
    if (_callSucceeded) {
        sessionToken = [_callResult retain];
        NSRange adminRange = [_callResult rangeOfString: @"admin-"];
        STAssertTrue(adminRange.length > 0, @"Result should match admin-* : %@", _callResult);
        STAssertEquals(adminRange.location, (NSUInteger) 0, @"Result should match admin-* : %@", _callResult);
    } else {
        [self assertErrorWasNetworkUnreachable];
        // Stop doing any more tests if the server is not reachable
        return;
    }
    
    _jsonRpcCall.method = @"listDataSetTypes";
    _jsonRpcCall.params = [NSArray arrayWithObjects: sessionToken, nil];
    [self waitForCallToComplete];
    
    STAssertTrue(_callSucceeded, @"listDataSetTypes should have succeeded");
    STAssertTrue([_callResult count] > 0, @"The server should have at least 1 data set type");
    [self assertDataSetTypeUnknownExists: _callResult];
    
    [sessionToken release];
}

@end
