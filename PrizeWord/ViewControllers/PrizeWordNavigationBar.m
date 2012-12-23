//
//  PrizeWordNavigationBar.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "PrizeWordNavigationBar.h"

@implementation PrizeWordNavigationBar

static UIImage * backgroundImage = nil;

+(UIView *)containerWithView:(UIView *)innerView
{
    UIView * container = [[UIView alloc] initWithFrame:CGRectMake(0, 0, innerView.frame.size.width, backgroundImage.size.height)];
    // TODO :: ATTENTION - HACK
    innerView.frame = CGRectMake(0, (backgroundImage.size.height - innerView.frame.size.height) / 4, innerView.frame.size.width, innerView.frame.size.height);
    [container addSubview:innerView];
    return container;
}

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        if (backgroundImage == nil)
        {
            backgroundImage = [UIImage imageNamed:@"navigationbar_bg"];
        }
        self.backgroundColor = [UIColor colorWithPatternImage:backgroundImage];
        self.clipsToBounds = NO;
    }
    return self;
}

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        if (backgroundImage == nil)
        {
            backgroundImage = [UIImage imageNamed:@"navigationbar_bg"];
        }
        self.backgroundColor = [UIColor colorWithPatternImage:backgroundImage];
        self.clipsToBounds = NO;
    }
    return self;
}

-(CGSize)sizeThatFits:(CGSize)size
{
    NSLog(@"sizeThatFits: %f %f", size.width, size.height);
    return CGSizeMake(backgroundImage.size.width, backgroundImage.size.height * 116 / 123);
}

- (void)drawRect:(CGRect)rect
{
    [backgroundImage drawInRect:CGRectMake(0, 0, backgroundImage.size.width, backgroundImage.size.height)];
}

@end
