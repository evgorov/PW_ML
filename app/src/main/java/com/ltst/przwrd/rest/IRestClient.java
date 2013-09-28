package com.ltst.przwrd.rest;

import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IRestClient
{
    @Nullable RestUserData getUserData(@Nonnull String sessionToken);
    @Nullable RestUserData resetUserPic(@Nonnull String token, @Nonnull byte[] userPic);
    @Nullable RestUserData resetUserName(@Nonnull String token, @Nonnull String userName);
    @Nullable RestUserData.RestUserDataHolder getSessionKeyByProvider(@Nonnull String provider, @Nonnull String access_token);
    @Nullable RestUserData.RestUserDataHolder getSessionKeyBySignUp(@Nonnull String email, @Nonnull String name,
                                                @Nonnull String surname, @Nonnull String password,
                                                @Nullable String birthdate, @Nullable String city, @Nullable byte[] userpic);
    @Nullable RestUserData.RestUserDataHolder getSessionKeyByLogin(@Nonnull String email, @Nonnull String password);

    HttpStatus forgotPassword(@Nonnull String email);
    HttpStatus resetPassword(@Nonnull String token,  @Nonnull String newPassword);

    @Nullable RestPuzzleSet.RestPuzzleSetsHolder getPublishedSets(@Nonnull String sessionKey);
    @Nullable RestPuzzleTotalSet.RestPuzzleSetsHolder getTotalPublishedSets(@Nonnull String sessionKey, int year, int month);
    @Nullable RestPuzzle.RestPuzzleHolder getPuzzle(@Nonnull String sessionKey, @Nonnull String puzzleServerId);
    @Nullable
    RestUserData.RestAnswerMessageHolder mergeAccounts(@Nonnull String sessionKey1, @Nonnull String sessionKey2);

    @Nullable RestInviteFriend.RestInviteFriendHolder getFriendsData(@Nonnull String sessionToken,@Nonnull String providerName);
    @Nullable RestInviteFriend.RestInviteFriendHolder sendInviteToFriends(@Nonnull String sessionToken,@Nonnull String providerName, @Nonnull String ids);

    @Nullable RestPuzzleUserData.RestPuzzleUserDataHolder getPuzzleUserData(@Nonnull String sessionKey, @Nonnull String puzzleId);
    HttpStatus putPuzzleUserData(@Nonnull String sessionKey, @Nonnull String puzzleId, @Nonnull String puzzleUserData);

    @Nullable RestUserData.RestUserDataHolder addOrRemoveHints(@Nonnull String sessionKey, int hintsToChange);

    @Nullable RestPuzzleUsers getUsers(@Nonnull String sessionKey);
    @Nullable RestCoefficients getCoefficients(@Nonnull String sessionKey);

    HttpStatus postPuzzleScore(@Nonnull String sessionKey, @Nonnull String puzzleId, int score);

    @Nullable RestInviteFriend.RestInviteFriendHolder getFriendsScoreData(@Nonnull String sessionToken,@Nonnull String providerName);
    @Nullable RestPuzzleTotalSet.RestPuzzleOneSetHolder postBuySet(@Nonnull String sessionKey, @Nonnull String serverSetId, @Nonnull String receiptData, @Nonnull String signature);

    @Nullable RestNews getNews(@Nonnull String sessionKey);

    HttpStatus shareMessageToVk(@Nonnull String sessionKey, @Nonnull String message);

    public HttpStatus sendRegistrationId(@Nonnull String sessionKey, @Nonnull String registrationId);
}
