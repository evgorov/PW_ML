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
    __block PuzzleData * puzzle;
    
    [DataContext performSyncInDataQueue:^{
        NSManagedObjectContext * moc = [DataContext currentContext];
        NSFetchRequest *request = [moc.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"PuzzlesByIdFetchRequest" substitutionVariables:@{@"PUZZLE_ID": [dict objectForKey:@"id"], @"USER_ID": userId}];
        
        NSError *error = nil;
        NSArray *puzzles = [moc executeFetchRequest:request error:&error];
        
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
            puzzle = (PuzzleData *)[NSEntityDescription insertNewObjectForEntityForName:@"Puzzle" inManagedObjectContext:moc];
                [puzzle setPuzzle_id:[dict objectForKey:@"id"]];
                [puzzle setTime_given:time_given];
                [puzzle setTime_left:time_given];
                [puzzle setName:[dict objectForKey:@"name"]];
                [puzzle setUser_id:userId];
                NSString * dateString = [dict objectForKey:@"issuedAt"];
                if (dateString != nil)
                {
                    NSDateFormatter * dateFormatter = [NSDateFormatter new];
                    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
                    [puzzle setIssuedAt:[dateFormatter dateFromString:dateString]];
                }
                [puzzle setHeight:[dict objectForKey:@"height"]];
                [puzzle setWidth:[dict objectForKey:@"width"]];
                [puzzle setScore:[NSNumber numberWithInt:0]];
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
    }];

    return puzzle;
}

+(PuzzleData *)puzzleWithId:(NSString *)puzzleId andUserId:(NSString *)userId
{
    __block PuzzleData * puzzleData = nil;
    if (userId == nil) {
        NSLog(@"userId is nil!");
    }
    [DataContext performSyncInDataQueue:^{
        if (userId == nil) {
            NSLog(@"userId is nil!");
        }
        NSManagedObjectContext * moc = [DataContext currentContext];
        NSFetchRequest *request = [moc.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"PuzzlesByIdFetchRequest" substitutionVariables:@{@"PUZZLE_ID": puzzleId, @"USER_ID": userId}];
        
        NSError *error = nil;
        NSArray *puzzles = [moc executeFetchRequest:request error:&error];
        
        if (puzzles != nil && puzzles.count > 0)
        {
            puzzleData = [puzzles objectAtIndex:0];
        }
    }];
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

    NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                              , @"id": self.puzzle_id};
    NSMutableURLRequest * request = [[APIClient sharedClient] requestWithMethod:@"GET" path:[NSString stringWithFormat:@"puzzles/%@", self.puzzle_id] parameters:params];
    [request setValue:self.etag forHTTPHeaderField:@"If-None-Match"];
    
    AFHTTPRequestOperation * requestOperation = [[APIClient sharedClient] HTTPRequestOperationWithRequest:request success:^(AFHTTPRequestOperation *operation, id      responseObject) {
        NSHTTPURLResponse * response = operation.response;
        NSLog(@"status: %d", operation.response.statusCode);
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
            NSLog(@"puzzle %@ synchronization failed: %@", self.puzzle_id, [[NSString alloc] initWithData:operation.responseData encoding:NSUTF8StringEncoding]);
        }
        else
        {
            [self.managedObjectContext performBlock:^{
                self.etag = [[response allHeaderFields] objectForKey:@"Etag"];
                
                NSString * receivedString = [[NSString alloc] initWithData:operation.responseData encoding:NSUTF8StringEncoding];
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
                    /*
                    if (self.managedObjectContext != [DataContext currentContext])
                    {
                        NSLog(@"ERROR: managed objects contexts are not equal");
                    }
                    else
                    */
                    {
                        NSAssert(self.managedObjectContext != nil, @"managed object context of managed object in nil");
                        [self.managedObjectContext save:nil];
                        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_PUZZLE_SYNCHRONIZED andData:self.puzzle_id]];
                    }
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
                    
                    NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                                              , @"puzzle_data": dataString};
                    [[APIClient sharedClient] putPath:[NSString stringWithFormat:@"puzzles/%@", self.puzzle_id] parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
                        NSLog(@"puz puzzle %@ completed", self.puzzle_id);
                    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
                        NSLog(@"puz puzzle %@ failed: %@", self.puzzle_id, error.description);
                    }];
                }
            }];
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"puzzle %@ synchronization failed: %@", self.puzzle_id, error.localizedDescription);
    }];
    [[APIClient sharedClient] enqueueHTTPRequestOperation:requestOperation];
}


@end
