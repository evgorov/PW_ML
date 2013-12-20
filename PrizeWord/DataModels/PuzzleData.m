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
#import "APIRequest.h"
#import "GlobalData.h"
#import "SBJsonParser.h"
#import "SBJson.h"
#import "EventManager.h"
#import "DataContext.h"

@implementation PuzzleData

@dynamic puzzle_id;
@dynamic name;
@dynamic user_id;
@dynamic issuedAt;
@dynamic time_given;
@dynamic time_left;
@dynamic height;
@dynamic width;
@dynamic score;
@dynamic questions;
@dynamic puzzleSet;
@dynamic etag;

+(PuzzleData *)puzzleWithDictionary:(NSDictionary *)dict andUserId:(NSString *)userId
{
    PuzzleData * puzzle;
    NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleEntity = [NSEntityDescription entityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleEntity];
    [request setFetchLimit:1];
    [request setPredicate:[NSPredicate predicateWithFormat:@"(puzzle_id = %@) AND (user_id = %@)", [dict objectForKey:@"id"], userId]];
    
    NSError *error = nil;
    [managedObjectContext lock];
    NSArray *puzzles = [managedObjectContext executeFetchRequest:request error:&error];
    [managedObjectContext unlock];
    
    NSNumber * time_given = nil;
    id time_givenData = [dict objectForKey:@"time_given"];
    if ([time_givenData isKindOfClass:[NSString class]])
    {
        NSString * time_givenString = time_givenData;
        time_given = [NSNumber numberWithInt:[time_givenString intValue]];
    }
    else
    {
        time_given = [dict objectForKey:@"time_given"];
    }
    if (time_given == nil)
    {
        // 15 minutes
        time_given = [NSNumber numberWithInt:900];
    }
    
    if (puzzles == nil || puzzles.count == 0)
    {
        [managedObjectContext lock];
        puzzle = (PuzzleData *)[NSEntityDescription insertNewObjectForEntityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
        [managedObjectContext unlock];
        [puzzle setPuzzle_id:[dict objectForKey:@"id"]];
        [puzzle setTime_given:time_given];
        [puzzle setTime_left:time_given];
        NSArray * questionsData = [dict objectForKey:@"questions"];
        for (NSDictionary * questionData in questionsData) {
            QuestionData * question = [QuestionData questionDataFromDictionary:questionData forPuzzle:puzzle andUserId:userId];
            if (question == nil)
            {
                NSLog(@"WARNING: question is nil!");
                continue;
            }
            [puzzle addQuestionsObject:question];
        }
    }
    else
    {
        puzzle = [puzzles objectAtIndex:0];
        [puzzle setTime_given:time_given];
        if (puzzle.time_left.intValue > time_given.intValue)
        {
            [puzzle setTime_left:time_given];
        }
    }
    
    id puzzleId = [dict objectForKey:@"id"];
    if (puzzleId == nil || puzzleId == [NSNull null])
    {
        NSLog(@"ERROR: puzzle id is null");
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
//    [puzzle setSolved:[dict objectForKey:@"solved"]];
//    [puzzle setScore:[dict objectForKey:@"score"]];
    [puzzle setHeight:[dict objectForKey:@"height"]];
    [puzzle setWidth:[dict objectForKey:@"width"]];
    if (puzzle.score == nil)
    {
        [puzzle setScore:[NSNumber numberWithInt:0]];
    }
    
/*
    [managedObjectContext save:&error];
    if (error != nil) {
        NSLog(@"DB error: %@", error.description);
    }
*/    
    return puzzle;
}

+(PuzzleData *)puzzleWithId:(NSString *)puzzleId andUserId:(NSString *)userId
{
    NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *puzzleEntity = [NSEntityDescription entityForName:@"Puzzle" inManagedObjectContext:managedObjectContext];
    
    [request setEntity:puzzleEntity];
    [request setFetchLimit:1];
    [request setPredicate:[NSPredicate predicateWithFormat:@"(puzzle_id = %@) AND (user_id = %@)", puzzleId, userId]];
    
    NSError *error = nil;
    [managedObjectContext lock];
    NSArray *puzzles = [managedObjectContext executeFetchRequest:request error:&error];
    [managedObjectContext unlock];
    
    PuzzleData * puzzleData = nil;
    if (puzzles != nil && puzzles.count > 0)
    {
        puzzleData = [puzzles objectAtIndex:0];
    }
    NSLog(@"loaded puzzle: %@ %@ %d", puzzleData.name, puzzleData.score, puzzleData.solved);
    return puzzleData;
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

-(float)progress
{
    return (float)[self solved] / self.questions.count;
}

-(void)synchronize
{
    if ([GlobalData globalData].sessionKey == nil)
    {
        return;
    }
    APIRequest * request = [APIRequest getRequest:[NSString stringWithFormat:@"puzzles/%@", self.puzzle_id] successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        if (response.statusCode == 304)
        {
            if (self.puzzle_id == nil)
            {
                NSLog(@"ERROR: puzzle id is null");
            }
            NSLog(@"puzzle %@ not modified", self.puzzle_id);
        }
        else if (response.statusCode != 200)
        {
            NSLog(@"puzzle %@ synchronization failed: %@", self.puzzle_id, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        }
        else
        {
            NSString * etag = [[response allHeaderFields] objectForKey:@"Etag"];
            self.etag = etag;
            
            NSString * receivedString = [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding];
            receivedString = [receivedString substringWithRange:NSMakeRange(1, receivedString.length - 2)];
            receivedString = [receivedString stringByReplacingOccurrencesOfString:@"\\\"" withString:@"\""];
            NSLog(@"puzzle %@ synchronization success", self.puzzle_id);
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithString:receivedString];
            NSArray * solvedData = [data objectForKey:@"solved_questions"];
            NSNumber * solvedScore = [data objectForKey:@"score"];
            NSNumber * solvedTimeLeft = [data objectForKey:@"time_left"];
            NSMutableSet * solvedQuestions = [NSMutableSet new];
            for (NSDictionary * questionData in solvedData)
            {
                NSNumber * column = [questionData objectForKey:@"column"];
                NSNumber * row = [questionData objectForKey:@"row"];
                [solvedQuestions addObject:[NSNumber numberWithInt:(column.intValue + row.intValue * self.width.intValue)]];
            }
            BOOL needUpdateServer = NO;
            BOOL wasUpdatedLocal = NO;
            
            if (solvedScore != nil && solvedScore.intValue > self.score.intValue)
            {
                self.score = solvedScore;
                wasUpdatedLocal = YES;
            }
            
            if (solvedTimeLeft == nil || solvedTimeLeft.intValue > self.time_left.intValue)
            {
                needUpdateServer = YES;
            }
            else if (solvedTimeLeft != nil && solvedTimeLeft.intValue < self.time_left.intValue)
            {
                self.time_left = solvedTimeLeft;
                if (self.time_left.intValue > self.time_given.intValue)
                {
                    NSLog(@"WARNING: time left is bigger than given time 3");
                }
                wasUpdatedLocal = YES;
            }
            
            for (QuestionData * question in self.questions)
            {
                NSNumber * position = [NSNumber numberWithInt:(question.columnAsUint + question.rowAsUint * self.width.intValue)];
                if (question.solved.boolValue)
                {
                    if (![solvedQuestions containsObject:position])
                    {
                        needUpdateServer = YES;
                    }
                }
                else
                {
                    if ([solvedQuestions containsObject:position])
                    {
                        wasUpdatedLocal = YES;
                        [question setSolved:[NSNumber numberWithBool:YES]];
                    }
                }
            }
            
            if (wasUpdatedLocal)
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    if (self.managedObjectContext != [DataContext currentContext])
                    {
                        NSLog(@"ERROR: managed objects contexts are not equal");
                    }
                    NSAssert(self.managedObjectContext != nil, @"managed object context of managed object in nil");
                    [self.managedObjectContext lock];
                    [self.managedObjectContext save:nil];
                    [self.managedObjectContext unlock];
                    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_PUZZLE_SYNCHRONIZED andData:self.puzzle_id]];
                });
            }
            
            if (needUpdateServer)
            {
                self.etag = @"";
                NSMutableArray * solvedQuestions = [NSMutableArray new];
                for (QuestionData * question in self.questions)
                {
                    if (question.solved.boolValue)
                    {
                        [solvedQuestions addObject:[NSDictionary dictionaryWithObjectsAndKeys:question.question_id, @"id", question.column, @"column", question.row, @"row", nil]];
                    }
                }
                
                NSString * dataString = [[SBJsonWriter new] stringWithObject:[NSDictionary dictionaryWithObjectsAndKeys:solvedQuestions, @"solved_questions", self.score, @"score", self.time_left, @"time_left", nil]];
                
                APIRequest * putRequest = [APIRequest putRequest:[NSString stringWithFormat:@"puzzles/%@", self.puzzle_id]  successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
//                    NSLog(@"put puzzle %@: %@", self.puzzle_id, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
                } failCallback:^(NSError *error) {
                    NSLog(@"puz puzzle %@ failed: %@", self.puzzle_id, error.description);
                }];
                [putRequest.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
                [putRequest.params setObject:dataString forKey:@"puzzle_data"];
                [putRequest runUsingCache:NO silentMode:YES];
            }
            
        }
    } failCallback:^(NSError *error) {
        NSLog(@"puzzle %@ synchronization failed: %@", self.puzzle_id, error.description);
    }];
    if (self.etag != nil) {
        [request.headers setObject:self.etag forKey:@"If-None-Match"];
    }
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    if (self.puzzle_id == nil)
    {
        NSLog(@"ERROR: puzzle id is nil");
    }
    [request.params setObject:self.puzzle_id forKey:@"id"];
    [request runUsingCache:NO silentMode:YES];
}


@end
