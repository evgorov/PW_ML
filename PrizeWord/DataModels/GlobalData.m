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
#import "UserData.h"
#import "EventManager.h"

NSString * MONTHS_ENG[] = {@"Jan", @"Feb", @"Mar", @"Apr", @"May", @"Jun", @"Jul", @"Aug", @"Sep", @"Oct", @"Nov", @"Dec"};
NSString * COEFFICIENTS_KEY = @"coefficients";

@implementation GlobalData

@synthesize sessionKey = _sessionKey;
@synthesize loggedInUser = _loggedInUser;
@synthesize monthSets = _monthSets;
@synthesize currentMonth = _currentMonth;
@synthesize currentYear = _currentYear;
@synthesize fbSession = _fbSession;

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
        _fbSession = nil;
        NSDate * currentDate = [NSDate date];
        NSCalendar * calendar = [NSCalendar currentCalendar];
        NSDateComponents * components = [calendar components:NSYearCalendarUnit|NSMonthCalendarUnit fromDate:currentDate];

        _currentMonth = [components month] - 1;
        _currentYear = [components year];
        coefficients = [[NSUserDefaults standardUserDefaults] dictionaryForKey:COEFFICIENTS_KEY];
    }
    return self;
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
    APIRequest * request = [APIRequest getRequest:@"sets_available" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        
        [self parseDateFromResponse:response];
        
        NSLog(@"available sets: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        NSMutableArray * sets = [NSMutableArray new];
        SBJsonParser * parser = [SBJsonParser new];
        NSArray * data = [parser objectWithData:receivedData];
        for (NSDictionary * setData in data)
        {
            [sets addObject:[PuzzleSetData puzzleSetWithDictionary:setData andUserId:[GlobalData globalData].loggedInUser.user_id]];
        }
        _monthSets = [NSArray arrayWithArray:sets];
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_MONTH_SETS_UPDATED andData:_monthSets]];
    } failCallback:^(NSError *error) {
        NSLog(@"Error: cannot load month sets!");
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_MONTH_SETS_UPDATED andData:_monthSets]];
    }];
    [request.params setObject:_sessionKey forKey:@"session_key"];
    [request runSilent];
}

-(void)loadMe
{
    APIRequest * request = [APIRequest getRequest:@"me" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        if (response.statusCode == 200)
        {
            [self parseDateFromResponse:response];
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:receivedData];
            NSLog(@"me: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            UserData * newMe = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
            if (newMe != nil)
            {
                _loggedInUser = newMe;
                [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:_loggedInUser]];
            }
        }
        else
        {
            NSLog(@"me response: %@", response.description);
        }
    } failCallback:^(NSError *error) {
        NSLog(@"me error: %@", error.description);
    }];
    [request.params setObject:_sessionKey forKey:@"session_key"];
    [request runSilent];
}

-(void)loadCoefficients
{
    APIRequest * request = [APIRequest getRequest:@"coefficients" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        if (response.statusCode == 200)
        {
            [self parseDateFromResponse:response];
            SBJsonParser * parser = [SBJsonParser new];
            NSDictionary * data = [parser objectWithData:receivedData];
            NSLog(@"coefficients: %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            if (data != nil)
            {
                coefficients = data;
                [[NSUserDefaults standardUserDefaults] setObject:coefficients forKey:COEFFICIENTS_KEY];
                [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_COEFFICIENTS_UPDATED]];
            }
        }
        else
        {
            NSLog(@"coefficients response: %@", response.description);
        }
    } failCallback:^(NSError *error) {
        NSLog(@"coefficients error: %@", error.description);
    }];
    [request.params setObject:_sessionKey forKey:@"session_key"];
    [request runSilent];
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

@end
