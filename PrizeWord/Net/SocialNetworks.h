//
//  SocialNetworks.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/23/13.
//
//

#import <Foundation/Foundation.h>

@class PrizeWordViewController;


@interface SocialNetworks : NSObject<UIAlertViewDelegate, UIWebViewDelegate>
{
    PrizeWordViewController * lastViewController;
    NSString * lastAccessToken;
    NSString * lastProvider;
    
    void (^successCallback)();
}

+(SocialNetworks *)socialNetwokrs;

+(void)loginFacebookWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback;
+(void)loginVkontakteWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback;

@end
