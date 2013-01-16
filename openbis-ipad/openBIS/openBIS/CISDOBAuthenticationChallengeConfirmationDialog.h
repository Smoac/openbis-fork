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
//  CISDOBAuthenticationChallengeConfirmationDialog.h
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 1/9/13.
//

#import <Foundation/Foundation.h>

@class CISDOBAsyncCall;

/**
 * \brief A class that presents a dialog for appropriate for authentication challenges. The class also handles the user input appropriately
 */
@interface CISDOBAuthenticationChallengeConfirmationDialog : NSObject {
@private
    UIAlertView         *_alertView;
    CISDOBAsyncCall     *_call;
        // The challenge that initiated ne need for confirmation.
    NSURLAuthenticationChallenge *_challenge;
        // Any other challenges that may have come up.
    NSMutableArray      *_challenges;
}

//! The delegate gets notified when the dialog has completed
@property(strong, nonatomic) id delegate;
@property(readonly) CISDOBAsyncCall *call;

- (id)initWithCall:(CISDOBAsyncCall *)call challenge:(NSURLAuthenticationChallenge *)challenge;

- (void)addChallenge:(NSURLAuthenticationChallenge *)challenge;

// Actions
- (void)start; //!< Present the dialog and handle the result

@end

@interface NSObject (CISDOBAuthenticationChallengeConfirmationDialogDelegate)

//! Called once the dialog has been dismissed. If didTrust is true, the user trusted the connection.
- (void)didDismissConfirmationDialog:(CISDOBAuthenticationChallengeConfirmationDialog *)dialog trusting:(BOOL)didTrust;

@end
