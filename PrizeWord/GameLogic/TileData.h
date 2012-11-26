//
//  TileData.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 11/26/12.
//  Copyright (c) 2012 A&P Media. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TileState.h"

@interface TileData : NSObject

@property () uint x;
@property () uint y;
@property () TileState state;
@property (nonatomic) NSString * word;
@property (nonatomic) NSString * currentLetter;
@property (nonatomic) NSString * targetLetter;
@property (nonatomic) NSString * imagePath;
@property (nonatomic) CGRect imagePart;

-(id)initWithPositionX:(uint)x y:(uint)y;

@end
