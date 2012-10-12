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
//  CISDOBIpadService.h
//  BisMac
//
//  Created by cramakri on 27.08.12.
//
//

#import <Foundation/Foundation.h>
#import "CISDOBShared.h"

//! The error domain for errors in the IpadService layer
FOUNDATION_EXPORT NSString *const CISDOBIpadServiceErrorDomain;

enum CISOBIpadServiceErrorCode {
    kCISOBIpadServiceError_NoIpadServiceAvailable = 1,
};

/**
 * \brief A facade for accessing openBIS iPad UI module.
 *
 * All calls to the connection are made asynchronously. Thus, the calls all return async call objects which can be configured.
 */
@class CISDOBConnection, CISDOBAsyncCall;
@interface CISDOBIpadService : NSObject {
@private
    // Internal State
    BOOL            _isLoggedIn;
    NSDictionary*   _ipadReadService;
}

@property(readonly) CISDOBConnection *connection;

//! Designated initializer.
- (id)initWithConnection:(CISDOBConnection *)connection;

//! Log the user into the openBIS instance
- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password;

//! Get all entities from the openBIS ipad service. The success message will be invoked with a collection of CISDOBIpadRawEntity objects.
- (CISDOBAsyncCall *)listAllEntities;

@end


/**
 * \brief An abstraction of the data returned from the ipad module of the openBIS server.
 */
@interface CISDOBIpadRawEntity : NSObject {
@private
    // Internal state
    NSArray*    _content;
}

@property(readonly) NSString *summaryHeader;
@property(readonly) NSString *summary;
@property(readonly) NSString *identifier;
@property(readonly) NSString *permId;
@property(readonly) NSString *refcon;
@property(readonly) NSString *group;
@property(readonly) NSString *imageUrl;
@property(readonly) NSString *children;     //<! The permIds of the children as a JSON string
@property(readonly) NSString *properties;   //<! The properties as a JSON string.

@end
