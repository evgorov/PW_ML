//
//  UserDataManager.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 9/17/13.
//
//

#import "UserDataManager.h"
#import "AppDelegate.h"
#import "GlobalData.h"
#import "UserData.h"
#import "ScoreQuery.h"
#import "HintsQuery.h"
#import "EventManager.h"
#import "APIRequest.h"
#import "SBJsonParser.h"
#import "DataContext.h"

@interface UserDataManager ()
{
    int scoreQueriesInProgress;
    int hintsQueriesInProgress;
}

- (void)sendScore:(int)score forKey:(NSString *)key;
- (void)sendHints:(int)hints forKey:(NSString *)key;

@end

@implementation UserDataManager

+ (UserDataManager *)sharedManager
{
    static UserDataManager * _sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedInstance = [[UserDataManager alloc] init];
    });
    return _sharedInstance;
}

- (id)init
{
    self = [super init];
    if (self)
    {
        scoreQueriesInProgress = 0;
        hintsQueriesInProgress = 0;
    
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_ALL_REQUESTS_CANCELED];
    }
    return self;
}

- (void)handleEvent:(Event *)event
{
    if (event.type == EVENT_ALL_REQUESTS_CANCELED)
    {
        scoreQueriesInProgress = 0;
        hintsQueriesInProgress = 0;
    }
}

- (void)addScore:(int)score forKey:(NSString *)key
{
    UserData * user = [GlobalData globalData].loggedInUser;
    if (user == nil || user.user_id == nil)
    {
        return;
    }
    NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
    NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"ScoreFetchRequest" substitutionVariables:@{@"USER" : user.user_id, @"KEY" : key}];
    NSError * error = nil;
    [managedObjectContext lock];
    NSArray * results = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
    [managedObjectContext unlock];
    
    if (results != nil && results.count != 0)
    {
        return;
    }
    ScoreQuery * scoreQuery = [[ScoreQuery alloc] initWithEntity:[NSEntityDescription entityForName:@"ScoreQuery" inManagedObjectContext:managedObjectContext] insertIntoManagedObjectContext:managedObjectContext];
    scoreQuery.score = [NSNumber numberWithInt:score];
    scoreQuery.user = user.user_id;
    scoreQuery.key = key;
    scoreQuery.done = [NSNumber numberWithBool:NO];
    [managedObjectContext lock];
    [managedObjectContext save:nil];
    [managedObjectContext unlock];
    [self sendScore:score forKey:key];
    
    user.month_score += score;
    [GlobalData globalData].loggedInUser = user;
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:user]];
}

- (void)addHints:(int)hints
{
    UserData * user = [GlobalData globalData].loggedInUser;
    if (user == nil || user.user_id == nil)
    {
        return;
    }
    NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
    HintsQuery * hintsQuery = [[HintsQuery alloc] initWithEntity:[NSEntityDescription entityForName:@"HintsQuery" inManagedObjectContext:managedObjectContext] insertIntoManagedObjectContext:managedObjectContext];
    hintsQuery.hints = [NSNumber numberWithInt:hints];
    hintsQuery.user = user.user_id;
    hintsQuery.key = [NSString stringWithFormat:@"%lld%04d", (long long)[[NSDate date] timeIntervalSince1970], rand() % 1000];
    hintsQuery.done = [NSNumber numberWithBool:NO];
    [managedObjectContext lock];
    [managedObjectContext save:nil];
    [managedObjectContext unlock];
    [self sendHints:hints forKey:hintsQuery.key];
    
    user.hints += hints;
    [GlobalData globalData].loggedInUser = user;
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:user]];
}

- (void)restoreUnfinishedScoreQueries
{
    UserData * user = [GlobalData globalData].loggedInUser;
    if (user == nil || user.user_id == nil || scoreQueriesInProgress != 0)
    {
        return;
    }
    NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
    NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"ScoreUndoneFetchRequest" substitutionVariables:@{@"USER" : user.user_id}];
    NSError * error = nil;
    [managedObjectContext lock];
    NSArray * results = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
    [managedObjectContext unlock];
    if (error != nil)
    {
        NSLog(@"error: %@", error.localizedDescription);
        return;
    }
    for (ScoreQuery * scoreQuery in results)
    {
        [self sendScore:scoreQuery.score.intValue forKey:scoreQuery.key];
        break;
    }
}

- (void)restoreUnfinishedHintsQueries
{
    UserData * user = [GlobalData globalData].loggedInUser;
    if (user == nil || user.user_id == nil || hintsQueriesInProgress != 0)
    {
        return;
    }
    NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
    NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"HintsUndoneFetchRequest" substitutionVariables:@{ @"USER" : user.user_id }];
    NSError * error = nil;
    [managedObjectContext lock];
    NSArray * results = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
    [managedObjectContext unlock];
    if (error != nil)
    {
        NSLog(@"error: %@", error.localizedDescription);
        return;
    }
    for (HintsQuery * hintsQuery in results)
    {
        [self sendHints:hintsQuery.hints.intValue forKey:hintsQuery.key];
        break;
    }
}

#pragma mark private
- (void)sendScore:(int)score forKey:(NSString *)key
{
    UserData * user = [GlobalData globalData].loggedInUser;
    if (user == nil || user.user_id == nil)
    {
        return;
    }
    
    scoreQueriesInProgress++;
    APIRequest * request = [APIRequest postRequest:@"score" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        scoreQueriesInProgress--;
        NSLog(@"score success! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        
        NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
        NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"ScoreFetchRequest" substitutionVariables:@{@"USER" : user.user_id, @"KEY" : key}];
        NSError * error = nil;
        [managedObjectContext lock];
        NSArray * results = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        [managedObjectContext unlock];
        
        if (error == nil && results != nil && results.count != 0)
        {
            ScoreQuery * scoreQuery = [results lastObject];
            scoreQuery.done = [NSNumber numberWithBool:YES];
            [managedObjectContext lock];
            [managedObjectContext save:nil];
            [managedObjectContext unlock];
        }
        if (error != nil)
        {
            NSLog(@"error: %@", error.localizedDescription);
        }
        
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        UserData * userData = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
        if (userData != nil)
        {
            [GlobalData globalData].loggedInUser = userData;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:userData]];
        }
    } failCallback:^(NSError *error) {
        scoreQueriesInProgress--;
        NSLog(@"send score error: %@", error.description);
    }];
    
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:[NSString stringWithFormat:@"%d", score] forKey:@"score"];
    [request.params setObject:@"1" forKey:@"solved"];
    [request.params setObject:key forKey:@"source"];
    [request runUsingCache:NO silentMode:YES];
}

- (void)sendHints:(int)hints forKey:(NSString *)key
{
    UserData * user = [GlobalData globalData].loggedInUser;
    if (user == nil || user.user_id == nil)
    {
        return;
    }
    
    hintsQueriesInProgress++;
    APIRequest * request = [APIRequest postRequest:@"hints" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        hintsQueriesInProgress--;
        
        NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
        NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"HintsFetchRequest" substitutionVariables:@{@"USER" : user.user_id, @"KEY" : key}];
        NSError * error = nil;
        [managedObjectContext lock];
        NSArray * results = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        [managedObjectContext unlock];
        
        if (error == nil && results != nil && results.count != 0)
        {
            HintsQuery * hintsQuery = [results lastObject];
            hintsQuery.done = [NSNumber numberWithBool:YES];
            [managedObjectContext lock];
            [managedObjectContext save:nil];
            [managedObjectContext unlock];
        }
        if (error != nil)
        {
            NSLog(@"error: %@", error.localizedDescription);
        }
        
        SBJsonParser * parser = [SBJsonParser new];
        NSDictionary * data = [parser objectWithData:receivedData];
        UserData * userData = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
        if (userData != nil)
        {
            [GlobalData globalData].loggedInUser = userData;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:userData]];
        }
        
    } failCallback:^(NSError *error) {
        hintsQueriesInProgress--;
        NSLog(@"send hints error: %@", error.description);
    }];
    [request.params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
    [request.params setObject:[NSString stringWithFormat:@"%d", hints] forKey:@"hints_change"];
    [request runUsingCache:NO silentMode:YES];
}

@end
