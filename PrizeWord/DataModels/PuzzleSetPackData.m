//
//  PuzzleSetPackData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import "PuzzleSetPackData.h"
#import "PuzzleSetData.h"
#import "AppDelegate.h"
#import "DataContext.h"

@implementation PuzzleSetPackData

@dynamic month;
@dynamic year;
@dynamic user_id;
@dynamic etag;
@dynamic puzzleSets;

+ (PuzzleSetPackData *)puzzleSetPackWithYear:(int)year andMonth:(int)month andUserId:(NSString *)userId
{
    PuzzleSetPackData * pack = nil;
    NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
    NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"PuzzleSetPackFetchRequest" substitutionVariables:@{@"YEAR": [NSNumber numberWithInt:year], @"MONTH": [NSNumber numberWithInt:month], @"USER_ID": userId}];
    [managedObjectContext lock];
    NSArray * results = [managedObjectContext executeFetchRequest:fetchRequest error:nil];
    if (results != nil && results.count > 0)
    {
        pack = results.lastObject;
    }
    else
    {
        pack = [NSEntityDescription insertNewObjectForEntityForName:@"PuzzleSetPack" inManagedObjectContext:managedObjectContext];
    }
    [managedObjectContext unlock];
    pack.user_id = userId;
    pack.year = [NSNumber numberWithInt:year];
    pack.month = [NSNumber numberWithInt:month];
    return pack;
}

@end
