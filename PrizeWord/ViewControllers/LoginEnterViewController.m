//
//  LoginEnterViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/9/12.
//
//

#import "LoginEnterViewController.h"
#import "PuzzlesViewController.h"
#import "LoginRemindViewController.h"

@interface LoginEnterViewController (private)

-(void)gotoRoot:(id)sender;

@end

@implementation LoginEnterViewController


- (void)viewDidUnload {
    txtEmail = nil;
    txtPassword = nil;
    [super viewDidUnload];
}

- (IBAction)handleEnterClick:(id)sender
{
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(gotoRoot:) userInfo:nil repeats:NO];
}

- (IBAction)handleForgetClick:(id)sender
{
    [self.navigationController pushViewController:[LoginRemindViewController new] animated:YES];
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
    [self hideActivityIndicator];
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[PuzzlesViewController new] animated:YES];
}

@end
