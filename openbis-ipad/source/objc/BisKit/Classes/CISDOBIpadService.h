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
@class CISDOBConnection, CISDOBAsyncCall, CISDOBClientPreferences;
@interface CISDOBIpadService : NSObject {
@private
    // Internal State
    BOOL            _isLoggedIn;
    NSDictionary    *_ipadReadService;
}

@property(readonly) CISDOBConnection *connection;
@property(strong, nonatomic) CISDOBClientPreferences *clientPreferences;

//! Designated initializer.
- (id)initWithConnection:(CISDOBConnection *)connection;

//! Log the user into the openBIS instance. The login procedure reqests the client preferences as well.
- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password;

//! A call that has no purpose except to inform the server that we are still here
- (CISDOBAsyncCall *)heartbeat;

//! Get all top-level categories from the openBIS ipad service. The success block will be invoked with a collection of CISDOBIpadRawEntity objects.
- (CISDOBAsyncCall *)listNavigationalEntities;

//! Get all root-level entities from the openBIS ipad service for the specified navigational entities. This should return all the entities associated with that navigational entity that are considered part of the root set. The success block will be invoked with a collection of CISDOBIpadRawEntity objects.
- (CISDOBAsyncCall *)listRootLevelEntities:(NSArray *)permIds refcons:(NSArray *)refcons;

//! Get drill information from the openBIS ipad service -- this will include information about the children of the entity and possibly their children as well. The permIds and refcons collections must have the same cardinality. The success block will be invoked with a collection of CISDOBIpadRawEntity objects.
- (CISDOBAsyncCall *)drillOnEntities:(NSArray *)permIds refcons:(NSArray *)refcons;

//! A convenience version of drillOnEntities:refcons: for one entity.
- (CISDOBAsyncCall *)drillOnEntityWithPermId:(NSString *)permId refcon:(id)refcon;

//! Get detail information from the openBIS ipad service. The permIds and refcons collections must have the same cardinality. The success block will be invoked with a collection of CISDOBIpadRawEntity objects.
- (CISDOBAsyncCall *)detailsForEntities:(NSArray *)permIds refcons:(NSArray *)refcons;

//! A convenience version of detailsForEntities:refcons: for one entity.
- (CISDOBAsyncCall *)detailsForEntityWithPermId:(NSString *)permId refcon:(id)refcon;

//! Search for entities matching the searchText. The success block will be invoked with a collection of CISDOBIpadRawEntity objects.
- (CISDOBAsyncCall *)searchForText:(NSString *)searchText;

// Utility Methods

- (NSArray *)convertToEntitiesPermIds:(NSArray *)permIds refcons:(NSArray *)refcons count:(NSUInteger)count;

@end


/**
 * \brief An abstraction of the data returned from the ipad module of the openBIS server.
 */
@interface CISDOBIpadRawEntity : NSObject {
@private
    // Internal state
    NSArray         *_content;
    NSDictionary    *_fieldMap;
}

@property(readonly) NSString *permId;
@property(readonly) NSString *refcon;
@property(readonly) NSString *category;
@property(readonly) NSString *summaryHeader;
@property(readonly) NSString *summary;
@property(readonly) NSString *children;     //<! The permIds of the children as a JSON string
@property(readonly) NSString *identifier;
@property(readonly) NSString *images;       //<! The image specifications as a JSON string
@property(readonly) NSString *imageUrl;     //<! Deprecated
@property(readonly) NSString *properties;   //<! The properties as a JSON string.
@property(readonly) NSString *rootLevel;

@end

/**
 * \brief The preferences for client behavior, as defined by the server.
 */
@interface CISDOBClientPreferences : NSObject {
@private
    // Internal state
    NSDictionary    *_preferences;
}

@property(readonly) NSTimeInterval rootSetRefreshInterval;   //<! The min interval (in seconds) to wait between refreshes of the root data set.

@property(readonly) NSDictionary *preferences; //<! Used to access the raw preferences dictionary. The recommended way to get preferences is with the above accessors.

- (id)initWithServerResult:(NSDictionary *)result;

@end
