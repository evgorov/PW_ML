//
//  GlobalData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/19/13.
//
//

#import <Foundation/Foundation.h>

@class UserData;

@interface GlobalData : NSObject

+(GlobalData *)globalData;

@property (nonatomic, strong) NSString * sessionKey;
@property (nonatomic, strong) UserData * loggedInUser;
@property (nonatomic, strong) NSArray * monthSets;
@property () int currentMonth;

-(void)loadMonthSets:(void(^)())onComplete;

@end
