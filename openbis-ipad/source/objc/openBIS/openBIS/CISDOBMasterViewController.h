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
//  CISDOBMasterViewController.h
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/3/12.
//  Copyright (c) 2012 ETHZ, CISD. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CISDOBDetailViewController, CISDOBOpenBisModel, CISDOBIpadServiceManager;
@class CISDOBTableBrowseState, CISDOBTableFilterState, CISDOBTableDisplayState, CISDOBTableSearchState;

#import <CoreData/CoreData.h>

@interface CISDOBMasterViewController : UITableViewController <NSFetchedResultsControllerDelegate, UISearchDisplayDelegate>

@property (strong, nonatomic) CISDOBDetailViewController *detailViewController;
@property (strong, nonatomic) CISDOBOpenBisModel *openBisModel;
@property (strong, nonatomic) CISDOBTableBrowseState *browseState;
@property (strong, nonatomic) CISDOBTableFilterState *filterState;
@property (strong, nonatomic) CISDOBTableSearchState *searchState;

// The state for the current searching or filtering mode.
@property (strong, nonatomic) CISDOBTableDisplayState *searchFilterState;

@property (weak, nonatomic) IBOutlet UITableView *browseTableView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *activityIndicator;
@property (strong, nonatomic) UIAlertView *waitDialog;

- (IBAction)refreshFromServer:(id)sender;

// Server Communication
- (void)didConnectServiceManager:(CISDOBIpadServiceManager *)serviceManager;

// Table Display
- (CISDOBTableDisplayState *)displayStateForTable:(UITableView *)tableView;
- (void)refreshTable;

@end

/**
 *  \brief An abstract superclass that handles interacting with and displaying the table
 */
@interface CISDOBTableDisplayState : NSObject

@property (strong, nonatomic) CISDOBOpenBisModel *openBisModel;
@property (weak, nonatomic) CISDOBMasterViewController *controller;

- (id)initWithController:(CISDOBMasterViewController *)controller;

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView;
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section;
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath;
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section;
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath;

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString;

@end

/**
 *  \brief The state that handles results for browing.
 */
@interface CISDOBTableBrowseState : CISDOBTableDisplayState


@end

/**
 *  \brief The state that handles results for filtering.
 */
@interface CISDOBTableFilterState : CISDOBTableDisplayState

@property (strong, nonatomic) NSArray *filteredResults;
@property (strong, nonatomic) NSPredicate *filterTemplate;

@end


/**
 *  \brief The state that handles results for search.
 */
@interface CISDOBTableSearchState : CISDOBTableDisplayState

@property (strong, nonatomic) NSArray *filteredResults;
@property (strong, nonatomic) NSPredicate *filterTemplate;

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString;

@end


