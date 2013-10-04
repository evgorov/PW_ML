//
//  APIRequest.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/14/13.
//
//

#import <Foundation/Foundation.h>

#define SERVER_ENDPOINT @"http://api.prize-word.com/"

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
    BOOL useCache;
}

@property (nonatomic, retain) NSMutableDictionary * params;
@property (nonatomic, retain) NSMutableDictionary * headers;

+(APIRequest *)getRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback;
+(APIRequest *)postRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback;
+(APIRequest *)putRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback;
+(void)cancelAll;
+(void)clearCache;
+(int)requestsCount;

-(void)runUsingCache:(BOOL)useCache silentMode:(BOOL)silentMode;
-(void)cancel;

@end
