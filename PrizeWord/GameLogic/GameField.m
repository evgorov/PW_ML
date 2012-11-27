//
//  GameField.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameField.h"
#import "TileData.h"
#import "EventManager.h"

@implementation GameField

@synthesize tilesPerRow = _tilesPerRow;
@synthesize tilesPerCol = _tilesPerCol;

-(id)initWithTilesPerRow:(uint)width andTilesPerCol:(uint)height
{
    self = [super init];
    
    if (self)
    {
        _tilesPerRow = width;
        _tilesPerCol = height;
 
        tiles = [[NSMutableArray alloc] initWithCapacity:_tilesPerCol * _tilesPerRow];
        for (uint j = 0; j != _tilesPerCol; ++j) {
            for (uint i = 0; i != _tilesPerRow; ++i) {
                [tiles addObject:[[TileData alloc] initWithPositionX:i y:j]];
            }
        }
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_TILE_TAP];
    }
    return self;
}

-(TileData *)dataForPositionX:(uint)x y:(uint)y
{
    return [tiles objectAtIndex:(x + y * _tilesPerRow)];
}

-(void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_TILE_TAP];
    [tiles removeAllObjects];
    tiles = nil;
}

-(void)handleEvent:(Event *)event
{
    switch (event.type) {
        case EVENT_TILE_TAP:
        {
            TileData * data = (TileData *)event.data;
            data = [tiles objectAtIndex:(data.x + data.y * _tilesPerRow)];
            if (data.state == TILE_LETTER_EMPTY)
            {
                TileData * newData = [data copy];
                newData.state = TILE_LETTER_EMPTY_INPUT;
                [tiles replaceObjectAtIndex:(data.x + data.y * _tilesPerRow) withObject:newData];
                [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_TILE_CHANGE andData:newData]];
            }
            else if (data.state == TILE_LETTER_EMPTY_INPUT)
            {
                TileData * newData = [data copy];
                newData.state = TILE_LETTER_CORRECT;
                [tiles replaceObjectAtIndex:(data.x + data.y * _tilesPerRow) withObject:newData];
                [[EventManager sharedManager] dispatchEventWithType:[Event eventWithType:EVENT_TILE_CHANGE andData:newData]];
            }
        }
            break;
            
        default:
            break;
    }
}

@end
