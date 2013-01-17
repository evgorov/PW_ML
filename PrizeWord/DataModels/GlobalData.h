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

@end
