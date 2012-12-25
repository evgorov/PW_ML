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
#import "TileData.h"
#import "EventManager.h"

#define kTileOffset 20

@interface GameFieldView (private)

-(void)switchFocusToTile:(TileData *)tile;
-(void)handlePinch:(id)sender;

@end

@implementation GameFieldView

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        scrollView = [[UIScrollView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)];
        scrollView.bounces = YES;
        [self addSubview:scrollView];
        tiles = [NSMutableArray new];
        scrollView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
        fieldView = [UIView new];
        fieldView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_sand_tile.jpg"]];
        [scrollView addSubview:fieldView];
        UIImage * stretchableBorder = [[UIImage imageNamed:@"bg_border"] stretchableImageWithLeftCapWidth:40 topCapHeight:40];
        scrollView.delegate = self;
        borderTopLeft = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomLeft = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomLeft.transform = CGAffineTransformMakeRotation(-M_PI_2);
        borderTopRight = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderTopRight.transform = CGAffineTransformMakeRotation(M_PI_2);
        borderBottomRight = [[UIImageView alloc] initWithImage:stretchableBorder];
        borderBottomRight.transform = CGAffineTransformMakeRotation(M_PI);
        [fieldView addSubview:borderTopLeft];
        [fieldView addSubview:borderTopRight];
        [fieldView addSubview:borderBottomLeft];
        [fieldView addSubview:borderBottomRight];
        focusedTile = nil;
        
        UIPinchGestureRecognizer * pinchGestureRecognizer = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinch:)];
        pinchGestureRecognizer.delegate = self;
        [scrollView addGestureRecognizer:pinchGestureRecognizer];
        
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_FOCUS_CHANGE];
        [[EventManager sharedManager] registerListener:self forEventType:EVENT_TILE_CHANGE];
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
    int tileWidth = [GameTileView tileWidth];
    int tileHeight = [GameTileView tileHeight];
    for (uint j = 0; j != tilesPerCol; ++j) {
        for (uint i = 0; i != tilesPerRow; ++i) {
            GameTileView * tile = [[GameTileView alloc] initWithFrame:CGRectMake(tileWidth * i + kTileOffset, tileHeight * j + kTileOffset, tileWidth, tileHeight) andData:[gameField dataForPositionX:i y:j]];
            [tiles addObject:tile];
            [fieldView insertSubview:tile atIndex:0];
        }
    }
    int width = tilesPerRow * tileWidth + 2 * kTileOffset;
    int height = tilesPerCol * tileHeight + 2 * kTileOffset;
    scrollView.contentSize = CGSizeMake(width, height);
    fieldView.frame = CGRectMake(0, 0, width, height);
    borderTopLeft.frame = CGRectMake(0, 0, width / 2, height / 2);
    borderTopRight.frame = CGRectMake(width / 2, 0, width - width / 2, height / 2);
    borderBottomLeft.frame = CGRectMake(0, height / 2, width / 2, height - height / 2);
    borderBottomRight.frame = CGRectMake(width / 2, height / 2, width - width / 2, height - height / 2);
}

-(void)dealloc
{
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_FOCUS_CHANGE];
    [[EventManager sharedManager] unregisterListener:self forEventType:EVENT_TILE_CHANGE];
    for (GameTileView * tile in tiles) {
        [tile removeFromSuperview];
    }
    tiles = nil;
    [scrollView removeFromSuperview];
    scrollView = nil;
    fieldView = nil;
    [borderTopLeft removeFromSuperview];
    [borderTopRight removeFromSuperview];
    [borderBottomLeft removeFromSuperview];
    [borderBottomRight removeFromSuperview];
    borderTopLeft = nil;
    borderTopRight = nil;
    borderBottomLeft = nil;
    borderBottomRight = nil;
}

// EventListener
-(void)handleEvent:(Event *)event
{
    if (event.type == EVENT_FOCUS_CHANGE)
    {
        [self switchFocusToTile:event.data];
    }
    else if (event.type == EVENT_TILE_CHANGE)
    {
        TileData * tileData = event.data;
        GameTileView * tileView = [tiles objectAtIndex:(tileData.x + tileData.y * tilesPerRow)];
        [tileView handleEvent:event];
    }
}

-(void)setFrame:(CGRect)frame
{
    [super setFrame:frame];
    [scrollView setFrame:CGRectMake(0, 0, frame.size.width, frame.size.height)];
}

-(void)switchFocusToTile:(TileData *)tile
{
    focusedTile = tile;
    [self refreshFocus];
}

-(void)refreshFocus
{
    if (focusedTile != nil) {
        int tileWidth = [GameTileView tileWidth];
        int tileHeight = [GameTileView tileHeight];
        int offsetX = kTileOffset + focusedTile.x * tileWidth + tileWidth / 2 - scrollView.frame.size.width / 2;
        int offsetY = kTileOffset + focusedTile.y * tileHeight + tileHeight / 2 - scrollView.frame.size.height / 2;
        if (offsetX > scrollView.contentSize.width - scrollView.frame.size.width)
            offsetX = scrollView.contentSize.width - scrollView.frame.size.width;
        if (offsetY > scrollView.contentSize.height - scrollView.frame.size.height)
            offsetY = scrollView.contentSize.height - scrollView.frame.size.height;
        if (offsetX < 0)
            offsetX = 0;
        if (offsetY < 0)
            offsetY = 0;
        [scrollView setContentOffset:scrollView.contentOffset animated:NO];
        [scrollView setContentOffset:CGPointMake(offsetX, offsetY) animated:YES];
    }
}

-(UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    return fieldView;
}

CGPoint pinchSavedOffset;
-(void)handlePinch:(id)sender
{
    UIPinchGestureRecognizer * pinchGestureRecognizer = sender;
    if (pinchGestureRecognizer.state == UIGestureRecognizerStateBegan)
    {
        pinchSavedOffset = scrollView.contentOffset;
        if (pinchGestureRecognizer.scale > 1)
        {
            return;
        }
        float targetZoom = scrollView.frame.size.width / scrollView.contentSize.width;
        if (scrollView.frame.size.height / scrollView.contentSize.height < targetZoom)
        {
            targetZoom = scrollView.frame.size.height / scrollView.contentSize.height;
        }
        if (targetZoom < 1)
        {
            scrollView.minimumZoomScale = targetZoom;
            [scrollView setZoomScale:targetZoom animated:YES];
        }
    }
    else if (pinchGestureRecognizer.state == UIGestureRecognizerStateEnded)
    {
        scrollView.minimumZoomScale = 1;
        [scrollView setZoomScale:1 animated:YES];
        [scrollView setContentOffset:pinchSavedOffset animated:YES];
    }
}

@end
