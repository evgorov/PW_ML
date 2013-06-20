//
//  GlobalData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import <Foundation/Foundation.h>
#import <FacebookSDK/FacebookSDK.h>
#import "PuzzleSetData.h"

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
// in range [0, 11]
@property () int currentMonth;
@property () int currentYear;

-(int)baseScoreForType:(PuzzleSetType)type;
-(int)scoreForFriend;
-(int)scoreForTime;

-(void)loadMonthSets;
-(void)loadMe;
-(void)loadCoefficients;
-(void)repeatUncompleteOperations;
-(void)parseDateFromResponse:(NSHTTPURLResponse *)response;

@end
