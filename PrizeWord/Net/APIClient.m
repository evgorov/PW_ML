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
    self = [super initWithBaseURL:[NSURL URLWithString:@"http://api.prize-word.com/"]];
    if (self) {
        [self setAllowsInvalidSSLCertificate:YES];
        [[AFNetworkActivityIndicatorManager sharedManager] setEnabled:YES];
    }
    return self;
}

- (AFHTTPRequestOperation *)HTTPRequestOperationWithRequest:(NSURLRequest *)urlRequest success:(void (^)(AFHTTPRequestOperation *, id))success failure:(void (^)(AFHTTPRequestOperation *, NSError *))failure
{
    [AFHTTPRequestOperation addAcceptableStatusCodes:[NSIndexSet indexSetWithIndex:304]];
    AFHTTPRequestOperation * operation = [super HTTPRequestOperationWithRequest:urlRequest success:success failure:failure];
    return operation;
}

@end
