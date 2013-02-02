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
#import "APIRequest.h"
#import "SBJson.h"
#import "GlobalData.h"
#import "UserData.h"

@interface LoginEnterViewController (private)

-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;

-(void)handleEnterComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData;
-(void)handleEnterFailed:(NSError *)error;

@end

@implementation LoginEnterViewController

-(void)viewDidLoad
{
    [super viewDidLoad];
    self.title = NSLocalizedString(@"TITLE_ENTER", nil);
    scrollView.autoresizesSubviews = NO;
    scrollView.bounces = NO;
    scrollView.contentSize = self.view.frame.size;
}

- (void)viewDidUnload {
    txtEmail = nil;
    txtPassword = nil;
    scrollView = nil;
    [super viewDidUnload];
}

-(void)viewWillAppear:(BOOL)animated
{
    NSLog(@"enter viewWillAppear");
    [super viewWillAppear:animated];
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    
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
}

- (IBAction)handleEnterClick:(id)sender
{
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:@"login" successCallback:^(NSHTTPURLResponse *response, NSData * receivedData) {
        [self handleEnterComplete:response receivedData:receivedData];
    } failCallback:^(NSError *error) {
        [self handleEnterFailed:error];
    }];
    
    NSDateFormatter * dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    
    [request.params setObject:txtEmail.text forKey:@"email"];
    [request.params setObject:txtPassword.text forKey:@"password"];
    [request runSilent];
}


-(void)handleEnterComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData
{
    [self hideActivityIndicator];
    if (response.statusCode == 200)
    {
        NSLog(@"login complete! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        NSDictionary * json = [[SBJsonParser new] objectWithData:receivedData];
        [GlobalData globalData].sessionKey = [json objectForKey:@"session_key"];;
        [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[json objectForKey:@"me"]];
        UINavigationController * navController = self.navigationController;
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[PuzzlesViewController new] animated:YES];
    }
    else
    {
        NSLog(@"login failed! %d %@", response.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"%d", response.statusCode] message:[[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding] delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
        alert.tag = 1;
        [alert show];
    }
}

-(void)handleEnterFailed:(NSError *)error
{
    [self hideActivityIndicator];
    NSLog(@"login failed! %@", error.description);
    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка" message:@"Ошибка соединения с сервером. Попробуйте ещё раз." delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
    alert.tag = 0;
    [alert show];
}

- (IBAction)handleForgetClick:(id)sender
{
    [self.navigationController pushViewController:[LoginRemindViewController new] animated:YES];
}

-(BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
    activeResponder = textField;
    return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if (textField == txtEmail)
    {
        [textField resignFirstResponder];
        activeResponder = txtPassword;
        [txtPassword becomeFirstResponder];
        return NO;
    }
    else if (textField == txtPassword)
    {
        [textField resignFirstResponder];
        activeResponder = nil;
        [self handleEnterClick:self];
    }
    return YES;
}

-(void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != alertView.cancelButtonIndex)
    {
        [self handleEnterClick:nil];
    }
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
            scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
        }];
    activeResponder = nil;
}


@end
