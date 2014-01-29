//
//  PuzzleSetPackProxy.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import "DataProxy.h"
#import "PuzzleSetPackData.h"

@interface PuzzleSetPackProxy : DataProxy

- (NSNumber *)month;
- (NSNumber *)year;
- (NSString *)user_id;
- (NSString *)etag;
- (void)setEtag:(NSString *)etag;

@end
