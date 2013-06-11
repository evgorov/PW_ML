//
//  TileData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "TileData.h"
#import "QuestionData.h"

@implementation TileData

@synthesize x = _x;
@synthesize y = _y;
@synthesize state = _state;
@synthesize prevState = _prevState;
@synthesize question = _question;
@synthesize answer = _answer;
@synthesize answerPosition = _answerPosition;
@synthesize currentLetter = _currentLetter;
@synthesize targetLetter = _targetLetter;
@synthesize letterType = _letterType;

-(id)initWithPositionX:(uint)x y:(uint)y
{
    self = [super init];
    if (self)
    {
        _x = x;
        _y = y;
        _state = TILE_LETTER_EMPTY;
        _prevState = _state;
        _question = @"";
        _answer = @"";
        _answerPosition = (kAnswerPositionNorth | kAnswerPositionTop);
        _currentLetter = @"";
        _targetLetter = @"";
        _letterType = LETTER_FREE;
    }
    return self;
}

-(int)currentLetterIdx
{
    int index = [ALPHABET rangeOfString:[_currentLetter lowercaseString]].location;
    if (index == NSNotFound)
    {
        index = 33;
    }
    return index;
}

-(void)setState:(TileState)state
{
    _prevState = _state;
    _state = state;
}

-(id)copy
{
    TileData * newData = [TileData new];
    newData.x = _x;
    newData.y = _y;
    newData.state = _state;
    newData.prevState = _prevState;
    newData.question = _question;
    newData.answer = _answer;
    newData.answerPosition = _answerPosition;
    newData.currentLetter= _currentLetter;
    newData.targetLetter = _targetLetter;
    newData.letterType = _letterType;
    return newData;
}

@end
