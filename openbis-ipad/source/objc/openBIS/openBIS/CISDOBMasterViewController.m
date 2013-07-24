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
//  CISDOBMasterViewController.m
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/3/12.
//  Copyright (c) 2012 ETHZ, CISD. All rights reserved.
//

#import "CISDOBMasterViewController.h"

#import "CISDOBDetailViewController.h"
#import "CISDOBIpadEntity.h"
#import "CISDOBOpenBisModel.h"
#import "CISDOBBarcodeViewController.h"

@interface CISDOBMasterViewController ()
- (void)configureCell:(UITableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath;
- (void)configureCell:(UITableViewCell *)cell forEntity:(CISDOBIpadEntity *)entity;
- (void)initializeDrillDownFromParent:(CISDOBMasterViewController *)parent;
- (void)selectionDidChangeForModel;
@end

@implementation CISDOBMasterViewController

- (void)awakeFromNib
{
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        self.clearsSelectionOnViewWillAppear = NO;
        self.contentSizeForViewInPopover = CGSizeMake(320.0, 600.0);
    }
    [super awakeFromNib];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
//  CR -- No edit support initially
//    self.navigationItem.leftBarButtonItem = self.editButtonItem;

//  CR -- No add support initially
//    UIBarButtonItem *addButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd target:self action:@selector(insertNewObject:)];
//    self.navigationItem.rightBarButtonItem = addButton;
    self.detailViewController = (CISDOBDetailViewController *)[[self.splitViewController.viewControllers lastObject] topViewController];
    self.browseState = [[CISDOBTableBrowseState alloc] initWithController: self];
    self.filterState = [[CISDOBTableFilterState alloc] initWithController: self];
    self.searchState = [[CISDOBTableSearchState alloc] initWithController: self];
    self.searchFilterState = self.filterState;
    
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(receiveSearchNotification:)
     name:@"SearchNotification"
     object:nil];
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(receiveDissmissNotification:)
     name:@"DissmissNotification"
     object:nil];
}

- (IBAction)refreshFromServer:(id)sender
{
    [self.openBisModel refreshParentFromServer: ^(id result) {
        [self refreshTable];
    }];
    
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)insertNewObject:(id)sender
{
    NSError *error;
    if (![self.openBisModel insertNewObjectOrError: &error]) {
        // TODO Implement error handling
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
    }
}

- (CISDOBTableDisplayState *)displayStateForTable:(UITableView *)tableView
{
    return (tableView == self.browseTableView) ? self.browseState : self.searchFilterState;
}

- (void)refreshTable
{
    [self.openBisModel refreshResults];
    [self.tableView reloadData];
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"showDetail"]) {
        [[segue destinationViewController] setOpenBisModel: self.openBisModel];
    }
}

/*
- (void)showActivityIndicatorDialog:(NSString *)message
{
    _waitDialog = [[UIAlertView alloc] initWithTitle:message message:nil delegate:self cancelButtonTitle:nil otherButtonTitles: nil];
    [_waitDialog show];
    
    UIActivityIndicatorView *indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle: UIActivityIndicatorViewStyleWhiteLarge];
    
    // Adjust the indicator so it is up a few pixels from the bottom of the alert
    indicator.center = CGPointMake(_waitDialog.bounds.size.width / 2, _waitDialog.bounds.size.height - 50);
    [indicator startAnimating];
    [_waitDialog addSubview:indicator];
}

- (void)removeActivityIndicatorDialog
{
    [_waitDialog dismissWithClickedButtonIndex:0 animated:YES];
}
*/

- (void)showActivityIndicatorOnView:(UIView *)view
{
    self.activityIndicator.center = CGPointMake(view.center.x, view.center.y - 242 - 90);
    [self.activityIndicator startAnimating];
    [view addSubview: self.activityIndicator];
}

- (void)removeActivityIndicatorFromView:(UIView *)view
{
    // Could check that the activity indicator is on the view, but not currently implemented
    [self.activityIndicator stopAnimating];
    [self.activityIndicator removeFromSuperview];
}


#pragma mark - Table View

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [[self displayStateForTable: tableView] numberOfSectionsInTableView: tableView];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [[self displayStateForTable: tableView] tableView: tableView numberOfRowsInSection: section];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return [[self displayStateForTable: tableView] tableView: tableView cellForRowAtIndexPath: indexPath];
}


- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return [[self displayStateForTable: tableView] tableView: tableView titleForHeaderInSection: section];
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return NO;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        NSError *error = nil;
        if (![self.openBisModel deleteObjectAtIndexPath: indexPath error: &error]) {
            // TODO Implement error handling
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        }
    }   
}

- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // The table view should not be re-orderable.
    return NO;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Segue to the detail view unless we are on the ipad
    if ([[UIDevice currentDevice] userInterfaceIdiom] != UIUserInterfaceIdiomPad) return;
    
    [[self displayStateForTable: tableView] tableView: tableView didSelectRowAtIndexPath: indexPath];
}

- (void)selectionDidChangeForModel
{
    // Figure out what to do with the detail view and the navigation view
    self.detailViewController.openBisModel = self.openBisModel;
    if ([self.openBisModel selectionHasChildren]) {
        // Keep ahead of the user and get any data necessary for navigation
        [self.openBisModel syncSelectedObjectForNavigationOnSuccess: ^(id result){ }];
        // Drill into the hierarchy
        UIStoryboard *storyboard = self.storyboard;
        CISDOBMasterViewController *child = [storyboard instantiateViewControllerWithIdentifier: @"Master"];
        [child initializeDrillDownFromParent: self];
        [self.navigationController pushViewController: child animated: YES];
        [self.detailViewController selectionIsChanging];
    } else {
        // Show the current selection
        [self.detailViewController selectionDidChange];
    }
}

- (void)configureCell:(UITableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    CISDOBIpadEntity *entity = [self.openBisModel objectAtIndexPath: indexPath];
    [self configureCell: cell forEntity: entity];
}

- (void)configureCell:(UITableViewCell *)cell forEntity:(CISDOBIpadEntity *)entity
{
    cell.textLabel.text = entity.summaryHeader;
    cell.detailTextLabel.text = entity.summary;
    if ([self.openBisModel entityHasChildren: entity]) {
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    } else {
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
}

#pragma mark - Fetched results controller

- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableView beginUpdates];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    switch(type) { 
        case NSFetchedResultsChangeInsert:
            [self.tableView insertSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [self.tableView deleteSections:[NSIndexSet indexSetWithIndex:sectionIndex] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
      newIndexPath:(NSIndexPath *)newIndexPath
{
    UITableView *tableView = self.tableView;
    
    switch(type) {
        case NSFetchedResultsChangeInsert:
            [tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeDelete:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
            
        case NSFetchedResultsChangeUpdate:
            [self configureCell:[tableView cellForRowAtIndexPath:indexPath] atIndexPath:indexPath];
            break;
            
        case NSFetchedResultsChangeMove:
            [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
            [tableView insertRowsAtIndexPaths:@[newIndexPath] withRowAnimation:UITableViewRowAnimationFade];
            break;
    }
}


- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.tableView endUpdates];
}


// Implementing the above methods to update the table view in response to individual changes may have performance implications if a large number of changes are made simultaneously. If this proves to be an issue, you can instead just implement controllerDidChangeContent: which notifies the delegate that all section and object changes have been processed. 
 /*
 - (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    // In the simplest, most efficient, case, reload the table view.
    [self.tableView reloadData];
} */


#pragma mark - UISearchDisplayDelegate

- (void) receiveSearchNotification:(NSNotification *) notification
{
    NSString * searchString = [notification object];
    [self searchDisplayControllerWillBeginSearch: self.searchDisplayController];
    self.searchDisplayController.searchBar.text = searchString;
    self.openBisModel.searchString = searchString;
    [self searchDisplayController: self.searchDisplayController shouldReloadTableForSearchScope: 0];
}

- (void) receiveDissmissNotification:(NSNotification *) notification
{
    self.searchDisplayController.searchBar.selectedScopeButtonIndex = 0;
}

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    self.openBisModel.searchString = searchString;
    return [self.searchFilterState searchDisplayController: controller shouldReloadTableForSearchString: searchString];
}

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchScope:(NSInteger)searchOption
{
    // BUGFIX - Avoid application crash
    if (self.openBisModel.searchString == nil) {
        self.openBisModel.searchString = @"";
    }
    // BUGFIX
    
    //Updating the state controller if the user presses a scope buttion
    self.openBisModel.selectedSearchScopeIndex = searchOption;
    self.searchFilterState = [self.openBisModel isSelectedSearchScopeIndexSearch] ? self.searchState : self.filterState;
    
    //Searching for barcodes or normal search
    NSString* searchTitle = [self scopeButtonTitles][searchOption];
    if ([searchTitle isEqualToString:@"Barcode"]) {
        UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"MainStoryboard_iPad" bundle:nil];
        CISDOBBarcodeViewController *barcodeController = [storyboard instantiateViewControllerWithIdentifier:@"Barcode"];
        barcodeController.modalPresentationStyle = UIModalPresentationFullScreen;
        [self presentModalViewController:barcodeController animated:YES];
        return NO;
    } else {
        return [self.searchFilterState searchDisplayController: controller shouldReloadTableForSearchString: self.openBisModel.searchString];
    }
}

- (void)searchDisplayControllerWillBeginSearch:(UISearchDisplayController *)controller
{
    // BUGFIX - Avoid inconsistent selected button when moving through sections that change the state
    if ([self.searchFilterState class] == [CISDOBTableSearchState class]) {
        controller.searchBar.selectedScopeButtonIndex = 0;
    } else if ([self.searchFilterState class] == [CISDOBTableFilterState class]) {
        controller.searchBar.selectedScopeButtonIndex = [self scopeButtonTitles].count - 1;
    }
    // BUGFIX
    
    controller.searchBar.scopeButtonTitles = [self scopeButtonTitles];
    controller.searchBar.showsScopeBar = YES;
}

- (void)searchDisplayControllerDidEndSearch:(UISearchDisplayController *)controller
{
    controller.searchBar.showsScopeBar = NO;
}

#pragma mark - Server Communication
- (void)didConnectServiceManager:(CISDOBIpadServiceManager *)serviceManager
{
    [self.openBisModel syncRootEntities: ^(id result) {
        [self refreshTable];
    }];
    
    self.searchFilterState = [self.openBisModel isSearchSupported] ? self.searchState : self.filterState;
}


#pragma mark - Properties
- (void)setOpenBisModel:(CISDOBOpenBisModel *)openBisModel
{
    _openBisModel = openBisModel;
    _openBisModel.delegate = self;
}

- (void)initializeDrillDownFromParent:(CISDOBMasterViewController *)parent
{
    self.openBisModel = [[CISDOBOpenBisModel alloc] initWithParentModel: parent.openBisModel];
    // The title of the drill-down is the parent's title
    self.title = parent.openBisModel.selectedObject.summaryHeader;
}

- (NSArray *)scopeButtonTitles
{
    return self.openBisModel.searchScopeTitles;
}

@end

@implementation CISDOBTableDisplayState

- (id)initWithController:(CISDOBMasterViewController *)controller;
{
    if (!(self = [super init])) return nil;
    
    self.controller = controller;
    self.openBisModel = controller.openBisModel;
    
    return self;
}


// Everything is a subclass responsibility
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView { return 0; }
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section { return 0; }
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath { return nil; }
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section { return nil; }
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath { }
- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString { return NO; }

@end


@implementation CISDOBTableBrowseState

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.openBisModel numberOfSections];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.openBisModel numberOfEntitiesInSection: section];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell ;
    cell = [tableView dequeueReusableCellWithIdentifier: @"Cell" forIndexPath: indexPath]; //  This only works in iOS 6,
    [self.controller configureCell: cell atIndexPath: indexPath];
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return [self.openBisModel titleForHeaderInSection: section];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [self.openBisModel selectObjectAtIndexPath: indexPath];
    [self.controller selectionDidChangeForModel];
}

@end

@implementation CISDOBTableFilterState

- (id)initWithController:(CISDOBMasterViewController *)controller
{
    if (!(self = [super initWithController: controller])) return nil;
    
    NSPredicate *filterTemplate = [NSPredicate predicateWithFormat: @"SELF.refconJson contains[cd] $SEARCH_STRING OR SELF.summary contains[cd] $SEARCH_STRING  OR SELF.summaryHeader contains[cd] $SEARCH_STRING"];
    self.filterTemplate = filterTemplate;
    
    return self;
}

- (CISDOBIpadEntity *)entityAtIndexPath:(NSIndexPath *)indexPath
{
    return [self.filteredResults objectAtIndex: [indexPath indexAtPosition: 1]];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return (self.filteredResults) ? [self.filteredResults count] : 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *cellId = @"FilterCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier: cellId];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle: UITableViewCellStyleSubtitle reuseIdentifier: cellId];
    }
    
    CISDOBIpadEntity *entity = [self entityAtIndexPath: indexPath];
    [self.controller configureCell: cell forEntity: entity];
    return cell;
}


- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"Filter Results";
}

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    NSArray *fullResult = [self.openBisModel.fetchedResultsController fetchedObjects];
    
    NSPredicate *filterPredicate =
        [self.filterTemplate predicateWithSubstitutionVariables:
            [NSDictionary dictionaryWithObject: searchString forKey: @"SEARCH_STRING"]];
    self.filteredResults = [fullResult filteredArrayUsingPredicate: filterPredicate];
    return YES;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    self.openBisModel.selectedObject = [self entityAtIndexPath: indexPath];
    [self.controller selectionDidChangeForModel];
}

@end

@implementation CISDOBTableSearchState

- (id)initWithController:(CISDOBMasterViewController *)controller
{
    if (!(self = [super initWithController: controller])) return nil;
    
    NSPredicate *filterTemplate = [NSPredicate predicateWithFormat: @"SELF.refconJson contains[cd] $SEARCH_STRING OR SELF.summary contains[cd] $SEARCH_STRING  OR SELF.summaryHeader contains[cd] $SEARCH_STRING"];
    self.filterTemplate = filterTemplate;
    
    return self;
}

- (CISDOBIpadEntity *)entityAtIndexPath:(NSIndexPath *)indexPath
{
    return [self.filteredResults objectAtIndex: [indexPath indexAtPosition: 1]];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return (self.filteredResults) ? [self.filteredResults count] : 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *cellId = @"SearchCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier: cellId];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle: UITableViewCellStyleSubtitle reuseIdentifier: cellId];
    }
    
    CISDOBIpadEntity *entity = [self entityAtIndexPath: indexPath];
    [self.controller configureCell: cell forEntity: entity];
    return cell;
}


- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"Search Results";
}

- (void) makeSearch:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    [self.controller showActivityIndicatorOnView: controller.searchResultsTableView];
    
    __weak CISDOBTableSearchState *weakSelf = self;
    [self.openBisModel searchServerOnSuccess: ^(NSArray *result) {
        [weakSelf.controller removeActivityIndicatorFromView: controller.searchResultsTableView];
        weakSelf.filteredResults = result;
        [controller.searchResultsTableView reloadData];
        
        if([result count] == 1) {
            [self.controller tableView: controller.searchResultsTableView didSelectRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0]];
        }
    }];
}

static NSTimer *timer = nil;

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    if(timer != nil) {
        [timer invalidate];
    }
    
    if(searchString.length > 2) {
        NSMethodSignature *methodSignature = [self methodSignatureForSelector:@selector(makeSearch:shouldReloadTableForSearchString:)];
        NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:methodSignature];
        [invocation setTarget:self];
        [invocation setSelector:@selector(makeSearch:shouldReloadTableForSearchString:)];
        [invocation setArgument:&controller atIndex:2];
        [invocation setArgument:&searchString atIndex:3];
        
        timer = [NSTimer scheduledTimerWithTimeInterval:1.0 invocation:invocation repeats:NO];
        return NO; // Do not refresh the table yet, wait until the server returns
    } else {
        __weak CISDOBTableSearchState *weakSelf = self;
        NSMutableArray *emptyResult = [NSMutableArray arrayWithCapacity:0];
        weakSelf.filteredResults = emptyResult;
        return YES;
    }
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    self.openBisModel.selectedObject = [self entityAtIndexPath: indexPath];
    [self.controller selectionDidChangeForModel];
}

@end
