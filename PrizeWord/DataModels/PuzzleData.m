//
//  PuzzleData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/28/12.
//
//

#import "PuzzleData.h"
#import "QuestionData.h"
#import "AppDelegate.h"

@implementation PuzzleData

@dynamic puzzle_id;
@dynamic name;
@dynamic user_id;
@dynamic issuedAt;
@dynamic base_score;
@dynamic time_given;
@dynamic time_left;
@dynamic height;
@dynamic width;
@dynamic score;
@dynamic questions;
@dynamic puzzleSet;

+(PuzzleData *)puzzleWithDictionary:(NSDictionary *)dict andUserId:(NSString *)userId
{
    NSManagedObjectContext * managedObjectContext = [AppDelegate currentDelegate].managedObjectContext;
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleEntity = [NSEntityDescription entityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleEntity];
    [request setFetchLimit:1];
    [request setPredicate:[NSPredicate predicateWithFormat:@"(puzzle_id = %@) AND (user_id = %@)", [dict objectForKey:@"id"], userId]];
    
    NSError *error = nil;
    NSArray *puzzles = [managedObjectContext executeFetchRequest:request error:&error];
    
    PuzzleData * puzzle;
    if (puzzles == nil || puzzles.count == 0)
    {
        puzzle = (PuzzleData *)[NSEntityDescription insertNewObjectForEntityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
    }
    else
    {
        puzzle = [puzzles objectAtIndex:0];
    }
    
    [puzzle setPuzzle_id:[dict objectForKey:@"id"]];
    [puzzle setName:[dict objectForKey:@"name"]];
    [puzzle setUser_id:userId];
    NSString * dateString = [dict objectForKey:@"issuedAt"];
    if (dateString != nil)
    {
        NSDateFormatter * dateFormatter = [NSDateFormatter new];
        [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        [puzzle setIssuedAt:[dateFormatter dateFromString:dateString]];
    }
    id base_scoreData = [dict objectForKey:@"base_score"];
    if ([base_scoreData isKindOfClass:[NSString class]]) {
        NSString * base_scoreString = base_scoreData;
        [puzzle setBase_score:[NSNumber numberWithInt:[base_scoreString intValue]]];
    } else {
        [puzzle setBase_score:[dict objectForKey:@"base_score"]];
    }
    if (puzzle.base_score == nil)
    {
        [puzzle setBase_score:[NSNumber numberWithInt:0]];
    }
    id time_givenData = [dict objectForKey:@"time_given"];
    if ([time_givenData isKindOfClass:[NSString class]]) {
        NSString * time_givenString = time_givenData;
        [puzzle setTime_given:[NSNumber numberWithInt:[time_givenString intValue]]];
    } else {
        [puzzle setTime_given:[dict objectForKey:@"time_given"]];
    }
    if (puzzle.time_given == nil)
    {
        [puzzle setTime_given:[NSNumber numberWithInt:60000]];
    }
//    [puzzle setTime_left:[dict objectForKey:@"time_left"]];
//    [puzzle setSolved:[dict objectForKey:@"solved"]];
//    [puzzle setScore:[dict objectForKey:@"score"]];
    [puzzle setHeight:[dict objectForKey:@"height"]];
    [puzzle setWidth:[dict objectForKey:@"width"]];
    if (puzzle.score == nil)
    {
        [puzzle setScore:[NSNumber numberWithInt:0]];
    }
    
    NSArray * questionsData = [dict objectForKey:@"questions"];
    for (NSDictionary * questionData in questionsData) {
        QuestionData * question = [QuestionData questionDataFromDictionary:questionData forPuzzle:puzzle andUserId:userId];
        [puzzle addQuestionsObject:question];
    }
    
    [managedObjectContext save:&error];
    if (error != nil) {
        NSLog(@"DB error: %@", error.description);
    }
    
    return puzzle;
}

-(float)progress
{
    return (float)[self solved] / self.questions.count;
}

-(int)solved
{
    int solvedQuestions = 0;
    for (QuestionData * question in self.questions) {
        if ([question.solved boolValue]) {
            ++solvedQuestions;
        }
    }
    return solvedQuestions;
}

@end
