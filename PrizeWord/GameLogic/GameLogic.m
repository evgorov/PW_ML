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
#import "QuestionData.h"
#import "PuzzleSetData.h"
#import "PrizeWordNavigationController.h"

@interface GameLogic ()

-(void)initGameFieldWithType:(LetterType)type;

@end

@implementation GameLogic

-(id)init
{
    self = [super init];
    if (self)
    {
        currentGameField = nil;
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_START];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_GAME_REQUEST_PAUSE];
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

-(GameField *)gameField
{
    return currentGameField;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type)
    {
        case EVENT_GAME_REQUEST_START:
        {
            LetterType type = (LetterType)([(NSNumber *)event.data intValue]);
            [self initGameFieldWithType:type];
            [[AppDelegate currentDelegate].navController pushViewController:[[GameViewController alloc] initWithGameField:currentGameField] animated:YES];
        }
            break;

        case EVENT_GAME_REQUEST_PAUSE:
        {
            currentGameField = nil;
        }
            break;
            
        default:
            break;
    }
}

-(void)initGameFieldWithType:(LetterType)type
{
    NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleSetEntity = [NSEntityDescription entityForName:@"PuzzleSet" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleSetEntity];
    [request setFetchLimit:1];
    [request setPredicate:[NSPredicate predicateWithFormat:@"type = %d", type]];
    
    NSError *error = nil;
    NSArray *puzzleSets = [managedObjectContext executeFetchRequest:request error:&error];
    
    PuzzleData * puzzle;
    if (puzzleSets == nil || puzzleSets.count == 0)
    {
        PuzzleSetData * puzzleSet = (PuzzleSetData *)[NSEntityDescription insertNewObjectForEntityForName:@"PuzzleSet" inManagedObjectContext:managedObjectContext];
        [puzzleSet setSet_id:[NSString stringWithFormat:@"set_%d", type]];
        [puzzleSet setType:[NSNumber numberWithInt:type]];
        [puzzleSet setBought:[NSNumber numberWithBool:YES]];
        [puzzleSet setName:@"Set"];
        
        puzzle = (PuzzleData *)[NSEntityDescription insertNewObjectForEntityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
        [puzzle setWidth:[NSNumber numberWithUnsignedInt:10]];
        [puzzle setHeight:[NSNumber numberWithUnsignedInt:10]];
        [puzzle setIssuedAt:[NSDate date]];
        [puzzle setBase_score:[NSNumber numberWithUnsignedInt:1000]];
        [puzzle setName:@"Сканворд 1"];
        [puzzle setPuzzle_id:@"puzzle_1"];
        [puzzle setTime_given:[NSNumber numberWithUnsignedInt:10000]];
        [puzzleSet addPuzzlesObject:puzzle];
        
        QuestionData * question;

        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"обор"];
        [question setAnswer_positionAsString:@"west:bottom"];
        [question setQuestion_text:@"Дорого-\nвизна,\nно иначе"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:0]];
        [puzzle addQuestionsObject:question];

        question = (QuestionData *)[NSEntityDescription insertNewObjectForEntityForName:@"Question" inManagedObjectContext:managedObjectContext];
        [question setAnswer:@"бакалавр"];
        [question setAnswer_positionAsString:@"north-west:right"];
        [question setQuestion_text:@"Недотя-\nнувший\nдо ма-\nгистра"];
        [question setColumn:[NSNumber numberWithUnsignedInt:1]];
        [question setRow:[NSNumber numberWithUnsignedInt:2]];
        [puzzle addQuestionsObject:question];
        
        [managedObjectContext save:&error];
        if (error != nil)
        {
            NSLog(@"%@", error.localizedDescription);
        }
    }
    else
    {
        PuzzleSetData * puzzleSet = [puzzleSets objectAtIndex:0];
        puzzle = [puzzleSet.puzzles anyObject];
    }
    
    currentGameField = [[GameField alloc] initWithData:puzzle];
}

@end
