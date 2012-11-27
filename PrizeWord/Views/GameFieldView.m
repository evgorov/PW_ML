//
//  GameFieldView.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import "GameFieldView.h"
#import "GameTileView.h"
#import "GameField.h"

@implementation GameFieldView

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        scrollView = [[UIScrollView alloc] initWithFrame:self.frame];
        scrollView.bounces = NO;
        [self addSubview:scrollView];
        tiles = [NSMutableArray new];
    }
    return self;
}

-(id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self)
    {
        scrollView = [[UIScrollView alloc] initWithFrame:frame];
        scrollView.bounces = NO;
        [self addSubview:scrollView];
        tiles = [NSMutableArray new];
    }
    return self;
}

-(void)setGameField:(GameField *)gameField;
{
    tilesPerRow = gameField.tilesPerRow;
    tilesPerCol = gameField.tilesPerCol;
        
    for (GameTileView * tile in tiles) {
        [tile removeFromSuperview];
    }
    [tiles removeAllObjects];
    for (uint j = 0; j != tilesPerCol; ++j) {
        for (uint i = 0; i != tilesPerRow; ++i) {
            GameTileView * tile = [[GameTileView alloc] initWithFrame:CGRectMake(kTileWidth * i, kTileHeight * j, kTileWidth, kTileHeight) andData:[gameField dataForPositionX:i y:j]];
            [tiles addObject:tile];
            [scrollView addSubview:tile];
        }
    }
    scrollView.contentSize = CGSizeMake(tilesPerRow * kTileWidth, tilesPerCol * kTileHeight);
}

-(void)dealloc
{
    for (GameTileView * tile in tiles) {
        [tile removeFromSuperview];
    }
    tiles = nil;
    [scrollView removeFromSuperview];
    scrollView = nil;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
