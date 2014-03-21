//
//  APIClient.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/5/13.
//
//

#import "APIClient.h"
#import "AFNetworkActivityIndicatorManager.h"
#import "AFHTTPRequestOperation.h"
#import <SBJsonParser.h>
#import "EventManager.h"

@implementation APIClient

+ (APIClient *)sharedClient
{
    static APIClient * _sharedClient;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedClient = [APIClient new];
    });
    return _sharedClient;
}

- (id)init
{
    self = [super initWithBaseURL:[NSURL URLWithString:@"https://api.prize-word.com/"]];
//    self = [super initWithBaseURL:[NSURL URLWithString:@"http://ec2-54-229-233-34.eu-west-1.compute.amazonaws.com/"]];
    if (self) {
        // DEBUG :: YES only for test purposes
        [self setAllowsInvalidSSLCertificate:NO];
        [AFHTTPRequestOperation addAcceptableStatusCodes:[NSIndexSet indexSetWithIndex:304]];
        [[AFNetworkActivityIndicatorManager sharedManager] setEnabled:YES];
    }
    return self;
}

- (AFHTTPRequestOperation *)HTTPRequestOperationWithRequest:(NSURLRequest *)urlRequest success:(void (^)(AFHTTPRequestOperation *, id))success failure:(void (^)(AFHTTPRequestOperation *, NSError *))failure
{
    return [super HTTPRequestOperationWithRequest:urlRequest success:^(AFHTTPRequestOperation * operation, id object){
        if (success != nil)
        {
            success(operation, object);
        }
        
    } failure:^(AFHTTPRequestOperation * operation, NSError * error) {
        NSLog(@"failure status code: %d", operation.response.statusCode);
        NSLog(@"failure request: %@ %@", operation.request, [[NSString alloc] initWithData:operation.request.HTTPBody encoding:NSUTF8StringEncoding]);
        if (operation.response.statusCode == 401)
        {
            NSString * message = NSLocalizedString(@"Session has ended. Please log in again.", @"Not-authorized HTTP status code");
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_GAME_REQUEST_PAUSE]];
            [[EventManager sharedManager] dispatchEvent:[Event eventWithType:EVENT_SESSION_ENDED]];
            [alert show];
            return;
        }
        
        // TODO :: silent mode
        
        if (operation.response.statusCode >= 400 && operation.response.statusCode < 500)
        {
            NSDictionary * data = [[SBJsonParser new] objectWithData:operation.responseData];
            NSString * message = [data objectForKey:@"message"];
            if (message == nil)
            {
                message = NSLocalizedString(@"Unknown error", @"Unknown error on server");
            }
            UIAlertView * alert = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Error", @"Error") message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
            [alert show];
            NSLog(@"request %@ result: %d %@", operation.request.URL.absoluteString, operation.response.statusCode, [[NSString alloc] initWithData:operation.responseData encoding:NSUTF8StringEncoding]);
            if (failure != nil)
            {
                failure(operation, [NSError errorWithDomain:NSLocalizedString(@"Unknown error", @"Unknown error on server") code:operation.response.statusCode userInfo:nil]);
            }
            return;
        }
        if (operation.response.statusCode >= 500 && operation.response.statusCode < 600)
        {
            NSLog(@"request %@ result: %d %@", operation.request.URL.absoluteString, operation.response.statusCode, [[NSString alloc] initWithData:operation.responseData encoding:NSUTF8StringEncoding]);
            if (failure != nil)
            {
                failure(operation, [NSError errorWithDomain:NSLocalizedString(@"Unknown error", @"Unknown error on server") code:operation.response.statusCode userInfo:nil]);
            }
            return;
        }
        
        failure(operation, error);
    }];
}

@end
