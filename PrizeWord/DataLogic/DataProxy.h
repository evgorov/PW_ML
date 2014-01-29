//
//  DataProxy.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import <Foundation/Foundation.h>
#import "QuestionData.h"

#define DATAPROXY_FORWARDING_GET(basetype,type,param) -(type)param \
{\
[self prepareManagedObject];\
__block type value;\
[DataContext performSyncInDataQueue:^{\
value = [(basetype)self.managedObject param];\
}];\
return value;\
}\

#define DATAPROXY_FORWARDING_SET(basetype,type,setter) -(void)setter:(type)value \
{\
[self prepareManagedObject];\
[DataContext performSyncInDataQueue:^{\
    [(basetype)self.managedObject setter:value];\
}];\
}\

@interface DataProxy : NSObject
{
    NSManagedObjectID * managedObjectID;
}

@property (retain) NSManagedObject * managedObject;

- (id)initWithObjectID:(NSManagedObjectID *)objectID;
- (void)commitChanges;
- (void)updateObject;

- (void)prepareManagedObject;

@end
