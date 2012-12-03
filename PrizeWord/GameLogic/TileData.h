//
//  TileData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <Foundation/Foundation.h>

#define ALPHABET @"абвгдеёжзийклмнопрстуфхцчшщъыьэюя- "

typedef enum TileState {
    TILE_UNUSED = 0,
    TILE_QUESTION_NEW,
    TILE_QUESTION_CORRECT,
    TILE_QUESTION_WRONG,
    TILE_QUESTION_INPUT,
    TILE_LETTER_EMPTY,
    TILE_LETTER_CORRECT,
    TILE_LETTER_WRONG,
    TILE_LETTER_EMPTY_INPUT,
    TILE_LETTER_CORRECT_INPUT,
    TILE_LETTER_INPUT,
} TileState;

typedef enum LetterType {
    LETTER_BRILLIANT = 0,
    LETTER_GOLD,
    LETTER_SILVER,
    LETTER_FREE,
    LETTER_INPUT,
    LETTER_WRONG,
} LetterType;

@interface TileData : NSObject

@property () uint x;
@property () uint y;
@property () TileState state;
// for questions and start letters
@property () uint answerPosition;
// for questions only
@property (nonatomic) NSString * question;
@property (nonatomic) NSString * answer;
// for letters only
@property (nonatomic) NSString * currentLetter;
@property (nonatomic) NSString * targetLetter;
@property () LetterType letterType;

-(id)initWithPositionX:(uint)x y:(uint)y;
-(int)currentLetterIdx;

@end
