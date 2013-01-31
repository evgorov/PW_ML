//
//  APIRequest.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/14/13.
//
//

#import <Foundation/Foundation.h>

#define SERVER_ENDPOINT @"http://ec2-54-247-173-247.eu-west-1.compute.amazonaws.com/"

typedef void(^SuccessCallback)(NSHTTPURLResponse * response, NSData * receivedData);
typedef void(^FailCallback)(NSError * error);

@interface APIRequest : NSObject<NSURLConnectionDelegate, NSURLConnectionDataDelegate>
{
    NSMutableURLRequest * request;
    NSURLConnection * connection;
    NSHTTPURLResponse * httpResponse;
    NSMutableData * receivedData;
    SuccessCallback successCallback;
    FailCallback failCallback;
    BOOL silentMode;
}

@property (nonatomic, retain) NSMutableDictionary * params;

+(APIRequest *)getRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback;
+(APIRequest *)postRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback;
+(APIRequest *)putRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback;

-(void)run;
-(void)runSilent;

@end
