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

NSString * MONTHS_ENG[] = {@"Jan", @"Feb", @"Mar", @"Apr", @"May", @"Jun", @"Jul", @"Aug", @"Sep", @"Oct", @"Nov", @"Dec"};

@implementation GlobalData

@synthesize sessionKey = _sessionKey;
@synthesize loggedInUser = _loggedInUser;
@synthesize monthSets = _monthSets;
@synthesize currentMonth = _currentMonth;
@synthesize currentYear = _currentYear;

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
        _currentMonth = 0;
        _currentYear = 2013;
    }
    return self;
}

-(void)loadMonthSets:(void(^)())onComplete
{
    APIRequest * request = [APIRequest getRequest:@"sets_available" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        
        NSString * dateString = [response.allHeaderFields objectForKey:@"Date"];
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
        
        NSLog(@"available sets: %@", [NSString stringWithUTF8String:receivedData.bytes]);
        NSMutableArray * sets = [NSMutableArray new];
        SBJsonParser * parser = [SBJsonParser new];
        NSArray * data = [parser objectWithData:receivedData];
        for (NSDictionary * setData in data)
        {
            [sets addObject:[PuzzleSetData puzzleSetWithDictionary:setData]];
        }
        _monthSets = [NSArray arrayWithArray:sets];
        onComplete();
    } failCallback:^(NSError *error) {
        NSLog(@"Error: cannot load month sets!");
        onComplete();
    }];
    [request.params setObject:_sessionKey forKey:@"session_key"];
    [request runSilent];
}

@end
