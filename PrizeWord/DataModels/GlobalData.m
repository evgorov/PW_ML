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
#import "PuzzleData.h"
#import "SBJson.h"
#import "UserData.h"
#import "EventManager.h"
#import "AppDelegate.h"

NSString * MONTHS_ENG[] = {@"Jan", @"Feb", @"Mar", @"Apr", @"May", @"Jun", @"Jul", @"Aug", @"Sep", @"Oct", @"Nov", @"Dec"};
NSString * COEFFICIENTS_KEY = @"coefficients";

@interface GlobalData (private)

-(void)registerDeviceToken;

@end

@implementation GlobalData

@synthesize sessionKey = _sessionKey;
@synthesize loggedInUser = _loggedInUser;
@synthesize monthSets = _monthSets;
@synthesize currentMonth = _currentMonth;
@synthesize currentYear = _currentYear;
@synthesize deviceToken = _deviceToken;
@synthesize products = _products;

#pragma mark initialization

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
        _deviceToken = nil;
        NSDate * currentDate = [NSDate date];
        NSCalendar * calendar = [NSCalendar currentCalendar];
        NSDateComponents * components = [calendar components:NSYearCalendarUnit|NSMonthCalendarUnit fromDate:currentDate];

        _currentMonth = [components month] - 1;
        _currentYear = [components year];
        coefficients = [[NSUserDefaults standardUserDefaults] dictionaryForKey:COEFFICIENTS_KEY];
        puzzleIdToSet = [NSMutableDictionary new];
        _products = [NSMutableDictionary new];
    }
    return self;
}

#pragma mark setters
-(void)setDeviceToken:(NSString *)deviceToken
{
    _deviceToken = deviceToken;
    if (_deviceToken != nil && _sessionKey != nil)
    {
        [self registerDeviceToken];
    }
}

-(void)setSessionKey:(NSString *)sessionKey
{
    _sessionKey = sessionKey;
    [[NSUserDefaults standardUserDefaults] setObject:sessionKey forKey:@"session-key"];
    if (_deviceToken != nil && _sessionKey != nil)
    {
        [self registerDeviceToken];
    }
    // logout
    if (sessionKey == nil)
    {
        [puzzleIdToSet removeAllObjects];
    }
}

#pragma mark getters
-(int)baseScoreForType:(PuzzleSetType)type
{
    NSNumber * value = nil;
    switch (type)
    {
        case PUZZLESET_FREE:
            value = [coefficients objectForKey:@"free-base-score"];
            break;
            
        case PUZZLESET_SILVER:
            value = [coefficients objectForKey:@"silver1-base-score"];
            break;
            
        case PUZZLESET_SILVER2:
            value = [coefficients objectForKey:@"silver2-base-score"];
            break;
            
        case PUZZLESET_GOLD:
            value = [coefficients objectForKey:@"gold-base-score"];
            break;
            
        case PUZZLESET_BRILLIANT:
            value = [coefficients objectForKey:@"brilliant-base-score"];
            break;
            
        default:
            break;
    }
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

-(int)scoreForFriend
{
    NSNumber * value = [coefficients objectForKey:@"friend-bonus"];
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

-(int)scoreForTime
{
    NSNumber * value = [coefficients objectForKey:@"time-bonus"];
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

#pragma mark public methods

-(void)loadMonthSets
{
    APIRequest * request = [APIRequest getRequest:@"published_sets" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        
        [self parseDateFromResponse:response];
        
        NSLog(@"published_sets loaded");
        NSMutableArray * sets = [NSMutableArray new];
        SBJsonParser * parser = [SBJsonParser new];
        NSArray * data = [parser objectWithData:receivedData];
        NSMutableString * puzzleIdsString = [[NSMutableString alloc] initWithCapacity:1024];
        for (NSDictionary * setData in data)
        {
            PuzzleSetData * puzzleSet = [PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.user_id];
            [sets addObject:puzzleSet];
            NSArray * puzzleIds = [setData objectForKey:@"puzzles"];
            for (NSString * puzzleId in puzzleIds)
            {
                PuzzleData * puzzle = [PuzzleData puzzleWithId:puzzleId andUserId:_loggedInUser.user_id];
                if (puzzle == nil && puzzleSet.bought.boolValue)
                {
                    if (puzzleIdsString.length != 0)
                    {
                        [puzzleIdsString appendString:@","];
                    }
                    [puzzleIdsString appendString:puzzleId];
                    [puzzleIdToSet setObject:puzzleSet forKey:puzzleId];
                }
                else if (puzzle != nil)
                {
                    [puzzleSet addPuzzlesObject:puzzle];
                    [puzzle synchronize];
                }
            }
        }
        _monthSets = [sets sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
            PuzzleSetData * set1 = obj1;
            PuzzleSetData * set2 = obj2;
            
            return [set1.type compare:set2.type];
        }];
        if (puzzleIdsString.length == 0)
        {
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_MONTH_SETS_UPDATED andData:_monthSets]];
        }
        else
        {
            NSLog(@"loading user_puzzles: %@", puzzleIdsString);
            APIRequest * puzzlesRequest = [APIRequest getRequest:@"user_puzzles" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
                NSLog(@"user_puzzles: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
                
                NSArray * puzzlesData = [[SBJsonParser new] objectWithData:receivedData];
                for (NSDictionary * puzzleData in puzzlesData)
                {
                    PuzzleData * puzzle = [PuzzleData puzzleWithDictionary:puzzleData andUserId:_loggedInUser.user_id];
                    if (puzzle != nil)
                    {
                        PuzzleSetData * puzzleSet = [puzzleIdToSet objectForKey:puzzle.puzzle_id];
                        if (puzzleSet != nil)
                        {
                            [puzzleIdToSet removeObjectForKey:puzzle.puzzle_id];
                            [puzzleSet addPuzzlesObject:puzzle];
                        }
                    }
                }
                
                [[AppDelegate currentDelegate].managedObjectContext save:nil];
                [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_MONTH_SETS_UPDATED andData:_monthSets]];
                
            } failCallback:^(NSError *error) {
                NSLog(@"Error: cannot load puzzles for month sets!");
                [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_MONTH_SETS_UPDATED andData:_monthSets]];
            }];
            [puzzlesRequest.params setObject:_sessionKey forKey:@"session_key"];
            [puzzlesRequest.params setObject:puzzleIdsString forKey:@"ids"];
            [puzzlesRequest runUsingCache:YES silentMode:YES];
        }
    } failCallback:^(NSError *error) {
        NSLog(@"Error: cannot load month sets!");
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_MONTH_SETS_UPDATED andData:_monthSets]];
    }];
    [request.params setObject:_sessionKey forKey:@"session_key"];
    [request.params setObject:[NSNumber numberWithInt:(_currentMonth + 1)] forKey:@"month"];
    [request.params setObject:[NSNumber numberWithInt:_currentYear] forKey:@"year"];
    [request.params setObject:@"short" forKey:@"mode"];
    [request runUsingCache:YES silentMode:YES];
}

-(void)loadMe
{
    APIRequest * request = [APIRequest getRequest:@"me" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self parseDateFromResponse:response];
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        [[NSUserDefaults standardUserDefaults] setObject:[data objectForKey:@"me"] forKey:@"user-data"];
        NSLog(@"me: %d %@", response.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        UserData * newMe = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
        if (newMe != nil)
        {
            _loggedInUser = newMe;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:_loggedInUser]];
        }
    } failCallback:^(NSError *error) {
        NSLog(@"me error: %@", error.description);
    }];
    [request.params setObject:_sessionKey forKey:@"session_key"];
    [request runUsingCache:NO silentMode:YES];
}

-(void)loadCoefficients
{
    APIRequest * request = [APIRequest getRequest:@"coefficients" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self parseDateFromResponse:response];
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        NSLog(@"coefficients: %d %@", response.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        if (data != nil)
        {
            coefficients = data;
            [[NSUserDefaults standardUserDefaults] setObject:coefficients forKey:COEFFICIENTS_KEY];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_COEFFICIENTS_UPDATED]];
        }
    } failCallback:^(NSError *error) {
        NSLog(@"coefficients error: %@", error.description);
    }];
    [request.params setObject:_sessionKey forKey:@"session_key"];
    [request runUsingCache:NO silentMode:YES];
}

-(void)sendSavedScores
{
    NSMutableDictionary * savedScore = [[[NSUserDefaults standardUserDefaults] dictionaryForKey:@"savedScore"] mutableCopy];
    
    if (savedScore == nil || savedScore.count == 0)
    {
        return;
    }
    
    for (NSString * key in savedScore)
    {
        NSMutableDictionary * params = [[savedScore objectForKey:savedScore] mutableCopy];
        [params setValue:[GlobalData globalData].sessionKey forKey:@"session_key"];

        APIRequest * request = [APIRequest postRequest:@"score" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
            NSLog(@"score success! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            
            NSMutableDictionary * savedScore = [[[NSUserDefaults standardUserDefaults] dictionaryForKey:@"savedScore"] mutableCopy];
            [savedScore removeObjectForKey:[params objectForKey:@"source"]];
            [[NSUserDefaults standardUserDefaults] setValue:savedScore forKey:@"savedScore"];
            
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:receivedData];
            UserData * userData = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
            if (userData != nil)
            {
                [GlobalData globalData].loggedInUser = userData;
                [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:userData]];
            }
        } failCallback:^(NSError *error) {
            NSLog(@"score error! %@", error.description);
        }];
        
        request.params = params;
        [request runUsingCache:NO silentMode:YES];
    }
}

-(void)parseDateFromResponse:(NSHTTPURLResponse *)response
{
    NSString * dateString = [response.allHeaderFields objectForKey:@"Date"];
    if (dateString == nil)
    {
        return;
    }
    for (int month = 0; month < 12; ++month) {
        if ([dateString rangeOfString:MONTHS_ENG[month]].location != NSNotFound)
        {
            _currentMonth = month;
            break;
        }
    }
    for (int year = 2000; year < 2100; ++year) {
        if ([dateString rangeOfString:[NSString stringWithFormat:@"%d", year]].location != NSNotFound)
        {
            _currentYear = year;
            break;
        }
    }
}

#pragma mark private methods

-(void)registerDeviceToken
{
    APIRequest * request = [APIRequest postRequest:@"register_device" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        NSLog(@"register_device: %d %@", response.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
    } failCallback:^(NSError *error) {
        NSLog(@"register_device fail: %@", error.description);
    }];
    [request.params setObject:_sessionKey forKey:@"session_key"];
    [request.params setObject:_deviceToken forKey:@"id"];
    [request runUsingCache:NO silentMode:YES];
}

@end
