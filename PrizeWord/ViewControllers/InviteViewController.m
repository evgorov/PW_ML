//
//  InviteViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "InviteViewController.h"

@interface InviteViewController (private)

-(void)disableButton:(id)sender;

@end

@implementation InviteViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self addFramedView:vkView];
    [self addFramedView:fbView];
}

- (void)viewDidUnload {
    vkView = nil;
    fbView = nil;
    [super viewDidUnload];
}

- (IBAction)handleAddClick:(id)sender
{
    [self showActivityIndicator];
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(disableButton:) userInfo:sender repeats:NO];
}

-(void)disableButton:(id)sender
{
    NSTimer * timer = (NSTimer *)sender;
    [self hideActivityIndicator];
    UIButton * button = (UIButton *)timer.userInfo;
    button.enabled = NO;
}

@end