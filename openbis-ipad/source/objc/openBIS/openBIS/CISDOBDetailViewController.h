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
//  CISDOBDetailViewController.h
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/3/12.
//  Copyright (c) 2012 ETHZ, CISD. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CISDOBIpadEntity, CISDOBOpenBisModel, CISDOBAppDelegate;
@interface CISDOBDetailViewController : UIViewController <UISplitViewControllerDelegate, UIWebViewDelegate, UIPopoverControllerDelegate>

@property (weak, nonatomic) CISDOBAppDelegate *appDelegate;
@property (strong, nonatomic) CISDOBOpenBisModel *openBisModel;

@property (weak, nonatomic) IBOutlet UITableView *propertiesTableView;
@property (weak, nonatomic) IBOutlet UILabel *summaryHeaderLabel;
@property (weak, nonatomic) IBOutlet UILabel *summaryLabel;
@property (weak, nonatomic) IBOutlet UILabel *identifierLabel;
@property (weak, nonatomic) IBOutlet UIWebView *webView;
@property (weak, nonatomic) IBOutlet UILabel *statusLabel;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *goOnlineButton;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *refreshButton;

// UI Actions
- (IBAction)goOnline:(id)sender;
- (IBAction)refreshFromServer:(id)sender;

// Actions
- (void)selectionDidChange;     //!< Signals that the user has made a final selction
- (void)selectionIsChanging;    //!< Signals that the user has changed the seleciton, but has not yet decided

@end
