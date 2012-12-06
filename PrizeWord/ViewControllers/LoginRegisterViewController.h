//
//  LoginRegisterViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/6/12.
//
//

#import <UIKit/UIKit.h>

@interface LoginRegisterViewController : UIViewController<UITextFieldDelegate>
{
    IBOutlet UIScrollView *scrollView;
    IBOutlet UIImageView *imgBackground;
    IBOutlet UITextField *tfName;
    IBOutlet UITextField *tfSurname;
    IBOutlet UITextField *tfEmail;
    IBOutlet UITextField *tfPassword;
    IBOutlet UITextField *tfPasswordRepeat;
    IBOutlet UITextField *tfCity;
    
    IBOutlet UIDatePicker *datePicker;
}

@end
