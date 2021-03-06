package io.rw.app

import java.time.Instant

object data {

  type ApiResult[R <: ApiOutput] = Either[ApiError, R]
  type AuthUser = Int

  object RequestBodies {
    case class WrappedUserBody[T](user: T)
    case class AuthenticateUserBody(email: String, password: String)
    case class RegisterUserBody(username: String, email: String, password: String)
    case class UpdateUserBody(username: Option[String], email: Option[String], password: Option[String], bio: Option[String], image: Option[String])
    case class WrappedArticleBody[T](article: T)
    case class CreateArticleBody(title: String, description: String, body: String, tagList: Option[List[String]])
    case class UpdateArticleBody(title: Option[String], description: Option[String], body: Option[String])
    case class WrappedCommentBody[T](comment: T)
    case class AddCommentBody(body: String)
  }

  sealed trait ApiInput
  object ApiInputs {
    case class AuthenticateUserInput(email: String, password: String) extends ApiInput
    case class RegisterUserInput(username: String, email: String, password: String) extends ApiInput
    case class GetUserInput(authUser: AuthUser) extends ApiInput
    case class UpdateUserInput(authUser: AuthUser, username: Option[String], email: Option[String], password: Option[String], bio: Option[String], image: Option[String]) extends ApiInput
    case class GetProfileInput(authUser: Option[AuthUser], username: String) extends ApiInput
    case class FollowUserInput(authUser: AuthUser, username: String) extends ApiInput
    case class UnfollowUserInput(authUser: AuthUser, username: String) extends ApiInput
    case class GetAllArticlesInput(authUser: Option[AuthUser], filter: ArticleFilter, pagination: Pagination) extends ApiInput
    case class GetArticlesFeedInput(authUser: AuthUser, pagination: Pagination) extends ApiInput
    case class GetArticleInput(authUser: Option[AuthUser], slug: String) extends ApiInput
    case class CreateArticleInput(authUser: AuthUser, title: String, description: String, body: String, tagList: List[String]) extends ApiInput
    case class UpdateArticleInput(authUser: AuthUser, slug: String, title: Option[String], description: Option[String], body: Option[String]) extends ApiInput
    case class DeleteArticleInput(authUser: AuthUser, slug: String) extends ApiInput
    case class FavoriteArticleInput(authUser: AuthUser, slug: String) extends ApiInput
    case class UnfavoriteArticleInput(authUser: AuthUser, slug: String) extends ApiInput
    case class AddCommentInput(authUser: AuthUser, slug: String, body: String) extends ApiInput
    case class GetCommentsInput(authUser: Option[AuthUser], slug: String) extends ApiInput
    case class DeleteCommentInput(authUser: AuthUser, slug: String, commentId: Int) extends ApiInput
    case class GetTagsInput() extends ApiInput
  }

  sealed trait ApiOutput
  object ApiOutputs {
    case class AuthenticateUserOutput(user: User) extends ApiOutput
    case class RegisterUserOutput(user: User) extends ApiOutput
    case class GetUserOutput(user: User) extends ApiOutput
    case class UpdateUserOutput(user: User) extends ApiOutput
    case class GetProfileOutput(profile: Profile) extends ApiOutput
    case class FollowUserOutput(profile: Profile) extends ApiOutput
    case class UnfollowUserOutput(profile: Profile) extends ApiOutput
    case class GetAllArticlesOutput(articles: List[Article], articlesCount: Int) extends ApiOutput
    case class GetArticlesFeedOutput(articles: List[Article], articlesCount: Int) extends ApiOutput
    case class GetArticleOutput(article: Article) extends ApiOutput
    case class CreateArticleOutput(article: Article) extends ApiOutput
    case class UpdateArticleOutput(article: Article) extends ApiOutput
    case class DeleteArticleOutput() extends ApiOutput
    case class FavoriteArticleOutput(article: Article) extends ApiOutput
    case class UnfavoriteArticleOutput(article: Article) extends ApiOutput
    case class AddCommentOutput(comment: Comment) extends ApiOutput
    case class GetCommentsOutput(comments: List[Comment]) extends ApiOutput
    // TODO return {} instead of null
    case class DeleteCommentOutput() extends ApiOutput
    case class GetTagsOutput(tags: List[String]) extends ApiOutput
  }

  sealed trait ApiError
  object ApiErrors {
    case class UserNotFound() extends ApiError
    case class UserFollowingHimself(profile: Profile) extends ApiError
    case class UserUnfollowingHimself(profile: Profile) extends ApiError
    case class UserNotFoundOrPasswordNotMatched() extends ApiError
    case class EmailAlreadyExists() extends ApiError
    case class UsernameAlreadyExists() extends ApiError
    case class ProfileNotFound() extends ApiError
    case class ArticleNotFound() extends ApiError
    case class CommentNotFound() extends ApiError
  }

  object Entities {
    case class WithId[T](id: Int, entity: T)
    case class User(email: String, username: String, password: String, bio: Option[String], image: Option[String], createdAt: Instant, updatedAt: Instant)
    case class UserForUpdate(username: Option[String], email: Option[String], password: Option[String], bio: Option[String], image: Option[String], updatedAt: Instant)
    case class Follower(followeeId: Int, followerId: Int)
    case class Article(slug: String, title: String, description: String, body: String, authorId: Int, createdAt: Instant, updatedAt: Instant)
    case class ArticleForUpdate(slug: Option[String], title: Option[String], description: Option[String], body: Option[String], updatedAt: Instant)
    case class Tag(articleId: Int, tag: String)
    case class Favorite(articleId: Int, userId: Int)
    case class Comment(body: String, articleId: Int, authorId: Int, createdAt: Instant, updatedAt: Instant)
  }

  case class AppConfig(apiHost: String, apiPort: Int, idHasherSalt: String, jwtTokenKey: String, jwtTokenExpiration: Int, dbUser: String, dbPassword: String, dbUrl: String)

  case class JwtTokenPayload(authUser: AuthUser)
  case class ArticleFilter(tag: Option[String], author: Option[String], favorited: Option[String])
  case class Pagination(limit: Int, offset: Int)
  case class User(email: String, token: String, username: String, bio: Option[String] = None, image: Option[String] = None)
  case class Profile(username: String, bio: Option[String], image: Option[String], following: Boolean)
  case class Article(slug: String, title: String, description: String, body: String, tagList: List[String], createdAt: Instant, updatedAt: Instant, favorited: Boolean, favoritesCount: Int, author: Profile)
  case class Comment(id: Int, createdAt: Instant, updatedAt: Instant, body: String, author: Profile)

  type ValidationErrors = Map[String, List[String]]
  case class ValidationErrorResponse(errors: ValidationErrors)
  case class NotFoundResponse(status: Int, error: String)
}
