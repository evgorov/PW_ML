//
//  GameFieldView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EventListenerDelegate.h"

@class GameField;
@class TileData;

@interface GameFieldView : UIView<EventListenerDelegate, UIScrollViewDelegate, UIGestureRecognizerDelegate>
{
    UIScrollView * scrollView;
    UIView * fieldView;
    UIImageView * borderTopLeft;
    UIImageView * borderBottomLeft;
    UIImageView * borderTopRight;
    UIImageView * borderBottomRight;
    NSMutableArray * tiles;
    TileData * focusedTile;
    uint tilesPerRow;
    uint tilesPerCol;
}

-(void)setGameField:(GameField *)gameField;
-(void)refreshFocus;

@end
