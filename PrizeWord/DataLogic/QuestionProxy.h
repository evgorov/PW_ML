//
//  QuestionProxy.h
//  PrizeWord
//
//  Created by Pavel Skorynin on 1/27/14.
//
//

#import <Foundation/Foundation.h>
#import "DataProxy.h"
#import "QuestionData.h"

@interface QuestionProxy : DataProxy

- (uint)columnAsUint;
- (uint)rowAsUint;
- (NSString *)question_text;
- (NSString *)answer;
- (uint)answer_positionAsUint;
- (NSNumber *)solved;
- (void)setSolved:(NSNumber *)solved;

@end
