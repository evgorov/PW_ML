//
//  ChangePasswordViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 3/25/13.
//
//

#import "PrizeWordViewController.h"

@interface ChangePasswordViewController : PrizeWordViewController <UITextFieldDelegate, UIGestureRecognizerDelegate>
{
    IBOutlet UIScrollView *scrollView;
    IBOutlet UITextField *tfPassword;
    IBOutlet UITextField *tfPasswordRepeat;
    
    UIView * activeResponder;
    
    NSString * token;
}

-(id)initWithToken:(NSString *)token;
-(IBAction)handleDoneClick:(id)sender;

@end
