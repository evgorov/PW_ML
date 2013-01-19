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

@implementation PuzzleSetData

@dynamic set_id;
@dynamic name;
@dynamic type;
@dynamic bought;
@dynamic puzzles;

+(PuzzleSetData *)puzzleSetWithDictionary:(NSDictionary *)dict
{
    return [[PuzzleSetData alloc] initWithDictionary:dict];
}

-(id)initWithDictionary:(NSDictionary *)dict
{
    self = [super init];
    if (self) {
        self.set_id = [dict objectForKey:@"id"];
        self.name = [dict objectForKey:@"name"];
        NSString * type = [dict objectForKey:@"type"];
        if (type == nil || [type compare:@"free"] == NSOrderedSame) {
            self.type = [NSNumber numberWithInt:LETTER_FREE];
        }
        else if ([type compare:@"brilliant"] == NSOrderedSame) {
            self.type = [NSNumber numberWithInt:LETTER_BRILLIANT];
        }
        else if ([type compare:@"silver"] == NSOrderedSame) {
            self.type = [NSNumber numberWithInt:LETTER_SILVER];
        }
        else if ([type compare:@"gold"] == NSOrderedSame) {
            self.type = [NSNumber numberWithInt:LETTER_GOLD];
        }
        else {
            NSLog(@"unknown set's type: %@", type);
        }
        self.bought = [dict objectForKey:@"bought"];
        
        NSArray * puzzlesData = [dict objectForKey:@"puzzles"];
        for (NSDictionary * puzzleData in puzzlesData) {
            PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:dict];
            [self addPuzzlesObject:puzzle];
        }
    }
    return self;
}

-(int)solved
{
    int value = 0;
    for (PuzzleData * puzzle in self.puzzles) {
        if ([puzzle.solved boolValue]) {
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
