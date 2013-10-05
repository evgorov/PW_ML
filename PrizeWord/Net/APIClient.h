//
//  APIClient.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 10/5/13.
//
//

#import "AFHTTPClient.h"

@interface APIClient : AFHTTPClient

+ (APIClient *)sharedClient;

@end
