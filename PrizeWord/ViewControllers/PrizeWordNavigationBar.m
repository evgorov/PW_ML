//
//  PrizeWordNavigationBar.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "PrizeWordNavigationBar.h"

@implementation PrizeWordNavigationBar

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        backgroundImage = [UIImage imageNamed:@"navigationbar_bg"];
        self.backgroundColor = [UIColor colorWithPatternImage:backgroundImage];
        self.clipsToBounds = NO;
    }
    return self;
}

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        backgroundImage = [UIImage imageNamed:@"navigationbar_bg"];
        self.backgroundColor = [UIColor colorWithPatternImage:backgroundImage];
        self.clipsToBounds = NO;
    }
    return self;
}

-(CGSize)sizeThatFits:(CGSize)size
{
    return CGSizeMake(self.frame.size.width, backgroundImage.size.height * 116 / 123);
}

- (void)drawRect:(CGRect)rect
{
    [backgroundImage drawInRect:CGRectMake(0, 0, self.frame.size.width, backgroundImage.size.height)];
}

@end
