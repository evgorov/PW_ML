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
#import "GlobalData.h"
#import "SBJson.h"
#import "UserData.h"
#import <MobileCoreServices/UTCoreTypes.h>
#import "AppDelegate.h"

@interface LoginRegisterViewController ()

-(void)handleDateChanged:(id)sender;
-(void)handleKeyboardWillShow:(NSNotification *)aNotification;
-(void)handleKeyboardWillHide:(NSNotification *)aNotification;
-(void)handleSignupComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData;
-(void)handleBackgroundTap:(id)sender;

-(void)recolorAllToOK;
-(BOOL)checkInputsAndRecolor;

-(void)startCameraControllerWithSourceType:(UIImagePickerControllerSourceType)sourceType;

@end

@implementation LoginRegisterViewController

UIColor * COLOR_ERROR;
UIColor * COLOR_OK;
NSRegularExpression * EMAIL_REGEXP;


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = NSLocalizedString(@"TITLE_REGISTRATION", nil);
    
    scrollView.contentSize = imgBackground.frame.size;

    scrollView.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"bg_dark_tile.jpg"]];
    if ([[UIDevice currentDevice].systemVersion compare:@"7.0" options:NSNumericSearch] != NSOrderedAscending)
    {
        scrollView.contentInset = UIEdgeInsetsMake([AppDelegate currentDelegate].isIPad ? 68 : 57, 0, 0, 0);
        scrollView.scrollIndicatorInsets = scrollView.contentInset;
    }
    [datePicker addTarget:self action:@selector(handleDateChanged:) forControlEvents:UIControlEventValueChanged];
    datePicker.date = [NSDate new];
    [self handleDateChanged:self];
    avatar = nil;
    UITapGestureRecognizer * tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleBackgroundTap:)];
    tapGestureRecognizer.numberOfTapsRequired = 1;
    tapGestureRecognizer.numberOfTouchesRequired = 1;
    tapGestureRecognizer.delegate = self;
    [scrollView addGestureRecognizer:tapGestureRecognizer];

    COLOR_ERROR = [UIColor colorWithRed:1 green:85/255.0f blue:85/255.0f alpha:1];
    COLOR_OK = [UIColor colorWithRed:194/255.0f green:184/255.0f blue:176/255.0f alpha:1];
    EMAIL_REGEXP = [[NSRegularExpression alloc] initWithPattern:@"^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$" options:NSRegularExpressionCaseInsensitive error:nil];
    
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    activeResponder = nil;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleKeyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    [self recolorAllToOK];
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
    btnAvatar = nil;
    lblEmail = nil;
    lblPassword = nil;
    lblPasswordRepeat = nil;
    [super viewDidUnload];
}

- (IBAction)handleAvaClick:(id)sender
{
    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera] && [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypePhotoLibrary])
    {
        UIActionSheet * actionSheet = [[UIActionSheet alloc] initWithTitle:NSLocalizedString(@"Select source", @"Select source for avatar") delegate:self cancelButtonTitle:NSLocalizedString(@"Cancel", @"Cancel") destructiveButtonTitle:nil otherButtonTitles:NSLocalizedString(@"Camera", @"Camera source for avatar"), NSLocalizedString(@"Gallery", @"Gallery source for avatar"), nil];
        [actionSheet showInView:self.view];
    }
    else if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
    {
        [self startCameraControllerWithSourceType:UIImagePickerControllerSourceTypeCamera];
    }
    else if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypePhotoLibrary])
    {
        [self startCameraControllerWithSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
    }
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

- (IBAction)handleRegisterClick:(id)sender
{
    [activeResponder resignFirstResponder];
    [self handleDatePickerDoneClick:self];
    
    if (![self checkInputsAndRecolor])
    {
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:@"Недопустимые значения полей." delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    [self showActivityIndicator];
    APIRequest * request = [APIRequest postRequest:@"signup" successCallback:^(NSHTTPURLResponse *response, NSData * receivedData) {
        [self handleSignupComplete:response receivedData:receivedData];
    } failCallback:^(NSError *error) {
        [self hideActivityIndicator];
    }];
    
    NSDateFormatter * dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    
    [request.params setObject:tfEmail.text forKey:@"email"];
    [request.params setObject:tfName.text forKey:@"name"];
    [request.params setObject:tfSurname.text forKey:@"surname"];
    [request.params setObject:tfPassword.text forKey:@"password"];
    [request.params setObject:[dateFormatter stringFromDate:datePicker.date] forKey:@"birthday"];
    NSString * city = tfCity.text;
    if (city == nil)
    {
        city = @"";
    }
    [request.params setObject:city forKey:@"city"];
    if (avatar != nil)
    {
        [request.params setObject:avatar forKey:@"userpic"];
    }
    [request runUsingCache:NO silentMode:NO];
}

-(void)handleSignupComplete:(NSHTTPURLResponse *)response receivedData:(NSData *)receivedData
{
    [self hideActivityIndicator];
    NSLog(@"signup complete! %@", [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
    SBJsonParser * parser = [SBJsonParser new];
    NSDictionary * dict = [parser objectWithData:receivedData];
    [GlobalData globalData].sessionKey = [dict objectForKey:@"session_key"];
    [GlobalData globalData].loggedInUser = [UserData userDataWithDictionary:[dict objectForKey:@"me"]];
    UINavigationController * navController = self.navigationController;
    [navController popViewControllerAnimated:NO];
    [navController pushViewController:[PuzzlesViewController new] animated:YES];
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
    if (activeResponder != nil)
    {
        [activeResponder resignFirstResponder];
    }
    [self handleDatePickerDoneClick:sender];
}

-(void)recolorAllToOK
{
    lblEmail.textColor = COLOR_OK;
    lblPassword.textColor = COLOR_OK;
    lblPasswordRepeat.textColor = COLOR_OK;
    [tfName setValue:COLOR_OK forKeyPath:@"_placeholderLabel.textColor"];
    [tfSurname setValue:COLOR_OK forKeyPath:@"_placeholderLabel.textColor"];
}

-(BOOL)checkInputsAndRecolor
{
    [self recolorAllToOK];
    
    BOOL hasInvalid = NO;
    
    int numberOfMatches = tfEmail.text == nil ? 0 : [EMAIL_REGEXP numberOfMatchesInString:tfEmail.text options:0 range:NSMakeRange(0, tfEmail.text.length)];
    if (numberOfMatches == 0)
    {
        lblEmail.textColor = COLOR_ERROR;
        hasInvalid = YES;
    }
    
    if (tfPassword.text == nil || tfPasswordRepeat.text == nil || [tfPassword.text compare:tfPasswordRepeat.text] != NSOrderedSame || tfPassword.text.length == 0)
    {
        lblPassword.textColor = COLOR_ERROR;
        lblPasswordRepeat.textColor = COLOR_ERROR;
        hasInvalid = YES;
    }
    
    if (tfName.text == nil || tfName.text.length == 0)
    {
        [tfName setValue:COLOR_ERROR forKeyPath:@"_placeholderLabel.textColor"];
        hasInvalid = YES;
    }
    
    if (tfSurname.text == nil || tfSurname.text.length == 0)
    {
        [tfSurname setValue:COLOR_ERROR forKeyPath:@"_placeholderLabel.textColor"];
        hasInvalid = YES;
    }
    return !hasInvalid;
}

#pragma mark select image source and start UIImagePickerController

-(void)actionSheet:(UIActionSheet *)actionSheet willDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (buttonIndex != actionSheet.cancelButtonIndex)
    {
        if (buttonIndex == actionSheet.firstOtherButtonIndex)
        {
            [self startCameraControllerWithSourceType:UIImagePickerControllerSourceTypeCamera];
        }
        else
        {
            [self startCameraControllerWithSourceType:UIImagePickerControllerSourceTypePhotoLibrary];
        }
    }
}

// Открывает ImagePicker с выбранным ресурсов (камера или галерея)
-(void)startCameraControllerWithSourceType:(UIImagePickerControllerSourceType)sourceType
{
    if (![UIImagePickerController isSourceTypeAvailable:sourceType])
    {
        return;
    }

    UIImagePickerController * imagePickerController = [[UIImagePickerController alloc] init];
    imagePickerController.sourceType = sourceType;
    imagePickerController.delegate = self;
    imagePickerController.allowsEditing = YES;
    imagePickerController.mediaTypes = [NSArray arrayWithObject:(NSString *)kUTTypeImage];
    if ([AppDelegate currentDelegate].isIPad && imagePickerController.sourceType == UIImagePickerControllerSourceTypePhotoLibrary)
    {
        popoverController = [[UIPopoverController alloc] initWithContentViewController:imagePickerController];
        popoverController.delegate = self;
        [popoverController presentPopoverFromRect:CGRectMake(btnAvatar.frame.origin.x, btnAvatar.frame.origin.y - scrollView.contentOffset.y, btnAvatar.frame.size.width, btnAvatar.frame.size.height) inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
    }
    else
    {
        [self presentViewController:imagePickerController animated:YES completion:nil];
    }
}

#pragma mark UIImagePickerControllerDelegate

-(void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
    if ([AppDelegate currentDelegate].isIPad && picker.sourceType == UIImagePickerControllerSourceTypePhotoLibrary)
    {
        [(UIPopoverController *)picker.parentViewController dismissPopoverAnimated:YES];
    }
    else
    {
        [picker.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }
}

-(void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    UIImage *originalImage, *editedImage, *imageToSave;

    // Handle a still image capture
    editedImage = (UIImage *) [info objectForKey:UIImagePickerControllerEditedImage];
    originalImage = (UIImage *) [info objectForKey:UIImagePickerControllerOriginalImage];
    if (editedImage) {
        imageToSave = editedImage;
    } else {
        imageToSave = originalImage;
    }
//    UIImageWriteToSavedPhotosAlbum (imageToSave, nil, nil, nil);
    int width = imageToSave.size.width;
    int height = imageToSave.size.height;
    int minDimension = width < height ? width : height;
    CGRect subrect = CGRectMake((width - minDimension) / 2, (height - minDimension) / 2, minDimension, minDimension);
    avatar = [UIImage imageWithCGImage:CGImageCreateWithImageInRect(imageToSave.CGImage, subrect)];
    // Save the new image (original or edited) to the Camera Roll
    [btnAvatar setBackgroundImage:avatar forState:UIControlStateNormal];
    [btnAvatar setBackgroundImage:nil forState:UIControlStateHighlighted];

    [self imagePickerControllerDidCancel:picker];
}

@end
