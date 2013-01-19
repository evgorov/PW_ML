//
//  PuzzleData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/28/12.
//
//

#import "PuzzleData.h"
#import "QuestionData.h"


@implementation PuzzleData

@dynamic name;
@dynamic issuedAt;
@dynamic base_score;
@dynamic time_given;
@dynamic time_left;
@dynamic height;
@dynamic width;
@dynamic score;
@dynamic solved;
@dynamic questions;
@dynamic puzzleSet;

+(PuzzleData *)puzzleWithDictionary:(NSDictionary *)dict
{
    return [[PuzzleData alloc] initWithDictionary:dict];
}

-(id)initWithDictionary:(NSDictionary *)dict
{
    self = [super init];
    if (self) {
        self.name = [dict objectForKey:@"name"];
        NSString * dateString = [dict objectForKey:@"issuedAt"];
        if (dateString != nil)
        {
            NSDateFormatter * dateFormatter = [NSDateFormatter new];
            [dateFormatter setDateFormat:@"yyyy-MM-dd"];
            self.issuedAt = [dateFormatter dateFromString:dateString];
        }
        self.base_score = [dict objectForKey:@"base_score"];
        self.time_given = [dict objectForKey:@"time_given"];
        self.time_left = [dict objectForKey:@"time_left"];
        self.solved = [dict objectForKey:@"solved"];
        self.score = [dict objectForKey:@"score"];
    }
    return self;
}

-(float)progress
{
    int solvedQuestions = 0;
    for (QuestionData * question in self.questions) {
        if ([question.solved boolValue]) {
            ++solvedQuestions;
        }
    }
    return (float)solvedQuestions / self.questions.count;
}


@end
