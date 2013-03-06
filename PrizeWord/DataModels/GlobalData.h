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

@property (nonatomic, strong) NSString * sessionKey;
@property (nonatomic, strong) UserData * loggedInUser;
@property (nonatomic, strong) NSArray * monthSets;
@property (nonatomic, strong) FBSession * fbSession;
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
