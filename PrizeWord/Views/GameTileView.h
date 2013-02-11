//
//  GameTileView.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EventListenerDelegate.h"
#import "TileData.h"

@interface GameTileView : UIView<EventListenerDelegate>
{
    TileData * tileData;
    TileState oldState;
    UIImageView * background;
    UILabel * questionLabel;
    UIImageView * arrow;
    BOOL arrowDone;
    UIImageView * overlay;
}

@property (readonly) int arrowTileX;
@property (readonly) int arrowTileY;

- (id)initWithFrame:(CGRect)frame andData:(TileData *)data;
+ (int)tileWidth;
+ (int)tileHeight;

@end
