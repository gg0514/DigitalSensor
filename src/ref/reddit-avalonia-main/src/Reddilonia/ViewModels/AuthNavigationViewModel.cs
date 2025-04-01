﻿using System;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Messaging;
using Reddilonia.BusinessLogic;
using Reddilonia.Models;

namespace Reddilonia.ViewModels;

public partial class AuthNavigationViewModel : ViewModelBase
{
    private readonly IAuthManager _authManager;
    private readonly IAuthTokenStorage _authTokenStorage;
    private readonly IMessenger _messenger;

    [ObservableProperty] private Uri _webViewUri = new("https://www.google.com");

    public AuthNavigationViewModel(IAuthManager authManager, IAuthTokenStorage authTokenStorage, IMessenger messenger)
    {
        _authManager = authManager;
        _authTokenStorage = authTokenStorage;
        _messenger = messenger;
        _authManager.AuthSuccess += AuthSuccess;
        _authManager.Start();
        WebViewUri = new Uri(_authManager.GetAuthUrl());
    }

    private async void AuthSuccess(object? sender, AuthSuccessEventArgs e)
    {
        await _authTokenStorage.StoreToken(e.AuthToken);
        _authManager.Stop();
        _messenger.Send<ReloadFeedsViewMessage>();
    }
}
