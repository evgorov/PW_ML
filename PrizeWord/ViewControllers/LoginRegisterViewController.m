//
//  LoginRegisterViewController.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/6/12.
//
//

#import "LoginRegisterViewController.h"
#import "PuzzlesViewController.h"
#import "APIRequest.h"

@interface LoginRegisterViewController ()

-(void)handleDateChanged:(id)sender;
-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;
-(void)handleSignupComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData;
-(void)handleSignupFailed:(NSError *)error;

@end

@implementation LoginRegisterViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"TITLE_REGISTRATION", nil);
    
    scrollView.contentSize = imgBackground.frame.size;
    [datePicker addTarget:self action:@selector(handleDateChanged:) forControlEvents:UIControlEventValueChanged];
    datePicker.date = [NSDate new];
    [self handleDateChanged:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

-(void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [activeResponder resignFirstResponder];
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
}

- (void)viewDidUnload {
    [datePicker removeTarget:self action:@selector(handleDateChanged:) forControlEvents:UIControlEventValueChanged];
    
    tfName = nil;
    tfSurname = nil;
    tfEmail = nil;
    tfPassword = nil;
    tfPasswordRepeat = nil;
    tfCity = nil;
    datePicker = nil;
    imgBackground = nil;
    scrollView = nil;
    btnBirthday = nil;
    
    datePickerView = nil;
    [super viewDidUnload];
}

- (IBAction)handleAvaClick:(id)sender
{
}

- (IBAction)handleBirthdayClick:(id)sender
{
    if (datePickerView.hidden)
    {
        CGRect datePickerFrame = datePickerView.frame;

        datePickerView.hidden = NO;
        datePickerFrame.origin.y = self.view.frame.size.height - datePickerFrame.size.height;
        [UIView animateWithDuration:0.3 animations:^{
            datePickerView.frame = datePickerFrame;
            scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height - datePickerView.frame.size.height);
        } completion:^(BOOL finished) {
            [scrollView scrollRectToVisible:btnBirthday.frame animated:YES];
        }];
        
        if (activeResponder != nil)
        {
            [activeResponder resignFirstResponder];
            activeResponder = nil;
        }
    }
}

- (IBAction)handleDatePickerDoneClick:(id)sender
{
    if (!datePickerView.hidden)
    {
        CGRect datePickerFrame = datePickerView.frame;
        datePickerFrame.origin.y = self.view.frame.size.height;

        [UIView animateWithDuration:0.3 animations:^{
            if (activeResponder == nil)
            {
                scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
            }
            datePickerView.frame = datePickerFrame;
        } completion:^(BOOL finished) {
            datePickerView.hidden = YES;
        }];
    }
}

- (IBAction)handleRegisterClick:(UIButton *)sender
{
    [activeResponder resignFirstResponder];
    [self handleDatePickerDoneClick:self];
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:@"signup" successCallback:^(NSHTTPURLResponse *response, NSData * receivedData) {
        [self handleSignupComplete:response receivedData:receivedData];
    } failCallback:^(NSError *error) {
        [self handleSignupFailed:error];
    }];
    
    NSDateFormatter * dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    
    [request.params setObject:tfEmail.text forKey:@"email"];
    [request.params setObject:tfName.text forKey:@"name"];
    [request.params setObject:tfSurname.text forKey:@"surname"];
    [request.params setObject:tfPassword.text forKey:@"password"];
    [request.params setObject:[dateFormatter stringFromDate:datePicker.date] forKey:@"birthday"];
    [request.params setObject:tfCity.text forKey:@"city"];
    // TODO :: set userpic
    [request runSilent];
}

-(void)handleSignupComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData
{
    [self hideActivityIndicator];
    if (response.statusCode == 200)
    {
        NSLog(@"signup complete! %@", [NSString stringWithUTF8String:receivedData.bytes]);
        UINavigationController * navController = self.navigationController;
        [navController popViewControllerAnimated:NO];
        [navController pushViewController:[PuzzlesViewController new] animated:YES];
    }
    else
    {
        NSLog(@"signup failed! %d %@", response.statusCode, [NSString stringWithUTF8String:receivedData.bytes]);
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"%d", response.statusCode] message:[NSString stringWithUTF8String:receivedData.bytes] delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
        alert.tag = 1;
        [alert show];
    }
}

-(void)handleSignupFailed:(NSError *)error
{
    [self hideActivityIndicator];
    NSLog(@"signup failed! %@", error.description);
    UIAlertView * alert = [[UIAlertView alloc] initWithTitle:@"Ошибка" message:@"Ошибка соединения с сервером. Попробуйте ещё раз." delegate:self cancelButtonTitle:@"Отмена" otherButtonTitles:@"Повторить", nil];
    alert.tag = 0;
    [alert show];
}

-(void)handleDateChanged:(id)sender
{
    NSString * dateString = [NSDateFormatter localizedStringFromDate:datePicker.date dateStyle:NSDateFormatterLongStyle timeStyle:NSDateFormatterNoStyle];
    
    [btnBirthday setTitle:dateString forState:UIControlStateNormal];
    [scrollView scrollRectToVisible:btnBirthday.frame animated:YES];
}

-(BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
    activeResponder = textField;
    if (!datePickerView.hidden)
    {
        [self handleDatePickerDoneClick:self];
    }
    return YES;
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    activeResponder = nil;
    
    if (textField == tfName)
    {
        activeResponder = tfSurname;
    }
    else if (textField == tfSurname)
    {
        activeResponder = tfEmail;
    }
    else if (textField == tfEmail)
    {
        activeResponder = tfPassword;
    }
    else if (textField == tfPassword)
    {
        activeResponder = tfPasswordRepeat;
    }
    else if (textField == tfPasswordRepeat)
    {
        [self handleBirthdayClick:self];
    }
    else if (textField == tfCity)
    {
    }
    if (activeResponder != nil)
    {
        [activeResponder becomeFirstResponder];
//        [scrollView scrollRectToVisible:activeResponder.frame animated:YES];
        return NO;
    }
    return YES;
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
    if (datePickerView.hidden)
    {
        NSDictionary * userInfo = aNotification.userInfo;

        UIViewAnimationCurve animationCurve = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationCurveUserInfoKey] intValue];
        double animationDuration = [(NSNumber *)[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] doubleValue];

        [UIView setAnimationCurve:animationCurve];
        [UIView animateWithDuration:animationDuration animations:^{
            scrollView.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
        }];
    }
}

-(void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != alertView.cancelButtonIndex)
    {
        [self handleRegisterClick:nil];
    }
}

@end
