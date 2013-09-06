//
//  CISDOBIpadServerInfo.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 9/4/13.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CISDOBIpadEntity;

@interface CISDOBIpadServerInfo : NSManagedObject

@property (nonatomic, retain) NSString * serverUrlString;
@property (nonatomic, retain) NSDate * lastSyncDate;
@property (nonatomic, retain) NSSet *entities;
@end

@interface CISDOBIpadServerInfo (CoreDataGeneratedAccessors)

- (void)addEntitiesObject:(CISDOBIpadEntity *)value;
- (void)removeEntitiesObject:(CISDOBIpadEntity *)value;
- (void)addEntities:(NSSet *)values;
- (void)removeEntities:(NSSet *)values;

@end
