//
//  TileImageHelper.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/2/12.
//
//

#import <Foundation/Foundation.h>
#import "TileData.h"

@interface TileImageHelper : NSObject
{
    UIImage * brilliantLetters[35];
    UIImage * goldLetters[35];
    UIImage * silverLetters[35];
    UIImage * freeLetters[35];
    UIImage * inputLetters[35];
    UIImage * wrongLetters[35];
}

+(void)initHelper;
+(void)uninitHelper;
+(TileImageHelper *)sharedHelper;

-(UIImage *)letterForType:(LetterType)type andIndex:(uint)index;

@end
