//
//  ChangePasswordViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 3/25/13.
//
//

#import "ChangePasswordViewController.h"
#import "AppDelegate.h"
#import "RootViewController.h"

@interface ChangePasswordViewController (private)

-(IBAction)handleBackgroundTap:(id)sender;

-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;

@end

@implementation ChangePasswordViewController

#pragma mark UIViewController lifecycle

-(id)initWithToken:(NSString *)token_ showMenu:(BOOL)showMenu_
{
    self = [super init];
    if (self)
    {
        token = token_;
        showMenu = showMenu_;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.title = NSLocalizedString(@"TITLE_CHANGE", nil);
    scrollView.autoresizesSubviews = NO;
    scrollView.bounces = NO;
    scrollView.contentSize = self.view.frame.size;
    UITapGestureRecognizer * tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleBackgroundTap:)];
    tapGestureRecognizer.numberOfTapsRequired = 1;
    tapGestureRecognizer.numberOfTouchesRequired = 1;
    tapGestureRecognizer.delegate = self;
    [scrollView addGestureRecognizer:tapGestureRecognizer];
    scrollViewDefaultHeight = scrollView.frame.size.height;
}

- (void)viewDidUnload
{
    tfPassword = nil;
    tfPasswordRepeat = nil;
    scrollView = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    
    [[AppDelegate currentDelegate].rootViewController hideMenuAnimated:animated];
}

-(void)viewWillDisappear:(BOOL)animated
{
    NSLog(@"enter viewWillDisappear");
    [super viewWillDisappear:animated];
    if (activeResponder != nil)
    {
        [activeResponder resignFirstResponder];
    }
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
    
    if (showMenu)
    {
        [[AppDelegate currentDelegate].rootViewController showMenuAnimated:animated];
    }
}

#pragma mark action handlers

- (IBAction)handleDoneClick:(id)sender
{
    if (tfPassword.text.length == 0)
    {
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:@"Пароль не может быть пустым." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
        return;
    }
    if ([tfPassword.text compare:tfPasswordRepeat.text] != NSOrderedSame)
    {
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:@"Пароли не совпадают." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
        return;
    }

    [self showActivityIndicator];
    
    NSDictionary * params = @{@"password": tfPassword.text
                              , @"token": token};
    
    [[APIClient sharedClient] postPath:@"password_reset" parameters:params success:^(AFHTTPRequestOperation *operation, id responseObject) {
        [self hideActivityIndicator];
        NSLog(@"password_reset success: %d %@", operation.response.statusCode, operation.responseString);
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Смена пароля" message:@"Новый пароль успешно установлен! Теперь вы можете войти с помощью своего e-mail и нового пароля." delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
    } failure:^(AFHTTPRequestOperation *operation, NSError *error) {
        [self hideActivityIndicator];
        NSLog(@"password_reset fail: %@", error.localizedDescription);
    }];
}

-(void)handleBackgroundTap:(id)sender
{
    if (activeResponder != nil)
    {
        [activeResponder resignFirstResponder];
    }
}

#pragma mark interface helpers

-(BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
    activeResponder = textField;
    return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if (textField == tfPassword)
    {
        [textField resignFirstResponder];
        activeResponder = tfPasswordRepeat;
        [tfPasswordRepeat becomeFirstResponder];
        return NO;
    }
    else if (textField == tfPasswordRepeat)
    {
        [textField resignFirstResponder];
        activeResponder = nil;
        [self handleDoneClick:self];
    }
    return YES;
}

-(void)handleKeyboardWillShow:(NSNotification *)aNotification
{
    scrollViewDefaultHeight = MAX(scrollViewDefaultHeight, self.view.frame.size.height);
    NSDictionary * userInfo = aNotification.userInfo;
    CGRect endFrame = [(NSValue *)[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    
    UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
    double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];
    
    [UIView setAnimationCurve:animationCurve];
    [UIView animateWithDuration:animationDuration animations:^{
        scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height - endFrame.size.height);
    } completion:^(BOOL finished) {
        [scrollView scrollRectToVisible:activeResponder.frame animated:YES];
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
    activeResponder = nil;
}

#pragma mark UIGestureRecognizerDelegate

-(BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch
{
    if ([touch.view isKindOfClass:[UIButton class]])
    {
        return NO;
    }
    return YES;
}

#pragma mark UIAlertViewDelegate

-(void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    [self.navigationController popViewControllerAnimated:YES];
}



@end
