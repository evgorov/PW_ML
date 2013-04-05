//
//  LoginRemindViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/11/12.
//
//

#import <UIKit/UIKit.h>
#import "PrizeWordViewController.h"

@interface LoginRemindViewController : PrizeWordViewController<UITextFieldDelegate, UIGestureRecognizerDelegate>
{
    IBOutlet UIScrollView *scrollView;
    IBOutlet UITextField *txtEmail;
    IBOutlet UIView *doneOverlay;
    float scrollViewDefaultHeight;
}

@end
