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
@dynamic name;
@dynamic type;
@dynamic bought;
@dynamic puzzles;

+(PuzzleSetData *)puzzleSetWithDictionary:(NSDictionary *)dict
{
    NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleSetEntity = [NSEntityDescription entityForName:@"PuzzleSet" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleSetEntity];
    [request setFetchLimit:1];
    [request setPredicate:[NSPredicate predicateWithFormat:@"set_id = %@", [dict objectForKey:@"id"]]];
    
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
    NSString * type = [dict objectForKey:@"type"];
    if (type == nil || [type compare:@"free" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
        [puzzleSet setType:[NSNumber numberWithInt:LETTER_FREE]];
    }
    else if ([type compare:@"brilliant" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
        [puzzleSet setType:[NSNumber numberWithInt:LETTER_BRILLIANT]];
    }
    else if ([type compare:@"silver" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
        [puzzleSet setType:[NSNumber numberWithInt:LETTER_SILVER]];
    }
    else if ([type compare:@"gold" options:NSCaseInsensitiveSearch] == NSOrderedSame) {
        [puzzleSet setType:[NSNumber numberWithInt:LETTER_GOLD]];
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
        PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:puzzleData];
        [puzzleSet addPuzzlesObject:puzzle];
    }
    
    [managedObjectContext save:&error];
    if (error != nil) {
        NSLog(@"DB error: %@", error.description);
    }
    
    return puzzleSet;
}

+(int)solved:(PuzzleSetData *)puzzleSet
{
    int value = 0;
    for (PuzzleData * puzzle in puzzleSet.puzzles) {
        if (puzzle.solved == puzzle.questions.count) {
            ++value;
        }
    }
    return value;
}

+(int)total:(PuzzleSetData *)puzzleSet
{
    return puzzleSet.puzzles.count;
}

+(float)percent:(PuzzleSetData *)puzzleSet
{
    if (puzzleSet.puzzles.count == 0) {
        return 1;
    }
    return (float)[PuzzleSetData solved:puzzleSet] / puzzleSet.puzzles.count;
}

+(int)score:(PuzzleSetData *)puzzleSet
{
    int value = 0;
    for (PuzzleData * puzzle in puzzleSet.puzzles) {
        value += [puzzle.score intValue];
    }
    return value;
}

+(int)minScore:(PuzzleSetData *)puzzleSet
{
    int value = 0;
    for (PuzzleData * puzzle in puzzleSet.puzzles) {
        value += [puzzle.base_score intValue];
    }
    return value;
}

@end
