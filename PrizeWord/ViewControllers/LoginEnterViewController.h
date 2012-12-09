//
//  LoginEnterViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/9/12.
//
//

#import <UIKit/UIKit.h>

@interface LoginEnterViewController : UIViewController<UITextFieldDelegate>
{
    IBOutlet UITextField *txtEmail;
    IBOutlet UITextField *txtPassword;
    
    UIActivityIndicatorView * activityIndicator;
}

@end
