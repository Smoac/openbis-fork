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
//  CISDOBIpadServiceInternal.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 12/19/12.
//

#import "CISDOBAsyncCall.h"
#import "CISDOBIpadService.h"

// Internal service call that includes the private state
@interface CISDOBIpadServiceCall : CISDOBAsyncCall

@property(weak, nonatomic) CISDOBIpadService *service;
@property(strong, nonatomic) CISDOBAsyncCall *connectionCall;

// Initialization
- (id)initWithService:(CISDOBIpadService *)service connectionCall:(CISDOBAsyncCall *)call;

// Actions
- (void)replaceSessionToken:(NSString *)oldSessionToken with:(NSString *)sessionToken;

@end

// An object that carries out all the steps that need to run after the user has logged into the server
@interface CISDOBIpadServicePostLoginCommand : NSObject

@property(weak, nonatomic) CISDOBIpadService *service;
@property(weak, nonatomic) CISDOBIpadServiceCall *ipadCall;

// Initialization
- (id)initWithService:(CISDOBIpadService *)service ipadCall:(CISDOBIpadServiceCall *)call;

// Actions
- (void)run;

@end

@interface CISDOBIpadService()

@property(strong, nonatomic) NSDictionary *ipadReadService;

@end

