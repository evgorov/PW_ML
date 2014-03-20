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
    void (^failCallback)();
}

+(SocialNetworks *)socialNetworks;

+(void)loginFacebookWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback;
+(void)loginVkontakteWithViewController:(PrizeWordViewController *)viewController andCallback:(void (^)())callback;
+(void)logout;
+(void)shareFacebook:(NSString *)message callback:(void (^)(BOOL success))callback;
+(void)shareVkontakte:(NSString *)message callback:(void (^)(BOOL success))callback;

@end
