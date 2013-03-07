//
//  PrizeWordButton.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 3/7/13.
//
//

#import "PrizeWordButton.h"
#import "FISoundEngine.h"

@implementation PrizeWordButton

-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    [[[FISoundEngine sharedEngine] soundNamed:@"interface_button.caf" error:nil] play];
    [super touchesEnded:touches withEvent:event];
}

@end
