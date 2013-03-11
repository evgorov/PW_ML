//
//  GlobalData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import <Foundation/Foundation.h>
#import "Facebook.h"
#import "PuzzleSetData.h"

@class UserData;

@interface GlobalData : NSObject
{
    NSDictionary * coefficients;
}

+(GlobalData *)globalData;

@property (nonatomic) NSString * sessionKey;
@property (nonatomic) UserData * loggedInUser;
@property (nonatomic) NSArray * monthSets;
@property (nonatomic) FBSession * fbSession;
@property (nonatomic) NSString * deviceToken;
@property () int currentMonth;
@property () int currentYear;

-(int)baseScoreForType:(PuzzleSetType)type;
-(int)scoreForFriend;
-(int)scoreForTime;

-(void)loadMonthSets;
-(void)loadMe;
-(void)loadCoefficients;
-(void)parseDateFromResponse:(NSHTTPURLResponse *)response;

@end
