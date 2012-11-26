//
//  TileData.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "TileData.h"

@implementation TileData

@synthesize x = _x;
@synthesize y = _y;
@synthesize state = _state;
@synthesize word = _word;
@synthesize currentLetter = _currentLetter;
@synthesize targetLetter = _targetLetter;
@synthesize imagePath = _imagePath;
@synthesize imagePart = _imagePart;

-(id)initWithPositionX:(uint)x y:(uint)y
{
    self = [super init];
    if (self)
    {
        _x = x;
        _y = y;
        _state = TILE_INACTIVE;
        _word = @"";
        _currentLetter = @"";
        _targetLetter = @"";
        _imagePath = @"";
        _imagePart = CGRectMake(0, 0, 0, 0);
    }
    return self;
}

-(id)copy
{
    TileData * newData = [TileData new];
    newData.x = _x;
    newData.y = _y;
    newData.state = _state;
    newData.word = _word;
    newData.currentLetter= _currentLetter;
    newData.targetLetter = _targetLetter;
    newData.imagePath = _imagePath;
    newData.imagePart = _imagePart;
    return newData;
}

@end
