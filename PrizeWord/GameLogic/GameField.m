//
//  GameField.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameField.h"
#import "TileData.h"

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
    }
    return self;
}

-(void)dealloc
{
    [tiles removeAllObjects];
    tiles = nil;
}

@end
