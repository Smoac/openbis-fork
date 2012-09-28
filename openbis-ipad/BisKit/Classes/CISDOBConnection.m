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
//  CISDOBConnection.m
//  BisMac
//
//  Created by cramakri on 27.08.12.
//
//

#import "CISDOBConnection.h"
#import "CISDOBJsonRpcCall.h"
#import "CISDOBAsyncCall.h"

// Internal connection call that includes the private state
@interface CISDOBConnectionCall : CISDOBAsyncCall {
@private
    // Internal state
    CISDOBConnection    *__weak _connection;
    NSString            *_method;
    NSArray             *_params;
    SuccessBlock        _successWrapper;
    FailBlock           _failWrapper;
}
@property(weak) CISDOBConnection *connection;
@property(strong) NSString *method;
@property(strong) NSArray *params;
@property(copy) SuccessBlock successWrapper;
@property(copy) FailBlock failWrapper;  

// Initialization
- (id)initWithConnection:(CISDOBConnection *)aConnection method:(NSString *)aString params:(NSArray *)anArray;

@end

@interface CISDOBConnection (CISDOBConnectionPrivate)

- (void)executeCall:(CISDOBConnectionCall *)call;

@end



@implementation CISDOBConnection

- (id)init
{
    if (!(self = [super init])) return nil;
    _timeoutInterval = 10.;
    
    return self;
}


- (void)subclassResponsibility
{
    NSException* exception = [NSException exceptionWithName: NSInvalidArgumentException reason: @"Subclass Responsibility" userInfo: nil];
    @throw exception;
}

- (CISDOBConnectionCall *)callWithMethod:(NSString *)method params:(NSArray *)params
{
    return 
        [[CISDOBConnectionCall alloc] 
            initWithConnection: self method: method params: params];    
}

- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password 
{
    NSString* method = @"tryToAuthenticateAtQueryServer";    
    NSArray* params = [NSArray arrayWithObjects: user, password, nil];
    
    CISDOBConnectionCall *call = [self callWithMethod: method params: params];
    call.successWrapper = ^(id result) { 
        if (!call.success) return;
        _sessionToken = result;
        call.success(result); 
    };
    
    return call;
}

- (CISDOBAsyncCall *)listAggregationServices 
{ 
    NSString *method = @"listAggregationServices";
    NSArray *params = [NSArray arrayWithObjects: _sessionToken, nil];
    
    return [self callWithMethod: method params: params];
}

- (CISDOBAsyncCall *)createReportFromDataStore:(NSString *)dataStoreCode aggregationService:(NSString *)service parameters:(id)parameters
{
    NSString *method = @"createReportFromAggregationService";
    NSDictionary *usedParameters = (parameters) ? parameters : [NSDictionary dictionary];
    NSArray *params = [NSArray arrayWithObjects: _sessionToken, dataStoreCode, service, usedParameters, nil];
    
    return [self callWithMethod: method params: params];
}

- (void)executeCall:(CISDOBAsyncCall *)call { [self subclassResponsibility]; }

@end



@implementation CISDOBLiveConnection

- (void)dealloc
{
    _url = nil;
}

- (id)initWithUrl:(NSURL *)aUrl { return [self initWithUrl: aUrl trusted: NO]; }

- (id)initWithUrl:(NSURL *)aUrl trusted:(BOOL)aBool
{
    if (!(self = [super init])) return nil;
    
    _url = aUrl;
    _trusted = aBool;

    return self;
}

- (void)executeCall:(CISDOBConnectionCall *)call
{
    // Convert the call into a JSON-RPC call and run it
    CISDOBJsonRpcCall *jsonRpcCall = [[CISDOBJsonRpcCall alloc] init];
    jsonRpcCall.url = [_url URLByAppendingPathComponent: @"openbis/openbis/rmi-query-v1.json"];
    jsonRpcCall.timeoutInterval = _timeoutInterval;
    jsonRpcCall.delegate = self;
    jsonRpcCall.method = call.method;
    jsonRpcCall.params = call.params;
    jsonRpcCall.success = call.successWrapper;
    jsonRpcCall.fail = call.failWrapper;
    [jsonRpcCall start];
}

// CISDOBJsonRpcCallDelegate
- (BOOL)jsonRpcCall:(CISDOBJsonRpcCall *)call canTrustHost:(NSString *)host { return _trusted; }

@end

@implementation CISDOBPlaybackConnection

@end


@implementation CISDOBConnectionCall


- (id)initWithConnection:(CISDOBConnection *)aConnection method:(NSString *)aString params:(NSArray *)anArray
{
    if (!(self = [super init])) return nil;
    
    self.connection = aConnection;
    self.method = aString;
    self.params = anArray;
    
    self.success = nil;
    self.fail = nil;

    // The success and fail blocks are actually wrapped to give the call an opportunity to modify the result. These are the defaults. Clients may provide alternates
    __weak CISDOBConnectionCall *lexicalParent = self; // weak reference to avoid a retain cycle
    self.successWrapper = ^(id result) { if (lexicalParent.success) lexicalParent.success(result); };
    self.failWrapper = ^(NSError *error) { if (lexicalParent.fail) lexicalParent.fail(error); };
        
    return self;
}

- (void)start
{
    [_connection executeCall: self];
}

@end