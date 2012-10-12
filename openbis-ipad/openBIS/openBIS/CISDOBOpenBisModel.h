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
//  CISDOBModel.h
//  openBIS
//
//  Created by Ramakrishnan  Chandrasekhar on 10/12/12.
//

#import <Foundation/Foundation.h>
#import "CISDOBShared.h"

@class CISDOBIpadEntity;

/**
 * \brief A model for the interaction with openBIS.
 */
@interface CISDOBOpenBisModel : NSObject <NSFetchedResultsControllerDelegate> {
    CISDOBIpadEntity *_selectedObject;
}

@property (strong, nonatomic) NSFetchedResultsController *fetchedResultsController;
@property (strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (weak, nonatomic) id <NSFetchedResultsControllerDelegate> delegate;
@property (readonly) CISDOBIpadEntity *selectedObject;

// Model
- (NSInteger)numberOfSections; //!< Get the number of categories for the current selection
- (NSInteger)numberOfEntitiesInSection:(NSInteger)section;
- (NSString *)titleForHeaderInSection:(NSInteger)section;
- (CISDOBIpadEntity *)objectAtIndexPath:(NSIndexPath *)indexPath;

// Selection
//! Select the object and return it
- (CISDOBIpadEntity *)selectObjectAtIndexPath:(NSIndexPath *)indexPath;
- (BOOL)isSelectionGroup; //!< Return YES if the selected object is a group (has children)

// Actions
- (BOOL)insertNewObjectOrError:(NSError **)error; //!< Return YES if operation succeeded
- (BOOL)deleteObjectAtIndexPath:(NSIndexPath *)indexPath error:(NSError **)error; //!< Return YES if operation succeeded

// Server Communication
//! Get the selected object from the server and invoke the succes block when the data is here
- (void)syncSelectedObjectOnSuccess:(SuccessBlock)success;


@end
