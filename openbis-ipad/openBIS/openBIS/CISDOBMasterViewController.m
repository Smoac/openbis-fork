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

@interface CISDOBMasterViewController ()
- (void)configureCell:(UITableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath;
- (void)initializeDrillDownFromParent:(CISDOBMasterViewController *)parent;
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

#pragma mark - Table View

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

    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath]; //  This only works in iOS 6,
//    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell"]; // iOS5
    [self configureCell:cell atIndexPath:indexPath];
    return cell;
}


- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return [self.openBisModel titleForHeaderInSection: section];
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
    
    [self.openBisModel selectObjectAtIndexPath: indexPath];

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

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:@"showDetail"]) {
        [[segue destinationViewController] setOpenBisModel: self.openBisModel];
    }
}

- (void)configureCell:(UITableViewCell *)cell atIndexPath:(NSIndexPath *)indexPath
{
    CISDOBIpadEntity *object = [self.openBisModel objectAtIndexPath: indexPath];
    cell.textLabel.text = object.summaryHeader;
    cell.detailTextLabel.text = object.summary;
    if ([self.openBisModel entityHasChildren: object]) {
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

/*
// Implementing the above methods to update the table view in response to individual changes may have performance implications if a large number of changes are made simultaneously. If this proves to be an issue, you can instead just implement controllerDidChangeContent: which notifies the delegate that all section and object changes have been processed. 
 
 - (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    // In the simplest, most efficient, case, reload the table view.
    [self.tableView reloadData];
}
 */

#pragma mark - UISearchBarDelegate
- (void)searchBarTextDidBeginEditing:(UISearchBar *)searchBar
{
    // TODO Put the controller in search mode
    NSLog(@"searchBarTextDidBeginEditing:");
    searchBar.showsCancelButton = YES;
}

- (void)searchBarTextDidEndEditing:(UISearchBar *)searchBar
{
    // TODO Figure out what to do here
    NSLog(@"searchBarTextDidEndEditing:");
}

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)searchText
{
    // TODO Update the results
    NSLog(@"searchBar:textDidChange:");
}

- (void)searchBarCancelButtonClicked:(UISearchBar *) searchBar
{
    // TODO Take the controller out of search mode
    searchBar.showsCancelButton = NO;
    [self.searchBar resignFirstResponder];
    [self.tableView becomeFirstResponder];
}

#pragma mark - Server Communication
- (void)didConnectServiceManager:(CISDOBIpadServiceManager *)serviceManager
{
    [self.openBisModel syncRootEntities: ^(id result) {
        [self.tableView reloadData];
    }];
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

@end
