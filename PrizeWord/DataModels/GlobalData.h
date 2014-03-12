//
//  GlobalData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import <Foundation/Foundation.h>
#import <FacebookSDK/FacebookSDK.h>
#import "PuzzleSetProxy.h"

@class UserData;

@interface GlobalData : NSObject
{
    NSDictionary * coefficients;
    NSMutableDictionary * puzzleIdToSet;
}

+(GlobalData *)globalData;

@property (nonatomic) NSString * sessionKey;
@property (nonatomic) UserData * loggedInUser;
@property (nonatomic) NSArray * monthSets;
@property (nonatomic) NSString * deviceToken;
@property (nonatomic) NSMutableDictionary * products;
@property () int currentDay;
// in range [1, 12]
@property () int currentMonth;
@property () int currentYear;

-(int)baseScoreForType:(PuzzleSetType)type;
-(int)scoreForFriend;
-(int)scoreForTime;
-(int)scoreForRate;
-(int)scoreForShare;

-(void)loadMonthSets;
-(void)loadMe;
-(void)loadCoefficients;
-(void)parseDateFromResponse:(NSHTTPURLResponse *)response;

@end
