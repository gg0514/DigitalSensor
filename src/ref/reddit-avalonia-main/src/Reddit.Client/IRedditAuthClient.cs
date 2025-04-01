using Reddit.Client.Dtos;

namespace Reddit.Client;

/// <summary>
/// Interface for accessing Reddit's authentication API endpoints at <see href="https://www.reddit.com/">https://www.reddit.com/</see>.
/// This interface provides methods to obtain and manage OAuth tokens without requiring prior authentication.
/// </summary>
public interface IRedditAuthClient
{
    /// <summary>
    /// Exchanges an authorization code for an OAuth token.
    /// </summary>
    /// <param name="code">The authorization code obtained through Reddit's OAuth flow.</param>
    /// <returns>A task that represents the asynchronous operation, containing the OAuth token.</returns>
    Task<OAuthToken> ExchangeCode(string code);

    /// <summary>
    /// Refreshes an expired OAuth token using a refresh token.
    /// </summary>
    /// <param name="refreshToken">The refresh token associated with the expired OAuth token.</param>
    /// <returns>A task that represents the asynchronous operation, containing the new OAuth token.</returns>
    Task<OAuthToken> RefreshToken(string refreshToken);
}
