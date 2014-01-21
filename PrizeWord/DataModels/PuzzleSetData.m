//
//  PuzzleSetData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/3/12.
//
//

#import "PuzzleSetData.h"
#import "PuzzleData.h"
#import "TileData.h"
#import "AppDelegate.h"
#import "GlobalData.h"
#import "DataContext.h"

@implementation PuzzleSetData

@dynamic set_id;
@dynamic user_id;
@dynamic name;
@dynamic puzzle_ids;
@dynamic type;
@dynamic bought;
@dynamic puzzles_count;
@dynamic puzzles;
@dynamic puzzleSetPack;

+(PuzzleSetData *)puzzleSetWithDictionary:(NSDictionary *)dict andUserId:(NSString *)userId
{
    __block PuzzleSetData * puzzleSet = nil;
    [DataContext performSyncInDataQueue:^{
        NSManagedObjectContext * moc = [DataContext currentContext];
        NSFetchRequest *request = [moc.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"PuzzleSetByIdFetchRequest" substitutionVariables:@{@"SET_ID": [dict objectForKey:@"id"], @"USER_ID": userId}];
        
        NSError * error = nil;
        NSArray *puzzleSets = [moc executeFetchRequest:request error:&error];
        if (error != nil)
        {
            NSLog(@"error: %@", error.localizedDescription);
        }
        
        if (puzzleSets == nil || puzzleSets.count == 0)
        {
            puzzleSet = (PuzzleSetData *)[NSEntityDescription insertNewObjectForEntityForName:@"PuzzleSet" inManagedObjectContext:moc];
        }
        else
        {
            puzzleSet = (PuzzleSetData *)[puzzleSets objectAtIndex:0];
        }
        
            [puzzleSet setSet_id:[dict objectForKey:@"id"]];
            [puzzleSet setName:[dict objectForKey:@"name"]];
            [puzzleSet setUser_id:userId];
            NSString * type = [dict objectForKey:@"type"];
            if (type == nil || [type compare:@"free" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
                [puzzleSet setType:[NSNumber numberWithInt:PUZZLESET_FREE]];
            }
            else if ([type compare:@"brilliant" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
                [puzzleSet setType:[NSNumber numberWithInt:PUZZLESET_BRILLIANT]];
            }
            else if ([type compare:@"silver2" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
                [puzzleSet setType:[NSNumber numberWithInt:PUZZLESET_SILVER2]];
            }
            else if ([type compare:@"silver" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
                [puzzleSet setType:[NSNumber numberWithInt:PUZZLESET_SILVER]];
            }
            else if ([type compare:@"gold" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
                [puzzleSet setType:[NSNumber numberWithInt:PUZZLESET_GOLD]];
            }
            else {
                NSLog(@"unknown set's type: %@", type);
            }
            if ([dict objectForKey:@"bought"] != nil && [(NSNumber *)[dict objectForKey:@"bought"] boolValue])
            {
                [puzzleSet setBought:[NSNumber numberWithBool:YES]];
            }
            /*
             NSArray * idParts = [(NSString *)[dict objectForKey:@"id"] componentsSeparatedByString:@"_"];
             [puzzleSet setYear:[NSNumber numberWithInt:[(NSString *)idParts[0] intValue]]];
             [puzzleSet setMonth:[NSNumber numberWithInt:[(NSString *)idParts[1] intValue]]];
             */
            NSArray * puzzlesData = [dict objectForKey:@"puzzles"];
            NSMutableString * puzzleIds = [NSMutableString new];
            int puzzlesCount = 0;
            for (id puzzleData in puzzlesData)
            {
                ++puzzlesCount;
                if ([puzzleData isKindOfClass:[NSDictionary class]])
                {
                    PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:puzzleData andUserId:userId];
                    if (puzzle == nil)
                    {
                        NSLog(@"WARNING: puzzle is nil!");
                        continue;
                    }
                    [puzzleSet addPuzzlesObject:puzzle];
                    [puzzleIds appendFormat:@"%@%@", puzzleIds.length == 0 ? @"" : @",", puzzle.puzzle_id];
                }
                else if ([puzzleData isKindOfClass:[NSString class]])
                {
                    [puzzleIds appendFormat:@"%@%@", puzzleIds.length == 0 ? @"" : @",", puzzleData];
                }
            }
            [puzzleSet setPuzzles_count:[NSNumber numberWithInt:puzzlesCount]];
            [puzzleSet setPuzzle_ids:puzzleIds];
    }];
    
    return puzzleSet;
}

+(PuzzleSetData *)puzzleSetWithId:(NSString *)setId andUserId:(NSString *)userId
{
    __block PuzzleSetData * puzzleSetData = nil;
    [DataContext performSyncInDataQueue:^{
        NSManagedObjectContext * moc = [DataContext currentContext];
        NSFetchRequest *request = [moc.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"PuzzleSetByIdFetchRequest" substitutionVariables:@{@"USER_ID": userId, @"SET_ID": setId}];
        
        NSArray *puzzleSets = [moc executeFetchRequest:request error:nil];
        if (puzzleSets != nil && puzzleSets.count != 0)
        {
            puzzleSetData = [puzzleSets lastObject];
        }
    }];
    return puzzleSetData;
}
 
-(int)solved
{
    int value = 0;
    for (PuzzleData * puzzle in self.puzzles)
    {
        if (puzzle.solved == puzzle.questions.count)
        {
            ++value;
        }
    }
    return value;
}

-(int)total
{
    return self.puzzles_count.intValue;
}

-(float)percent
{
    if (self.puzzles_count.intValue == 0)
    {
        return 1;
    }
    float value = 0;
    for (PuzzleData * puzzle in self.puzzles) {
        value += puzzle.progress;
    }
    return value / self.puzzles_count.intValue;
}

-(int)score
{
    int value = 0;
    for (PuzzleData * puzzle in self.puzzles) {
        value += [puzzle.score intValue];
    }
    return value;
}

-(int)minScore
{
    return [[GlobalData globalData] baseScoreForType:self.type.intValue] * self.puzzles_count.intValue;
}

-(NSArray *)orderedPuzzles
{
    return [[self.puzzles allObjects] sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
        PuzzleData * puzzle1 = obj1;
        PuzzleData * puzzle2 = obj2;
        return [puzzle1.puzzle_id compare:puzzle2.puzzle_id];
    }];
}

@end
