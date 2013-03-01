//
//  PrizeWordNavigationBar.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "PrizeWordNavigationBar.h"
#import "AppDelegate.h"

@interface PrizeWordNavigationBar (private)

-(void)loadNavigationBarImage;

@end

@implementation PrizeWordNavigationBar

static UIImage * backgroundImage = nil;

+(UIView *)containerWithView:(UIView *)innerView
{
    NSLog(@"backgroundImage: %f %f", backgroundImage.size.height, innerView.frame.size.height);
    UIView * container = [[UIView alloc] initWithFrame:CGRectMake(0, 0, innerView.frame.size.width, backgroundImage.size.height * 2 - 44)];
    innerView.frame = CGRectMake(0, (backgroundImage.size.height - innerView.frame.size.height) / 2, innerView.frame.size.width, innerView.frame.size.height);
    [container addSubview:innerView];
    return container;
}

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self loadNavigationBarImage];
        self.clipsToBounds = NO;
    }
    return self;
}

-(id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self loadNavigationBarImage];
        self.clipsToBounds = NO;
    }
    return self;
}

-(void)loadNavigationBarImage
{
    if (UIInterfaceOrientationIsPortrait([AppDelegate currentDelegate].viewOrientation))
    {
        backgroundImage = [UIImage imageNamed:@"navigationbar_bg"];
    }
    else
    {
        backgroundImage = [UIImage imageNamed:@"navigationbar_bg_landscape"];
    }
    self.backgroundColor = [UIColor clearColor];
}

-(CGSize)sizeThatFits:(CGSize)size
{
    [self loadNavigationBarImage];
    return CGSizeMake(backgroundImage.size.width, backgroundImage.size.height * 116 / 123);
}

- (void)drawRect:(CGRect)rect
{
    [self loadNavigationBarImage];
    [backgroundImage drawInRect:CGRectMake(0, 0, backgroundImage.size.width, backgroundImage.size.height)];
}

-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    NSLog(@"nav bar touch");
}

@end
