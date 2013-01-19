//
//  GlobalData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import "GlobalData.h"
#import "APIRequest.h"
#import "PuzzleSetData.h"
#import "SBJson.h"

@implementation GlobalData

@synthesize sessionKey = _sessionKey;
@synthesize loggedInUser = _loggedInUser;
@synthesize monthSets = _monthSets;

+(GlobalData *)globalData
{
    static GlobalData * _globalData = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _globalData = [[GlobalData alloc] init];
    });
    return _globalData;
}

-(id)init
{
    self = [super init];
    if (self)
    {
        _sessionKey = nil;
        _loggedInUser = nil;
        _monthSets = nil;
    }
    return self;
}

-(void)loadMonthSets:(void(^)())onComplete
{
    APIRequest * request = [APIRequest getRequest:@"puzzles" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSMutableArray * sets = [NSMutableArray new];
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        NSLog(@"month score: %@", [data objectForKey:@"score"]);
        NSArray * setsData = [data objectForKey:@"sets"];
        for (NSDictionary * setData in setsData)
        {
            [sets addObject:[PuzzleSetData puzzleSetWithDictionary:setData]];
        }
        sets = [NSArray arrayWithArray:sets];
    } failCallback:^(NSError *error) {
        NSLog(@"Error: cannot load month sets!");
    }];
    [request.params setObject:_sessionKey forKey:@"session_token"];
    [request runSilent];
}

@end
