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
    NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"ScoreFetchRequest" substitutionVariables:@{@"USER_ID" : user.user_id, @"KEY" : key}];
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

- (void)addHints:(int)hints withKey:(NSString *)key
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
    hintsQuery.key = key;
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
    NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"ScoreUndoneFetchRequest" substitutionVariables:@{@"USER_ID" : user.user_id}];
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
    NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"HintsUndoneFetchRequest" substitutionVariables:@{ @"USER_ID" : user.user_id }];
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
    
    void (^successCallback)(AFHTTPRequestOperation *operation, id responseObject) = ^(AFHTTPRequestOperation *operation, id responseObject) {
        scoreQueriesInProgress--;
        NSLog(@"score success! %@", operation.responseString);
        
        NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
        NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"ScoreFetchRequest" substitutionVariables:@{@"USER_ID" : user.user_id, @"KEY" : key}];
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
        NSDictionary * data = [parser objectWithData:operation.responseData];
        UserData * userData = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
        if (userData != nil)
        {
            [GlobalData globalData].loggedInUser = userData;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:userData]];
        }
    };
    
    void (^failureCallback)(AFHTTPRequestOperation *operation, NSError *error) = ^(AFHTTPRequestOperation *operation, NSError *error) {
        scoreQueriesInProgress--;
        NSLog(@"send score error: %@", error.description);
    };
    
    
    if ([key compare:@"rateapp"] == NSOrderedSame)
    {
        NSMutableDictionary * params = [NSMutableDictionary new];
        [params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        
        [[APIClient sharedClient] postPath:@"score_app_rate" parameters:params success:successCallback failure:failureCallback];
    }
    else if ([key rangeOfString:@"shareset"].location == 0)
    {
        NSArray * parts = [key componentsSeparatedByString:@"|"];
        if (parts.count != 3) {
            NSLog(@"invalid key for shareset: %@", key);
            scoreQueriesInProgress--;
            return;
        }
        
        NSMutableDictionary * params = [NSMutableDictionary new];
        [params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        [params setObject:[parts objectAtIndex:1] forKey:@"social_network"];
        [params setObject:[parts objectAtIndex:2] forKey:@"set_id"];
        
        [[APIClient sharedClient] postPath:@"score_set_share" parameters:params success:successCallback failure:failureCallback];
    }
    else
    {
        NSMutableDictionary * params = [NSMutableDictionary new];
        [params setObject:[GlobalData globalData].sessionKey forKey:@"session_key"];
        [params setObject:[NSString stringWithFormat:@"%d", score] forKey:@"score"];
        [params setObject:@"1" forKey:@"solved"];
        [params setObject:key forKey:@"source"];
        
        [[APIClient sharedClient] postPath:@"score" parameters:params success:successCallback failure:failureCallback];
    }
}

- (void)sendHints:(int)hints forKey:(NSString *)key
{
    UserData * user = [GlobalData globalData].loggedInUser;
    if (user == nil || user.user_id == nil)
    {
        return;
    }
    
    hintsQueriesInProgress++;
    
    if (hints > 0)
    {
        NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                                  , @"receipt_data": key};
        
        [[APIClient sharedClient] postPath:@"hints/buy" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
            hintsQueriesInProgress--;
            
            NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
            NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"HintsFetchRequest" substitutionVariables:@{@"USER_ID" : user.user_id, @"KEY" : key}];
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
            
            [[GlobalData globalData] loadMe];
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            NSLog(@"hints request: %@", [[NSString alloc] initWithData:operation.request.HTTPBody encoding:NSUTF8StringEncoding]);
            hintsQueriesInProgress--;
            NSLog(@"send hints error: %@", error.description);
        }];
    }
    else
    {
        NSDictionary * params = @{@"session_key": [GlobalData globalData].sessionKey
                                  , @"hints_change": [NSString stringWithFormat:@"%d", hints]};
        
        [[APIClient sharedClient] postPath:@"hints" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
            hintsQueriesInProgress--;
            
            NSManagedObjectContext * managedObjectContext = [DataContext currentContext];
            NSFetchRequest * fetchRequest = [managedObjectContext.persistentStoreCoordinator.managedObjectModel fetchRequestFromTemplateWithName:@"HintsFetchRequest" substitutionVariables:@{@"USER_ID" : user.user_id, @"KEY" : key}];
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
            NSDictionary * data = [parser objectWithData:operation.responseData];
            UserData * userData = [UserData userDataWithDictionary:[data objectForKey:@"me"]];
            if (userData != nil)
            {
                [GlobalData globalData].loggedInUser = userData;
                [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_ME_UPDATED andData:userData]];
            }
        } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
            NSLog(@"hints request: %@", [[NSString alloc] initWithData:operation.request.HTTPBody encoding:NSUTF8StringEncoding]);
            hintsQueriesInProgress--;
            NSLog(@"send hints error: %@", error.description);
        }];
    }
}

@end
