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
-(void)prepareRequest;

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
        _params = [NSMutableDictionary new];
        request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:command relativeToURL:[NSURL URLWithString:SERVER_ENDPOINT]] cachePolicy:NSURLCacheStorageNotAllowed timeoutInterval:10];
        request.HTTPMethod = httpMethod;
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

+(APIRequest *)putRequest:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback
{
    return [[APIRequest alloc] initWithMethod:@"PUT" command:command successCallback:successCallback failCallback:failCallback];
}

-(void)prepareRequest
{
    if ([request.HTTPMethod compare:@"GET"] == NSOrderedSame || [request.HTTPMethod compare:@"PUT"] == NSOrderedSame)
    {
        NSMutableString * paramsString = [NSMutableString new];
        [_params enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
            if (paramsString.length == 0)
            {
                [paramsString appendFormat:@"%@=%@", key, obj];
            }
            else
            {
                [paramsString appendFormat:@"&%@=%@", key, obj];
            }
        }];
        request.URL = [NSURL URLWithString:[NSString stringWithFormat:@"?%@", [paramsString stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]] relativeToURL:request.URL];
    }
    else if ([request.HTTPMethod compare:@"POST"] == NSOrderedSame) {
        NSString * boundary = @"qpojw49j023n4fn1983cdh10239cn";
        // set Content-Type in HTTP header
        NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", boundary];
        [request setValue:contentType forHTTPHeaderField: @"Content-Type"];
        
        // post body
        NSMutableData *body = [NSMutableData data];
        [_params enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
            if ([obj isKindOfClass:[UIImage class]])
            {
                // add image data
                NSData *imageData = UIImageJPEGRepresentation((UIImage *)obj, 1.0f);
                if (imageData) {
                    [body appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
                    [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"image.jpg\"\r\n", key] dataUsingEncoding:NSUTF8StringEncoding]];
                    [body appendData:[@"Content-Type: image/jpeg\r\n\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
                    [body appendData:imageData];
                    [body appendData:[[NSString stringWithFormat:@"\r\n"] dataUsingEncoding:NSUTF8StringEncoding]];
                }
            }
            else
            {
                // add string params
                [body appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
                [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n", key] dataUsingEncoding:NSUTF8StringEncoding]];
                [body appendData:[[NSString stringWithFormat:@"%@\r\n", obj] dataUsingEncoding:NSUTF8StringEncoding]];
            }
        }];
        
        [body appendData:[[NSString stringWithFormat:@"--%@--\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
        
        NSLog(@"POST request data: %@", [[NSString alloc] initWithData:body encoding:NSUTF8StringEncoding]);

        [request setHTTPBody:body];
    }
}

-(void)run
{
    silentMode = NO;
    [self prepareRequest];
    connection = [NSURLConnection connectionWithRequest:request delegate:self];
}

-(void)runSilent
{
    silentMode = YES;
    [self prepareRequest];
    NSLog(@"request: %@", request.URL.description);
    connection = [NSURLConnection connectionWithRequest:request delegate:self];
}

#pragma mark NSURLConnectionDelegate
-(BOOL)connection:(NSURLConnection *)connection canAuthenticateAgainstProtectionSpace:(NSURLProtectionSpace *)protectionSpace
{
    NSLog(@"canAuthenticateAgainstProtectionSpace: %@", protectionSpace.description);
    return YES;
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
    return YES;
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
