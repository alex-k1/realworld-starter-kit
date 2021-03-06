package io.rw.app.apis

import cats.data.{EitherT, OptionT}
import cats.implicits._
import cats.Monad
import io.rw.app.data.{Entities => E, _}
import io.rw.app.data.ApiErrors._
import io.rw.app.data.ApiInputs._
import io.rw.app.data.ApiOutputs._
import java.time.Instant
import io.rw.app.repos._
import io.rw.app.security.{JwtToken, PasswordHasher}
import io.rw.app.data

trait ProfileApis[F[_]] {
  def get(input: GetProfileInput): F[ApiResult[GetProfileOutput]]
  def follow(input: FollowUserInput): F[ApiResult[FollowUserOutput]]
  def unfollow(input: UnfollowUserInput): F[ApiResult[UnfollowUserOutput]]
}

object ProfileApis {

  def impl[F[_] : Monad](userRepo: UserRepo[F], followerRepo: FollowerRepo[F]) = new ProfileApis[F]() {

    def get(input: GetProfileInput): F[ApiResult[GetProfileOutput]] = {
      val profile = for {
        userWithId <- OptionT(userRepo.findUserByUsername(input.username))
        following <- OptionT.liftF(input.authUser.flatTraverse(followerRepo.findFollower(userWithId.id, _)).map(_.nonEmpty))
      } yield mkProfile(userWithId.entity, following)

      profile.value.map(_.map(GetProfileOutput).toRight(ProfileNotFound()))
    }

    def follow(input: FollowUserInput): F[ApiResult[FollowUserOutput]] = {
      val profile = for {
        userWithId <- EitherT.fromOptionF(userRepo.findUserByUsername(input.username), ProfileNotFound())
        _ <- EitherT.cond[F](input.authUser != userWithId.id, (), UserFollowingHimself(mkProfile(userWithId.entity, false)))
        _ <- EitherT.liftF[F, ApiError, E.Follower](followerRepo.createFollower(E.Follower(userWithId.id, input.authUser)))
      } yield mkProfile(userWithId.entity, true)

      profile.value.map(_.recover({ case UserFollowingHimself(p) => p }).map(FollowUserOutput(_)))
    }

    def unfollow(input: UnfollowUserInput): F[ApiResult[UnfollowUserOutput]] = {
      val profile = for {
        userWithId <- EitherT.fromOptionF(userRepo.findUserByUsername(input.username), ProfileNotFound())
        _ <- EitherT.cond[F](input.authUser != userWithId.id, (), UserUnfollowingHimself(mkProfile(userWithId.entity, false)))
        _ <- EitherT.liftF[F, ApiError, Unit](followerRepo.deleteFollower(userWithId.id, input.authUser))
      } yield mkProfile(userWithId.entity, false)

      profile.value.map(_.recover({ case UserUnfollowingHimself(p) => p }).map(UnfollowUserOutput(_)))
    }
  }
}
