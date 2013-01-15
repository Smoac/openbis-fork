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
//  CISDOBConnectionInternal.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 12/19/12.
//

#import "CISDOBAsyncCall.h"
#import "CISDOBConnection.h"

// Internal connection call that includes the private state
@interface CISDOBConnectionCall : CISDOBAsyncCall {
@private
    // Internal state
    CISDOBConnection    *__weak _connection;
}
@property(weak, nonatomic) CISDOBConnection *connection;
@property(strong, nonatomic) NSString *method;
@property(strong, nonatomic) NSArray *params;
@property(copy, nonatomic) SuccessBlock successWrapper;
@property(copy, nonatomic) FailBlock failWrapper;

// Initialization
- (id)initWithConnection:(CISDOBConnection *)aConnection method:(NSString *)aString params:(NSArray *)anArray;

// Actions
- (void)replaceSessionToken:(NSString *)oldSessionToken with:(NSString *)sessionToken;
@end


/**
 *  \brief An interface with methods that support testing. These methods are used to get the connection into error states. There should be no need to use them outside of testing.
 */
@interface CISDOBConnection (CISDOBConnectionTesting)

- (void)setSessionTokenForTesting:(NSString *)bogusSessionToken;

@end
