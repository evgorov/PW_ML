//
//  LoginRemindViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/11/12.
//
//

#import "LoginRemindViewController.h"
#import "AppDelegate.h"
#import "RootViewController.h"
#import "APIRequest.h"
#import "SBJsonParser.h"

@interface LoginRemindViewController (private)

-(void)handleSent:(id)sender;
-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;
-(void)handleBackgroundTap:(id)sender;

@end

@implementation LoginRemindViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = NSLocalizedString(@"TITLE_RECOVER", nil);
    UITapGestureRecognizer * tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleBackgroundTap:)];
    tapGestureRecognizer.numberOfTapsRequired = 1;
    tapGestureRecognizer.numberOfTouchesRequired = 1;
    tapGestureRecognizer.delegate = self;
    [scrollView addGestureRecognizer:tapGestureRecognizer];
    scrollViewDefaultHeight = scrollView.frame.size.height;
    scrollView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
    /*
    if ([[UIDevice currentDevice].systemVersion compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending)
    {
        scrollView.contentInset = UIEdgeInsetsMake([AppDelegate currentDelegate].isIPad ? 68 : 57, 0, 0, 0);
        scrollView.scrollIndicatorInsets = scrollView.contentInset;
    }
    */
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    scrollView.contentSize = scrollView.frame.size;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
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
    [txtEmail resignFirstResponder];
    if (txtEmail.text == nil || txtEmail.text.length == 0)
    {
        [[[UIAlertView alloc] initWithTitle:@"Ошибка" message:@"Вы не ввели e-mail. Введите и попробуйте еще раз." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
        return;
    }
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:@"forgot_password" successCallback:^(NSHTTPURLResponse *response, NSData *receivedData) {
        [self hideActivityIndicator];
        if (response.statusCode == 200)
        {
            [self handleSent:sender];
        }
        else
        {
            NSDictionary * data = [[SBJsonParser new] objectWithData:receivedData];
            NSString * message = [data objectForKey:@"message"];
            if (response.statusCode == 404)
            {
                message = NSLocalizedString(@"No user with this e-mail.", @"No user with this e-mail!");
            }
            else if (message == nil)
            {
                message = [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding];
            }
                            UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
                            [alertView show];
        }
    } failCallback:^(NSError *error) {
        [self hideActivityIndicator];
        UIAlertView * alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:error.localizedDescription delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alertView show];
    }];
    [request.params setObject:txtEmail.text forKey:@"email"];
    [request runUsingCache:NO silentMode:YES];
}

-(void)handleSent:(id)sender
{
    self.title = NSLocalizedString(@"TITLE_SENT", nil);
    [[AppDelegate currentDelegate].rootViewController showOverlay:doneOverlay];
}

- (IBAction)handleDoneClick:(id)sender
{
    [[AppDelegate currentDelegate].rootViewController hideOverlay];
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
        scrollView.frame = CGRectMake(0, (self.view.frame.size.height - scrollViewDefaultHeight) / 2, self.view.frame.size.width, scrollViewDefaultHeight);
    }];
}


-(BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch
{
    if ([touch.view isKindOfClass:[UIButton class]])
    {
        return NO;
    }
    return YES;
}

-(void)handleBackgroundTap:(id)sender
{
    [txtEmail resignFirstResponder];
}

- (void)viewDidUnload {
    scrollView = nil;
    txtEmail = nil;
    doneOverlay = nil;
    [super viewDidUnload];
}
@end
