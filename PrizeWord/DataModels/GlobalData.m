//
//  GlobalData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import "GlobalData.h"
#import "PuzzleSetProxy.h"
#import "PuzzleProxy.h"
#import "SBJson.h"
#import "UserData.h"
#import "EventManager.h"
#import "AppDelegate.h"
#import "DataManager.h"
#import "APIClient.h"
#import "DataContext.h"

NSString * MONTHS_ENG[] = {@"Jan", @"Feb", @"Mar", @"Apr", @"May", @"Jun", @"Jul", @"Aug", @"Sep", @"Oct", @"Nov", @"Dec"};
NSString * COEFFICIENTS_KEY = @"coefficients";

@interface GlobalData (private)

-(void)registerDeviceToken;

@end

@implementation GlobalData

@synthesize sessionKey = _sessionKey;
@synthesize loggedInUser = _loggedInUser;
@synthesize monthSets = _monthSets;
@synthesize currentDay = _currentDay;
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
        NSDateComponents * components = [calendar components:NSYearCalendarUnit|NSMonthCalendarUnit|NSDayCalendarUnit fromDate:currentDate];

        _currentDay = [components day];
        _currentMonth = [components month];
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
    NSString * prevSessionKey = _sessionKey;
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
    if (prevSessionKey == nil && _sessionKey != nil && _loggedInUser != nil)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_USER_LOGGED_IN]];
    }
}

-(void)setLoggedInUser:(UserData *)loggedInUser
{
    UserData * prevLoggedInUser = _loggedInUser;
    _loggedInUser = loggedInUser;
    [[NSUserDefaults standardUserDefaults] setObject:[loggedInUser dictionaryRepresentation] forKey:@"user-data"];
    [[NSUserDefaults standardUserDefaults] synchronize];
    if (prevLoggedInUser == nil && _sessionKey != nil && _loggedInUser != nil)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_USER_LOGGED_IN]];
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

- (int)scoreForRate
{
    NSNumber * value = [coefficients objectForKey:@"user-rated-score"];
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

- (int)scoreForFreeShare
{
    NSNumber * value = [coefficients objectForKey:@"shared-free-score"];
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

- (int)scoreForBrilliantShare
{
    NSNumber * value = [coefficients objectForKey:@"shared-brilliant-score"];
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

- (int)scoreForSilverShare
{
    NSNumber * value = [coefficients objectForKey:@"shared-silver1-score"];
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

- (int)scoreForSilver2Share
{
    NSNumber * value = [coefficients objectForKey:@"shared-silver2-score"];
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

- (int)scoreForGoldShare
{
    NSNumber * value = [coefficients objectForKey:@"shared-gold-score"];
    if (value == nil || value == (id)[NSNull null])
    {
        return 0;
    }
    return value.intValue;
}

- (int)scoreForShareSetType:(PuzzleSetType)setType
{
    int score = 0;
    switch (setType) {
        case PUZZLESET_FREE:
            score = [self scoreForFreeShare];
            break;
            
        case PUZZLESET_BRILLIANT:
            score = [self scoreForBrilliantShare];
            break;
            
        case PUZZLESET_GOLD:
            score = [self scoreForGoldShare];
            break;
            
        case PUZZLESET_SILVER:
            score = [self scoreForSilverShare];
            break;
            
        case PUZZLESET_SILVER2:
            score = [self scoreForSilver2Share];
            break;
            
        default:
            break;
    }
    
    return score;
}

#pragma mark public methods

-(void)loadMonthSets
{
    if ([GlobalData globalData].sessionKey == nil || [GlobalData globalData].loggedInUser == nil || [GlobalData globalData].loggedInUser.user_id == nil)
    {
        NSLog(@"WARNING: try to load month sets when user is not logged in");
        return;
    }
        
    [[DataManager sharedManager] fetchCurrentMonthSetsWithCompletion:^(NSArray *data, NSError *error) {
        if (data != nil)
        {
            _monthSets = data;
            if (_monthSets.count > 0)
            {
                PuzzleSetProxy * puzzleSet = [_monthSets lastObject];
                [puzzleSet commitChanges];
            }
            for (PuzzleSetProxy * puzzleSet in _monthSets)
            {
                if (puzzleSet.set_id == nil)
                {
                    NSLog(@"puzzle set id is nil");
                }
                for (PuzzleProxy * puzzle in puzzleSet.orderedPuzzles)
                {
                    NSLog(@"puzzle id: %@", puzzle.puzzle_id);
                }
            }
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_MONTH_SETS_UPDATED andData:_monthSets]];
        }
    }];
}

-(void)loadMe
{
    if (_sessionKey == nil)
    {
        NSLog(@"WARNING: try to load ME with session_key == nil");
        return;
    }
    
    NSDictionary * params = @{@"session_key": _sessionKey};
    
    [[APIClient sharedClient] getPath:@"me" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        [self parseDateFromResponse:operation.response];
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:operation.responseData];
//        NSLog(@"me: %d %@", operation.response.statusCode, operation.responseString);
        UserData * newMe = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
        if (newMe != nil)
        {
            [self setLoggedInUser:newMe];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:_loggedInUser]];
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"me error: %@", error.description);
        NSDictionary * data = [[NSUserDefaults standardUserDefaults] dictionaryForKey:@"user-data"];
        UserData * newMe = [UserData userDataWithDictionary:data];
        if (newMe != nil)
        {
            [self setLoggedInUser:newMe];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:_loggedInUser]];
        }
    }];
}

-(void)loadCoefficients
{
    if (_sessionKey == nil)
    {
        NSLog(@"WARNING: try to load coefficients with session_key == nil");
        return;
    }

    NSDictionary * params = @{@"session_key": _sessionKey};

    [[APIClient sharedClient] getPath:@"coefficients" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        [self parseDateFromResponse:operation.response];
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:operation.responseData];
        NSLog(@"coefficients: %d %@", operation.response.statusCode, operation.responseString);
        if (data != nil)
        {
            coefficients = data;
            [[NSUserDefaults standardUserDefaults] setObject:coefficients forKey:COEFFICIENTS_KEY];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_COEFFICIENTS_UPDATED]];
        }
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"coefficients error: %@", error.description);
    }];
}

-(void)parseDateFromResponse:(NSHTTPURLResponse *)response
{
    NSString * dateString = [response.allHeaderFields objectForKey:@"Date"];
    if (dateString == nil)
    {
        return;
    }
    for (int month = 1; month <= 12; ++month) {
        if ([dateString rangeOfString:MONTHS_ENG[month - 1]].location != NSNotFound)
        {
            _currentMonth = month;

            int monthLocation = [dateString rangeOfString:MONTHS_ENG[month - 1]].location;
            NSString * dayString = [dateString substringWithRange:NSMakeRange(monthLocation - 3, 2)];
            _currentDay = [dayString intValue];
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
    NSDictionary * params = @{@"session_key": _sessionKey
                              , @"id": _deviceToken};
    
    [[APIClient sharedClient] postPath:@"register_device" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        NSLog(@"register_device: %d %@", operation.response.statusCode, operation.responseString);
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        NSLog(@"register_device fail: %@", error.localizedDescription);
    }];
}

@end
