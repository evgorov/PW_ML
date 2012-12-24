//
//  InviteViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/16/12.
//
//

#import "InviteViewController.h"
#import "PrizeWordNavigationBar.h"

@interface InviteViewController (private)

-(void)handleInviteAllClick:(id)sender;
-(void)disableAllButtons:(id)sender;
-(void)disableAllButtonsInView:(UIView *)view;
-(void)disableButton:(id)sender;

@end

@implementation InviteViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"TITLE_INVITE", nil);
    
    [self addFramedView:vkView];
    [self addFramedView:fbView];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    UIImage * inviteAllImage = [UIImage imageNamed:@"invite_invite_all_btn"];
    UIImage * inviteAllHighlightedImage = [UIImage imageNamed:@"invite_invite_all_btn_down"];
    UIButton * inviteAllButton = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, inviteAllImage.size.width, inviteAllImage.size.height)];
    [inviteAllButton setBackgroundImage:inviteAllImage forState:UIControlStateNormal];
    [inviteAllButton setBackgroundImage:inviteAllHighlightedImage forState:UIControlStateHighlighted];
    [inviteAllButton addTarget:self action:@selector(handleInviteAllClick:) forControlEvents:UIControlEventTouchUpInside];
    inviteAllItem = [[UIBarButtonItem alloc] initWithCustomView:
                [PrizeWordNavigationBar containerWithView:inviteAllButton]];
    [self.navigationItem setRightBarButtonItem:inviteAllItem animated:animated];
}

- (void)viewDidUnload {
    vkView = nil;
    fbView = nil;
    [super viewDidUnload];
}

- (IBAction)handleAddClick:(id)sender
{
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(disableButton:) userInfo:sender repeats:NO];
}

-(void)handleInviteAllClick:(id)sender
{
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(disableAllButtons:) userInfo:nil repeats:NO];
}

-(void)disableAllButtons:(id)sender
{
    [self hideActivityIndicator];
    [self disableAllButtonsInView:self.view];
}

-(void)disableAllButtonsInView:(UIView *)view
{
    for (UIView * subview in view.subviews) {
        if ([subview isKindOfClass:[UIButton class]])
        {
            UIButton * button = (UIButton *)subview;
            button.enabled = NO;
        }
        else
        {
            [self disableAllButtonsInView:subview];
        }
    }
}

-(void)disableButton:(id)sender
{
    NSTimer * timer = (NSTimer *)sender;
    [self hideActivityIndicator];
    UIButton * button = (UIButton *)timer.userInfo;
    button.enabled = NO;
}

@end
