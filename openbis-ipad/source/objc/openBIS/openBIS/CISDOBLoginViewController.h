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
//  CISDOBLoginViewController.h
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 11/15/12.
//

#import <UIKit/UIKit.h>

@class CISDOBDetailViewController, CISDOBAppDelegate;

@interface CISDOBLoginViewController : UIViewController

@property (weak, nonatomic) CISDOBAppDelegate *appDelegate;

@property (weak, nonatomic) IBOutlet UITextField *urlTextField;
@property (weak, nonatomic) IBOutlet UITextField *usernameTextField;
@property (weak, nonatomic) IBOutlet UITextField *passwordTextField;
@property (weak, nonatomic) IBOutlet UILabel *errorLabel;

// Actions
- (IBAction)demoClicked:(id)sender;
- (IBAction)loginClicked:(id)sender;
- (void)showError:(NSError *)error;

@end
