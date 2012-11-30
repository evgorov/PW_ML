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

#define kTileOffset 20

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
        scrollView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_tiling"]];
        UIImage * stretchableBorder = [[UIImage imageNamed:@"bg_border"] stretchableImageWithLeftCapWidth:40 topCapHeight:40];
        borderTopLeft = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomLeft = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomLeft.transform = CGAffineTransformMakeRotation(-M_PI_2);
        borderTopRight = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderTopRight.transform = CGAffineTransformMakeRotation(M_PI_2);
        borderBottomRight = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomRight.transform = CGAffineTransformMakeRotation(M_PI);
        [scrollView addSubview:borderTopLeft];
        [scrollView addSubview:borderTopRight];
        [scrollView addSubview:borderBottomLeft];
        [scrollView addSubview:borderBottomRight];
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
            GameTileView * tile = [[GameTileView alloc] initWithFrame:CGRectMake(kTileWidth * i + kTileOffset, kTileHeight * j + kTileOffset, kTileWidth, kTileHeight) andData:[gameField dataForPositionX:i y:j]];
            [tiles addObject:tile];
            [scrollView insertSubview:tile atIndex:0];
        }
    }
    int width = tilesPerRow * kTileWidth + 2 * kTileOffset;
    int height = tilesPerCol * kTileHeight + 2 * kTileOffset;
    scrollView.contentSize = CGSizeMake(width, height);
    borderTopLeft.frame = CGRectMake(0, 0, width / 2, height / 2);
    borderTopRight.frame = CGRectMake(width / 2, 0, width - width / 2, height / 2);
    borderBottomLeft.frame = CGRectMake(0, height / 2, width / 2, height - height / 2);
    borderBottomRight.frame = CGRectMake(width / 2, height / 2, width - width / 2, height - height / 2);
}

-(void)dealloc
{
    for (GameTileView * tile in tiles) {
        [tile removeFromSuperview];
    }
    tiles = nil;
    [scrollView removeFromSuperview];
    scrollView = nil;
    [borderTopLeft removeFromSuperview];
    [borderTopRight removeFromSuperview];
    [borderBottomLeft removeFromSuperview];
    [borderBottomRight removeFromSuperview];
    borderTopLeft = nil;
    borderTopRight = nil;
    borderBottomLeft = nil;
    borderBottomRight = nil;
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
