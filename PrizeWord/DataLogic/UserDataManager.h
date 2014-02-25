//
//  UserDataManager.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 9/17/13.
//
//

#import <Foundation/Foundation.h>
#import "EventListenerDelegate.h"

@interface UserDataManager : NSObject<EventListenerDelegate>

+ (UserDataManager *)sharedManager;

- (void)addScore:(int)score forKey:(NSString *)key;
- (void)addHints:(int)hints withKey:(NSString *)key;
- (void)restoreUnfinishedScoreQueries;
- (void)restoreUnfinishedHintsQueries;

@end
