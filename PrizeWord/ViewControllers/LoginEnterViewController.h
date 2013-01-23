//
//  LoginEnterViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/9/12.
//
//

#import <UIKit/UIKit.h>
#import "PrizeWordViewController.h"

@interface LoginEnterViewController : PrizeWordViewController<UITextFieldDelegate, UIAlertViewDelegate>
{
    IBOutlet UIScrollView *scrollView;
    IBOutlet UITextField *txtEmail;
    IBOutlet UITextField *txtPassword;
    
    UIView * activeResponder;
}

@end
