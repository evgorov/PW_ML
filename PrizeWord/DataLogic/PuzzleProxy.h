//
//  PuzzleProxy.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import "DataProxy.h"
#import "PuzzleData.h"

@class PuzzleSetProxy;

@interface PuzzleProxy : DataProxy

- (NSString *)puzzle_id;
- (NSString *)name;
- (NSString *)user_id;
- (NSString *)etag;
- (NSNumber *)time_given;
- (NSNumber *)time_left;
- (NSNumber *)height;
- (NSNumber *)width;
- (NSNumber *)score;
- (NSDate *)issuedAt;
- (int)solved;
- (float)progress;

- (void)setEtag:(NSString *)etag;
- (void)setTime_left:(NSNumber *)time_left;
- (void)setScore:(NSNumber *)score;

- (PuzzleSetProxy *)puzzleSet;
- (NSSet *)questions;
- (void)synchronize;

@end
