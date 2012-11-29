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
// for questions and start letters
@property () uint answerPosition;
// for questions only
@property (nonatomic) NSString * question;
@property (nonatomic) NSString * answer;
// for letters only
@property (nonatomic) NSString * currentLetter;
@property (nonatomic) NSString * targetLetter;

-(id)initWithPositionX:(uint)x y:(uint)y;

@end
