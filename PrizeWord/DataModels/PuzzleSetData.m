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

@implementation PuzzleSetData

@dynamic set_id;
@dynamic user_id;
@dynamic name;
@dynamic type;
@dynamic bought;
@dynamic puzzles;

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
    
    NSArray * puzzlesData = [dict objectForKey:@"puzzles"];
    for (NSDictionary * puzzleData in puzzlesData) {
        PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:puzzleData andUserId:userId];
        [puzzleSet addPuzzlesObject:puzzle];
    }
    
    [managedObjectContext save:&error];
    if (error != nil) {
        NSLog(@"DB error: %@", error.description);
    }
    
    return puzzleSet;
}

-(int)solved
{
    int value = 0;
    for (PuzzleData * puzzle in self.puzzles) {
        if (puzzle.solved == puzzle.questions.count) {
            ++value;
        }
    }
    return value;
}

-(int)total
{
    return self.puzzles.count;
}

-(float)percent
{
    if (self.puzzles.count == 0) {
        return 1;
    }
    return (float)[self solved] / self.puzzles.count;
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
    int value = 0;
    for (PuzzleData * puzzle in self.puzzles) {
        value += [puzzle.base_score intValue];
    }
    return value;
}

@end