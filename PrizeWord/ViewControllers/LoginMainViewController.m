//
//  LoginMainViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import "LoginMainViewController.h"
#import "RootViewController.h"
#import "LoginRegisterViewController.h"

@interface LoginMainViewController ()

-(void)gotoRoot:(id)sender;

@end

@implementation LoginMainViewController

-(id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
        activityIndicator.hidesWhenStopped = YES;
        activityIndicator.userInteractionEnabled = NO;
        activityIndicator.center = CGPointMake(self.view.frame.size.width / 2, self.view.frame.size.height / 2);
        [self.view addSubview:activityIndicator];
    }
    return self;
}

-(void)dealloc
{
    [activityIndicator removeFromSuperview];
    activityIndicator = nil;
}

-(void)viewWillAppear:(BOOL)animated
{
    [self.navigationController setNavigationBarHidden:YES animated:animated];
}

- (IBAction)handleEnterClick:(UIButton *)sender
{
    [self.navigationController pushViewController:[RootViewController new] animated:YES];
}

- (IBAction)handleRegisterClick:(UIButton *)sender
{
    [self.navigationController setNavigationBarHidden:NO animated:YES];
    [self.navigationController pushViewController:[LoginRegisterViewController new] animated:YES];
}

- (IBAction)handleFacebookClick:(UIButton *)sender
{
    self.view.userInteractionEnabled = NO;
    [activityIndicator startAnimating];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(gotoRoot:) userInfo:nil repeats:NO];
}

- (IBAction)handleVKClick:(UIButton *)sender
{
    self.view.userInteractionEnabled = NO;
    [activityIndicator startAnimating];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(gotoRoot:) userInfo:nil repeats:NO];
}

-(void)gotoRoot:(id)sender
{
    [activityIndicator stopAnimating];
    self.view.userInteractionEnabled = YES;
    [self.navigationController pushViewController:[RootViewController new] animated:YES];
}

@end
