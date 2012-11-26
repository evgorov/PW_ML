//
//  GameField.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/25/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GameField : NSObject
{
    NSMutableArray * tiles;
}

@property (readonly) uint tilesPerRow;
@property (readonly) uint tilesPerCol;

-(id)initWithTilesPerRow:(uint)width andTilesPerCol:(uint)height;

@end
