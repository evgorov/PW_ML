//
//  GameField.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EventListenerDelegate.h"
#import "TileData.h"

@class PuzzleData;

@interface GameField : NSObject<EventListenerDelegate>
{
    NSMutableArray * tiles;
    NSArray * currentWord;
    uint currentLetterIdx;
    TileData * currentQuestion;
    NSMutableSet * questions;
    BOOL saveQuestionAsNew;
}

@property (readonly) PuzzleData * puzzle;
@property (readonly) uint tilesPerRow;
@property (readonly) uint tilesPerCol;
@property (readonly) uint questionsTotal;
@property (readonly) uint questionsComplete;

-(id)initWithData:(PuzzleData *)puzzleData;
-(id)initWithTilesPerRow:(uint)width tilesPerCol:(uint)height andType:(LetterType)type;
-(TileData *)dataForPositionX:(uint)x y:(uint)y;

-(TileData *)activeQuestion;

@end
