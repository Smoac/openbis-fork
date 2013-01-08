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
//  CISDOBAsyncCall.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 9/25/12.
//
//

#import <Foundation/Foundation.h>
#import "CISDOBShared.h"

/**
 *  \brief An asynchronous call to a server.
 *
 *  The call object is used to configure aspects of the asynchronous calls to servers.
 *  Users will want to usually want to configure at least the success block and probably the fail block as well.
 */
@interface CISDOBAsyncCall : NSObject {
@protected
    // Exposed state
    SuccessBlock    _success;
    FailBlock       _fail;
}

// Configuration
@property(copy) SuccessBlock success;   //!< The block invoked if the invocation was successful. Can be nil.
@property(copy) FailBlock fail;         //!< The block invoked if the invocation failed. Can be nil.
@property(assign) NSTimeInterval timeoutInterval;   //!< The amount of time to wait for a response


// Actions
- (void)start;  //!< Make the call (asynchronously).

// Handling authentication challenges
- (void)trustProtectionSpaceForAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge;   //!< Tell the challenge to trust the protection space

@end


//
//! The interface that delegates to async calls implement
//
@interface NSObject (CISDOBAsyncCallDelegate)

//! Called e.g., when the call is sent over https to a server with a self-signed certificate.
//! If the host can be trusted, the call will continue, otherwise it will fail
- (void)asyncCall:(CISDOBAsyncCall *)call didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)authenticationChallenge;

@end
