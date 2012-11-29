//
//  GameLogic.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/22/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameLogic.h"
#import "AppDelegate.h"
#import "GameViewController.h"
#import "EventManager.h"
#import "GameField.h"
#import "PuzzleData.h"
#import "HintData.h"

@interface GameLogic ()

-(void)initGameField;

@end

@implementation GameLogic

-(id)init
{
    self = [super init];
    if (self)
    {
        currentGameField = nil;
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_START];
    }
    return self;
}

+(GameLogic *)sharedLogic
{
    static GameLogic * _sharedLogic = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedLogic = [[GameLogic alloc] init];
    });
    return _sharedLogic;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type)
    {
        case EVENT_GAME_REQUEST_START:
            [self initGameField];
            [[AppDelegate currentDelegate].navController pushViewController:[[GameViewController alloc] initWithGameField:currentGameField] animated:YES];

            break;
            
        default:
            break;
    }
}

-(void)initGameField
{
    NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleEntity = [NSEntityDescription entityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleEntity];
    [request setFetchLimit:1];
    
    NSError *error = nil;
    NSArray *puzzles = [managedObjectContext executeFetchRequest:request error:&error];
    
    PuzzleData * puzzle;
    if (puzzles == nil || puzzles.count == 0)
    {
        puzzle = (PuzzleData *)[NSEntityDescription insertNewObjectForEntityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
        [puzzle setWidth:[NSNumber numberWithUnsignedInt:10]];
        [puzzle setHeight:[NSNumber numberWithUnsignedInt:10]];
        [puzzle setIssuedAt:[NSDate date]];
        [puzzle setBase_score:[NSNumber numberWithUnsignedInt:1000]];
        [puzzle setName:@"Сканворд 1"];
        [puzzle setPuzzle_id:@"puzzle_1"];
        [puzzle setSet_id:@"puzzle_set_1"];
        [puzzle setTime_given:[NSNumber numberWithUnsignedInt:10000]];
        
        HintData * hint;

        hint = (HintData *)[NSEntityDescription insertNewObjectForEntityForName:@"Hint" inManagedObjectContext:managedObjectContext];
        [hint setAnswer:@"обор"];
        [hint setAnswer_positionAsString:@"west:bottom"];
        [hint setHint_text:@"Дорого-\nвизна,\nно иначе"];
        [hint setColumn:[NSNumber numberWithUnsignedInt:1]];
        [hint setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addHintsObject:hint];

        hint = (HintData *)[NSEntityDescription insertNewObjectForEntityForName:@"Hint" inManagedObjectContext:managedObjectContext];
        [hint setAnswer:@"бакалавр"];
        [hint setAnswer_positionAsString:@"north-west:right"];
        [hint setHint_text:@"Недотя-\nнувший\nдо ма-\nгистра"];
        [hint setColumn:[NSNumber numberWithUnsignedInt:1]];
        [hint setRow:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addHintsObject:hint];
        
        [managedObjectContext save:&error];
        if (error != nil)
        {
            NSLog(@"%@", error.localizedDescription);
        }
    }
    else
    {
        puzzle = [puzzles objectAtIndex:0];
    }
    
    currentGameField = [[GameField alloc] initWithData:puzzle];
}

@end
