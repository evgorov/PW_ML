//
//  PuzzleSetCell.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/8/13.
//
//

#import "PuzzleSetCell.h"
#import "AppDelegate.h"

@implementation PuzzleSetCell

+ (float)height
{
    return [AppDelegate currentDelegate].isIPad ? 116 : 84;
}

@end
