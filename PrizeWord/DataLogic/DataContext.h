//
//  DataContext.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import <CoreData/CoreData.h>

@interface DataContext : NSManagedObjectContext

+ (NSManagedObjectContext *)currentContext;
+ (void)performAsyncInDataQueue:(void(^)())block;
+ (void)performSyncInDataQueue:(void(^)())block;

@end
