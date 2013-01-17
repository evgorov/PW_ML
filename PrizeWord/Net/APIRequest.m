//
//  APIRequest.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/14/13.
//
//

#import "APIRequest.h"

@interface APIRequest (private)
-(id)initWithMethod:(NSString *)httpMethod command:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback;
@end

@implementation APIRequest

@synthesize params = _params;

-(id)initWithMethod:(NSString *)httpMethod command:(NSString *)command successCallback:(SuccessCallback)success failCallback:(FailCallback)fail
{
    self = [super init];
    if (self)
    {
        successCallback = success;
        failCallback = fail;
        request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:command relativeToURL:[NSURL URLWithString:SERVER_ENDPOINT]] cachePolicy:NSURLCacheStorageNotAllowed timeoutInterval:10];
        request.HTTPMethod = httpMethod;
        _params = [NSMutableDictionary new];
        receivedData = [NSMutableData new];
    }
    return self;
}


+(APIRequest *)getRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback
{
    return [[APIRequest alloc] initWithMethod:@"GET" command:command successCallback:successCallback failCallback:failCallback];
}

+(APIRequest *)postRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback
{
    return [[APIRequest alloc] initWithMethod:@"POST" command:command successCallback:successCallback failCallback:failCallback];
}

-(void)run
{
    silentMode = NO;
    NSMutableString * postString = [NSMutableString new];
    [_params enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        if (postString.length == 0)
        {
            [postString appendFormat:@"%@=%@", key, obj];
        }
        else
        {
            [postString appendFormat:@"&%@=%@", key, obj];
        }
    }];
    NSLog(@"params: %@", postString);
    [request setHTTPBody:[postString dataUsingEncoding:NSUTF8StringEncoding]];
    connection = [NSURLConnection connectionWithRequest:request delegate:self];
}

-(void)runSilent
{
    silentMode = YES;
    NSMutableString * postString = [NSMutableString new];
    [_params enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        if (postString.length == 0)
        {
            [postString appendFormat:@"%@=%@", key, obj];
        }
        else
        {
            [postString appendFormat:@"&%@=%@", key, obj];
        }
    }];
    NSLog(@"params: %@", postString);
    [request setHTTPBody:[postString dataUsingEncoding:NSUTF8StringEncoding]];
    connection = [NSURLConnection connectionWithRequest:request delegate:self];
}

#pragma mark NSURLConnectionDelegate
-(BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace
{
    NSLog(@"canAuthenticateAgainstProtectionSpace: %@", protectionSpace.description);
    return NO;
}

-(void)connection:(NSURLConnection *)connection didCancelAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
    NSLog(@"didCancelAuthenticationChallenge: %@", challenge.description);
}

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error
{
    NSLog(@"didFailWithError: %@", error.description);
    [receivedData setLength:0];
    if (silentMode)
    {
        failCallback(error);
    }
}

-(void)connection:(NSURLConnection *)connection didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
    NSLog(@"didReceiveAuthenticationChallenge: %@", challenge.description);
}

-(void)connection:(NSURLConnection *)connection willSendRequestForAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge
{
    NSLog(@"willSendRequestForAuthenticationChallenge: %@", challenge.description);
}

-(BOOL)connectionShouldUseCredentialStorage:(NSURLConnection *)conn
{
    NSLog(@"connectionShouldUseCredentialStorage: %@", conn.description);
    return NO;
}

#pragma mark NSURLConnectionDataDelegate
-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
    NSLog(@"didReceiveData");
    [receivedData appendData:data];
}

-(void)connectionDidFinishLoading:(NSURLConnection *)connection
{
    NSLog(@"connectionDidFinishLoading");
    successCallback(httpResponse, receivedData);
    [receivedData setLength:0];
}

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
    httpResponse = (NSHTTPURLResponse *)response;
    NSLog(@"didReceiveResponse %d %@", httpResponse.statusCode, response.description);
    [receivedData setLength:0];
}

-(void)connection:(NSURLConnection *)connection didSendBodyData:(NSInteger)bytesWritten totalBytesWritten:(NSInteger)totalBytesWritten totalBytesExpectedToWrite:(NSInteger)totalBytesExpectedToWrite
{
    NSLog(@"didSendBodyData");
}

@end
