//
//  CIDOBIpadEntity.h
//  BisMac
//
//  Created by Ramakrishnan  Chandrasekhar on 10/1/12.
//
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface CIDOBIpadEntity : NSManagedObject

@property (nonatomic, retain) NSString * summaryHeader;
@property (nonatomic, retain) NSString * summary;
@property (nonatomic, retain) NSString * identifier;
@property (nonatomic, retain) NSString * permId;
@property (nonatomic, retain) NSString * entityKind;
@property (nonatomic, retain) NSString * entityType;
@property (nonatomic, retain) NSString * propertiesJson;

@end
