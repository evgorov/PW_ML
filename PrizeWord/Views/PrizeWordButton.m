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

-(id)init
{
    self = [super init];
    if (self)
    {
        clickSound = [[FISoundEngine sharedEngine] soundNamed:@"interface_button.caf" error:nil];
    }
    return self;
}

-(void)awakeFromNib
{
    clickSound = [[FISoundEngine sharedEngine] soundNamed:@"interface_button.caf" error:nil];
}

-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    [clickSound play];
    [super touchesEnded:touches withEvent:event];
}

@end
