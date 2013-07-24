package com.ltst.prizeword.rest;

import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IRestClient
{
    @Nullable RestUserData getUserData(@Nonnull String sessionToken);
    @Nullable RestUserData.RestUserDataHolder resetUserData(@Nonnull RestUserData.RestUserDataSender userData);
    @Nullable RestUserData.RestUserDataHolder getSessionKeyByProvider(@Nonnull String provider, @Nonnull String access_token);
    @Nullable RestUserData.RestUserDataHolder getSessionKeyBySignUp(@Nonnull String email, @Nonnull String name,
                                                @Nonnull String surname, @Nonnull String password,
                                                @Nullable String birthdate, @Nullable String city, @Nullable byte[] userpic);
    @Nullable RestUserData.RestUserDataHolder getSessionKeyByLogin(@Nonnull String email, @Nonnull String password);

    HttpStatus forgotPassword(@Nonnull String email);
    HttpStatus resetPassword(@Nonnull String token,  @Nonnull String newPassword);

    @Nullable RestPuzzleSet.RestPuzzleSetsHolder getPublishedSets(@Nonnull String sessionKey);
    @Nullable RestPuzzle getPuzzle(@Nonnull String sessionKey, @Nonnull String puzzleServerId);
}
