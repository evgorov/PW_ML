//
//  LoginRemindViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/11/12.
//
//

#import "LoginRemindViewController.h"

@interface LoginRemindViewController (private)

-(void)handleSent:(id)sender;
-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;

@end

@implementation LoginRemindViewController

-(void)viewWillAppear:(BOOL)animated
{
    scrollView.contentSize = self.view.frame.size;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

-(void)viewDidDisappear:(BOOL)animated
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [self handleSendClick:textField];
    return YES;
}

- (IBAction)handleSendClick:(id)sender
{
    [self showActivityIndicator];
    [NSTimer scheduledTimerWithTimeInterval:2 target:self selector:@selector(handleSent:) userInfo:nil repeats:NO];
}

-(void)handleSent:(id)sender
{
    [self hideActivityIndicator];
    [self.navigationController popViewControllerAnimated:YES];
}

-(void)handleKeyboardWillShow:(NSNotification *)aNotification
{
    NSDictionary * userInfo = aNotification.userInfo;
    CGRect endFrame = [(NSValue *)[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    
    UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
    double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    [UIView setAnimationCurve:animationCurve];
    [UIView animateWithDuration:animationDuration animations:^{
        scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height - endFrame.size.height);
    } completion:^(BOOL finished) {
        [scrollView scrollRectToVisible:txtEmail.frame animated:YES];
    }];
}

-(void)handleKeyboardWillHide:(NSNotification *)aNotification
{
    NSDictionary * userInfo = aNotification.userInfo;

    UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
    double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    [UIView setAnimationCurve:animationCurve];
    [UIView animateWithDuration:animationDuration animations:^{
        scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
    }];
}

- (void)viewDidUnload {
    scrollView = nil;
    txtEmail = nil;
    [super viewDidUnload];
}
@end
