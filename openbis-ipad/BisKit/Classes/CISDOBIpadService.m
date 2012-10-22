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
//  CISDOBIpadService.m
//  BisMac
//
//  Created by cramakri on 27.08.12.
//
//

#import "CISDOBIpadService.h"
#import "CISDOBConnection.h"
#import "CISDOBAsyncCall.h"

NSString *const CISDOBIpadServiceErrorDomain = @"CISDOBIpadServiceErrorDomain";

// Internal service call that includes the private state
@interface CISDOBIpadServiceCall : CISDOBAsyncCall {
@private
    // Internal state
    CISDOBAsyncCall     *_connectionCall;
}
@property(weak) CISDOBIpadService *service;
@property(nonatomic) CISDOBAsyncCall *connectionCall;

// Initialization
- (id)initWithService:(CISDOBIpadService *)service connectionCall:(CISDOBAsyncCall *)call;

@end

// Internal methods
@interface CISDOBIpadRawEntity (CISDOBIpadRawEntityPrivate)

- (id)initWithContent:(NSArray *)content fieldMap:(NSDictionary *)fieldMap;

@end


@implementation CISDOBIpadService

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
    }
    
    return self;
}

- (id)initWithConnection:(CISDOBConnection *)aConn
{
    self = [super init];
    if (!self) return nil;
    
    _connection = aConn;
    _isLoggedIn = NO;

    return self;
}

- (BOOL)isIpadSupported { return _ipadReadService != nil; }

- (void)rememberIpadService:(NSArray *)services notifying:(CISDOBIpadServiceCall *)iPadCall
{    
    for (NSDictionary *service in services) {
        if ([@"ipad-read-service-v1" isEqualToString: [service objectForKey: @"serviceKey"]]) {
            _ipadReadService = service;
            break;
        }
    }
    
    if (_ipadReadService == nil) {
        NSString *errorMessage = @"The iPad service is not installed on the selected server";
        NSDictionary *userInfo =
            [NSDictionary dictionaryWithObjectsAndKeys: errorMessage, NSLocalizedDescriptionKey, nil];
        NSError *error = [NSError errorWithDomain: CISDOBIpadServiceErrorDomain code: kCISOBIpadServiceError_NoIpadServiceAvailable userInfo: userInfo];
        if (iPadCall.fail) iPadCall.fail(error);
        return;
    }
    
    if (iPadCall.success) iPadCall.success(_connection.sessionToken);
    
}

- (void)determineIsIpadSupported:(CISDOBIpadServiceCall *)iPadCall
{
    CISDOBAsyncCall *connectionCall = [_connection listAggregationServices];
    iPadCall.connectionCall = connectionCall;
    connectionCall.success = ^(id result) { [self rememberIpadService: result notifying: iPadCall]; };
    connectionCall.fail = ^(NSError *error) { if (iPadCall.fail) iPadCall.fail(error); };
    [connectionCall start];
}

- (CISDOBIpadServiceCall *)iPadCallWrappingConnectionCall:(CISDOBAsyncCall *)connectionCall
{
    CISDOBIpadServiceCall *iPadCall = [[CISDOBIpadServiceCall alloc] initWithService: self connectionCall: connectionCall];
    
    connectionCall.fail = ^(NSError *error) { if (iPadCall.fail) iPadCall.fail(error); };
    
    return iPadCall;
}

- (NSDictionary *)computeColumnsFieldMapFromColumns:(NSArray *)columns
{
    // Convert the array to a map indexed by field name.
    NSMutableDictionary *fieldMap = [[NSMutableDictionary alloc] initWithCapacity: [columns count]];
    NSUInteger i = 0;
    for (NSDictionary *col in columns) {
        [fieldMap
            setObject: [NSNumber numberWithUnsignedInteger: i++]
            forKey: [col objectForKey: @"title"]];
    }
    
    return fieldMap;
}

- (NSArray *)rawEntitiesFromResult:(NSDictionary *)result
{
    NSArray *columns = [result objectForKey: @"columns"];
    NSDictionary *fieldMap = [self computeColumnsFieldMapFromColumns: columns];
    NSMutableArray *rawEntities = [[NSMutableArray alloc] init];
    NSArray *rows = [result objectForKey: @"rows"];
    for (NSArray *row in rows) {
        CISDOBIpadRawEntity* rawEntity = [[CISDOBIpadRawEntity alloc] initWithContent: row fieldMap: fieldMap];
        [rawEntities addObject: rawEntity];
    }
    
    return rawEntities;
}

- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password
{
    CISDOBAsyncCall *connectionCall = [_connection loginUser: user password: password];
    CISDOBIpadServiceCall *iPadCall = [self iPadCallWrappingConnectionCall: connectionCall];
    
    connectionCall.success = ^(id result) {
        // Note that we are logged in, but wait until we figure out if the ipad is supported
        // to notify the client.
        _isLoggedIn = YES;
        [self determineIsIpadSupported: iPadCall];
    };

    return iPadCall;
}

- (CISDOBAsyncCall *)listRootLevelEntities;
{
    NSDictionary *parameters = [NSDictionary dictionaryWithObject: @"ROOT" forKey: @"requestKey"];
    CISDOBAsyncCall *connectionCall = [_connection
        createReportFromDataStore: [_ipadReadService objectForKey: @"dataStoreCode"]
        aggregationService: [_ipadReadService objectForKey: @"serviceKey"]
        parameters: parameters];
    CISDOBIpadServiceCall *iPadCall = [self iPadCallWrappingConnectionCall: connectionCall];
    
    connectionCall.success = ^(id result) {
        if (iPadCall.success) {
            iPadCall.success([self rawEntitiesFromResult: result]);
        }
    };
    
    return iPadCall;
}

- (CISDOBAsyncCall *)drillOnEntities:(NSArray *)permIds refcons:(NSArray *)refcons
{
    NSUInteger count = [permIds count];
    NSAssert([refcons count] == count, @"Drilling requires permIds and refcons. There must be an equal number of these.");
    NSMutableArray *entities = [[NSMutableArray alloc] initWithCapacity: [permIds count]];
    for (NSUInteger i = 0; i < count; ++i) {
        NSDictionary *entity =
            [NSDictionary dictionaryWithObjectsAndKeys:
                [permIds objectAtIndex: i], @"PERM_ID",
                [refcons objectAtIndex: i], @"REFCON", nil];
        [entities addObject: entity];
    }

    NSDictionary *parameters =
        [NSDictionary dictionaryWithObjectsAndKeys:
            @"DRILL", @"requestKey",
            entities, @"entities", nil];
    CISDOBAsyncCall *connectionCall =
        [_connection
            createReportFromDataStore: [_ipadReadService objectForKey: @"dataStoreCode"]
            aggregationService: [_ipadReadService objectForKey: @"serviceKey"]
            parameters: parameters];
    CISDOBIpadServiceCall *iPadCall = [self iPadCallWrappingConnectionCall: connectionCall];
    
    connectionCall.success = ^(id result) {
        if (iPadCall.success) {
            iPadCall.success([self rawEntitiesFromResult: result]);
        }
    };
    
    return iPadCall;
}

- (CISDOBAsyncCall *)drillOnEntityWithPermId:(NSString *)permId refcon:(id)refcon
{
    NSArray *permIds = [NSArray arrayWithObject: permId];
    NSArray *refcons = [NSArray arrayWithObject: refcon];
    return [self drillOnEntities: permIds refcons: refcons];
}

@end

@implementation CISDOBIpadServiceCall

- (id)initWithService:(CISDOBIpadService *)service connectionCall:(CISDOBAsyncCall *)call
{
    if (!(self = [super init])) return nil;
 
    _service = service;
    _connectionCall = call;
    
    return self;
}

- (void)start
{
    [_connectionCall start];
}

@end

@implementation CISDOBIpadRawEntity

- (id)initWithContent:(NSArray *)content fieldMap:(NSDictionary *)fieldMap
{
    if (!(self = [super init])) return nil;
    
    _content = content;
    _fieldMap = fieldMap;

    return self;
}

- (NSString *)stringContentValueAtName:(NSString *)name
{
    // Look up the index in the map
    NSNumber *indexHolder = [_fieldMap objectForKey: name];
    // This value was not provided
    if (!indexHolder) return nil;
    NSUInteger index = [indexHolder unsignedIntegerValue];
    return [[_content objectAtIndex: index] objectForKey: @"value"];
}

- (NSString *)permId { return [self stringContentValueAtName: @"PERM_ID"]; }
- (NSString *)refcon { return [self stringContentValueAtName: @"REFCON"]; }
- (NSString *)category { return [self stringContentValueAtName: @"CATEGORY"]; }
- (NSString *)summaryHeader { return [self stringContentValueAtName: @"SUMMARY_HEADER"]; }
- (NSString *)summary { return [self stringContentValueAtName: @"SUMMARY"]; }
- (NSString *)children { return [self stringContentValueAtName: @"CHILDREN"]; }
- (NSString *)identifier { return [self stringContentValueAtName: @"IDENTIFIER"]; }
- (NSString *)imageUrl { return [self stringContentValueAtName: @"IMAGE_URL"]; }
- (NSString *)properties { return [self stringContentValueAtName: @"PROPERTIES"]; }
- (NSString *)rootLevel { return [self stringContentValueAtName: @"ROOT_LEVEL"]; }

@end
