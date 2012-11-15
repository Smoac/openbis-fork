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
//  CISDOBConnection.h
//  BisMac
//
//  Created by cramakri on 27.08.12.
//
//

#import <Foundation/Foundation.h>
#import "CISDOBShared.h"

//! The error domain for errors in the Connection layer
FOUNDATION_EXPORT NSString *const CISDOBConnectionErrorDomain;

enum CISDOBConnectionErrorCode {
    kCISDOBConnectionError_NoServerAvailable = 1,
};


/**
 *  \brief A connection to an openBIS server.
 * 
 *  The connection is an abstract superclass.
 *  There are two concrete subclasses:
 *      - CISDOBLiveConnection -- a connection that runs over the network
 *      - CISDOBPlaybackConnection -- a simulated connection that responds to requests 
 *        by returning data that was previously saved to a file. Useful for testing.
 * 
 *  The methods on the connection do not immidiately execute the call. Instead, they return call objects that can be configured.
 *  Typical configuration will include setting the success and fail blocks.
 */
@class CISDOBAsyncCall;
@interface CISDOBConnection : NSObject {
@protected
        // Internal state
    NSString        *_sessionToken;
    NSTimeInterval  _timeoutInterval;
}

@property(readonly) NSString *sessionToken;             //!< The session token for the connection
@property NSTimeInterval timeoutInterval;               //!< Timeout interval for calls. Defaults to 10s.

// Actions
- (CISDOBAsyncCall *)loginUser:(NSString *)user password:(NSString *)password;
- (CISDOBAsyncCall *)listAggregationServices;
    // parameters may be nil
- (CISDOBAsyncCall *)createReportFromDataStore:(NSString *)dataStoreCode aggregationService:(NSString *)service parameters:(id)parameters;

@end


/**
 *  \brief An actual, live connection to an openBIS server
 *
 */
@interface CISDOBLiveConnection : CISDOBConnection {
@private
    // Exposed state
    NSURL           *_url;
    BOOL            _trusted;
}

@property(readonly) NSURL *url;                         //!< The URL for openBIS. This should just be the address and port.
@property(readonly, getter=isTrusted) BOOL trusted;     //!< Is the server trusted? If so, self-signed certificates will be automatically accepted. By default, NO.

// Initialization
- (id)initWithUrl:(NSURL *)url;                         //!< Initialize with trusted = NO
- (id)initWithUrl:(NSURL *)url trusted:(BOOL)trusted;   //!< Designated initializer.

@end

@interface CISDOBPlaybackConnection : CISDOBConnection {
@private
    
}

@end

/**
 *  \brief A fake connection to openBIS
 *
 */
@interface CISDOBDeadConnection : CISDOBConnection {
@private
    
}

@end
