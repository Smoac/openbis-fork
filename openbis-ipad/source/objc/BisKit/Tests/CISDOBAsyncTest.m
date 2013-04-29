//
//  CISDOBAsyncTest.m
//  
//
//  Created by Ramakrishnan  Chandrasekhar on 9/26/12.
//
//

#import "CISDOBAsyncTest.h"
#import "CISDOBAsyncCall.h"

NSString* GetDefaultUserName() { return @"admin"; }
NSString* GetDefaultUserPassword() { return @"password"; }

@implementation CISDOBAsyncTest

- (void)waitSeconds:(int)seconds forCallToComplete:(CISDOBAsyncCall *)call
{
    _callCompleted = NO;
    _callSucceeded = NO;
    
    [call start];
    
    for(int i = 0; i < seconds && !_callCompleted; ++i) {
        // Run the runloop until an answer is returned
        CFRunLoopRunInMode(kCFRunLoopDefaultMode, 1, 0);
    }
    STAssertTrue(_callCompleted, @"Call did not complete");
}

- (void)configureCall:(CISDOBAsyncCall *)call
{
    SuccessBlock success = ^(id result) {
        _callCompleted = YES, _callSucceeded = YES;
        _callError = nil;
        if (_callResult) [_callResult release];
        _callResult = [result retain];
    };
    
    FailBlock fail = ^(NSError *error) {
        _callCompleted = YES, _callSucceeded = NO;
        _callResult = nil;
        if (_callError) [_callError release];
        _callError = [error retain];
    };
    call.success = success;
    call.fail = fail;
}

@end
