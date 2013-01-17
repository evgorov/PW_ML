//
//  LoginRegisterViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/6/12.
//
//

#import <UIKit/UIKit.h>
#import "PrizeWordViewController.h"

@interface LoginRegisterViewController : PrizeWordViewController<UITextFieldDelegate, UIAlertViewDelegate>
{
    IBOutlet UIScrollView * scrollView;
    IBOutlet UIImageView * imgBackground;
    IBOutlet UITextField * tfName;
    IBOutlet UITextField * tfSurname;
    IBOutlet UITextField * tfEmail;
    IBOutlet UITextField * tfPassword;
    IBOutlet UITextField * tfPasswordRepeat;
    IBOutlet UITextField * tfCity;
    
    IBOutlet UIDatePicker * datePicker;
    IBOutlet UIView * datePickerView;
    IBOutlet UIButton * btnBirthday;
    
    UIView * activeResponder;
}

@end
