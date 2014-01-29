//
//  DataProxy.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import "DataProxy.h"
#import "DataContext.h"

@interface DataProxy()

@end

@implementation DataProxy

@synthesize managedObject = managedObject;

- (id)init
{
    self = [super init];
    if (self) {
        managedObject = nil;
        managedObjectID = nil;
    }
    return self;
}

- (id)initWithObjectID:(NSManagedObjectID *)objectID
{
    self = [self init];
    if (self) {
        managedObjectID = objectID;
    }
    return self;
}

- (void)commitChanges
{
    [DataContext performAsyncInDataQueue:^{
        NSError * error = nil;
        [[DataContext currentContext] save:&error];
        if (error != nil) {
            NSLog(@"failed to save MOC: %@", error.localizedDescription);
        }
    }];
}

- (void)updateObject
{
    [DataContext performSyncInDataQueue:^{
        managedObject = [[DataContext currentContext] objectWithID:managedObjectID];
    }];
}

- (void)prepareManagedObject
{
    if (managedObject == nil && managedObjectID != nil) {
        [DataContext performSyncInDataQueue:^{
            managedObject = [[DataContext currentContext] objectWithID:managedObjectID];
        }];
    }
}

@end
