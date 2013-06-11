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

    TileData * focusedTile;
    
    UIPinchGestureRecognizer * pinchGestureRecognizer;
    UITapGestureRecognizer * tapGestureRecognizer;
}

-(void)setGameField:(GameField *)gameField;
-(void)refreshFocus;

@end
