//
//  GameFieldView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>

@class GameField;

@interface GameFieldView : UIView
{
    UIScrollView * scrollView;
    UIImageView * borderTopLeft;
    UIImageView * borderBottomLeft;
    UIImageView * borderTopRight;
    UIImageView * borderBottomRight;
    NSMutableArray * tiles;
    uint tilesPerRow;
    uint tilesPerCol;
}

-(void)setGameField:(GameField *)gameField;

@end
