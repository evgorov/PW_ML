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
    TILE_INACTIVE = 0,
    TILE_WORD,
    TILE_IMAGE,
    TILE_LETTER_EMPTY,
    TILE_LETTER_READY_TO_INPUT,
    TILE_LETTER_INPUT,
    TILE_LETTER_UNCHECKED_INPUT,
    TILE_LETTER_WRONG_INPUT,
    TILE_LETTER_RIGHT_INPUT,
    TILE_LETTER_FIXED
} TileState;

#endif
