//
//  GameFieldView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface GameFieldView : UIView
{
    UIScrollView * scrollView;
    NSMutableArray * tiles;
    uint tilesPerRow;
    uint tilesPerCol;
}

-(void)setHorTiles:(uint)width andVertTiles:(uint)height;

@end
