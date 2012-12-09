//
//  LoginEnterViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/9/12.
//
//

#import "LoginEnterViewController.h"
#import "RootViewController.h"

@interface LoginEnterViewController (private)

-(void)gotoRoot:(id)sender;

@end

@implementation LoginEnterViewController

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

- (void)viewDidUnload {
    txtEmail = nil;
    txtPassword = nil;
    [super viewDidUnload];
}

- (IBAction)handleEnterClick:(id)sender
{
    [activityIndicator startAnimating];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(gotoRoot:) userInfo:nil repeats:NO];
}

- (IBAction)handleForgetClick:(id)sender
{
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if (textField == txtEmail)
    {
        [textField resignFirstResponder];
        [txtPassword becomeFirstResponder];
        return NO;
    }
    else if (textField == txtPassword)
    {
        [textField resignFirstResponder];
        [self handleEnterClick:self];
    }
    return YES;
}

-(void)gotoRoot:(id)sender
{
    [activityIndicator stopAnimating];
    self.view.userInteractionEnabled = YES;
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[RootViewController new] animated:YES];
}

@end
