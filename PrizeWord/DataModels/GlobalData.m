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

-(void)setLoggedInUser:(UserData *)loggedInUser
{
    _loggedInUser = loggedInUser;
    [[NSUserDefaults standardUserDefaults] setObject:[loggedInUser dictionaryRepresentation] forKey:@"user-data"];
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
    [[DataManager sharedManager] fetchCurrentMonthSetsWithCompletion:^(NSArray *data, NSError *error) {
        if (data != nil)
        {
            __block NSMutableArray * objectIDs = [NSMutableArray arrayWithCapacity:data.count];
            for (NSManagedObject * object in data) {
                [objectIDs addObject:object.objectID];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                NSMutableArray * objects = [NSMutableArray arrayWithCapacity:objectIDs.count];
                for (NSManagedObjectID * objectID in objectIDs)
                {
                    [objects addObject:[[DataContext currentContext] objectWithID:objectID]];
                }
                _monthSets = objects;
                if (_monthSets.count > 0)
                {
                    PuzzleSetData * puzzleSet = [_monthSets lastObject];
                    NSAssert(puzzleSet.managedObjectContext != nil, @"managed object context of managed object in nil");
                    [puzzleSet.managedObjectContext save:nil];
                }
                for (PuzzleSetData * puzzleSet in _monthSets)
                {
                    if (puzzleSet.set_id == nil)
                    {
                        NSLog(@"puzzle set id is nil");
                    }
                    for (PuzzleData * puzzle in puzzleSet.puzzles)
                    {
                        NSLog(@"puzzle id: %@", puzzle.puzzle_id);
                    }
                }
                [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_MONTH_SETS_UPDATED andData:_monthSets]];
            });

        }
    }];
}

-(void)loadMe
{
    APIRequest * request = [APIRequest getRequest:@"me" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self parseDateFromResponse:response];
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        NSLog(@"me: %d %@", response.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        UserData * newMe = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
        if (newMe != nil)
        {
            [self setLoggedInUser:newMe];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:_loggedInUser]];
        }
    } failCallback:^(NSError *error) {
        NSLog(@"me error: %@", error.description);
        NSDictionary * data = [[NSUserDefaults standardUserDefaults] dictionaryForKey:@"user-data"];
        UserData * newMe = [UserData userDataWithDictionary:data];
        if (newMe != nil)
        {
            [self setLoggedInUser:newMe];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:_loggedInUser]];
        }
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
