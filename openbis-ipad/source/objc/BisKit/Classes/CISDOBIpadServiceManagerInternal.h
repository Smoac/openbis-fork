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
//  CISDOBIpadServiceManagerInternal.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 12/17/12.
//
//  Internal classes for the service manager, exposed here to be used in tests.
//

#import "CISDOBIpadServiceManager.h"
#import "CISDOBAsyncCall.h"

// Internal service call that includes the private state
@interface CISDOBIpadServiceManagerCall : CISDOBAsyncCall

@property(weak, nonatomic) CISDOBIpadServiceManager *serviceManager;
@property(strong, nonatomic) CISDOBAsyncCall *serviceCall;
@property(nonatomic) NSUInteger retryCount;     //<! How many times has this call been retried

@property(copy, nonatomic) NSString *willCallNotificationName;  //<! The notification called before the call takes place, may be nil
@property(copy, nonatomic) NSString *didCallNotificationName;   //<! The notification called after the call has completed, may be nil

// Initialization
- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager serviceCall:(CISDOBAsyncCall *)call;

- (void)notifySuccess:(id)result;
- (void)notifyFailure:(NSError *)error;

@end

// Internal service call that includes the private state
@interface CISDOBImageRetrievalCall : CISDOBAsyncCall {
@private
    // Internal state
    CISDOBIpadImage *_image;
    NSURLConnection *_connection;
}

@property(weak, nonatomic) CISDOBIpadServiceManager *serviceManager;
@property(strong, nonatomic) CISDOBIpadEntity *entity;

- (id)initWithServiceManager:(CISDOBIpadServiceManager *)serviceManager entity:(CISDOBIpadEntity *)entity;

@end

// An object that carries out all the steps that need to run after the user has logged into the server
@interface CISDOBIpadServiceManagerRetrieveRootSetCommand : NSObject

// These properties must all be set before running the commmand
@property(weak, nonatomic) CISDOBIpadServiceManager *serviceManager;
@property(weak, nonatomic) CISDOBIpadServiceManagerCall *serviceManagerCall;
@property(strong, nonatomic) NSArray *topLevelNavigationEntities;

// This property is updated while running
@property(nonatomic) NSUInteger currentIndex;

// Actions
- (void)run;

@end

// Internal methods of the service manager
@interface CISDOBIpadServiceManager (CISDOBIpadServiceManagerInternal)
- (BOOL)shouldRefreshRootLevelEntitiesCall;
- (CISDOBAsyncCall *)retrieveRootLevelEntitiesFromServer;
@end
