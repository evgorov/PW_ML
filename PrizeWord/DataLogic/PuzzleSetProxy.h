//
//  PuzzleSetProxy.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import "DataProxy.h"
#import "PuzzleSetData.h"

@class PuzzleSetPackProxy;

@interface PuzzleSetProxy : DataProxy

- (NSString *)set_id;
- (NSString *)user_id;
- (NSNumber *)type;
- (NSNumber *)bought;
- (NSArray *)orderedPuzzles;

-(int)solved;
-(int)total;
-(float)percent;
-(int)score;
-(int)minScore;

- (PuzzleSetPackProxy *)puzzleSetPack;

@end
