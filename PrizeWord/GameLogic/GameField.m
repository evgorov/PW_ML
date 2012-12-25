//
//  GameField.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameField.h"
#import "TileData.h"
#import "PuzzleData.h"
#import "EventManager.h"
#import "QuestionData.h"
#import "PuzzleSetData.h"

@interface GameField (private)

// select question: set currentQuestion, currentWord and select them
-(void)selectQuestion:(TileData *)question;
// unselect current question tile and all corresponding letters tiles
-(void)dropQuestion;
// check current question tile and all corresponding letters tiles
// if word is correct, mark all corresponding tiles as correct
// if word is wrong, mark all corresponding tiles as wrong
-(BOOL)checkQuestion;

@end

@implementation GameField

@synthesize tilesPerRow = _tilesPerRow;
@synthesize tilesPerCol = _tilesPerCol;
@synthesize questionsComplete = _questionsComplete;
@synthesize questionsTotal = _questionsTotal;

-(id)initWithTilesPerRow:(uint)width tilesPerCol:(uint)height andType:(LetterType)type
{
    self = [super init];
    
    if (self)
    {
        currentQuestion = nil;
        currentWord = [NSMutableArray new];
        _tilesPerRow = width;
        _tilesPerCol = height;
 
        tiles = [[NSMutableArray alloc] initWithCapacity:_tilesPerCol * _tilesPerRow];
        for (uint j = 0; j != _tilesPerCol; ++j) {
            for (uint i = 0; i != _tilesPerRow; ++i) {
                TileData * tile = [[TileData alloc] initWithPositionX:i y:j];
                tile.letterType = type;
                [tiles addObject:tile];
            }
        }
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_TILE_TAP];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_PUSH_LETTER];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_POP_LETTER];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_REQUEST_FINISH_INPUT];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_REQUEST_APPLY_HINT];
    }
    return self;
}

-(id)initWithData:(PuzzleData *)puzzleData
{
    self = [self initWithTilesPerRow:[puzzleData.width unsignedIntValue] tilesPerCol:[puzzleData.height unsignedIntValue] andType:[puzzleData.puzzleSet.type intValue]];
    
    if (self)
    {
        _questionsTotal = puzzleData.questions.count;
        _questionsComplete = 0;
        for (QuestionData * question in puzzleData.questions) {
            TileData * tile = [tiles objectAtIndex:([question.column unsignedIntValue] + [question.row unsignedIntValue] * _tilesPerRow)];
            tile.question = question.question_text;
            tile.answer = question.answer;
            tile.answerPosition = question.answer_positionAsUint;
            tile.state = TILE_QUESTION_NEW;
            ;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:tile]];
        }
    }
    return self;
}

-(TileData *)dataForPositionX:(uint)x y:(uint)y
{
    return [tiles objectAtIndex:(x + y * _tilesPerRow)];
}

-(TileData *)activeQuestion
{
    return currentQuestion;
}

-(void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_TILE_TAP];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_PUSH_LETTER];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_POP_LETTER];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_REQUEST_FINISH_INPUT];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_REQUEST_APPLY_HINT];
    [tiles removeAllObjects];
    tiles = nil;
    currentQuestion = nil;
    currentWord = nil;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type) {
        case EVENT_TILE_TAP:
        {
            TileData * data = (TileData *)event.data;
            data = [tiles objectAtIndex:(data.x + data.y * _tilesPerRow)];
            switch (data.state) {
                case TILE_QUESTION_NEW:
                case TILE_QUESTION_WRONG:
                    [self dropQuestion];
                    [self selectQuestion:data];
                    return;
                    break;
                    
                case TILE_QUESTION_INPUT:
                    return;
                    break;
                    
                case TILE_LETTER_INPUT:
                case TILE_LETTER_EMPTY_INPUT:
                    for (int idx = 0; idx < currentWord.count; ++idx) {
                        TileData * letter = [currentWord objectAtIndex:idx];
                        if (data.x == letter.x && data.y == letter.y)
                        {
                            if (currentLetterIdx != idx && currentLetterIdx < currentWord.count)
                            {
                                letter = [currentWord objectAtIndex:currentLetterIdx];
                                if (letter.currentLetter.length != 0)
                                {
                                    letter.state = TILE_LETTER_INPUT;
                                    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:letter]];
                                }
                            }
                            currentLetterIdx = idx;
                            break;
                        }
                    }
                    if (data.state == TILE_LETTER_EMPTY_INPUT)
                    {
                        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FOCUS_CHANGE andData:data]];
                        return;
                    }
                    data.state = TILE_LETTER_EMPTY_INPUT;
                    break;
                    
                default:
                    return;
            }
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FOCUS_CHANGE andData:data]];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:data]];
        }
            break;

        case EVENT_PUSH_LETTER:
        {
            if (currentLetterIdx >= currentWord.count)
            {
                break;
            }
            TileData * letter = [currentWord objectAtIndex:currentLetterIdx];
            letter.state = TILE_LETTER_INPUT;
            letter.currentLetter = (NSString *)event.data;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:letter]];
            for (++currentLetterIdx; currentLetterIdx < currentWord.count; ++currentLetterIdx) {
                letter = [currentWord objectAtIndex:currentLetterIdx];
                if (letter.state == TILE_LETTER_EMPTY_INPUT)
                {
                    break;
                }
            }
            if (currentLetterIdx >= currentWord.count)
            {
                [self checkQuestion];
            }
            else
            {
                letter = [currentWord objectAtIndex:currentLetterIdx];
                [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FOCUS_CHANGE andData:letter]];
            }
        }
            break;

        case EVENT_POP_LETTER:
        {
            if (currentLetterIdx == 0)
            {
                return;
            }
            TileData * letter;
            for (--currentLetterIdx; currentLetterIdx > 0; --currentLetterIdx) {
                letter = [currentWord objectAtIndex:currentLetterIdx];
                if (letter.state == TILE_LETTER_INPUT)
                {
                    break;
                }
            }

            letter = [currentWord objectAtIndex:currentLetterIdx];
            if (letter.state != TILE_LETTER_INPUT)
            {
                [self dropQuestion];
                return;
            }
            letter.currentLetter = (NSString *)event.data;
            letter.state = TILE_LETTER_EMPTY_INPUT;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:letter]];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FOCUS_CHANGE andData:letter]];
        }
            break;
            
        case EVENT_REQUEST_FINISH_INPUT:
            [self dropQuestion];
            break;

        case EVENT_REQUEST_APPLY_HINT:
        {
            if (currentQuestion == nil)
            {
                break;
            }
            for (TileData * letter in currentWord) {
                if (letter.state != TILE_LETTER_CORRECT_INPUT)
                {
                    letter.currentLetter = letter.targetLetter;
                    letter.state = TILE_LETTER_INPUT;
                }
            }
            [self checkQuestion];
        }
            break;
            
        default:
            break;
    }
}

-(void)selectQuestion:(TileData *)question
{
    currentQuestion = question;
    currentQuestion.state = TILE_QUESTION_INPUT;

    int letterX = currentQuestion.x;
    int letterY = currentQuestion.y;
    int offsetX = 0;
    int offsetY = 0;
    if ((currentQuestion.answerPosition & kAnswerPositionNorth) != 0)
        letterY--;
    if ((currentQuestion.answerPosition & kAnswerPositionSouth) != 0)
        letterY++;
    if ((currentQuestion.answerPosition & kAnswerPositionWest) != 0)
        letterX--;
    if ((currentQuestion.answerPosition & kAnswerPositionEast) != 0)
        letterX++;
    if ((currentQuestion.answerPosition & kAnswerPositionTop) != 0)
        offsetY--;
    if ((currentQuestion.answerPosition & kAnswerPositionBottom) != 0)
        offsetY++;
    if ((currentQuestion.answerPosition & kAnswerPositionLeft) != 0)
        offsetX--;
    if ((currentQuestion.answerPosition & kAnswerPositionRight) != 0)
        offsetX++;
    uint len = currentQuestion.answer.length;
    for (uint i = 0; i != len; ++i, letterX += offsetX, letterY += offsetY) {
        TileData * letter = [tiles objectAtIndex:(letterX + letterY * _tilesPerRow)];
        [currentWord addObject:letter];
        letter.targetLetter = [currentQuestion.answer substringWithRange:NSMakeRange(i, 1)];
        if (letter.state == TILE_LETTER_EMPTY || letter.state == TILE_LETTER_WRONG)
        {
            letter.currentLetter = @"";
            letter.state = TILE_LETTER_EMPTY_INPUT;
        }
        else if (letter.state == TILE_LETTER_CORRECT)
        {
            letter.state = TILE_LETTER_CORRECT_INPUT;
        }
        else
        {
            continue;
        }
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:letter]];
    }
    currentLetterIdx = 0;
    for (TileData * letter in currentWord) {
        if (letter.state == TILE_LETTER_EMPTY_INPUT)
        {
            break;
        }
        ++currentLetterIdx;
    }
    if (currentLetterIdx < currentWord.count)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_BEGIN_INPUT]];
        TileData * selectedLetter = [currentWord objectAtIndex:currentLetterIdx];
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FOCUS_CHANGE andData:selectedLetter]];
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:currentQuestion]];
    }
    else
    {
        [self checkQuestion];
    }
}

// unselect current question tile and all corresponding letters tiles
-(void)dropQuestion
{
    if (currentQuestion == nil)
    {
        return;
    }
    
    TileData * prevQuestion = currentQuestion;
    currentQuestion = nil;

    for (TileData * letter in currentWord)
    {
        if (letter.state == TILE_LETTER_EMPTY_INPUT)
        {
            letter.state = TILE_LETTER_EMPTY;
        }
        else if (letter.state == TILE_LETTER_CORRECT_INPUT)
        {
            letter.state = TILE_LETTER_CORRECT;
        }
        else if (letter.state == TILE_LETTER_INPUT)
        {
            letter.state = TILE_LETTER_WRONG;
        }
        else
        {
            continue;
        }
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:letter]];
    }
    prevQuestion.state = TILE_QUESTION_WRONG;
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:prevQuestion]];
    
    [currentWord removeAllObjects];
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FINISH_INPUT]];
}

// check current question tile and all corresponding letters tiles
// if word is correct, mark all corresponding tiles as correct
// if word is wrong, mark all corresponding tiles as wrong
-(BOOL)checkQuestion
{
    if (currentQuestion == nil)
    {
        return NO;
    }

    for (TileData * letter in currentWord)
    {
        if ([letter.currentLetter caseInsensitiveCompare:letter.targetLetter] != NSOrderedSame)
        {
            currentQuestion.state = TILE_QUESTION_WRONG;
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:currentQuestion]];
            return NO;
        }
    }
    for (TileData * letter in currentWord)
    {
        if (letter.state == TILE_LETTER_EMPTY_INPUT)
        {
            letter.state = TILE_LETTER_EMPTY;
        }
        else if (letter.state == TILE_LETTER_CORRECT_INPUT)
        {
            letter.state = TILE_LETTER_CORRECT;
        }
        else if (letter.state == TILE_LETTER_INPUT)
        {
            letter.state = TILE_LETTER_CORRECT;
        }
        else
        {
            continue;
        }
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:letter]];
    }
    [currentWord removeAllObjects];

    currentQuestion.state = TILE_QUESTION_CORRECT;
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_TILE_CHANGE andData:currentQuestion]];
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FINISH_INPUT]];
    [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_FOCUS_CHANGE andData:currentQuestion]];
    currentQuestion = nil;
    _questionsComplete++;
    if (_questionsComplete == _questionsTotal)
    {
        [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_COMPLETE]];
    }
    return YES;
}


@end
