//
//  ExternalImage.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 2/10/13.
//
//

#import <UIKit/UIKit.h>

@interface ExternalImage : UIImageView<NSURLConnectionDelegate, NSURLConnectionDataDelegate>
{
    NSURL * imageURL;
    NSURLConnection * connection;
    NSMutableData * receivedData;
}

+(void)clearCache;

-(void)loadImageFromURL:(NSURL*)url;
-(void)cancelLoading;
-(void)clear;

@end
