//
//  TileData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "TileData.h"
#import "HintData.h"

@implementation TileData

@synthesize x = _x;
@synthesize y = _y;
@synthesize state = _state;
@synthesize question = _question;
@synthesize answer = _answer;
@synthesize answerPosition = _answerPosition;
@synthesize currentLetter = _currentLetter;
@synthesize targetLetter = _targetLetter;

-(id)initWithPositionX:(uint)x y:(uint)y
{
    self = [super init];
    if (self)
    {
        _x = x;
        _y = y;
        _state = TILE_LETTER_EMPTY;
        _question = @"";
        _answer = @"";
        _answerPosition = (kAnswerPositionNorth | kAnswerPositionTop);
        _currentLetter = @"";
        _targetLetter = @"";
    }
    return self;
}

-(id)copy
{
    TileData * newData = [TileData new];
    newData.x = _x;
    newData.y = _y;
    newData.state = _state;
    newData.question = _question;
    newData.answer = _answer;
    newData.answerPosition = _answerPosition;
    newData.currentLetter= _currentLetter;
    newData.targetLetter = _targetLetter;
    return newData;
}

@end
