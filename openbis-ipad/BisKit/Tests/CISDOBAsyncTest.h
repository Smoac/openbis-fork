//
//  CISDOBAsyncTest.h
//  
//
//  Created by Ramakrishnan  Chandrasekhar on 9/26/12.
//
//

#import <SenTestingKit/SenTestingKit.h>

/**
 * \brief Abstract superclass for tests that use asynchronous calls.
 */
@class CISDOBAsyncCall;
@interface CISDOBAsyncTest : SenTestCase {
@protected
    BOOL                _callCompleted;
    BOOL                _callSucceeded;
    id                  _callResult;
    NSError             *_callError;
}

// Utility methods for subclasses to use
- (void)configureCall:(CISDOBAsyncCall *)call;
- (void)waitSeconds:(int)seconds forCallToComplete:(CISDOBAsyncCall *)call;

@end

// Useful helper functions
NSString* GetDefaultUserName();         //<! Return the username used in tests
NSString* GetDefaultUserPassword();     //<! Return the password used in tests
