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
#import "CISDOBIpadServiceManager.h"
#import "CISDOBAsyncCall.h"

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
        self.serviceManager = parentModel.serviceManager;
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
    
    return ((CISDOBIpadEntity *)[objects objectAtIndex: 0]).category;
}

- (CISDOBIpadEntity *)objectAtIndexPath:(NSIndexPath *)indexPath
{
    return [self.fetchedResultsController objectAtIndexPath:indexPath];
}

#pragma mark - Utilities
- (NSString *)sessionToken
{
    return self.serviceManager.sessionToken;
}

- (NSURL *)urlFromUrlString:(NSString *)urlString
{
    // if this is a datastore_server url, add the session token
    NSRange dataStoreServerRange = [urlString rangeOfString: @"datastore_server"];
    if (dataStoreServerRange.length == 0) return [NSURL URLWithString: urlString];
    
    NSMutableString *urlStringWithSession = [NSMutableString stringWithString: urlString];
    [urlStringWithSession appendFormat: @"?sessionID=%@", [self sessionToken]];

    return [NSURL URLWithString: urlStringWithSession];
}

#pragma mark - Selection
- (CISDOBIpadEntity *)selectObjectAtIndexPath:(NSIndexPath *)indexPath
{
    self.selectedObject = [self objectAtIndexPath: indexPath];
    return self.selectedObject;
}

- (BOOL)selectionHasChildren
{
    if (!self.selectedObject) return NO;
    return [self entityHasChildren: self.selectedObject];
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
- (void)syncRootEntities:(SuccessBlock)success;
{
    // The manager has connected, initialize the root
    CISDOBAsyncCall *call = [self.serviceManager retrieveRootLevelEntities];
    call.success = success;
    [call start];
}

- (void)syncSelectedObjectForDetailOnSuccess:(SuccessBlock)success
{
    // Call the server to get the children.
    CISDOBAsyncCall *call = [self.serviceManager detailsForEntity: _selectedObject];
    // Assign this to a local var to get the compiler to copy it
    SuccessBlock localSuccess = success;
    call.success = ^(id result) {
        // Update the UI
        localSuccess(_selectedObject);
    };
    [call start];

}

- (void)syncSelectedObjectForNavigationOnSuccess:(SuccessBlock)success
{
    // Call the server to get the children.
    CISDOBAsyncCall *call = [self.serviceManager drillOnEntity: _selectedObject];
    // Assign this to a local var to get the compiler to copy it
    SuccessBlock localSuccess = success;
    call.success = ^(id result) {
        localSuccess(_selectedObject);
    };
    [call start];
}

#pragma mark - Fetched results controller

- (void)initializeRootFetchedResultsController
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName: @"CISDOBIpadEntity" inManagedObjectContext: self.managedObjectContext];
    [fetchRequest setEntity:entity];
    [fetchRequest setFetchBatchSize:20];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat: @"rootLevel == YES"];
    [fetchRequest setPredicate: predicate];
    
    NSSortDescriptor *categorySortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"category" ascending: NO];
    NSSortDescriptor *summaryHeaderSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"summaryHeader" ascending: YES];
    NSArray *sortDescriptors = @[categorySortDescriptor, summaryHeaderSortDescriptor];
    [fetchRequest setSortDescriptors:sortDescriptors];
    

    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest: fetchRequest managedObjectContext: self.managedObjectContext sectionNameKeyPath: @"category" cacheName: @"Root"];
    aFetchedResultsController.delegate = self;
    self.fetchedResultsController = aFetchedResultsController;

    NSError *error;
    if (![self.fetchedResultsController performFetch: &error]) {
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
    
    NSSortDescriptor *categorySortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"category" ascending: NO];
    NSSortDescriptor *summaryHeaderSortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"summaryHeader" ascending: YES];
    NSArray *sortDescriptors = @[categorySortDescriptor, summaryHeaderSortDescriptor];
    [fetchRequest setSortDescriptors:sortDescriptors];
    
    NSFetchedResultsController *aFetchedResultsController = [[NSFetchedResultsController alloc] initWithFetchRequest: fetchRequest managedObjectContext: self.managedObjectContext sectionNameKeyPath: @"category" cacheName: _parentModel.selectedObject.permId];
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
