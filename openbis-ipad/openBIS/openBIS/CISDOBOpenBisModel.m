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
//  CISDOBModel.m
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/12/12.
//

#import "CISDOBOpenBisModel.h"

@implementation CISDOBOpenBisModel

#pragma mark - Fetched results controller

- (NSFetchedResultsController *)fetchedResultsController
{
    if (_fetchedResultsController != nil) {
        return _fetchedResultsController;
    }
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: self.managedObjectContext];
    [fetchRequest setEntity:entity];
    [fetchRequest setFetchBatchSize:20];
    
    NSSortDescriptor *groupSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"group" ascending: NO];
    NSSortDescriptor *summaryHeaderSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"summaryHeader" ascending: YES];
    NSArray *sortDescriptors = @[groupSortDescriptor, summaryHeaderSortDescriptor];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest: fetchRequest managedObjectContext: self.managedObjectContext sectionNameKeyPath: @"group" cacheName: @"Master"];
    aFetchedResultsController.delegate = self;
    self.fetchedResultsController = aFetchedResultsController;
    
	NSError *error = nil;
	if (![self.fetchedResultsController performFetch:&error]) {
        // TODO Implement error handling
	    NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	    abort();
	}
    
    return _fetchedResultsController;
}    

- (void)controllerWillChangeContent:(NSFetchedResultsController *)controller
{
    [self.delegate controllerWillChangeContent: controller];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeSection:(id <NSFetchedResultsSectionInfo>)sectionInfo
           atIndex:(NSUInteger)sectionIndex forChangeType:(NSFetchedResultsChangeType)type
{
    [self.delegate controller: controller didChangeSection: sectionInfo atIndex: sectionIndex forChangeType: type];
}

- (void)controller:(NSFetchedResultsController *)controller didChangeObject:(id)anObject
       atIndexPath:(NSIndexPath *)indexPath forChangeType:(NSFetchedResultsChangeType)type
      newIndexPath:(NSIndexPath *)newIndexPath
{
    [self.delegate controller: controller didChangeObject: anObject
       atIndexPath: indexPath forChangeType: type
      newIndexPath: newIndexPath];
}

- (void)controllerDidChangeContent:(NSFetchedResultsController *)controller
{
    [self.delegate controllerDidChangeContent: controller];
}

@end
