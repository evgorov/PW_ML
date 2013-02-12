//
//  LoginMainViewController.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 12/5/12.
//
//

#import <UIKit/UIKit.h>
#import "PrizeWordViewController.h"

@interface LoginMainViewController : PrizeWordViewController<UIAlertViewDelegate, UIWebViewDelegate>
{
    NSString * lastAccessToken;
    NSString * lastProvider;
}

@end
