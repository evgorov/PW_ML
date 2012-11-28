//
//  TileState.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#ifndef PrizeWord_TileState_h
#define PrizeWord_TileState_h

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

#endif
