//
//  LoginEnterViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/9/12.
//
//

#import <UIKit/UIKit.h>
#import "PrizeWordViewController.h"

@interface LoginEnterViewController : PrizeWordViewController<UITextFieldDelegate>
{
    IBOutlet UITextField *txtEmail;
    IBOutlet UITextField *txtPassword;
}

@end
