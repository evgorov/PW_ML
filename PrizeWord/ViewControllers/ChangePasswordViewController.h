//
//  ChangePasswordViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 3/25/13.
//
//

#import "PrizeWordViewController.h"

@interface ChangePasswordViewController : PrizeWordViewController <UITextFieldDelegate, UIGestureRecognizerDelegate, UIAlertViewDelegate>
{
    IBOutlet UIScrollView *scrollView;
    IBOutlet UITextField *tfPassword;
    IBOutlet UITextField *tfPasswordRepeat;
    
    UIView * activeResponder;
    float scrollViewDefaultHeight;
    
    NSString * token;
    BOOL showMenu;
}

-(id)initWithToken:(NSString *)token showMenu:(BOOL)showMenu;
-(IBAction)handleDoneClick:(id)sender;

@end
