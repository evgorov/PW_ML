//
//  APIRequest.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/14/13.
//
//

#import "APIRequest.h"
#import "NSData+Base64.h"
#import "SBJsonParser.h"

@interface APIRequest (private)
-(id)initWithMethod:(NSString *)httpMethod command:(NSString *)command successCallback:(SuccessCallback)successCallback failCallback:(FailCallback)failCallback;
-(void)prepareRequest;

@end

@implementation APIRequest

@synthesize params = _params;

static NSMutableSet * apiRequests = nil;
static NSMutableDictionary * apiCache = nil;

-(id)initWithMethod:(NSString *)httpMethod command:(NSString *)command successCallback:(SuccessCallback)success failCallback:(FailCallback)fail
{
    self = [super init];
    if (self)
    {
        if (apiRequests == nil)
        {
            apiRequests = [NSMutableSet new];
            apiCache = [NSMutableDictionary new];
        }
        
        successCallback = success;
        failCallback = fail;
        _params = [NSMutableDictionary new];
        request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:command relativeToURL:[NSURL URLWithString:SERVER_ENDPOINT]] cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData timeoutInterval:20];
        request.HTTPMethod = httpMethod;
        receivedData = [NSMutableData new];
        useCache = NO;
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

+(void)cancelAll
{
    if (apiRequests != nil)
    {
        NSSet * toCancel = [apiRequests copy];
        [apiRequests removeAllObjects];
        for (APIRequest * request in toCancel)
        {
            [request cancel];
        }
    }
}

+(void)clearCache
{
    if (apiCache != nil)
    {
        [apiCache removeAllObjects];
    }
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
                    [body appendData:[[NSString stringWithFormat:@"--%@\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];
                    [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"image.jpg\"\r\n", key] dataUsingEncoding:NSUTF8StringEncoding]];
                    [body appendData:[@"Content-Type: application/octet-stream\r\n\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
                    [body appendData:imageData];
                    [body appendData:[@"\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
                }
            }
            else if ([obj isKindOfClass:[NSData class]])
            {
                NSString * base64Encoded = [(NSData *)obj base64EncodedString];
                [body appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
                [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n", key] dataUsingEncoding:NSUTF8StringEncoding]];
                [body appendData:[[NSString stringWithFormat:@"%@\r\n", base64Encoded] dataUsingEncoding:NSUTF8StringEncoding]];
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

-(void)runUsingCache:(BOOL)_useCache silentMode:(BOOL)_silentMode
{
    useCache = _useCache;
    silentMode = _silentMode;
    [self prepareRequest];
    NSLog(@"request: %@", request.URL.description);
    NSDictionary * cachedData = [apiCache objectForKey:request.URL.absoluteString];
    if (cachedData != nil)
    {
        NSLog(@"load from cache: %@", request.URL.absoluteString);
        successCallback([cachedData objectForKey:@"response"], [cachedData objectForKey:@"data"]);
    }

    connection = [NSURLConnection connectionWithRequest:request delegate:self];
    [apiRequests addObject:self];
}

-(void)cancel
{
    [apiRequests removeObject:self];
    [connection cancel];
}

#pragma mark NSURLConnectionDelegate
-(NSCachedURLResponse *)connection:(NSURLConnection *)connection willCacheResponse:(NSCachedURLResponse *)cachedResponse
{
    return nil;
}

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
    [apiRequests removeObject:self];
    NSLog(@"didFailWithError: %@", error.description);
    [receivedData setLength:0];
    if (!silentMode)
    {
        UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:NSLocalizedString(@"Connection error", @"Connection error") delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alert show];
    }
    if (failCallback != nil)
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
    [apiRequests removeObject:self];
    
    if (!silentMode)
    {
        if (httpResponse.statusCode >= 400 && httpResponse.statusCode < 500)
        {
            NSDictionary * data = [[SBJsonParser new] objectWithData:receivedData];
            NSString * message = [data objectForKey:@"message"];
            if (message == nil)
            {
                message = NSLocalizedString(@"Unknown error", @"Unknown error on server");
            }
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [alert show];
            NSLog(@"request %@ result: %d %@", request.URL.absoluteString, httpResponse.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            if (failCallback != nil)
            {
                failCallback([NSError errorWithDomain:NSLocalizedString(@"Unknown error", @"Unknown error on server") code:httpResponse.statusCode userInfo:nil]);
            }
            return;
        }
        if (httpResponse.statusCode >= 500 && httpResponse.statusCode < 600)
        {
            NSLog(@"request %@ result: %d %@", request.URL.absoluteString, httpResponse.statusCode, [[NSString alloc] initWithData:receivedData encoding:NSUTF8StringEncoding]);
            if (failCallback != nil)
            {
                failCallback([NSError errorWithDomain:NSLocalizedString(@"Unknown error", @"Unknown error on server") code:httpResponse.statusCode userInfo:nil]);
            }
            return;
        }
    }
    
    if (useCache)
    {
        NSDictionary * cachedValue = [apiCache objectForKey:request.URL.absoluteString];
        if (cachedValue != nil && [(NSHTTPURLResponse *)[cachedValue objectForKey:@"response"] statusCode] == httpResponse.statusCode && [(NSMutableData *)[cachedValue objectForKey:@"data"] isEqualToData:receivedData])
        {
            NSLog(@"cached response is actual");
            // already know actual data
            return;
        }
        [apiCache setObject:[NSDictionary dictionaryWithObjectsAndKeys:httpResponse, @"response", receivedData, @"data", nil] forKey:request.URL.absoluteString];
    }
    if (successCallback != nil)
    {
        successCallback(httpResponse, receivedData);
    }
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
