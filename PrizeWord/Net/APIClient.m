//
//  APIClient.m
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/5/13.
//
//

#import "APIClient.h"
#import "AFNetworkActivityIndicatorManager.h"

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



@end
