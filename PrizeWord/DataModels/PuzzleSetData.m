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

@implementation PuzzleSetData

@dynamic set_id;
@dynamic user_id;
@dynamic name;
@dynamic puzzle_ids;
@dynamic type;
@dynamic bought;
@dynamic puzzles_count;
@dynamic puzzles;
@dynamic month;
@dynamic year;

+(PuzzleSetData *)puzzleSetWithDictionary:(NSDictionary *)dict andUserId:(NSString *)userId
{
    NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleSetEntity = [NSEntityDescription entityForName:@"PuzzleSet" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleSetEntity];
    [request setFetchLimit:1];
    [request setPredicate:[NSPredicate predicateWithFormat:@"(set_id = %@) AND (user_id = %@)", [dict objectForKey:@"id"], userId]];
    
    NSError *error = nil;
    NSArray *puzzleSets = [managedObjectContext executeFetchRequest:request error:&error];
    
    PuzzleSetData * puzzleSet;
    if (puzzleSets == nil || puzzleSets.count == 0)
    {
        puzzleSet = (PuzzleSetData *)[NSEntityDescription insertNewObjectForEntityForName:@"PuzzleSet" inManagedObjectContext:managedObjectContext];
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
    NSArray * idParts = [(NSString *)[dict objectForKey:@"id"] componentsSeparatedByString:@"_"];
    [puzzleSet setYear:[NSNumber numberWithInt:[(NSString *)idParts[0] intValue]]];
    [puzzleSet setMonth:[NSNumber numberWithInt:[(NSString *)idParts[1] intValue]]];
    
    NSArray * puzzlesData = [dict objectForKey:@"puzzles"];
    NSMutableString * puzzleIds = [NSMutableString new];
    int puzzlesCount = 0;
    for (id puzzleData in puzzlesData)
    {
        ++puzzlesCount;
        if ([puzzleData isKindOfClass:[NSDictionary class]])
        {
            PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:puzzleData andUserId:userId];
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
    
    [managedObjectContext save:&error];
    if (error != nil)
    {
        NSLog(@"DB error: %@", error.description);
    }
    
    return puzzleSet;
}

+(NSArray *)puzzleSetsForMonth:(int)month year:(int)year
{
    NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleSetEntity = [NSEntityDescription entityForName:@"PuzzleSet" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleSetEntity];
    [request setPredicate:[NSPredicate predicateWithFormat:@"(month = %d) AND (year = %d)", month, year]];
    
    NSArray *puzzleSets = [managedObjectContext executeFetchRequest:request error:nil];
    return puzzleSets;
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
    return (float)[self solved] / self.puzzles_count.intValue;
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
        return [puzzle1.puzzle_id compare:puzzle2.puzzle_id options:NSLiteralSearch|NSNumericSearch|NSCaseInsensitiveSearch];
    }];
}

@end
