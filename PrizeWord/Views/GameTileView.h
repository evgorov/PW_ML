//
//  GameTileView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EventListenerDelegate.h"

@class TileData;

@interface GameTileView : UIView<EventListenerDelegate>
{
    TileData * tileData;
    UIImageView * background;
    UILabel * questionLabel;
    UIImageView * arrow;
    UIImageView * overlay;
}

- (id)initWithFrame:(CGRect)frame andData:(TileData *)data;
+ (int)tileWidth;
+ (int)tileHeight;

@end
