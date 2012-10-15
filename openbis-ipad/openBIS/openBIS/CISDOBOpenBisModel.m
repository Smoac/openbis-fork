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
#import "CISDOBIpadEntity.h"

@implementation CISDOBOpenBisModel

#pragma mark - Initialize
- (id)init
{
    if (!(self = [self initWithParentModel: nil])) return nil;

    return self;
}

- (id)initWithParentModel:(CISDOBOpenBisModel *)parentModel
{
    if (!(self = [super init])) return nil;
    
    self.parentModel = parentModel;
    _selectedObject = nil;
    
    if (self.parentModel) {
        self.managedObjectContext = parentModel.managedObjectContext;
    }
    
    return self;
}

#pragma mark - Model
- (NSInteger)numberOfSections
{
    return [[self.fetchedResultsController sections] count];
}

- (NSInteger)numberOfEntitiesInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [self.fetchedResultsController sections][section];
    return [sectionInfo numberOfObjects];
}

- (NSString *)titleForHeaderInSection:(NSInteger)section
{
    id <NSFetchedResultsSectionInfo> sectionInfo = [[self.fetchedResultsController sections] objectAtIndex: section];
    NSArray *objects = [sectionInfo objects];
    if ([objects count] < 1) return @"";
    
    return ((CISDOBIpadEntity *)[objects objectAtIndex: 0]).group;
}

- (CISDOBIpadEntity *)objectAtIndexPath:(NSIndexPath *)indexPath
{
    return [self.fetchedResultsController objectAtIndexPath:indexPath];
}

#pragma mark - Selection
- (CISDOBIpadEntity *)selectObjectAtIndexPath:(NSIndexPath *)indexPath
{
    _selectedObject = [self objectAtIndexPath: indexPath];
    return _selectedObject;
}

- (BOOL)selectionHasChildren
{
    if (!_selectedObject) return NO;
    return [self entityHasChildren: _selectedObject];
}

- (BOOL)entityHasChildren:(CISDOBIpadEntity *)entity
{
    // In this case we are already looking at the children. No need to allow circular recursion.
    if (_parentModel && [entity isEqual: _parentModel.selectedObject]) return NO;
    
    return [entity.childrenPermIds count] > 0;
}

#pragma mark - Actions
- (BOOL)insertNewObjectOrError:(NSError **)error
{
    NSManagedObjectContext *context = [self.fetchedResultsController managedObjectContext];
    
    // TODO Implement insert
    NSLog(@"Do not support adding new objects");
    abort();
    
//    NSEntityDescription *entity = [[self.openBisModel.fetchedResultsController fetchRequest] entity];
//    NSManagedObject *newManagedObject = [NSEntityDescription insertNewObjectForEntityForName:[entity name] inManagedObjectContext:context];
    
    
    // Save the context.
    return [context save: error];
}

- (BOOL)deleteObjectAtIndexPath:(NSIndexPath *)indexPath error:(NSError **)error
{
    NSManagedObjectContext *context = [self.fetchedResultsController managedObjectContext];
    [context deleteObject:[self.fetchedResultsController objectAtIndexPath:indexPath]];
    
   return [context save: error];
}

#pragma mark - Server Communication
- (void)syncSelectedObjectOnSuccess:(SuccessBlock)success
{
    // Load the image if necessary
    if (_selectedObject.imageUrl && !_selectedObject.image) {
        NSBlockOperation *blockOp = [NSBlockOperation blockOperationWithBlock:  ^{
            NSURL *imageUrl = [NSURL URLWithString: _selectedObject.imageUrl];
            NSData *imageData = [NSData dataWithContentsOfURL: imageUrl];
            _selectedObject.image = [UIImage imageWithData: imageData];
            success(_selectedObject);
        }];
        [blockOp start];        
    } else {
        success(_selectedObject);
    }
}

#pragma mark - Fetched results controller

- (void)initializeRootFetchedResultsController
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: self.managedObjectContext];
    [fetchRequest setEntity:entity];
    [fetchRequest setFetchBatchSize:20];
    
    NSSortDescriptor *groupSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"group" ascending: NO];
    NSSortDescriptor *summaryHeaderSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"summaryHeader" ascending: YES];
    NSArray *sortDescriptors = @[groupSortDescriptor, summaryHeaderSortDescriptor];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest: fetchRequest managedObjectContext: self.managedObjectContext sectionNameKeyPath: @"group" cacheName: @"Root"];
    aFetchedResultsController.delegate = self;
    self.fetchedResultsController = aFetchedResultsController;
    
	NSError *error = nil;
	if (![self.fetchedResultsController performFetch:&error]) {
        // TODO Implement error handling
	    NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	    abort();
	}
}

- (void)initializeChildFetchedResultsController
{
    NSAssert(_parentModel.selectedObject != nil, @"Cannot initialize the model as a child of an existing model.");
    NSEntityDescription *entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: self.managedObjectContext];
    NSManagedObjectModel *model = [entity managedObjectModel];
    NSDictionary *fetchVariables =
        [NSDictionary dictionaryWithObjectsAndKeys:
            _parentModel.selectedObject, @"ENTITY",
            _parentModel.selectedObject.childrenPermIds, @"CHILDREN",
            nil];
    NSFetchRequest *fetchRequest = [model fetchRequestFromTemplateWithName: @"EntityAndChildren" substitutionVariables: fetchVariables];
    
    NSSortDescriptor *groupSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"group" ascending: NO];
    NSSortDescriptor *summaryHeaderSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"summaryHeader" ascending: YES];
    NSArray *sortDescriptors = @[groupSortDescriptor, summaryHeaderSortDescriptor];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest: fetchRequest managedObjectContext: self.managedObjectContext sectionNameKeyPath: @"group" cacheName: _parentModel.selectedObject.permId];
    aFetchedResultsController.delegate = self;
    self.fetchedResultsController = aFetchedResultsController;
    
	NSError *error = nil;
	if (![self.fetchedResultsController performFetch:&error]) {
        // TODO Implement error handling
	    NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
	    abort();
	}
}


- (NSFetchedResultsController *)fetchedResultsController
{
    if (_fetchedResultsController != nil) {
        return _fetchedResultsController;
    }
    
    if (nil == _parentModel || nil == _parentModel.selectedObject)
        [self initializeRootFetchedResultsController];
    else
        [self initializeChildFetchedResultsController];
    
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
